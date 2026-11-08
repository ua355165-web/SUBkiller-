package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.FinanceBorder
import com.example.ui.theme.FinanceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodel.DashboardFilter

@Composable
fun FilterChipRow(
    selectedFilter: DashboardFilter,
    onSelectFilter: (DashboardFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = listOf(
        DashboardFilter.ALL to "All",
        DashboardFilter.UTILITIES to "⚡ Utilities",
        DashboardFilter.SUBSCRIPTIONS to "💳 Subscriptions",
        DashboardFilter.PENDING_ACTION to "⚠️ Action"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        filters.forEach { (filter, label) ->
            val isSelected = selectedFilter == filter
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isSelected) EmeraldPrimary.copy(alpha = 0.2f) else FinanceCard
                    )
                    .border(
                        1.dp,
                        if (isSelected) EmeraldGlow else FinanceBorder,
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { onSelectFilter(filter) }
                    .padding(vertical = 8.dp)
                    .testTag("filter_chip_${filter.name}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isSelected) EmeraldGlow else TextMuted
                )
            }
        }
    }
}
