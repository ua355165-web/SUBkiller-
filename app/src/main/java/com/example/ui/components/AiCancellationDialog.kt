package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.SubscriptionEntity
import com.example.ui.theme.AmberPending
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.FinanceBorder
import com.example.ui.theme.FinanceCard
import com.example.ui.theme.FinanceCardElevated
import com.example.ui.theme.FinanceObsidian
import com.example.ui.theme.FinanceSurface
import com.example.ui.theme.PurplePremium
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.rememberCoroutineScope
import com.example.data.ai.GeminiCancellationService
import kotlinx.coroutines.launch

@Composable
fun AiCancellationDialog(
    subscription: SubscriptionEntity,
    userEmail: String,
    userName: String,
    pregeneratedRequest: com.example.data.model.CancellationRequestEntity? = null,
    onSendEmailAndTrack: (reason: String, tone: String) -> Unit,
    onMarkCancelled: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTone by remember { mutableStateOf(pregeneratedRequest?.tone ?: "Firm & Legal") }
    var userReason by remember {
        mutableStateOf(
            pregeneratedRequest?.userReason ?: if (subscription.isUtility) "Service address relocation & tariff review"
            else "Reducing monthly recurring digital expenses"
        )
    }

    var isGeminiGenerating by remember { mutableStateOf(false) }

    val dateStr = SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(Date())
    val defaultSubject = "Formal Cancellation Request - ${subscription.name} [Account: ${subscription.consumerNumber ?: userName}]"
    val subject = pregeneratedRequest?.emailSubject ?: defaultSubject

    val fallbackBody = remember(subscription, selectedTone, userReason, userEmail, userName) {
        GeminiCancellationService.buildFallbackTemplate(subscription, userEmail, userName, selectedTone, userReason)
    }

    val initialBody = pregeneratedRequest?.emailBody ?: fallbackBody
    var editableBody by remember(initialBody) { mutableStateOf(initialBody) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(FinanceSurface)
                .border(1.dp, PurplePremium.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(20.dp)
                .testTag("ai_cancellation_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PurplePremium.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Cancellation",
                                tint = PurplePremium,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "One-Click AI Cancellation",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Service: ${subscription.name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (pregeneratedRequest != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(EmeraldDark.copy(alpha = 0.35f))
                            .border(1.dp, EmeraldGlow.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = EmeraldGlow,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Legal Notice Fired Automatically ✓",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldGlow
                                )
                                Text(
                                    text = "Dispatched to ${pregeneratedRequest.recipientEmail}. Tracked in live Room Database DAO.",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Tone Selector
                Text(
                    text = "SELECT AI DRAFT TONE",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val tones = listOf(
                        "Firm & Legal" to Icons.Default.Gavel,
                        "Price Sensitive" to Icons.Default.PriceChange,
                        "Polite & Formal" to Icons.Default.Handshake
                    )
                    tones.forEach { (tone, icon) ->
                        val isSelected = selectedTone == tone
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) PurplePremium.copy(alpha = 0.25f) else FinanceCard
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) PurplePremium else FinanceBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedTone = tone }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = tone,
                                    tint = if (isSelected) PurplePremium else TextMuted,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = tone.split(" ").first(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) TextPrimary else TextMuted
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Reason input
                Text(
                    text = "REASON FOR CANCELLATION",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = userReason,
                    onValueChange = { userReason = it },
                    modifier = Modifier.fillMaxWidth().testTag("cancellation_reason_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurplePremium,
                        unfocusedBorderColor = FinanceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = FinanceCard,
                        unfocusedContainerColor = FinanceCard
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Recipient & Subject Info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(FinanceCard)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Target Recipient",
                        tint = CyanAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "From: $userEmail (Auto-attached login email)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldGlow
                        )
                        Text(
                            text = "To: ${subscription.supportEmail}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Subject: $subject",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Generated Draft Preview (Editable)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GEMINI AI LEGAL DRAFT",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                        color = TextMuted
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PurplePremium.copy(alpha = 0.2f))
                            .border(1.dp, PurplePremium.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .clickable(enabled = !isGeminiGenerating) {
                                scope.launch {
                                    isGeminiGenerating = true
                                    val aiDraft = GeminiCancellationService.generateCancellationEmail(
                                        subscription = subscription,
                                        userEmail = userEmail,
                                        userName = userName,
                                        tone = selectedTone,
                                        reason = userReason
                                    )
                                    editableBody = aiDraft
                                    isGeminiGenerating = false
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("gemini_ai_draft_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isGeminiGenerating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 1.5.dp,
                                    color = PurplePremium
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Drafting...",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                    color = PurplePremium
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = PurplePremium,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "✨ Draft with Gemini AI",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                    color = PurplePremium
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = editableBody,
                    onValueChange = { editableBody = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp, max = 180.dp)
                        .testTag("cancellation_body_input"),
                    textStyle = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurplePremium,
                        unfocusedBorderColor = FinanceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextSecondary,
                        focusedContainerColor = FinanceObsidian,
                        unfocusedContainerColor = FinanceObsidian
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Trigger Action: Launch real email intent & save state to database
                Button(
                    onClick = {
                        // 1. Copy draft to clipboard as backup
                        try {
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("SubKiller Cancellation Notice", editableBody)
                            clipboard?.setPrimaryClip(clip)
                        } catch (e: Exception) {
                            // ignore clipboard errors
                        }

                        // 2. Dispatch Email Intent with auto-attached authenticated sender & carrier target
                        try {
                            val mailUri = Uri.parse("mailto:${subscription.supportEmail}?subject=${Uri.encode(subject)}&body=${Uri.encode(editableBody)}")
                            val intent = Intent(Intent.ACTION_SENDTO, mailUri).apply {
                                putExtra(Intent.EXTRA_EMAIL, arrayOf(subscription.supportEmail))
                                putExtra(Intent.EXTRA_CC, arrayOf(userEmail))
                                putExtra(Intent.EXTRA_SUBJECT, subject)
                                putExtra(Intent.EXTRA_TEXT, editableBody)
                                putExtra("EXTRA_REPLY_TO", arrayOf(userEmail))
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Cancellation draft copied for $userEmail", Toast.LENGTH_SHORT).show()
                        }
                        // 3. Track in database & log events
                        onSendEmailAndTrack(userReason, selectedTone)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("send_cancellation_email_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PurplePremium,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Trigger Email",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (pregeneratedRequest != null) "Re-dispatch / Open Email App" else "Dispatch Cancellation Notice",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Secondary direct mark as cancelled simulation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(FinanceCard)
                        .border(1.dp, FinanceBorder, RoundedCornerShape(10.dp))
                        .clickable { onMarkCancelled() }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Mark Cancelled",
                            tint = EmeraldGlow,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Mark As Successfully Cancelled",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}
