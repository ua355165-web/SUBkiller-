package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.data.model.SubscriptionEntity
import com.example.data.model.UserProfileEntity
import com.example.ui.components.ProminentGreenBarChartCard
import com.example.ui.theme.AmberPending
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.FinanceBorder
import com.example.ui.theme.FinanceCard
import com.example.ui.theme.FinanceCardElevated
import com.example.ui.theme.FinanceObsidian
import com.example.ui.theme.KElectricGreen
import com.example.ui.theme.LescoOrange
import com.example.ui.theme.PurplePremium
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.FinancialSummary

@Composable
fun InsightsScreen(
    summary: FinancialSummary,
    subscriptions: List<SubscriptionEntity>,
    currentCurrency: CurrencyCode,
    userProfile: UserProfileEntity? = null,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FinanceObsidian)
            .testTag("insights_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Column {
                Text(
                    text = "Financial Insights",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = TextPrimary
                )
                Text(
                    text = "Deep dive into recurring spend & utility burn",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        // Dynamic Green Bar Chart in Insights matching Room Database state
        item {
            val liveMonthlySpendUsd = remember(subscriptions) {
                subscriptions.filter { it.status == "active" || it.status == "negotiating" }.sumOf { item ->
                    val sourceCur = if (item.currency.equals("PKR", ignoreCase = true)) com.example.data.model.CurrencyCode.PKR else com.example.data.model.CurrencyCode.USD
                    com.example.data.model.CurrencyManager.convert(item.amount, sourceCur, com.example.data.model.CurrencyCode.USD)
                }
            }
            val liveSavedUsd = remember(subscriptions, userProfile) {
                val dbSaved = userProfile?.totalSaved ?: 0.0
                val cancelledSaved = subscriptions.filter { it.status == "cancelled" }.sumOf { item ->
                    val sourceCur = if (item.currency.equals("PKR", ignoreCase = true)) com.example.data.model.CurrencyCode.PKR else com.example.data.model.CurrencyCode.USD
                    com.example.data.model.CurrencyManager.convert(item.amount, sourceCur, com.example.data.model.CurrencyCode.USD)
                }
                Math.max(dbSaved, cancelledSaved)
            }

            ProminentGreenBarChartCard(
                activeMonthlySpendUsd = liveMonthlySpendUsd,
                cumulativeSavedUsd = liveSavedUsd,
                summary = summary,
                currentCurrency = currentCurrency
            )
        }

        // Category Spend Distribution Card (Currency Normalized)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(FinanceCard)
                    .border(1.dp, FinanceBorder, RoundedCornerShape(18.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PieChart,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CATEGORY BREAKDOWN",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                                color = TextPrimary
                            )
                        }

                        Text(
                            text = summary.totalMonthlyFormatted,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = EmeraldGlow
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val activeItems = subscriptions.filter { it.status == "active" || it.status == "negotiating" }
                    val itemsWithUsd = activeItems.map { item ->
                        val usdAmount = if (item.currency.equals("PKR", ignoreCase = true)) {
                            com.example.data.model.CurrencyManager.convert(item.amount, com.example.data.model.CurrencyCode.PKR, com.example.data.model.CurrencyCode.USD)
                        } else {
                            item.amount
                        }
                        Pair(item, usdAmount)
                    }
                    val totalSpendUsd = itemsWithUsd.sumOf { it.second }
                    val categoriesMap = itemsWithUsd.groupBy { it.first.category }

                    val computedCategories = if (totalSpendUsd > 0.0 && categoriesMap.isNotEmpty()) {
                        categoriesMap.map { (cat, pairs) ->
                            val catSumUsd = pairs.sumOf { it.second }
                            val pct = ((catSumUsd / totalSpendUsd) * 100).toInt()
                            val color = when {
                                cat.contains("Electric", ignoreCase = true) || cat.contains("Power", ignoreCase = true) || cat.contains("Utility", ignoreCase = true) -> LescoOrange
                                cat.contains("AI", ignoreCase = true) || cat.contains("Productivity", ignoreCase = true) -> CyanAccent
                                cat.contains("Telecom", ignoreCase = true) || cat.contains("Internet", ignoreCase = true) || cat.contains("Fiber", ignoreCase = true) -> EmeraldGlow
                                else -> PurplePremium
                            }
                            Triple(cat, "$pct%", color)
                        }.sortedByDescending { it.second.replace("%", "").toIntOrNull() ?: 0 }
                    } else {
                        listOf(
                            Triple("Add subscriptions to see breakdown", "0%", CyanAccent)
                        )
                    }

                    computedCategories.forEach { (cat, pct, color) ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = cat, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text(text = pct, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(FinanceObsidian)
                            ) {
                                val weight = pct.replace("%", "").toFloatOrNull() ?: 10f
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(weight / 100f)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(color)
                                )
                            }
                        }
                    }
                }
            }
        }

        // LESCO vs K-Electric Tariff & Consumption Intelligence Card (Dynamic from Room DB)
        item {
            val lescoBill = subscriptions.firstOrNull { it.provider.equals("LESCO", ignoreCase = true) }
            val kelectBill = subscriptions.firstOrNull { it.provider.equals("K_ELECTRIC", ignoreCase = true) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(FinanceCard)
                    .border(1.dp, LescoOrange.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = LescoOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "UTILITY TARIFF AUDIT (LESCO & KE)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // LESCO Breakdown
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(FinanceObsidian)
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(text = "LESCO Lahore", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = LescoOrange)
                                if (lescoBill != null) {
                                    val units = lescoBill.unitsConsumed
                                    Text(
                                        text = if (units != null && units > 0) "$units kWh units" else "Active Account",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = lescoBill.consumerNumber?.let { "Ref: $it" } ?: "Tariff: A-1 General",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted,
                                        maxLines = 1
                                    )
                                    Text(text = "Peak: 6pm - 10pm", style = MaterialTheme.typography.labelSmall, color = AmberPending)
                                } else {
                                    Text(
                                        text = "No Bill Tracked",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = TextMuted
                                    )
                                    Text(
                                        text = "Tap + to add LESCO",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary,
                                        maxLines = 1
                                    )
                                    Text(text = "Unlinked", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                }
                            }
                        }

                        // K-Electric Breakdown
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(FinanceObsidian)
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(text = "K-Electric Karachi", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = KElectricGreen)
                                if (kelectBill != null) {
                                    val units = kelectBill.unitsConsumed
                                    Text(
                                        text = if (units != null && units > 0) "$units kWh units" else "Active Account",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = kelectBill.consumerNumber?.let { "Acc: $it" } ?: "Clifton Division",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted,
                                        maxLines = 1
                                    )
                                    Text(text = "Off-Peak Optimized", style = MaterialTheme.typography.labelSmall, color = EmeraldGlow)
                                } else {
                                    Text(
                                        text = "No Bill Tracked",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = TextMuted
                                    )
                                    Text(
                                        text = "Tap + to add KE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary,
                                        maxLines = 1
                                    )
                                    Text(text = "Unlinked", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

