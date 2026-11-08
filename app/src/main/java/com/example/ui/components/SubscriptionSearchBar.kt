package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.FinanceBorder
import com.example.ui.theme.FinanceCard
import com.example.ui.theme.FinanceCardElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.DueDateRange

@Composable
fun SubscriptionSearchBar(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    selectedDueDateRange: DueDateRange,
    onDueDateRangeSelected: (DueDateRange) -> Unit,
    onClearFilters: () -> Unit,
    matchingCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val hasActiveFilter = searchQuery.isNotBlank() || selectedDueDateRange != DueDateRange.ALL

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Search Input Box
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = {
                Text(
                    text = "Search by name, provider (e.g. Netflix, LESCO)...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    fontSize = 13.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search icon",
                    tint = CyanAccent,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchQueryChanged("") },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("clear_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = FinanceCardElevated,
                unfocusedContainerColor = FinanceCard,
                focusedBorderColor = CyanAccent,
                unfocusedBorderColor = FinanceBorder,
                cursorColor = CyanAccent,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("subscriptions_search_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Upcoming Due Date Range Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = CyanAccent,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "UPCOMING DUE RANGE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = TextMuted
            )

            Spacer(modifier = Modifier.weight(1f))

            if (hasActiveFilter) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onClearFilters() }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .testTag("clear_filters_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterAltOff,
                        contentDescription = "Reset filter",
                        tint = CyanAccent,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Reset ($matchingCount/$totalCount)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = CyanAccent
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Due Date Filter Chips Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("due_date_filter_row")
        ) {
            items(DueDateRange.entries.toTypedArray()) { range ->
                val isSelected = selectedDueDateRange == range
                val chipBorderColor = when {
                    isSelected && range == DueDateRange.OVERDUE -> Color(0xFFEF4444)
                    isSelected -> CyanAccent
                    else -> FinanceBorder
                }
                val chipBg = when {
                    isSelected && range == DueDateRange.OVERDUE -> Color(0xFFEF4444).copy(alpha = 0.18f)
                    isSelected -> CyanAccent.copy(alpha = 0.18f)
                    else -> FinanceCard
                }
                val textColor = when {
                    isSelected && range == DueDateRange.OVERDUE -> Color(0xFFF87171)
                    isSelected -> CyanAccent
                    else -> TextMuted
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(chipBg)
                        .border(1.dp, chipBorderColor, RoundedCornerShape(10.dp))
                        .clickable { onDueDateRangeSelected(range) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("due_date_chip_${range.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = range.displayName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp
                        ),
                        color = textColor
                    )
                }
            }
        }
    }
}
