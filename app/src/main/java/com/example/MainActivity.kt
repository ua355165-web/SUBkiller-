package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.example.ui.screens.DashboardScreen
import com.example.ui.theme.FinanceObsidian
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.SubTrackViewModel
import com.example.worker.BillReminderWorkScheduler

class MainActivity : FragmentActivity() {
    private val viewModel: SubTrackViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Schedule WorkManager 3-day due date background worker
        BillReminderWorkScheduler.schedulePeriodicBillCheck(this)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = FinanceObsidian
                ) {
                    DashboardScreen(viewModel = viewModel)
                }
            }
        }
    }
}
