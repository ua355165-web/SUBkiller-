package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
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

        // Strictly enforce locked / unauthenticated state on cold start
        viewModel.resetAuthenticationState()

        // Safely initialize Firebase on startup before any UI or Firebase operations
        try {
            if (com.google.firebase.FirebaseApp.getApps(this).isEmpty()) {
                com.google.firebase.FirebaseApp.initializeApp(this)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        // Configure App Check with launch intent before any UI or Firebase operations
        com.example.security.configureAppCheck(this, intent)

        try {
            BillReminderWorkScheduler.schedulePeriodicBillCheck(this)
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        // Observe process lifecycle to automatically lock app whenever it goes into the background
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_STOP) {
                    viewModel.lockAppBiometrically(showNotice = false)
                }
            }
        )

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
