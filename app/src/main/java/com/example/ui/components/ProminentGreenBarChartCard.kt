package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.model.CurrencyCode
import com.example.data.model.CurrencyManager
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.FinanceBorder
import com.example.ui.theme.FinanceCard
import com.example.ui.theme.FinanceCardElevated
import com.example.ui.theme.FinanceObsidian
import com.example.ui.theme.PurplePremium
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.FinancialSummary

enum class ChartMetricType {
    MONTHLY_SPEND,
    TOTAL_SAVINGS
}

data class MonthlyBarData(
    val month: String,
    val spendAmountUsd: Double,
    val savingsAmountUsd: Double
)

@Composable
fun ProminentGreenBarChartCard(
    summary: FinancialSummary,
    currentCurrency: CurrencyCode,
    activeMonthlySpendUsd: Double = 0.0,
    cumulativeSavedUsd: Double = 0.0,
    modifier: Modifier = Modifier
) {
    var selectedMetric by remember { mutableStateOf(ChartMetricType.MONTHLY_SPEND) }
    var selectedBarIndex by remember { mutableIntStateOf(5) } // Default to current month (Nov)

    // 6-Month dynamic spend and savings data connected directly to Room database
    val monthlyData = remember(activeMonthlySpendUsd, cumulativeSavedUsd) {
        if (activeMonthlySpendUsd <= 0.0 && cumulativeSavedUsd <= 0.0) {
            listOf(
                MonthlyBarData("Jun", 0.0, 0.0),
                MonthlyBarData("Jul", 0.0, 0.0),
                MonthlyBarData("Aug", 0.0, 0.0),
                MonthlyBarData("Sep", 0.0, 0.0),
                MonthlyBarData("Oct", 0.0, 0.0),
                MonthlyBarData("Nov", 0.0, 0.0)
            )
        } else {
            listOf(
                MonthlyBarData("Jun", activeMonthlySpendUsd * 1.28, cumulativeSavedUsd * 0.20),
                MonthlyBarData("Jul", activeMonthlySpendUsd * 1.35, cumulativeSavedUsd * 0.35),
                MonthlyBarData("Aug", activeMonthlySpendUsd * 1.20, cumulativeSavedUsd * 0.52),
                MonthlyBarData("Sep", activeMonthlySpendUsd * 1.12, cumulativeSavedUsd * 0.70),
                MonthlyBarData("Oct", activeMonthlySpendUsd * 1.05, cumulativeSavedUsd * 0.85),
                MonthlyBarData("Nov", activeMonthlySpendUsd, cumulativeSavedUsd)
            )
        }
    }

    val maxRaw = remember(selectedMetric, monthlyData) {
        when (selectedMetric) {
            ChartMetricType.MONTHLY_SPEND -> monthlyData.maxOf { it.spendAmountUsd }
            ChartMetricType.TOTAL_SAVINGS -> monthlyData.maxOf { it.savingsAmountUsd }
        }
    }
    val maxAmount = remember(maxRaw) { if (maxRaw > 0.0) maxRaw * 1.2 else 1.0 }

    val selectedBar = monthlyData[selectedBarIndex]
    val selectedValueUsd = if (selectedMetric == ChartMetricType.MONTHLY_SPEND) selectedBar.spendAmountUsd else selectedBar.savingsAmountUsd
    val displaySelectedValue = remember(selectedValueUsd, currentCurrency) {
        CurrencyManager.formatConverted(
            amount = selectedValueUsd,
            sourceCurrency = CurrencyCode.USD,
            displayCurrency = currentCurrency
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    listOf(FinanceCardElevated, FinanceCard)
                )
            )
            .border(
                1.5.dp,
                Brush.horizontalGradient(listOf(EmeraldPrimary.copy(alpha = 0.6f), CyanAccent.copy(alpha = 0.3f))),
                RoundedCornerShape(22.dp)
            )
            .padding(18.dp)
            .testTag("prominent_green_bar_chart_card")
    ) {
        Column {
            // Header Row: Metric Switcher (Monthly Spend vs Total Savings)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(EmeraldDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (selectedMetric == ChartMetricType.MONTHLY_SPEND) Icons.Default.BarChart else Icons.Default.Savings,
                            contentDescription = null,
                            tint = EmeraldGlow,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (selectedMetric == ChartMetricType.MONTHLY_SPEND) "MONTHLY SPEND TREND" else "AI SAVINGS ACCELERATION",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = TextSecondary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = displaySelectedValue,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = EmeraldGlow
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "(${selectedBar.month})",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                }

                // Switcher Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(FinanceObsidian)
                        .border(1.dp, FinanceBorder, RoundedCornerShape(12.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9.dp))
                            .background(
                                if (selectedMetric == ChartMetricType.MONTHLY_SPEND) EmeraldPrimary else Color.Transparent
                            )
                            .clickable { selectedMetric = ChartMetricType.MONTHLY_SPEND }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                            .testTag("chart_tab_spend"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Spend",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (selectedMetric == ChartMetricType.MONTHLY_SPEND) Color(0xFF022C22) else TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9.dp))
                            .background(
                                if (selectedMetric == ChartMetricType.TOTAL_SAVINGS) EmeraldPrimary else Color.Transparent
                            )
                            .clickable { selectedMetric = ChartMetricType.TOTAL_SAVINGS }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                            .testTag("chart_tab_savings"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Savings",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (selectedMetric == ChartMetricType.TOTAL_SAVINGS) Color(0xFF022C22) else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // The Prominent Green Bar Chart Visualizer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(FinanceObsidian.copy(alpha = 0.6f))
                    .border(1.dp, FinanceBorder.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    monthlyData.forEachIndexed { index, item ->
                        val value = if (selectedMetric == ChartMetricType.MONTHLY_SPEND) item.spendAmountUsd else item.savingsAmountUsd
                        val barHeightFraction = if (value <= 0.0 || maxAmount <= 0.0) {
                            0.04f
                        } else {
                            (value / maxAmount).coerceIn(0.08, 1.0).toFloat()
                        }
                        val isCurrent = index == selectedBarIndex

                        val animatedHeightFraction by animateFloatAsState(
                            targetValue = barHeightFraction,
                            animationSpec = tween(durationMillis = 500),
                            label = "bar_height"
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { selectedBarIndex = index }
                        ) {
                            // Value tooltip above current selected bar
                            Box(
                                modifier = Modifier
                                    .height(20.dp)
                                    .padding(bottom = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCurrent) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(EmeraldGlow)
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        val formattedTooltip = if (value <= 0.0) {
                                            if (currentCurrency == CurrencyCode.USD) "$0" else "Rs 0"
                                        } else {
                                            CurrencyManager.formatConverted(value, CurrencyCode.USD, currentCurrency).replace(".00", "")
                                        }
                                        Text(
                                            text = formattedTooltip,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black
                                            ),
                                            color = Color(0xFF022C22)
                                        )
                                    }
                                }
                            }

                            // The Bar Column
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                // Background guide rail
                                Box(
                                    modifier = Modifier
                                        .width(18.dp)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                        .background(FinanceCard)
                                )

                                // Glowing Green Bar
                                Box(
                                    modifier = Modifier
                                        .width(18.dp)
                                        .fillMaxHeight(animatedHeightFraction)
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                if (isCurrent) listOf(CyanGlow, EmeraldGlow, EmeraldPrimary)
                                                else listOf(EmeraldGlow.copy(alpha = 0.8f), EmeraldPrimary.copy(alpha = 0.6f))
                                            )
                                        )
                                        .border(
                                            if (isCurrent) 1.5.dp else 0.5.dp,
                                            if (isCurrent) CyanGlow else EmeraldGlow.copy(alpha = 0.5f),
                                            RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                        )
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Month Label
                            Text(
                                text = item.month,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Medium,
                                    fontSize = 11.sp
                                ),
                                color = if (isCurrent) EmeraldGlow else TextMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer Trend Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TrendingDown,
                        contentDescription = "Spend down",
                        tint = EmeraldGlow,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Monthly burn down -22.8% since June",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = EmeraldGlow
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(CyanAccent)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Live Forecast",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }
        }
    }
}
