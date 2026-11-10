package com.example

import android.app.Application
import com.example.worker.BillReminderWorkScheduler
import com.google.firebase.FirebaseApp

class BillKillerApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Safely initialize Firebase before any component access
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        // Automatically initialize and enqueue the 24-hour periodic bill reminder WorkManager
        // so it schedules itself even if the app process is killed, closed, or restarted.
        BillReminderWorkScheduler.schedulePeriodicBillCheck(this)
    }
}
