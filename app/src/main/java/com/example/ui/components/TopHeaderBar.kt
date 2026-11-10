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
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
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
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Identity with Glowing Shield
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f, fill = false)
                .testTag("app_identity_row")
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
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
                        .clip(RoundedCornerShape(9.dp))
                        .background(FinanceCard),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "SubKiller Icon",
                        tint = EmeraldGlow,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "SubKiller",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.3.sp
                        ),
                        color = TextPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    if (isPremium) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "VIP Member",
                            tint = PurplePremium,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
                Text(
                    text = if (isPremium) "VIP Black Edition" else "VIP Finance",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = if (isPremium) PurplePremium else EmeraldGlow,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Action controls: Currency Toggle + Smart Scan + Mic + Lock + Share
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Currency Toggle Pill (USD <-> PKR)
            Box(
                modifier = Modifier
                    .height(34.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(FinanceCard)
                    .border(1.dp, FinanceBorder, RoundedCornerShape(17.dp))
                    .clickable { onToggleCurrency() }
                    .padding(horizontal = 7.dp)
                    .testTag("currency_toggle_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CurrencyExchange,
                        contentDescription = "Change Currency",
                        tint = EmeraldGlow,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (currentCurrency == CurrencyCode.USD) "$ USD" else "Rs PKR",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            // Smart Scan Button with Lock badge if free
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isPremium) CyanAccent.copy(alpha = 0.15f) else FinanceCard)
                    .border(
                        1.dp,
                        if (isPremium) CyanAccent.copy(alpha = 0.5f) else FinanceBorder,
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { onOpenSmartScan() }
                    .testTag("smart_scan_top_button"),
                contentAlignment = Alignment.Center
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.DocumentScanner,
                        contentDescription = "Smart Scan",
                        tint = if (isPremium) CyanAccent else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    if (!isPremium) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(PurplePremium)
                        )
                    }
                }
            }

            // Audio Transcribe Mic Button
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(CyanAccent.copy(alpha = 0.15f))
                    .border(1.dp, CyanAccent.copy(alpha = 0.5f), CircleShape)
                    .clickable { onOpenAudioTranscribe() }
                    .testTag("audio_transcribe_top_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Audio Transcribe",
                    tint = CyanAccent,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Biometric Quick Lock Button
            if (onLockBiometrically != null) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(FinanceCard)
                        .border(1.dp, FinanceBorder, CircleShape)
                        .clickable { onLockBiometrically() }
                        .testTag("biometric_quick_lock_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock App Biometrically",
                        tint = EmeraldGlow,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            // Social Share Button
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(FinanceCard)
                    .border(1.dp, FinanceBorder, CircleShape)
                    .clickable { onOpenSocialShare() }
                    .testTag("social_share_top_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share Card",
                    tint = TextPrimary,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}
