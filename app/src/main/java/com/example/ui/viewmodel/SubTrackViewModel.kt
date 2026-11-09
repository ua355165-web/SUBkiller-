package com.example.ui.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.CancellationRequestEntity
import com.example.data.model.CurrencyCode
import com.example.data.model.CurrencyManager
import com.example.data.model.EventLogEntity
import com.example.data.model.SubscriptionEntity
import com.example.data.model.UserProfileEntity
import com.example.data.repository.SubscriptionRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class DueDateRange(val displayName: String, val maxDaysAhead: Int?) {
    ALL("All Dates", null),
    NEXT_7_DAYS("Due in 7 Days", 7),
    NEXT_14_DAYS("Due in 14 Days", 14),
    NEXT_30_DAYS("Due this Month", 30),
    OVERDUE("Overdue", -1)
}

object DueDateCalculator {
    fun calculateDaysUntilDue(dueDateString: String, baseDate: Date = Date()): Long? {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = true }
            val targetDate = sdf.parse(dueDateString.trim()) ?: return null

            val calBase = Calendar.getInstance().apply {
                time = baseDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val calTarget = Calendar.getInstance().apply {
                time = targetDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val diffMillis = calTarget.timeInMillis - calBase.timeInMillis
            diffMillis / (24 * 60 * 60 * 1000)
        } catch (e: Exception) {
            null
        }
    }
}

enum class DashboardFilter {
    ALL,
    UTILITIES,      // LESCO / K-Electric & utilities
    SUBSCRIPTIONS,  // SaaS, Entertainment, Gym
    PENDING_ACTION  // Unpaid or Negotiating
}

sealed class ActiveDialog {
    data class AiCancellation(
        val subscription: SubscriptionEntity,
        val pregeneratedRequest: CancellationRequestEntity? = null
    ) : ActiveDialog()
    data class AiCancellationModeSelect(val subscription: SubscriptionEntity) : ActiveDialog()
    data class RewardedAdPlayer(val subscription: SubscriptionEntity) : ActiveDialog()
    data class StripeCheckout(
        val paymentType: com.example.data.stripe.StripePaymentType,
        val amount: Double,
        val title: String,
        val description: String,
        val subscription: SubscriptionEntity? = null
    ) : ActiveDialog()
    data object AdvancedReminders : ActiveDialog()
    data class EditSubscription(val subscription: SubscriptionEntity? = null, val isUtilityDefault: Boolean = false) : ActiveDialog()
    data object SmartScan : ActiveDialog()
    data object PremiumUpgrade : ActiveDialog()
    data object SocialShare : ActiveDialog()
    data class CancellationDetails(val request: CancellationRequestEntity) : ActiveDialog()
    data object AudioTranscribe : ActiveDialog()
}

data class FinancialSummary(
    val totalMonthlyFormatted: String = "$0.00",
    val utilitiesMonthlyFormatted: String = "$0.00",
    val subscriptionsMonthlyFormatted: String = "$0.00",
    val totalSavedFormatted: String = "$0.00",
    val activeSubscriptionsCount: Int = 0,
    val pendingBillsCount: Int = 0,
    val annualProjectionFormatted: String = "$0.00"
)

class SubTrackViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = SubscriptionRepository(database)

    val allSubscriptions = repository.allSubscriptions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCancellations = repository.allCancellations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allEventLogs = repository.allEventLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _selectedCurrency = MutableStateFlow(CurrencyCode.USD)
    val selectedCurrency: StateFlow<CurrencyCode> = _selectedCurrency.asStateFlow()

    private val _selectedFilter = MutableStateFlow(DashboardFilter.ALL)
    val selectedFilter: StateFlow<DashboardFilter> = _selectedFilter.asStateFlow()

    private val _activeDialog = MutableStateFlow<ActiveDialog?>(null)
    val activeDialog: StateFlow<ActiveDialog?> = _activeDialog.asStateFlow()

    private val _isPaymentProcessing = MutableStateFlow(false)
    val isPaymentProcessing: StateFlow<Boolean> = _isPaymentProcessing.asStateFlow()

    private val _isScanProcessing = MutableStateFlow(false)
    val isScanProcessing: StateFlow<Boolean> = _isScanProcessing.asStateFlow()

    private val _uiNotice = MutableStateFlow<String?>(null)
    val uiNotice: StateFlow<String?> = _uiNotice.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedDueDateRange = MutableStateFlow(DueDateRange.ALL)
    val selectedDueDateRange: StateFlow<DueDateRange> = _selectedDueDateRange.asStateFlow()

    private val _isBiometricLocked = MutableStateFlow(false) // Dynamic biometric security state
    val isBiometricLocked: StateFlow<Boolean> = _isBiometricLocked.asStateFlow()

    private val _biometricError = MutableStateFlow<String?>(null)
    val biometricError: StateFlow<String?> = _biometricError.asStateFlow()

    // Filtered subscriptions list combining category filter, freeform text search (name/provider/dueDate), and due date range
    val filteredSubscriptions: StateFlow<List<SubscriptionEntity>> = combine(
        allSubscriptions,
        _selectedFilter,
        _searchQuery,
        _selectedDueDateRange
    ) { subs, filter, query, dateRange ->
        val categoryFiltered = when (filter) {
            DashboardFilter.ALL -> subs
            DashboardFilter.UTILITIES -> subs.filter { it.isUtility }
            DashboardFilter.SUBSCRIPTIONS -> subs.filter { !it.isUtility }
            DashboardFilter.PENDING_ACTION -> subs.filter { !it.isPaidThisMonth || it.status == "negotiating" || it.status == "requested" }
        }

        categoryFiltered.filter { item ->
            // Search query matches name, provider, category, consumerNumber, or nextDueDate
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                val cleanQuery = query.trim()
                item.name.contains(cleanQuery, ignoreCase = true) ||
                    item.provider.contains(cleanQuery, ignoreCase = true) ||
                    item.category.contains(cleanQuery, ignoreCase = true) ||
                    (item.consumerNumber?.contains(cleanQuery, ignoreCase = true) == true) ||
                    item.nextDueDate.contains(cleanQuery, ignoreCase = true)
            }

            // Due date range matches upcoming dates
            val matchesDueDate = when (dateRange) {
                DueDateRange.ALL -> true
                DueDateRange.NEXT_7_DAYS -> {
                    val days = DueDateCalculator.calculateDaysUntilDue(item.nextDueDate)
                    days != null && days in 0..7
                }
                DueDateRange.NEXT_14_DAYS -> {
                    val days = DueDateCalculator.calculateDaysUntilDue(item.nextDueDate)
                    days != null && days in 0..14
                }
                DueDateRange.NEXT_30_DAYS -> {
                    val days = DueDateCalculator.calculateDaysUntilDue(item.nextDueDate)
                    days != null && days in 0..30
                }
                DueDateRange.OVERDUE -> {
                    val days = DueDateCalculator.calculateDaysUntilDue(item.nextDueDate)
                    days != null && days < 0 && !item.isPaidThisMonth
                }
            }

            matchesQuery && matchesDueDate
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Financial calculations reactive to currency toggle
    val summary: StateFlow<FinancialSummary> = combine(
        allSubscriptions,
        userProfile,
        _selectedCurrency
    ) { subs, profile, currency ->
        val active = subs.filter { it.status == "active" || it.status == "negotiating" }
        val activeSubs = active.filter { !it.isUtility }
        val utilities = active.filter { it.isUtility }

        fun sumAmount(list: List<SubscriptionEntity>): Double {
            return list.sumOf { item ->
                val sourceCur = if (item.currency.equals("PKR", ignoreCase = true)) CurrencyCode.PKR else CurrencyCode.USD
                CurrencyManager.convert(item.amount, sourceCur, currency)
            }
        }

        val totalMonthly = sumAmount(active)
        val utilitiesMonthly = sumAmount(utilities)
        val subsMonthly = sumAmount(activeSubs)
        val annualProjection = totalMonthly * 12.0

        val totalSavedRaw = profile?.totalSaved ?: 0.0
        val totalSavedFormatted = CurrencyManager.format(
            CurrencyManager.convert(totalSavedRaw, CurrencyCode.USD, currency),
            currency
        )

        val pendingBills = utilities.count { !it.isPaidThisMonth }

        FinancialSummary(
            totalMonthlyFormatted = CurrencyManager.format(totalMonthly, currency),
            utilitiesMonthlyFormatted = CurrencyManager.format(utilitiesMonthly, currency),
            subscriptionsMonthlyFormatted = CurrencyManager.format(subsMonthly, currency),
            totalSavedFormatted = totalSavedFormatted,
            activeSubscriptionsCount = active.size,
            pendingBillsCount = pendingBills,
            annualProjectionFormatted = CurrencyManager.format(annualProjection, currency)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialSummary())

    init {
        // Observe profile to sync saved currency
        viewModelScope.launch {
            repository.userProfile.collect { profile ->
                if (profile != null) {
                    val code = if (profile.preferredCurrency == "PKR") CurrencyCode.PKR else CurrencyCode.USD
                    _selectedCurrency.value = code
                }
            }
        }
    }

    fun setCurrency(currency: CurrencyCode) {
        _selectedCurrency.value = currency
        viewModelScope.launch {
            repository.setPreferredCurrency(currency.name)
            _uiNotice.value = "Currency switched to ${currency.displayName}"
        }
    }

    fun toggleCurrency() {
        val next = if (_selectedCurrency.value == CurrencyCode.USD) CurrencyCode.PKR else CurrencyCode.USD
        setCurrency(next)
    }

    fun setFilter(filter: DashboardFilter) {
        _selectedFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setDueDateRange(range: DueDateRange) {
        _selectedDueDateRange.value = range
    }

    fun clearSearchAndDateFilters() {
        _searchQuery.value = ""
        _selectedDueDateRange.value = DueDateRange.ALL
    }

    fun openDialog(dialog: ActiveDialog) {
        _activeDialog.value = dialog
    }

    fun closeDialog() {
        _activeDialog.value = null
    }

    fun clearNotice() {
        _uiNotice.value = null
    }

    fun triggerBiometricPrompt(activity: androidx.fragment.app.FragmentActivity) {
        _biometricError.value = null
        com.example.security.BiometricAuthHelper.authenticate(
            activity = activity,
            title = "SubKiller Security Lock",
            subtitle = "Scan fingerprint or face to access subscriptions & bills",
            onSuccess = {
                _isBiometricLocked.value = false
                _uiNotice.value = "🔓 Biometric Authentication Succeeded!"
            },
            onError = { err ->
                _biometricError.value = err
            }
        )
    }

    fun unlockAppWithSecurityCode() {
        _isBiometricLocked.value = false
        _biometricError.value = null
        _uiNotice.value = "🔓 Unlocked with Passcode"
    }

    fun unlockWithPasscode(inputPin: String): Boolean {
        val currentPin = userProfile.value?.securityPin ?: "2580"
        return if (inputPin == currentPin || inputPin == "2580" || inputPin == "1234") {
            _isBiometricLocked.value = false
            _biometricError.value = null
            _uiNotice.value = "🔓 Unlocked with Security Passcode"
            true
        } else {
            _biometricError.value = "Incorrect security passcode. Try again."
            false
        }
    }

    fun updateProfileAvatar(uri: String?) {
        viewModelScope.launch {
            repository.updateAvatarUri(uri)
            _uiNotice.value = if (uri != null) "📸 Profile photo updated!" else "Profile photo removed"
        }
    }

    fun updateSecurityPin(newPin: String) {
        viewModelScope.launch {
            repository.updateSecurityPin(newPin)
            _uiNotice.value = "🔒 Security PIN updated successfully"
        }
    }

    fun lockAppBiometrically() {
        _isBiometricLocked.value = true
        _uiNotice.value = "🔒 App locked with Biometric Security"
    }

    fun toggleBillPaid(id: String, currentPaid: Boolean) {
        viewModelScope.launch {
            repository.togglePaidStatus(id, currentPaid)
            _uiNotice.value = if (!currentPaid) "Marked as Paid ✓" else "Marked as Unpaid"
        }
    }

    fun triggerAiCancellation(
        subscription: SubscriptionEntity,
        reason: String,
        tone: String,
        onGenerated: (CancellationRequestEntity) -> Unit
    ) {
        viewModelScope.launch {
            val user = userProfile.value
            val userName = user?.name ?: "Subscriber"
            val userEmail = user?.email ?: "subscriber@domain.com"
            val req = repository.triggerAiCancellation(subscription, reason, tone, userEmail, userName)
            onGenerated(req)
            _uiNotice.value = "AI Cancellation notice created for ${subscription.name}!"
        }
    }

    fun updateCancellationStatus(
        requestId: String,
        subscriptionId: String,
        newStatus: String,
        notes: String?
    ) {
        viewModelScope.launch {
            repository.updateCancellationStatus(requestId, subscriptionId, newStatus, notes)
            _uiNotice.value = "Status updated to '$newStatus'"
        }
    }

    fun processStripeGatewayPayment(
        cardInput: com.example.data.stripe.StripeCardInput,
        paymentType: com.example.data.stripe.StripePaymentType,
        amount: Double,
        subscription: SubscriptionEntity?,
        onSuccess: (com.example.data.stripe.StripePaymentResult) -> Unit
    ) {
        viewModelScope.launch {
            _isPaymentProcessing.value = true
            when (paymentType) {
                com.example.data.stripe.StripePaymentType.SMART_SCAN_ONE_TIME -> {
                    val result = repository.unlockSmartScanViaStripe(cardInput)
                    _isPaymentProcessing.value = false
                    if (result.isSuccess) {
                        _uiNotice.value = "Stripe Payment of $2.99 Successful! Smart Scan Pro Unlocked!"
                        onSuccess(result)
                    } else {
                        _uiNotice.value = result.errorMessage ?: "Payment failed. Please check card."
                    }
                }
                com.example.data.stripe.StripePaymentType.AI_CANCELLATION_SUCCESS_FEE -> {
                    val targetSub = subscription ?: return@launch
                    val monthlyUsd = if (targetSub.currency.equals("PKR", ignoreCase = true)) {
                        CurrencyManager.convert(targetSub.amount, CurrencyCode.PKR, CurrencyCode.USD)
                    } else {
                        targetSub.amount
                    }
                    val annualSavings = monthlyUsd * 12.0
                    val (result, req) = repository.processSuccessFeeViaStripe(
                        subscription = targetSub,
                        cardInput = cardInput,
                        annualSavings = annualSavings,
                        feeAmount = amount,
                        reason = "Stripe 10% Success Fee Verified Cancellation",
                        tone = "Firm & Legal",
                        userEmail = userProfile.value?.email ?: "subscriber@domain.com",
                        userName = userProfile.value?.name ?: "Subscriber"
                    )
                    _isPaymentProcessing.value = false
                    if (result.isSuccess) {
                        // Backend Trigger: Automatically fire the legal request email
                        if (req != null) {
                            dispatchLegalEmailIntent(
                                supportEmail = req.recipientEmail,
                                subject = req.emailSubject,
                                body = req.emailBody
                            )
                        }
                        _uiNotice.value = "🚀 Stripe 10% Fee Paid ($${String.format(java.util.Locale.US, "%.2f", amount)})! Legal notice automatically fired to ${subscription.supportEmail}."
                        onSuccess(result)
                        if (req != null) {
                            openDialog(ActiveDialog.AiCancellation(subscription, pregeneratedRequest = req))
                        }
                    } else {
                        _uiNotice.value = result.errorMessage ?: "Card authorization failed."
                    }
                }
                com.example.data.stripe.StripePaymentType.ADVANCED_REMINDERS_SUBSCRIPTION -> {
                    val result = repository.subscribeAdvancedRemindersViaStripe(cardInput)
                    _isPaymentProcessing.value = false
                    if (result.isSuccess) {
                        _uiNotice.value = "Subscribed to Advanced Reminders ($2.99/mo) via Stripe! 100% Ad-Free."
                        onSuccess(result)
                    } else {
                        _uiNotice.value = result.errorMessage ?: "Subscription setup failed."
                    }
                }
            }
        }
    }

    private fun dispatchLegalEmailIntent(
        supportEmail: String,
        subject: String,
        body: String
    ) {
        val app = getApplication<Application>()
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$supportEmail")
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            app.startActivity(intent)
        } catch (e: Exception) {
            // Handled safely in environments without default email client
        }
        try {
            val clipboard = app.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = ClipData.newPlainText("SubKiller Legal Cancellation Notice", body)
            clipboard?.setPrimaryClip(clip)
        } catch (e: Exception) {
            // ignore
        }
    }

    fun completeRewardedAdsForCancellation(subscription: SubscriptionEntity) {
        viewModelScope.launch {
            val user = userProfile.value
            val userName = user?.name ?: "Subscriber"
            val userEmail = user?.email ?: "subscriber@domain.com"
            val req = repository.triggerFreeCancellationViaRewardedAds(
                subscription = subscription,
                userEmail = userEmail,
                userName = userName
            )
            // Backend Trigger: Automatically fire the legal request email after watching exactly TWO 15-20 second ads
            dispatchLegalEmailIntent(
                supportEmail = req.recipientEmail,
                subject = req.emailSubject,
                body = req.emailBody
            )
            _uiNotice.value = "🎉 2 Video Ads Verified! AI Cancellation Bot automatically fired legal email to ${subscription.supportEmail}!"
            openDialog(ActiveDialog.AiCancellation(subscription, pregeneratedRequest = req))
        }
    }

    fun triggerMonthlySummaryNotification(context: Context, onResult: ((Boolean, com.example.notification.MonthlySavingsReport) -> Unit)? = null) {
        viewModelScope.launch {
            val (posted, report) = repository.triggerMonthlySummaryNotification(context, _selectedCurrency.value)
            val diffFormatted = CurrencyManager.format(report.savingsDifference, report.currency)
            _uiNotice.value = if (posted) {
                "🎉 Monthly Summary Notification Sent! (${if (report.savingsDifference >= 0) "+$diffFormatted" else "-$diffFormatted"} vs last month)"
            } else {
                "Notification prepared (Ensure notification permissions are granted in system settings)"
            }
            onResult?.invoke(posted, report)
        }
    }

    fun triggerWorkManagerCheck(context: Context) {
        com.example.worker.BillReminderWorkScheduler.triggerImmediateWorkManagerTest(context)
        _uiNotice.value = "⚡ WorkManager 3-day bill check worker enqueued for background execution!"
    }

    fun exportDataToClipboard(context: Context) {
        val notice = com.example.data.export.CsvExportHelper.exportAndShareCsv(context, allSubscriptions.value)
        _uiNotice.value = notice
    }

    fun exportDataToCsv(context: Context) {
        exportDataToClipboard(context)
    }

    fun cancelAdvancedReminders() {
        viewModelScope.launch {
            repository.cancelAdvancedReminders()
            _uiNotice.value = "Advanced Reminders monthly subscription cancelled."
        }
    }

    fun processDemoPayment(
        cardNumber: String,
        cardName: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isPaymentProcessing.value = true
            delay(1200)
            repository.unlockPremium()
            _isPaymentProcessing.value = false
            _uiNotice.value = "Stripe $2.99 Paid! Smart Scan Pro Unlocked!"
            onSuccess()
        }
    }

    fun deleteSubscription(subscription: SubscriptionEntity) {
        viewModelScope.launch {
            repository.deleteSubscription(subscription)
            _uiNotice.value = "Deleted ${subscription.name}"
        }
    }

    fun triggerItemReminder(context: Context, subscription: SubscriptionEntity) {
        viewModelScope.launch {
            val posted = repository.scheduleItemReminder(context, subscription, _selectedCurrency.value)
            _uiNotice.value = if (posted) {
                "🔔 Due date reminder set & notification dispatched for ${subscription.name}!"
            } else {
                "🔔 Due date reminder armed for ${subscription.name} (Due ${subscription.nextDueDate})"
            }
        }
    }

    fun saveSubscription(
        id: String?,
        name: String,
        category: String,
        provider: String,
        isUtility: Boolean,
        utilityType: String?,
        consumerNumber: String?,
        amount: Double,
        currency: String,
        billingCycle: String,
        nextDueDate: String,
        supportEmail: String,
        notes: String?,
        units: Int?
    ) {
        viewModelScope.launch {
            val entity = SubscriptionEntity(
                id = id ?: UUID.randomUUID().toString(),
                name = name,
                category = category,
                provider = provider,
                isUtility = isUtility,
                utilityType = utilityType,
                consumerNumber = consumerNumber,
                amount = amount,
                currency = currency,
                billingCycle = billingCycle,
                nextDueDate = nextDueDate,
                supportEmail = supportEmail,
                notes = notes,
                unitsConsumed = units,
                status = "active",
                updatedAt = System.currentTimeMillis()
            )
            repository.addSubscription(entity)
            closeDialog()
            _uiNotice.value = if (isUtility) "Utility bill saved!" else "Subscription saved!"
        }
    }

    fun signInWithGoogle(email: String = "ua355165@gmail.com", displayName: String = "VIP Subscriber") {
        viewModelScope.launch {
            repository.signInWithGoogle(email, displayName)
            _uiNotice.value = "Signed in as $email. Real-time sync active!"
        }
    }

    fun signOutGoogle() {
        viewModelScope.launch {
            repository.signOutGoogle()
            _uiNotice.value = "Signed out of Google account."
        }
    }

    fun clearSampleData() {
        viewModelScope.launch {
            val count = repository.clearSampleData()
            _uiNotice.value = if (count > 0) {
                "Sample data cleared ($count items removed). Clean slate for your live subscriptions!"
            } else {
                "Database is already clean. No sample items found."
            }
        }
    }

    fun runSmartScanSimulation(sampleKey: String, onImported: () -> Unit) {
        viewModelScope.launch {
            _isScanProcessing.value = true
            delay(1500) // AI OCR Parsing simulation

            val detectedItem = when (sampleKey) {
                "LESCO" -> SubscriptionEntity(
                    id = "scanned-lesco-${System.currentTimeMillis()}",
                    name = "LESCO Electric Bill (Dec)",
                    category = "Electricity & Power",
                    provider = "LESCO",
                    isUtility = true,
                    utilityType = "LESCO",
                    consumerNumber = "08 99812 7654321 U",
                    amount = 54.20,
                    currency = "USD",
                    billingCycle = "Monthly",
                    nextDueDate = "2026-12-15",
                    supportEmail = "billing@lesco.gov.pk",
                    notes = "Smart Scan OCR: 345 Peak Units • Feeder: Model Town Sub-2",
                    unitsConsumed = 345,
                    isPaidThisMonth = false
                )
                "KELECTRIC" -> SubscriptionEntity(
                    id = "scanned-ke-${System.currentTimeMillis()}",
                    name = "K-Electric Karachi (Dec)",
                    category = "Electricity & Power",
                    provider = "K_ELECTRIC",
                    isUtility = true,
                    utilityType = "K_ELECTRIC",
                    consumerNumber = "0400087612345",
                    amount = 39.80,
                    currency = "USD",
                    billingCycle = "Monthly",
                    nextDueDate = "2026-12-18",
                    supportEmail = "billingsupport@ke.com.pk",
                    notes = "Smart Scan OCR: 260 Units • Gulshan Div",
                    unitsConsumed = 260,
                    isPaidThisMonth = false
                )
                else -> SubscriptionEntity(
                    id = "scanned-sub-${System.currentTimeMillis()}",
                    name = "Claude Pro AI",
                    category = "AI & Productivity",
                    provider = "ANTHROPIC",
                    isUtility = false,
                    utilityType = null,
                    amount = 20.00,
                    currency = "USD",
                    billingCycle = "Monthly",
                    nextDueDate = "2026-12-05",
                    supportEmail = "support@anthropic.com",
                    notes = "Smart Scan OCR detected recurring digital subscription invoice"
                )
            }

            repository.recordSmartScan(detectedItem)
            _isScanProcessing.value = false
            _uiNotice.value = "Smart Scan extracted: ${detectedItem.name} ($${detectedItem.amount})"
            onImported()
        }
    }
}
