package com.example.ui.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CurrencyCode
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.FinanceBorder
import com.example.ui.theme.FinanceCard
import com.example.ui.theme.PurplePremium
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

import androidx.compose.material.icons.filled.Mic

@Composable
fun TopHeaderBar(
    currentCurrency: CurrencyCode,
    isPremium: Boolean,
    onToggleCurrency: () -> Unit,
    onOpenSmartScan: () -> Unit,
    onOpenSocialShare: () -> Unit,
    onOpenAudioTranscribe: () -> Unit = {},
    onLockBiometrically: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Identity with Glowing Shield
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.testTag("app_identity_row")
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(EmeraldPrimary, CyanAccent)
                        )
                    )
                    .padding(1.5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(11.dp))
                        .background(FinanceCard),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "SubKiller Icon",
                        tint = EmeraldGlow,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "SubKiller",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        ),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    if (isPremium) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "VIP Member",
                            tint = PurplePremium,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
                Text(
                    text = if (isPremium) "VIP Black Edition" else "VIP Finance Edition",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isPremium) PurplePremium else EmeraldGlow
                )
            }
        }

        // Action controls: Currency Toggle + Smart Scan + Share
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Currency Toggle Pill (USD <-> PKR)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(FinanceCard)
                    .border(1.dp, FinanceBorder, RoundedCornerShape(20.dp))
                    .clickable { onToggleCurrency() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("currency_toggle_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CurrencyExchange,
                        contentDescription = "Change Currency",
                        tint = EmeraldGlow,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (currentCurrency == CurrencyCode.USD) "$ USD" else "Rs PKR",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }
            }

            // Smart Scan Button with Lock badge if free
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isPremium) CyanAccent.copy(alpha = 0.15f) else FinanceCard)
                    .border(
                        1.dp,
                        if (isPremium) CyanAccent.copy(alpha = 0.5f) else FinanceBorder,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onOpenSmartScan() }
                    .padding(8.dp)
                    .testTag("smart_scan_top_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DocumentScanner,
                        contentDescription = "Smart Scan",
                        tint = if (isPremium) CyanAccent else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    if (!isPremium) {
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Pro Feature",
                            tint = PurplePremium,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            // Audio Transcribe Mic Button
            IconButton(
                onClick = onOpenAudioTranscribe,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(CyanAccent.copy(alpha = 0.15f))
                    .border(1.dp, CyanAccent.copy(alpha = 0.5f), CircleShape)
                    .testTag("audio_transcribe_top_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Audio Transcribe",
                    tint = CyanAccent,
                    modifier = Modifier.size(17.dp)
                )
            }

            // Biometric Quick Lock Button
            if (onLockBiometrically != null) {
                IconButton(
                    onClick = onLockBiometrically,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(FinanceCard)
                        .border(1.dp, FinanceBorder, CircleShape)
                        .testTag("biometric_quick_lock_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock App Biometrically",
                        tint = EmeraldGlow,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Social Share Button
            IconButton(
                onClick = onOpenSocialShare,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(FinanceCard)
                    .border(1.dp, FinanceBorder, CircleShape)
                    .testTag("social_share_top_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share Card",
                    tint = TextPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
