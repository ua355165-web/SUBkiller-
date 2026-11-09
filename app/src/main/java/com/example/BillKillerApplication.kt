package com.example

import android.app.Application
import com.example.worker.BillReminderWorkScheduler

class BillKillerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Automatically initialize and enqueue the 24-hour periodic bill reminder WorkManager
        // so it schedules itself even if the app process is killed, closed, or restarted.
        BillReminderWorkScheduler.schedulePeriodicBillCheck(this)
    }
}
