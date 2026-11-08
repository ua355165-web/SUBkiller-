package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ui.viewmodel.SubTrackViewModel

/**
 * UI_Screen_Dashboard
 * Direct wrapper for DashboardScreen ensuring exact file naming compatibility
 * with VIP animations, Room DB live reactivity, and Material 3 design system.
 */
@Composable
fun UI_Screen_Dashboard(
    viewModel: SubTrackViewModel,
    modifier: Modifier = Modifier
) {
    DashboardScreen(
        viewModel = viewModel,
        modifier = modifier
    )
}
