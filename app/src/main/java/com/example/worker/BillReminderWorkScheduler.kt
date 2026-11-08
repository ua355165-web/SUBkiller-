package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object BillReminderWorkScheduler {
    private const val TAG = "BillWorkScheduler"
    const val UNIQUE_PERIODIC_WORK_NAME = "subkiller_bill_due_reminder_periodic_work"
    const val UNIQUE_ONETIME_WORK_NAME = "subkiller_bill_due_reminder_onetime_work"

    fun schedulePeriodicBillCheck(context: Context) {
        try {
            val workManager = WorkManager.getInstance(context)
            val periodicRequest = PeriodicWorkRequestBuilder<BillDueReminderWorker>(1, TimeUnit.DAYS)
                .build()

            workManager.enqueueUniquePeriodicWork(
                UNIQUE_PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
            )
            Log.d(TAG, "Scheduled 24h periodic WorkManager task for bill due reminders.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule periodic WorkManager job", e)
        }
    }

    fun triggerImmediateWorkManagerTest(context: Context) {
        try {
            val workManager = WorkManager.getInstance(context)
            val oneTimeRequest = OneTimeWorkRequestBuilder<BillDueReminderWorker>()
                .build()

            workManager.enqueueUniqueWork(
                UNIQUE_ONETIME_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                oneTimeRequest
            )
            Log.d(TAG, "Enqueued immediate one-time WorkManager test execution.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enqueue immediate WorkManager task", e)
        }
    }
}
