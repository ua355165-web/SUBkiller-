package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CurrencyCode
import com.example.data.model.CurrencyManager
import com.example.data.model.SubscriptionEntity
import com.example.ui.theme.AmberPending
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.FinanceBorder
import com.example.ui.theme.FinanceCard
import com.example.ui.theme.FinanceCardElevated
import com.example.ui.theme.KElectricGreen
import com.example.ui.theme.LescoOrange
import com.example.ui.theme.PtclCyan
import com.example.ui.theme.PurplePremium
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UtilityBillCard(
    bill: SubscriptionEntity,
    currentCurrency: CurrencyCode,
    onTogglePaid: (String, Boolean) -> Unit,
    onAiCancelOrAudit: (SubscriptionEntity) -> Unit,
    onDelete: (SubscriptionEntity) -> Unit,
    onRemindMe: (SubscriptionEntity) -> Unit = {},
    onClick: ((SubscriptionEntity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val brandColor = remember(bill.provider) {
        when (bill.provider.uppercase()) {
            "LESCO" -> LescoOrange
            "K_ELECTRIC" -> KElectricGreen
            "PTCL" -> PtclCyan
            "SNGPL" -> AmberPending
            else -> CyanAccent
        }
    }

    val displayAmount = remember(bill.amount, bill.currency, currentCurrency) {
        CurrencyManager.formatConverted(
            amount = bill.amount,
            sourceCurrency = if (bill.currency.equals("PKR", ignoreCase = true)) CurrencyCode.PKR else CurrencyCode.USD,
            displayCurrency = currentCurrency
        )
    }

    var showContextMenu by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .clip(RoundedCornerShape(16.dp))
            .background(FinanceCard)
            .border(1.dp, FinanceBorder, RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = { onClick?.invoke(bill) },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showContextMenu = true
                }
            )
            .padding(14.dp)
            .testTag("utility_bill_card_${bill.id}")
    ) {
        Column {
            // Header Row: Provider Badge + Status + Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(brandColor.copy(alpha = 0.15f))
                            .border(1.dp, brandColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        val icon = when (bill.utilityType?.uppercase()) {
                            "GAS" -> Icons.Default.LocalGasStation
                            "INTERNET" -> Icons.Default.Wifi
                            else -> Icons.Default.Bolt
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = bill.provider,
                            tint = brandColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = bill.name,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Utility tag
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(brandColor.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = bill.provider,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = brandColor
                                )
                            }
                        }

                        bill.consumerNumber?.let { consumerId ->
                            Text(
                                text = "Consumer # $consumerId",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                }

                // Delete Action
                IconButton(
                    onClick = { onDelete(bill) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Utility",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Units Consumed & Feeder notes if available
            if (bill.notes != null || bill.unitsConsumed != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(FinanceCardElevated)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ElectricMeter,
                            contentDescription = "Meter Units",
                            tint = brandColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = bill.notes ?: "${bill.unitsConsumed ?: 0} Units Recorded",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }

                    bill.unitsConsumed?.let { units ->
                        Text(
                            text = "$units kWh",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Bottom Row: Amount & Actions (Paid toggle & AI Cancel/Dispute)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = displayAmount,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = TextPrimary
                    )
                    Text(
                        text = "Due: ${bill.nextDueDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (bill.isPaidThisMonth) TextMuted else AmberPending
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Mark as Paid toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (bill.isPaidThisMonth) EmeraldPrimary.copy(alpha = 0.2f) else FinanceCardElevated
                            )
                            .border(
                                1.dp,
                                if (bill.isPaidThisMonth) EmeraldGlow else FinanceBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onTogglePaid(bill.id, bill.isPaidThisMonth) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("toggle_paid_${bill.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Paid status",
                                tint = if (bill.isPaidThisMonth) EmeraldGlow else TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (bill.isPaidThisMonth) "Paid ✓" else "Mark Paid",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (bill.isPaidThisMonth) EmeraldGlow else TextSecondary
                            )
                        }
                    }

                    // AI Cancellation / Audit Notice button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PurplePremium.copy(alpha = 0.15f))
                            .border(1.dp, PurplePremium.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable { onAiCancelOrAudit(bill) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("ai_cancel_button_${bill.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Cancel / Dispute",
                                tint = PurplePremium,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "AI Cancel",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = PurplePremium
                            )
                        }
                    }
                }
            }
        }

        // Long-Press Context Menu for immediate 'Delete' or 'Remind Me' without opening details
        DropdownMenu(
            expanded = showContextMenu,
            onDismissRequest = { showContextMenu = false },
            modifier = Modifier
                .background(FinanceCardElevated)
                .border(1.dp, FinanceBorder, RoundedCornerShape(14.dp))
                .testTag("context_menu_${bill.id}")
        ) {
            // Header: Utility Bill Name
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Column {
                    Text(
                        text = bill.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary,
                        maxLines = 1
                    )
                    Text(
                        text = "Quick Actions (Long-Press)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(FinanceBorder)
            )

            // 1. Immediate 'Remind Me'
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyanAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Remind Me",
                                tint = CyanAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Remind Me",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Alert for due date ${bill.nextDueDate}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                },
                onClick = {
                    showContextMenu = false
                    onRemindMe(bill)
                },
                modifier = Modifier.testTag("context_remind_me_${bill.id}")
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(FinanceBorder.copy(alpha = 0.5f))
            )

            // 2. Immediate 'Delete'
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CrimsonAlert.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete",
                                tint = CrimsonAlert,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Delete",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = CrimsonAlert
                            )
                            Text(
                                text = "Remove immediately from database",
                                style = MaterialTheme.typography.labelSmall,
                                color = CrimsonAlert.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                onClick = {
                    showContextMenu = false
                    onDelete(bill)
                },
                modifier = Modifier.testTag("context_delete_${bill.id}")
            )
        }
    }
}

