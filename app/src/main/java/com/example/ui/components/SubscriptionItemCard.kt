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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Psychology
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
import com.example.ui.theme.NetflixRed
import com.example.ui.theme.OpenAITeal
import com.example.ui.theme.PurplePremium
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SubscriptionItemCard(
    subscription: SubscriptionEntity,
    currentCurrency: CurrencyCode,
    onAiCancel: (SubscriptionEntity) -> Unit,
    onDelete: (SubscriptionEntity) -> Unit,
    onRemindMe: (SubscriptionEntity) -> Unit = {},
    onClick: ((SubscriptionEntity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val brandColor = when (subscription.provider.uppercase()) {
        "NETFLIX" -> NetflixRed
        "SPOTIFY" -> SpotifyGreen
        "OPENAI" -> OpenAITeal
        else -> CyanAccent
    }

    val displayAmount = CurrencyManager.formatConverted(
        amount = subscription.amount,
        sourceCurrency = if (subscription.currency.equals("PKR", ignoreCase = true)) CurrencyCode.PKR else CurrencyCode.USD,
        displayCurrency = currentCurrency
    )

    val isNegotiating = subscription.status == "negotiating" || subscription.status == "requested"
    val isCancelled = subscription.status == "cancelled"

    var showContextMenu by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(FinanceCard)
            .border(
                1.dp,
                if (isNegotiating) AmberPending.copy(alpha = 0.5f) else FinanceBorder,
                RoundedCornerShape(16.dp)
            )
            .combinedClickable(
                onClick = { onClick?.invoke(subscription) },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showContextMenu = true
                }
            )
            .padding(14.dp)
            .testTag("subscription_card_${subscription.id}")
    ) {
        Column {
            // Top Row: Brand Icon + Title/Category + Status Pill + Delete
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
                        val icon = when (subscription.provider.uppercase()) {
                            "NETFLIX" -> Icons.Default.Movie
                            "SPOTIFY" -> Icons.Default.MusicNote
                            "OPENAI" -> Icons.Default.Psychology
                            else -> Icons.Default.CreditCard
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = subscription.name,
                            tint = brandColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = subscription.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "${subscription.category} • ${subscription.billingCycle}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Status Badge
                    val (badgeText, badgeColor) = when {
                        isCancelled -> "Cancelled" to CrimsonAlert
                        isNegotiating -> "AI Pending" to AmberPending
                        else -> "Active" to EmeraldGlow
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = badgeColor
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = { onDelete(subscription) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Row: Price & Renewal Date + One-Click AI Cancellation Button
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
                        text = "Renews: ${subscription.nextDueDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }

                if (!isCancelled) {
                    // One-Click AI Cancellation Trigger Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isNegotiating) AmberPending.copy(alpha = 0.18f)
                                else PurplePremium.copy(alpha = 0.18f)
                            )
                            .border(
                                1.dp,
                                if (isNegotiating) AmberPending.copy(alpha = 0.5f)
                                else PurplePremium.copy(alpha = 0.5f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onAiCancel(subscription) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("ai_cancel_trigger_${subscription.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "One-Click AI Cancellation",
                                tint = if (isNegotiating) AmberPending else PurplePremium,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isNegotiating) "View AI Notice" else "AI Cancel",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isNegotiating) AmberPending else PurplePremium
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
                .testTag("context_menu_${subscription.id}")
        ) {
            // Header: Service Name
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Column {
                    Text(
                        text = subscription.name,
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
                                text = "Alert for ${subscription.nextDueDate}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                },
                onClick = {
                    showContextMenu = false
                    onRemindMe(subscription)
                },
                modifier = Modifier.testTag("context_remind_me_${subscription.id}")
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
                    onDelete(subscription)
                },
                modifier = Modifier.testTag("context_delete_${subscription.id}")
            )
        }
    }
}

