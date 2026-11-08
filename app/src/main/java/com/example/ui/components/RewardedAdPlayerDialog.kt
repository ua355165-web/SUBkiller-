package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.SubscriptionEntity
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.FinanceBorder
import com.example.ui.theme.FinanceCard
import com.example.ui.theme.FinanceObsidian
import com.example.ui.theme.FinanceSurface
import com.example.ui.theme.PurplePremium
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun RewardedAdPlayerDialog(
    subscription: SubscriptionEntity,
    onBothAdsCompleted: () -> Unit,
    onDismiss: () -> Unit
) {
    var currentAdIndex by remember { mutableIntStateOf(1) } // 1 or 2
    var secondsRemaining by remember { mutableIntStateOf(18) }
    val totalSecondsForAd = if (currentAdIndex == 1) 18 else 16

    val adSponsor = if (currentAdIndex == 1) "Google Pixel 9 Pro • Gemini AI" else "NordVPN Enterprise • Cyber Shield"
    val adIcon = if (currentAdIndex == 1) Icons.Default.Smartphone else Icons.Default.VpnKey
    val adColor = if (currentAdIndex == 1) CyanAccent else PurplePremium

    // Countdown timer ticker
    LaunchedEffect(currentAdIndex) {
        secondsRemaining = if (currentAdIndex == 1) 18 else 16
        while (secondsRemaining > 0) {
            delay(1000)
            secondsRemaining--
        }
    }

    val isCurrentAdFinished = secondsRemaining == 0
    val progressFraction = 1f - (secondsRemaining.toFloat() / totalSecondsForAd.toFloat())

    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 300),
        label = "ad_progress"
    )

    Dialog(onDismissRequest = {
        // Can only dismiss if completed or user actively closes
        onDismiss()
    }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(FinanceSurface)
                .border(
                    1.5.dp,
                    Brush.verticalGradient(listOf(adColor, EmeraldPrimary)),
                    RoundedCornerShape(24.dp)
                )
                .padding(20.dp)
                .testTag("rewarded_ad_player_dialog")
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(FinanceCard)
                            .border(1.dp, FinanceBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "SPONSORED AD ($currentAdIndex OF 2)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            ),
                            color = adColor
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Simulated High-Definition Video Player Viewport
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(FinanceObsidian)
                        .border(1.dp, adColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(adColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = adIcon,
                                contentDescription = null,
                                tint = adColor,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = adSponsor,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (isCurrentAdFinished) "Video Completed ✓" else "Playing Sponsor Clip (${secondsRemaining}s)",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isCurrentAdFinished) EmeraldGlow else TextMuted
                        )
                    }

                    // Top Right Live Timer Pill
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isCurrentAdFinished) "Reward Ready" else "${secondsRemaining}s remaining",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isCurrentAdFinished) EmeraldGlow else Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Continuous Progress Bar
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = adColor,
                    trackColor = FinanceCard
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Ad Explanation
                Text(
                    text = if (currentAdIndex == 1) {
                        "Watching Ad 1 of 2 to unlock free legal cancellation for ${subscription.name}."
                    } else {
                        "Almost done! Finishing Ad 2 of 2 will immediately launch your free AI cancellation notice."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action Button: Either next ad or claim reward
                if (currentAdIndex == 1) {
                    Button(
                        onClick = { currentAdIndex = 2 },
                        enabled = isCurrentAdFinished,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("continue_to_ad_2_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCurrentAdFinished) CyanAccent else FinanceCard,
                            contentColor = if (isCurrentAdFinished) Color(0xFF082F49) else TextMuted
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isCurrentAdFinished) "Proceed to Ad 2 of 2 →" else "Please watch ${secondsRemaining}s...",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                } else {
                    Button(
                        onClick = onBothAdsCompleted,
                        enabled = isCurrentAdFinished,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("claim_free_cancellation_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCurrentAdFinished) EmeraldPrimary else FinanceCard,
                            contentColor = if (isCurrentAdFinished) Color.White else TextMuted
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isCurrentAdFinished) "Claim Free AI Cancellation ✓" else "Reward in ${secondsRemaining}s...",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
