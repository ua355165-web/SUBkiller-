package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.model.CancellationRequestEntity
import com.example.data.model.CurrencyCode
import com.example.data.model.CurrencyManager
import com.example.data.model.EventLogEntity
import com.example.data.model.SubscriptionEntity
import com.example.data.model.UserProfileEntity
import com.example.notification.MonthlySavingsReport
import com.example.notification.MonthlySummaryNotificationManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class SubscriptionRepository(private val database: AppDatabase) {
    private val subDao = database.subscriptionDao()
    private val cancellationDao = database.cancellationDao()
    private val eventLogDao = database.eventLogDao()
    private val profileDao = database.userProfileDao()

    val allSubscriptions: Flow<List<SubscriptionEntity>> = subDao.getAllSubscriptionsFlow()
    val utilityBills: Flow<List<SubscriptionEntity>> = subDao.getUtilityBillsFlow()
    val nonUtilitySubscriptions: Flow<List<SubscriptionEntity>> = subDao.getNonUtilitySubscriptionsFlow()
    val allCancellations: Flow<List<CancellationRequestEntity>> = cancellationDao.getAllCancellationsFlow()
    val allEventLogs: Flow<List<EventLogEntity>> = eventLogDao.getAllLogsFlow()
    val userProfile: Flow<UserProfileEntity?> = profileDao.getUserProfileFlow()

    suspend fun addSubscription(subscription: SubscriptionEntity) {
        subDao.insert(subscription)
        val eventType = if (subscription.isUtility) "BILL_ADDED" else "SUBSCRIPTION_ADDED"
        val title = if (subscription.isUtility) "Utility Bill Added" else "Subscription Added"
        eventLogDao.insert(
            EventLogEntity(
                relatedSubscriptionId = subscription.id,
                eventType = eventType,
                title = "$title: ${subscription.name}",
                description = "Tracked with billing cycle ${subscription.billingCycle} at $${subscription.amount} (${subscription.consumerNumber ?: "No Ref"})"
            )
        )
    }

    suspend fun recordSmartScan(subscription: SubscriptionEntity) {
        subDao.insert(subscription)
        eventLogDao.insert(
            EventLogEntity(
                relatedSubscriptionId = subscription.id,
                eventType = "SMART_SCAN_IMPORTED",
                title = "Smart Scan Imported: ${subscription.name}",
                description = "OCR detected ${subscription.provider} bill of $${subscription.amount} with due date ${subscription.nextDueDate}."
            )
        )
    }

    suspend fun updateSubscription(subscription: SubscriptionEntity) {
        subDao.update(subscription)
    }

    suspend fun deleteSubscription(subscription: SubscriptionEntity) {
        subDao.delete(subscription)
        eventLogDao.insert(
            EventLogEntity(
                relatedSubscriptionId = subscription.id,
                eventType = "ITEM_DELETED",
                title = "Removed: ${subscription.name}",
                description = "Removed from active tracking."
            )
        )
    }

    suspend fun togglePaidStatus(id: String, currentPaid: Boolean) {
        val newPaid = !currentPaid
        subDao.updatePaidStatus(id, newPaid)
        eventLogDao.insert(
            EventLogEntity(
                relatedSubscriptionId = id,
                eventType = if (newPaid) "BILL_PAID" else "BILL_UNPAID",
                title = if (newPaid) "Bill Marked as Paid" else "Bill Marked as Unpaid",
                description = "Payment status updated for this billing period."
            )
        )
    }

    suspend fun triggerAiCancellation(
        subscription: SubscriptionEntity,
        userReason: String,
        tone: String,
        userEmail: String,
        userName: String
    ): CancellationRequestEntity {
        val subject = "Formal Cancellation Request & Revocation of Authorization - Account ${subscription.consumerNumber ?: userName}"
        val dateStr = SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(Date())

        val bodyTone = when (tone) {
            "Firm & Legal" -> """
Dear ${subscription.name} Support & Billing Department,

I am writing to formally request immediate cancellation of my account and subscription (${subscription.name}), effective $dateStr.

Account / Reference ID: ${subscription.consumerNumber ?: subscription.referenceNumber ?: "Account linked to $userEmail"}
Registered Email: $userEmail
Subscriber: $userName
Primary Reason: $userReason

Per consumer protection standards and electronic funds transfer terms, I hereby explicitly revoke authorization for any future automatic recurring debits or charges to my credit card, bank account, or digital payment method associated with this service.

Please confirm in writing to $userEmail that:
1. This cancellation has been processed without cancellation fees or penalties.
2. No further charges will occur.
3. My payment details and recurring billing profiles have been deleted.

Thank you for your prompt confirmation.

Sincerely,
$userName
            """.trimIndent()

            "Price Sensitive" -> """
Dear ${subscription.name} Customer Support,

I am writing regarding my ongoing subscription (${subscription.name}). Due to recent budget restructuring and financial constraints, I am requesting cancellation of this service as of $dateStr.

Account / Reference ID: ${subscription.consumerNumber ?: subscription.referenceNumber ?: userEmail}
Reason: $userReason

If your retention team has any promotional rates, paused tier, or reduced pricing packages currently available, please let me know at $userEmail before processing the final termination. Otherwise, please proceed with the complete cancellation and confirm the final billing date.

Best regards,
$userName
            """.trimIndent()

            else -> """
Hello ${subscription.name} Team,

I would like to cancel my subscription (${subscription.name}) starting from $dateStr.

Subscriber: $userName
Registered Email: $userEmail
Reason for cancellation: $userReason

Please process this cancellation and confirm once complete with no further renewals or charges.

Thank you for your assistance.

Warm regards,
$userName
            """.trimIndent()
        }

        val cancellationRequest = CancellationRequestEntity(
            id = UUID.randomUUID().toString(),
            subscriptionId = subscription.id,
            serviceName = subscription.name,
            recipientEmail = subscription.supportEmail,
            tone = tone,
            userReason = userReason,
            emailSubject = subject,
            emailBody = bodyTone,
            status = "contacted", // State progression: requested -> contacted -> negotiating -> cancelled
            monthlySavings = subscription.amount,
            currency = subscription.currency,
            sentAt = System.currentTimeMillis(),
            lastUpdateNotes = "Email generated and dispatched via user mail client."
        )

        cancellationDao.insert(cancellationRequest)
        subDao.updateStatus(subscription.id, "negotiating")

        eventLogDao.insert(
            EventLogEntity(
                relatedSubscriptionId = subscription.id,
                eventType = "AI_CANCELLATION_TRIGGERED",
                title = "AI Cancellation Dispatched: ${subscription.name}",
                description = "Generated $tone legal cancellation notice to ${subscription.supportEmail}. Status moved to 'contacted'."
            )
        )

        return cancellationRequest
    }

    suspend fun updateCancellationStatus(
        requestId: String,
        subscriptionId: String,
        newStatus: String, // "requested", "contacted", "negotiating", "cancelled"
        notes: String?
    ) {
        val existingReq = cancellationDao.getBySubscriptionId(subscriptionId)
        if (existingReq != null) {
            cancellationDao.updateStatus(existingReq.id, newStatus, notes)
        } else {
            val sub = subDao.getSubscriptionById(subscriptionId)
            if (sub != null) {
                cancellationDao.insert(
                    CancellationRequestEntity(
                        id = requestId,
                        subscriptionId = subscriptionId,
                        serviceName = sub.name,
                        recipientEmail = sub.supportEmail,
                        tone = "Direct Cancellation",
                        userReason = notes ?: "Service status updated to $newStatus",
                        emailSubject = "Service Cancellation: ${sub.name}",
                        emailBody = "Subscription was marked as $newStatus by user.",
                        status = newStatus,
                        monthlySavings = sub.amount,
                        currency = sub.currency,
                        sentAt = System.currentTimeMillis(),
                        lastUpdateNotes = notes
                    )
                )
            } else {
                cancellationDao.updateStatus(requestId, newStatus, notes)
            }
        }

        if (newStatus == "cancelled") {
            val sub = subDao.getSubscriptionById(subscriptionId)
            if (sub != null && sub.status != "cancelled") {
                subDao.updateStatus(subscriptionId, "cancelled")
                val subAmountUsd = if (sub.currency.equals("PKR", ignoreCase = true)) {
                    CurrencyManager.convert(sub.amount, CurrencyCode.PKR, CurrencyCode.USD)
                } else {
                    sub.amount
                }
                val profile = profileDao.getUserProfile()
                val currentSaved = profile?.totalSaved ?: 0.0
                profileDao.update(
                    profile?.copy(totalSaved = currentSaved + subAmountUsd)
                        ?: UserProfileEntity(totalSaved = subAmountUsd)
                )
            } else {
                subDao.updateStatus(subscriptionId, "cancelled")
            }
        } else {
            subDao.updateStatus(subscriptionId, newStatus)
        }

        eventLogDao.insert(
            EventLogEntity(
                relatedSubscriptionId = subscriptionId,
                eventType = "STATUS_UPDATED",
                title = "Status Changed to '$newStatus'",
                description = notes ?: "Cancellation workflow updated."
            )
        )
    }

    suspend fun unlockPremium() {
        profileDao.updatePremium(true)
        eventLogDao.insert(
            EventLogEntity(
                eventType = "PREMIUM_PURCHASED",
                title = "Smart Scan Pro Unlocked ($2.99)",
                description = "Lifetime access to AI Smart Scan, LESCO/K-Electric OCR parser, and unlimited cancellation triggers activated."
            )
        )
    }

    suspend fun unlockSmartScanViaStripe(cardInput: com.example.data.stripe.StripeCardInput): com.example.data.stripe.StripePaymentResult {
        val result = com.example.data.stripe.StripePaymentManager.processSmartScanOneTimePayment(cardInput)
        if (result.isSuccess) {
            profileDao.updatePremium(true)
            eventLogDao.insert(
                EventLogEntity(
                    eventType = "STRIPE_PAYMENT_SUCCESS",
                    title = "Stripe $2.99 Paid - Smart Scan Pro",
                    description = "Card ending in ${result.cardLast4} charged $2.99. Funds routed to backend bank ${result.destinationBankAccount}. Ref: ${result.paymentIntentId}"
                )
            )
        }
        return result
    }

    suspend fun processSuccessFeeViaStripe(
        subscription: SubscriptionEntity,
        cardInput: com.example.data.stripe.StripeCardInput,
        annualSavings: Double,
        feeAmount: Double,
        reason: String,
        tone: String,
        userEmail: String,
        userName: String
    ): Pair<com.example.data.stripe.StripePaymentResult, CancellationRequestEntity?> {
        val result = com.example.data.stripe.StripePaymentManager.processCancellationSuccessFeePayment(
            cardInput = cardInput,
            subscriptionName = subscription.name,
            annualSavings = annualSavings,
            feeAmount = feeAmount
        )
        if (result.isSuccess) {
            val req = triggerAiCancellation(subscription, reason, tone, userEmail, userName)
            eventLogDao.insert(
                EventLogEntity(
                    relatedSubscriptionId = subscription.id,
                    eventType = "STRIPE_SUCCESS_FEE_PAID",
                    title = "Stripe 10% Success Fee Paid ($${String.format(java.util.Locale.US, "%.2f", feeAmount)})",
                    description = "10% fee charged on $${String.format(java.util.Locale.US, "%.2f", annualSavings)} annual savings for ${subscription.name}. Settled to backend bank ${result.destinationBankAccount}. Intent: ${result.paymentIntentId}"
                )
            )
            return Pair(result, req)
        }
        return Pair(result, null)
    }

    suspend fun triggerFreeCancellationViaRewardedAds(
        subscription: SubscriptionEntity,
        userEmail: String,
        userName: String
    ): CancellationRequestEntity {
        profileDao.addCancellationTokens(1)
        val req = triggerAiCancellation(
            subscription = subscription,
            userReason = "Monthly budget optimization (Verified via exactly 2 Rewarded Video Ads)",
            tone = "Firm & Legal",
            userEmail = userEmail,
            userName = userName
        )
        eventLogDao.insert(
            EventLogEntity(
                relatedSubscriptionId = subscription.id,
                eventType = "REWARDED_ADS_COMPLETED",
                title = "AI Cancellation Bot Triggered (2 Ads Watched)",
                description = "User completed exactly 2 sponsored video clips (15-20s). Automatic legal cancellation notice dispatched to ${subscription.supportEmail} for ${subscription.name}."
            )
        )
        return req
    }

    suspend fun unlockFreeCancellationViaRewardedAds(subscription: SubscriptionEntity) {
        profileDao.addCancellationTokens(1)
        eventLogDao.insert(
            EventLogEntity(
                relatedSubscriptionId = subscription.id,
                eventType = "REWARDED_ADS_COMPLETED",
                title = "Free AI Cancel Unlocked (2 Ads Watched)",
                description = "User completed 2 sponsored video clips (15-20s). Free AI legal notice token granted for ${subscription.name}."
            )
        )
    }

    suspend fun subscribeAdvancedRemindersViaStripe(cardInput: com.example.data.stripe.StripeCardInput): com.example.data.stripe.StripePaymentResult {
        val result = com.example.data.stripe.StripePaymentManager.processAdvancedRemindersSubscription(cardInput)
        if (result.isSuccess) {
            val subId = "sub_stripe_${System.currentTimeMillis()}"
            profileDao.updateRemindersSubscription(true, subId)
            eventLogDao.insert(
                EventLogEntity(
                    eventType = "STRIPE_SUBSCRIPTION_ACTIVE",
                    title = "Advanced Reminders Subscribed ($2.99/mo)",
                    description = "Recurring monthly subscription active via Stripe (${result.cardBrand} *${result.cardLast4}). Zero ads, priority LESCO/KE peak unit alerts. Ref: ${result.paymentIntentId}"
                )
            )
        }
        return result
    }

    suspend fun cancelAdvancedReminders() {
        com.example.data.stripe.StripePaymentManager.cancelRecurringSubscription("sub_active")
        profileDao.updateRemindersSubscription(false, null)
        eventLogDao.insert(
            EventLogEntity(
                eventType = "STRIPE_SUBSCRIPTION_CANCELLED",
                title = "Advanced Reminders Cancelled",
                description = "Monthly recurring billing of $2.99 stopped in Stripe Elements."
            )
        )
    }

    suspend fun setPreferredCurrency(currencyCode: String) {
        profileDao.updateCurrency(currencyCode)
        eventLogDao.insert(
            EventLogEntity(
                eventType = "CURRENCY_CHANGED",
                title = "Currency Switched",
                description = "Display currency updated to $currencyCode."
            )
        )
    }

    suspend fun signInWithGoogle(email: String, displayName: String) {
        val googleId = "goog_" + email.hashCode().toString().replace("-", "")
        profileDao.updateGoogleAuth(
            email = email,
            name = displayName,
            isSignedIn = true,
            googleId = googleId
        )

        // Force-Connect Room Database: Purge any stale demo/sample data so only real user data is displayed
        subDao.clearSampleData()

        eventLogDao.insert(
            EventLogEntity(
                eventType = "GOOGLE_SIGN_IN",
                title = "Signed In with Google",
                description = "Authenticated as $email ($displayName). Force-connected to Room Database DAO with zero mock data."
            )
        )
    }

    suspend fun signOutGoogle() {
        profileDao.signOutGoogle()
        eventLogDao.insert(
            EventLogEntity(
                eventType = "GOOGLE_SIGN_OUT",
                title = "Signed Out",
                description = "Google session ended."
            )
        )
    }

    suspend fun clearSampleData(): Int {
        val count = subDao.clearSampleData()
        if (count > 0) {
            eventLogDao.insert(
                EventLogEntity(
                    eventType = "SAMPLE_DATA_PURGED",
                    title = "Cleared Sample Data",
                    description = "Removed $count demo subscriptions and utilities. Workspace is ready for user live data."
                )
            )
        }
        return count
    }

    suspend fun updateAvatarUri(uri: String?) {
        profileDao.updateAvatarUri(uri)
        eventLogDao.insert(
            EventLogEntity(
                eventType = "AVATAR_UPDATED",
                title = if (uri != null) "Profile Picture Updated" else "Profile Picture Removed",
                description = "User avatar image successfully updated in Room Database."
            )
        )
    }

    suspend fun updateSecurityPin(pin: String) {
        profileDao.updateSecurityPin(pin)
        eventLogDao.insert(
            EventLogEntity(
                eventType = "SECURITY_PIN_CHANGED",
                title = "Security Passcode Updated",
                description = "App security unlock PIN successfully updated."
            )
        )
    }

    suspend fun computeMonthlySavingsReport(displayCurrency: CurrencyCode): MonthlySavingsReport {
        val allSubs = subDao.getAllSubscriptionsFlow().first()
        val profile = profileDao.getUserProfile()

        val activeSubs = allSubs.filter { it.status == "active" || it.status == "negotiating" }
        val cancelledSubs = allSubs.filter { it.status == "cancelled" }

        val activeMonthlyTotal = activeSubs.sumOf { item ->
            val sourceCur = if (item.currency.equals("PKR", ignoreCase = true)) CurrencyCode.PKR else CurrencyCode.USD
            CurrencyManager.convert(item.amount, sourceCur, displayCurrency)
        }

        // Calculate current month savings from actual cancelled subscriptions and profile totalSaved in Room DB
        val totalCancelledAmountUsd = cancelledSubs.sumOf { item ->
            val sourceCur = if (item.currency.equals("PKR", ignoreCase = true)) CurrencyCode.PKR else CurrencyCode.USD
            CurrencyManager.convert(item.amount, sourceCur, CurrencyCode.USD)
        }
        val currentSavedUsd = Math.max(profile?.totalSaved ?: 0.0, totalCancelledAmountUsd)
        val prevSavedUsd = profile?.previousMonthSaved ?: 0.0

        val currentMonthSavings = CurrencyManager.convert(currentSavedUsd, CurrencyCode.USD, displayCurrency)
        val previousMonthSavings = CurrencyManager.convert(prevSavedUsd, CurrencyCode.USD, displayCurrency)
        val diff = currentMonthSavings - previousMonthSavings

        val topCancelledService = cancelledSubs.maxByOrNull { it.amount }?.name

        return MonthlySavingsReport(
            currentMonthSavings = currentMonthSavings,
            previousMonthSavings = previousMonthSavings,
            savingsDifference = diff,
            activeMonthlyBillsTotal = activeMonthlyTotal,
            cancelledServicesCount = cancelledSubs.size,
            topCancelledService = topCancelledService,
            currency = displayCurrency
        )
    }

    suspend fun triggerMonthlySummaryNotification(context: Context, displayCurrency: CurrencyCode): Pair<Boolean, MonthlySavingsReport> {
        val report = computeMonthlySavingsReport(displayCurrency)
        val posted = MonthlySummaryNotificationManager.postMonthlySummaryNotification(context, report)

        val profile = profileDao.getUserProfile()
        val currentSavedUsd = profile?.totalSaved ?: 0.0
        // Record timestamp and snapshot current savings as previous month baseline for next month's comparison
        profileDao.recordMonthlyNotificationSent(currentSavedUsd)

        val diffFormatted = CurrencyManager.format(report.savingsDifference, displayCurrency)
        val currentFormatted = CurrencyManager.format(report.currentMonthSavings, displayCurrency)

        eventLogDao.insert(
            EventLogEntity(
                eventType = "MONTHLY_SUMMARY_NOTIFICATION_PUSHED",
                title = "Monthly Savings Report Pushed",
                description = "Pushed local summary: Saved $currentFormatted (Net: $diffFormatted vs previous month) across ${report.cancelledServicesCount} cancelled bills."
            )
        )

        return Pair(posted, report)
    }

    suspend fun scheduleItemReminder(
        context: Context,
        subscription: SubscriptionEntity,
        displayCurrency: CurrencyCode
    ): Boolean {
        val posted = MonthlySummaryNotificationManager.postItemReminderNotification(context, subscription, displayCurrency)
        val formattedAmount = CurrencyManager.formatConverted(
            amount = subscription.amount,
            sourceCurrency = if (subscription.currency.equals("PKR", ignoreCase = true)) CurrencyCode.PKR else CurrencyCode.USD,
            displayCurrency = displayCurrency
        )
        eventLogDao.insert(
            EventLogEntity(
                relatedSubscriptionId = subscription.id,
                eventType = "REMINDER_ARMED",
                title = "Due Alert Armed: ${subscription.name}",
                description = "Quick reminder armed for due date ${subscription.nextDueDate} ($formattedAmount). Notification ${if (posted) "dispatched" else "registered (system notification permissions pending)"}."
            )
        )
        return posted
    }
}

