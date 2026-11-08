package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.stripe.StripePaymentType
import com.example.ui.components.AddEditSubscriptionDialog
import com.example.ui.components.AdvancedRemindersDialog
import com.example.ui.components.AiCancellationDialog
import com.example.ui.components.AiCancellationModeSelectDialog
import com.example.ui.components.AppNavTab
import com.example.ui.components.EventLogDialog
import com.example.ui.components.FilterChipRow
import com.example.ui.components.PremiumUpgradeDialog
import com.example.ui.components.ProminentGreenBarChartCard
import com.example.ui.components.RewardedAdPlayerDialog
import com.example.ui.components.SmartScanDialog
import com.example.ui.components.SocialShareDialog
import com.example.ui.components.SpendSummaryCard
import com.example.ui.components.StandardBannerAdCard
import com.example.ui.components.StripePaymentSheetDialog
import com.example.ui.components.SubKillerBottomNavBar
import com.example.ui.components.SubscriptionItemCard
import com.example.ui.components.SubscriptionSearchBar
import com.example.ui.components.TopHeaderBar
import com.example.ui.components.UtilityBillCard
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.OutlinedButton
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.FinanceBorder
import com.example.ui.theme.FinanceCard
import com.example.ui.theme.FinanceObsidian
import com.example.ui.theme.LescoOrange
import com.example.ui.theme.PurplePremium
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.ActiveDialog
import com.example.ui.viewmodel.DashboardFilter
import com.example.ui.viewmodel.DueDateRange
import com.example.ui.viewmodel.SubTrackViewModel
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: SubTrackViewModel,
    modifier: Modifier = Modifier
) {
    val allSubscriptions by viewModel.allSubscriptions.collectAsState()
    val filteredSubscriptions by viewModel.filteredSubscriptions.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val currentCurrency by viewModel.selectedCurrency.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedDueDateRange by viewModel.selectedDueDateRange.collectAsState()
    val activeDialog by viewModel.activeDialog.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val isPaymentProcessing by viewModel.isPaymentProcessing.collectAsState()
    val isScanProcessing by viewModel.isScanProcessing.collectAsState()
    val uiNotice by viewModel.uiNotice.collectAsState()
    val eventLogs by viewModel.allEventLogs.collectAsState()
    val isBiometricLocked by viewModel.isBiometricLocked.collectAsState()
    val biometricError by viewModel.biometricError.collectAsState()

    var currentTab by remember { mutableStateOf(AppNavTab.HOME) }
    var showEventLogsDialog by remember { mutableStateOf(false) }

    val isPremium = userProfile?.isPremium ?: false
    val isRemindersActive = userProfile?.isAdvancedRemindersActive ?: false
    val snackbarHostState = remember { SnackbarHostState() }

    val utilitiesList = allSubscriptions.filter { it.isUtility }
    val regularSubsList = allSubscriptions.filter { !it.isUtility }

    val liveMonthlySpendUsd = remember(allSubscriptions) {
        allSubscriptions.filter { it.status == "active" || it.status == "negotiating" }.sumOf { item ->
            val sourceCur = if (item.currency.equals("PKR", ignoreCase = true)) com.example.data.model.CurrencyCode.PKR else com.example.data.model.CurrencyCode.USD
            com.example.data.model.CurrencyManager.convert(item.amount, sourceCur, com.example.data.model.CurrencyCode.USD)
        }
    }
    val liveSavedUsd = userProfile?.totalSaved ?: 0.0

    val infiniteTransition = rememberInfiniteTransition(label = "vip_ambient_motion")
    val vipGlowPulse by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "vip_glow_pulse"
    )

    LaunchedEffect(uiNotice) {
        uiNotice?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearNotice()
        }
    }

    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        viewModel.triggerMonthlySummaryNotification(context)
    }

    val onPushMonthlyNotification = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                viewModel.triggerMonthlySummaryNotification(context)
            } else {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            viewModel.triggerMonthlySummaryNotification(context)
        }
    }

    if (isBiometricLocked) {
        val activity = context as? androidx.fragment.app.FragmentActivity
        BiometricLockScreen(
            onTriggerBiometric = {
                if (activity != null) {
                    viewModel.triggerBiometricPrompt(activity)
                } else {
                    viewModel.unlockAppWithSecurityCode()
                }
            },
            onBypassWithPasscode = { pin ->
                viewModel.unlockWithPasscode(pin)
            },
            errorMessage = biometricError
        )
        return
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(FinanceObsidian),
        containerColor = FinanceObsidian,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            SubKillerBottomNavBar(
                currentTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = currentTab == AppNavTab.HOME || currentTab == AppNavTab.SUBSCRIPTIONS,
                enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                exit = scaleOut(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)) + fadeOut()
            ) {
                FloatingActionButton(
                    onClick = { viewModel.openDialog(ActiveDialog.EditSubscription(isUtilityDefault = false)) },
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.testTag("fab_add_item")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Item",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    val forward = targetState.ordinal > initialState.ordinal
                    (fadeIn(animationSpec = tween(220, easing = LinearOutSlowInEasing)) +
                            slideInHorizontally(animationSpec = tween(240, easing = LinearOutSlowInEasing)) { width ->
                                if (forward) width / 14 else -width / 14
                            }).togetherWith(
                        fadeOut(animationSpec = tween(180, easing = FastOutLinearInEasing))
                    )
                },
                label = "tab_navigation_motion",
                modifier = Modifier.fillMaxSize()
            ) { activeTab ->
                when (activeTab) {
                AppNavTab.HOME -> {
                    // TAB 1: VIP HOME DASHBOARD
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("home_screen_content"),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        // Top Header Bar with SubKiller branding & quick currency switch
                        item {
                            TopHeaderBar(
                                currentCurrency = currentCurrency,
                                isPremium = isPremium,
                                onToggleCurrency = { viewModel.toggleCurrency() },
                                onOpenSmartScan = { viewModel.openDialog(ActiveDialog.SmartScan) },
                                onOpenSocialShare = { viewModel.openDialog(ActiveDialog.SocialShare) },
                                onLockBiometrically = { viewModel.lockAppBiometrically() }
                            )
                        }

                        // Prominent Green Bar Charts/Graphs for Total Savings & Monthly Spend
                        item {
                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                ProminentGreenBarChartCard(
                                    summary = summary,
                                    currentCurrency = currentCurrency,
                                    activeMonthlySpendUsd = liveMonthlySpendUsd,
                                    cumulativeSavedUsd = liveSavedUsd
                                )
                            }
                        }

                        // Spend Summary Card (Total monthly burn, annualized, AI savings)
                        item {
                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                                SpendSummaryCard(
                                    summary = summary,
                                    currentCurrency = currentCurrency,
                                    onToggleCurrency = { viewModel.toggleCurrency() }
                                )
                            }
                        }

                        // Quick Action Bar (+ Add Utility, + Add Sub, Reminders, Logs)
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(FinanceCard)
                                        .border(1.dp, LescoOrange.copy(alpha = 0.25f + vipGlowPulse * 0.40f), RoundedCornerShape(12.dp))
                                        .clickable {
                                            viewModel.openDialog(ActiveDialog.EditSubscription(isUtilityDefault = true))
                                        }
                                        .padding(vertical = 10.dp, horizontal = 6.dp)
                                        .testTag("quick_add_utility_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = LescoOrange,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "+ Utility",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimary
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(FinanceCard)
                                        .border(1.dp, CyanAccent.copy(alpha = 0.25f + vipGlowPulse * 0.40f), RoundedCornerShape(12.dp))
                                        .clickable {
                                            viewModel.openDialog(ActiveDialog.EditSubscription(isUtilityDefault = false))
                                        }
                                        .padding(vertical = 10.dp, horizontal = 6.dp)
                                        .testTag("quick_add_sub_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CreditCard,
                                            contentDescription = null,
                                            tint = CyanAccent,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "+ Sub",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimary
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1.1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(FinanceCard)
                                        .border(1.dp, if (isRemindersActive) EmeraldGlow.copy(alpha = 0.45f + vipGlowPulse * 0.50f) else CyanAccent.copy(alpha = 0.25f + vipGlowPulse * 0.40f), RoundedCornerShape(12.dp))
                                        .clickable {
                                            viewModel.openDialog(ActiveDialog.AdvancedReminders)
                                        }
                                        .padding(vertical = 10.dp, horizontal = 6.dp)
                                        .testTag("quick_reminders_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.NotificationsActive,
                                            contentDescription = "Alerts",
                                            tint = if (isRemindersActive) EmeraldGlow else CyanAccent,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Reminders",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isRemindersActive) EmeraldGlow else TextPrimary
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(FinanceCard)
                                        .border(1.dp, FinanceBorder, RoundedCornerShape(12.dp))
                                        .clickable { showEventLogsDialog = true }
                                        .padding(horizontal = 10.dp, vertical = 10.dp)
                                        .testTag("audit_logs_shortcut_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = "History",
                                        tint = EmeraldGlow,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        if (allSubscriptions.isEmpty()) {
                            // Empty Database State Card
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(FinanceCard)
                                        .border(1.dp, EmeraldPrimary.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                                        .padding(22.dp)
                                        .testTag("empty_database_prompt_card"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(CircleShape)
                                                .background(EmeraldPrimary.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Bolt,
                                                contentDescription = null,
                                                tint = EmeraldGlow,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Text(
                                            text = "No Bills or Subscriptions Tracked Yet",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimary
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = "Directly connected to Room Database. Add your first real bill (LESCO, K-Electric, Sui Gas, Jazz) or start an AI Smart Scan to begin tracking.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Button(
                                                onClick = { viewModel.openDialog(ActiveDialog.EditSubscription(isUtilityDefault = true)) },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(44.dp)
                                                    .testTag("empty_state_add_bill_button"),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = EmeraldPrimary,
                                                    contentColor = Color.White
                                                ),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Add First Bill",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                                )
                                            }

                                            Button(
                                                onClick = { viewModel.openDialog(ActiveDialog.SmartScan) },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(44.dp)
                                                    .testTag("empty_state_start_scan_button"),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = CyanAccent,
                                                    contentColor = Color(0xFF082F49)
                                                ),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.DocumentScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Start Scan",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Utility Bills Section (LESCO & K-Electric) Header
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = LescoOrange,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "UTILITY BILLS (LESCO & K-ELECTRIC)",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.5.sp
                                            ),
                                            color = TextPrimary
                                        )
                                    }

                                    Text(
                                        text = "${utilitiesList.size} bills",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                            }

                            if (utilitiesList.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 6.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(FinanceCard)
                                            .border(1.dp, FinanceBorder, RoundedCornerShape(12.dp))
                                            .clickable { viewModel.openDialog(ActiveDialog.EditSubscription(isUtilityDefault = true)) }
                                            .padding(14.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No utility bills tracked. Tap here to add LESCO, K-Electric, or Sui Gas bill.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            } else {
                                items(utilitiesList, key = { it.id }) { bill ->
                                    Box(
                                        modifier = Modifier
                                            .animateItem()
                                            .padding(horizontal = 16.dp, vertical = 5.dp)
                                    ) {
                                        UtilityBillCard(
                                            bill = bill,
                                            currentCurrency = currentCurrency,
                                            onTogglePaid = { id, currentPaid -> viewModel.toggleBillPaid(id, currentPaid) },
                                            onAiCancelOrAudit = { viewModel.openDialog(ActiveDialog.AiCancellationModeSelect(it)) },
                                            onDelete = { viewModel.deleteSubscription(it) },
                                            onRemindMe = { viewModel.triggerItemReminder(context, it) },
                                            onClick = { viewModel.openDialog(ActiveDialog.EditSubscription(subscription = it, isUtilityDefault = it.isUtility)) }
                                        )
                                    }
                                }
                            }

                            // Subscriptions Section Header
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CreditCard,
                                            contentDescription = null,
                                            tint = CyanAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "ACTIVE SUBSCRIPTIONS",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.5.sp
                                            ),
                                            color = TextPrimary
                                        )
                                    }

                                    Text(
                                        text = "${regularSubsList.size} services",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                            }

                            if (regularSubsList.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 6.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(FinanceCard)
                                            .border(1.dp, FinanceBorder, RoundedCornerShape(12.dp))
                                            .clickable { viewModel.openDialog(ActiveDialog.EditSubscription(isUtilityDefault = false)) }
                                            .padding(14.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No active subscriptions tracked. Tap here to add Netflix, Spotify, or Jazz package.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            } else {
                                items(regularSubsList, key = { it.id }) { sub ->
                                    Box(
                                        modifier = Modifier
                                            .animateItem()
                                            .padding(horizontal = 16.dp, vertical = 5.dp)
                                    ) {
                                        SubscriptionItemCard(
                                            subscription = sub,
                                            currentCurrency = currentCurrency,
                                            onAiCancel = { viewModel.openDialog(ActiveDialog.AiCancellationModeSelect(it)) },
                                            onDelete = { viewModel.deleteSubscription(it) },
                                            onRemindMe = { viewModel.triggerItemReminder(context, it) },
                                            onClick = { viewModel.openDialog(ActiveDialog.EditSubscription(subscription = it, isUtilityDefault = it.isUtility)) }
                                        )
                                    }
                                }
                            }
                        }

                        // Standard Banner Ad for basic free 1-day reminders (Zero ads if Advanced Reminders subscribed)
                        if (!isRemindersActive) {
                            item {
                                StandardBannerAdCard(
                                    onUpgradeClicked = { viewModel.openDialog(ActiveDialog.AdvancedReminders) },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                )
                            }
                        }
                    }
                }

                AppNavTab.SUBSCRIPTIONS -> {
                    // TAB 2: FULL SUBSCRIPTIONS & UTILITIES LIST
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("subscriptions_screen_content"),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        item {
                            TopHeaderBar(
                                currentCurrency = currentCurrency,
                                isPremium = isPremium,
                                onToggleCurrency = { viewModel.toggleCurrency() },
                                onOpenSmartScan = { viewModel.openDialog(ActiveDialog.SmartScan) },
                                onOpenSocialShare = { viewModel.openDialog(ActiveDialog.SocialShare) },
                                onLockBiometrically = { viewModel.lockAppBiometrically() }
                            )
                        }

                        item {
                            SubscriptionSearchBar(
                                searchQuery = searchQuery,
                                onSearchQueryChanged = { viewModel.setSearchQuery(it) },
                                selectedDueDateRange = selectedDueDateRange,
                                onDueDateRangeSelected = { viewModel.setDueDateRange(it) },
                                onClearFilters = { viewModel.clearSearchAndDateFilters() },
                                matchingCount = filteredSubscriptions.size,
                                totalCount = allSubscriptions.size
                            )
                        }

                        item {
                            FilterChipRow(
                                selectedFilter = selectedFilter,
                                onSelectFilter = { viewModel.setFilter(it) }
                            )
                        }

                        if (filteredSubscriptions.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 24.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(FinanceCard)
                                        .border(1.dp, FinanceBorder, RoundedCornerShape(16.dp))
                                        .padding(20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        val hasSearchOrDateFilter = searchQuery.isNotBlank() || selectedDueDateRange != DueDateRange.ALL

                                        Icon(
                                            imageVector = if (hasSearchOrDateFilter) Icons.Default.SearchOff else Icons.Default.Info,
                                            contentDescription = null,
                                            tint = if (hasSearchOrDateFilter) CyanAccent else TextMuted,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = when {
                                                hasSearchOrDateFilter -> "No matching subscriptions found"
                                                allSubscriptions.isEmpty() -> "Your Room Database is empty"
                                                else -> "No items in this category"
                                            },
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = when {
                                                hasSearchOrDateFilter -> "No active bills match '$searchQuery' for ${selectedDueDateRange.displayName}."
                                                allSubscriptions.isEmpty() -> "Tap '+ Add Item' below or start Smart Scan."
                                                else -> "Try switching the filter chips above."
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary,
                                            textAlign = TextAlign.Center
                                        )

                                        if (hasSearchOrDateFilter) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            OutlinedButton(
                                                onClick = { viewModel.clearSearchAndDateFilters() },
                                                modifier = Modifier.testTag("reset_search_empty_state_button")
                                            ) {
                                                Text("Reset Search & Filters", color = CyanAccent)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        items(filteredSubscriptions, key = { it.id }) { item ->
                            Box(
                                modifier = Modifier
                                    .animateItem()
                                    .padding(horizontal = 16.dp, vertical = 5.dp)
                            ) {
                                if (item.isUtility) {
                                    UtilityBillCard(
                                        bill = item,
                                        currentCurrency = currentCurrency,
                                        onTogglePaid = { id, currentPaid -> viewModel.toggleBillPaid(id, currentPaid) },
                                        onAiCancelOrAudit = { viewModel.openDialog(ActiveDialog.AiCancellationModeSelect(it)) },
                                        onDelete = { viewModel.deleteSubscription(it) },
                                        onRemindMe = { viewModel.triggerItemReminder(context, it) },
                                        onClick = { viewModel.openDialog(ActiveDialog.EditSubscription(subscription = it, isUtilityDefault = it.isUtility)) }
                                    )
                                } else {
                                    SubscriptionItemCard(
                                        subscription = item,
                                        currentCurrency = currentCurrency,
                                        onAiCancel = { viewModel.openDialog(ActiveDialog.AiCancellationModeSelect(it)) },
                                        onDelete = { viewModel.deleteSubscription(it) },
                                        onRemindMe = { viewModel.triggerItemReminder(context, it) },
                                        onClick = { viewModel.openDialog(ActiveDialog.EditSubscription(subscription = it, isUtilityDefault = it.isUtility)) }
                                    )
                                }
                            }
                        }

                        if (!isRemindersActive) {
                            item {
                                StandardBannerAdCard(
                                    onUpgradeClicked = { viewModel.openDialog(ActiveDialog.AdvancedReminders) },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }
                }

                AppNavTab.INSIGHTS -> {
                    // TAB 3: ANALYTICS & INSIGHTS
                    InsightsScreen(
                        summary = summary,
                        subscriptions = allSubscriptions,
                        currentCurrency = currentCurrency,
                        userProfile = userProfile
                    )
                }

                AppNavTab.PROFILE -> {
                    // TAB 4: PROFILE & VIP SETTINGS
                    ProfileScreen(
                        userProfile = userProfile,
                        currentCurrency = currentCurrency,
                        onToggleCurrency = { viewModel.toggleCurrency() },
                        onUpgradeRequested = {
                            viewModel.openDialog(
                                ActiveDialog.StripeCheckout(
                                    paymentType = StripePaymentType.SMART_SCAN_ONE_TIME,
                                    amount = 2.99,
                                    title = "Smart Scan Pro (Lifetime)",
                                    description = "LESCO & K-Electric OCR Bill Parser • Zero Ads"
                                )
                            )
                        },
                        onOpenSocialShare = { viewModel.openDialog(ActiveDialog.SocialShare) },
                        onOpenEventLogs = { showEventLogsDialog = true },
                        onOpenSmartScan = { viewModel.openDialog(ActiveDialog.SmartScan) },
                        onOpenAdvancedReminders = { viewModel.openDialog(ActiveDialog.AdvancedReminders) },
                        onSignInGoogle = { viewModel.signInWithGoogle() },
                        onSignOutGoogle = { viewModel.signOutGoogle() },
                        onClearSampleData = { viewModel.clearSampleData() },
                        onPushMonthlySummaryNotification = onPushMonthlyNotification,
                        onExportData = { viewModel.exportDataToClipboard(context) },
                        onLockBiometrically = { viewModel.lockAppBiometrically() },
                        onUpdateAvatar = { viewModel.updateProfileAvatar(it) },
                        onUpdateSecurityPin = { viewModel.updateSecurityPin(it) }
                    )
                }
            }
        }
    }
}

    // Modal Overlays
    when (val dialog = activeDialog) {
        is ActiveDialog.AiCancellationModeSelect -> {
            AiCancellationModeSelectDialog(
                subscription = dialog.subscription,
                currentCurrency = currentCurrency,
                onSelectPremiumRoute = { annualSavings, successFee ->
                    viewModel.openDialog(
                        ActiveDialog.StripeCheckout(
                            paymentType = StripePaymentType.AI_CANCELLATION_SUCCESS_FEE,
                            amount = successFee,
                            title = "10% AI Cancellation Success Fee",
                            description = "${dialog.subscription.name} • 1-Yr Savings: $${String.format(Locale.US, "%.2f", annualSavings)}",
                            subscription = dialog.subscription
                        )
                    )
                },
                onSelectFreeRoute = {
                    viewModel.openDialog(ActiveDialog.RewardedAdPlayer(dialog.subscription))
                },
                onDismiss = { viewModel.closeDialog() }
            )
        }

        is ActiveDialog.RewardedAdPlayer -> {
            RewardedAdPlayerDialog(
                subscription = dialog.subscription,
                onBothAdsCompleted = {
                    viewModel.completeRewardedAdsForCancellation(dialog.subscription)
                },
                onDismiss = { viewModel.closeDialog() }
            )
        }

        is ActiveDialog.StripeCheckout -> {
            StripePaymentSheetDialog(
                paymentType = dialog.paymentType,
                amount = dialog.amount,
                title = dialog.title,
                description = dialog.description,
                isProcessing = isPaymentProcessing,
                onConfirmStripePayment = { cardInput ->
                    viewModel.processStripeGatewayPayment(
                        cardInput = cardInput,
                        paymentType = dialog.paymentType,
                        amount = dialog.amount,
                        subscription = dialog.subscription
                    ) {
                        viewModel.closeDialog()
                    }
                },
                onDismiss = { viewModel.closeDialog() }
            )
        }

        is ActiveDialog.AdvancedReminders -> {
            AdvancedRemindersDialog(
                isSubscribed = isRemindersActive,
                onLaunchStripeSubscription = {
                    viewModel.openDialog(
                        ActiveDialog.StripeCheckout(
                            paymentType = StripePaymentType.ADVANCED_REMINDERS_SUBSCRIPTION,
                            amount = 2.99,
                            title = "Advanced Reminders Subscription",
                            description = "Recurring monthly billing of $2.99 • 100% Ad-Free Priority Delivery"
                        )
                    )
                },
                onCancelSubscription = {
                    viewModel.cancelAdvancedReminders()
                    viewModel.closeDialog()
                },
                onPushMonthlySummaryNotification = onPushMonthlyNotification,
                onTriggerWorkManagerCheck = { viewModel.triggerWorkManagerCheck(context) },
                onDismiss = { viewModel.closeDialog() }
            )
        }

        is ActiveDialog.AiCancellation -> {
            val activeUserEmail = userProfile?.email?.takeIf { it.isNotBlank() } ?: "ua355165@gmail.com"
            val activeUserName = userProfile?.name?.takeIf { it.isNotBlank() } ?: "SubKiller Subscriber"

            AiCancellationDialog(
                subscription = dialog.subscription,
                pregeneratedRequest = dialog.pregeneratedRequest,
                userEmail = activeUserEmail,
                userName = activeUserName,
                onSendEmailAndTrack = { reason, tone ->
                    viewModel.triggerAiCancellation(dialog.subscription, reason, tone) {
                        viewModel.closeDialog()
                    }
                },
                onMarkCancelled = {
                    viewModel.updateCancellationStatus(
                        requestId = dialog.pregeneratedRequest?.id ?: "manual-${System.currentTimeMillis()}",
                        subscriptionId = dialog.subscription.id,
                        newStatus = "cancelled",
                        notes = "Marked cancelled by subscriber."
                    )
                    viewModel.closeDialog()
                },
                onDismiss = { viewModel.closeDialog() }
            )
        }

        is ActiveDialog.PremiumUpgrade -> {
            // Replaced demo payment with real Stripe gateway for $2.99
            StripePaymentSheetDialog(
                paymentType = StripePaymentType.SMART_SCAN_ONE_TIME,
                amount = 2.99,
                title = "Smart Scan Pro (Lifetime)",
                description = "Automated OCR Bill Parser for LESCO & K-Electric • No Ads",
                isProcessing = isPaymentProcessing,
                onConfirmStripePayment = { cardInput ->
                    viewModel.processStripeGatewayPayment(
                        cardInput = cardInput,
                        paymentType = StripePaymentType.SMART_SCAN_ONE_TIME,
                        amount = 2.99,
                        subscription = null
                    ) {
                        viewModel.closeDialog()
                    }
                },
                onDismiss = { viewModel.closeDialog() }
            )
        }

        is ActiveDialog.SmartScan -> {
            SmartScanDialog(
                isPremium = isPremium,
                isScanning = isScanProcessing,
                onRunScanSimulation = { sampleKey ->
                    viewModel.runSmartScanSimulation(sampleKey) {
                        viewModel.closeDialog()
                    }
                },
                onUpgradeRequested = {
                    viewModel.openDialog(
                        ActiveDialog.StripeCheckout(
                            paymentType = StripePaymentType.SMART_SCAN_ONE_TIME,
                            amount = 2.99,
                            title = "Smart Scan Pro (Lifetime)",
                            description = "Automated OCR Bill Parser for LESCO & K-Electric • No Ads"
                        )
                    )
                },
                onDismiss = { viewModel.closeDialog() }
            )
        }

        is ActiveDialog.SocialShare -> {
            SocialShareDialog(
                summary = summary,
                currentCurrency = currentCurrency,
                isPremium = isPremium,
                onDismiss = { viewModel.closeDialog() }
            )
        }

        is ActiveDialog.EditSubscription -> {
            AddEditSubscriptionDialog(
                initialItem = dialog.subscription,
                isUtilityDefault = dialog.isUtilityDefault,
                currentCurrency = currentCurrency,
                onSave = { id, name, cat, prov, isUtil, utilType, consNo, amt, cur, cycle, nextDate, email, notes, units ->
                    viewModel.saveSubscription(
                        id = id,
                        name = name,
                        category = cat,
                        provider = prov,
                        isUtility = isUtil,
                        utilityType = utilType,
                        consumerNumber = consNo,
                        amount = amt,
                        currency = cur,
                        billingCycle = cycle,
                        nextDueDate = nextDate,
                        supportEmail = email,
                        notes = notes,
                        units = units
                    )
                },
                onDismiss = { viewModel.closeDialog() }
            )
        }

        else -> { /* No active modal */ }
    }

    if (showEventLogsDialog) {
        EventLogDialog(
            logs = eventLogs,
            onDismiss = { showEventLogsDialog = false }
        )
    }
}
