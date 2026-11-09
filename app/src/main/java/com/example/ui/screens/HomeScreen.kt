package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ui.viewmodel.SubTrackViewModel

/**
 * HomeScreen
 * Seamless wrapper for DashboardScreen ensuring exact naming compatibility
 * with 60/120fps smooth scrolling performance, keyed item rendering, and Room DB reactivity.
 */
@Composable
fun HomeScreen(
    viewModel: SubTrackViewModel,
    modifier: Modifier = Modifier
) {
    DashboardScreen(
        viewModel = viewModel,
        modifier = modifier
    )
}
