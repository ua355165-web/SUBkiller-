package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.AppDatabase
import com.example.data.model.CurrencyCode
import com.example.data.model.EventLogEntity
import com.example.notification.MonthlySummaryNotificationManager
import com.example.ui.viewmodel.DueDateCalculator
import kotlinx.coroutines.flow.first

class BillDueReminderWorker(
    private val appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Executing WorkManager bill due date background check...")
            val database = AppDatabase.getDatabase(appContext)
            val subDao = database.subscriptionDao()
            val eventLogDao = database.eventLogDao()

            val allSubscriptions = subDao.getAllSubscriptionsFlow().first()
            val activeBills = allSubscriptions.filter { it.status == "active" || it.status == "negotiating" }

            var alertsSentCount = 0

            val userProfile = database.userProfileDao().getUserProfile()
            val userPreferredCurrency = if (userProfile?.preferredCurrency.equals("PKR", ignoreCase = true)) {
                CurrencyCode.PKR
            } else {
                CurrencyCode.USD
            }

            for (bill in activeBills) {
                // Focus on LESCO, K-Electric, Sui Gas, or any utility bill due within 3 days
                val daysUntilDue = DueDateCalculator.calculateDaysUntilDue(bill.nextDueDate)

                // Trigger alert if bill is due in 3 days or less (including due today or overdue) and unpaid
                if (daysUntilDue != null && daysUntilDue <= 3 && !bill.isPaidThisMonth) {
                    val billCurrency = if (bill.currency.equals("PKR", ignoreCase = true)) CurrencyCode.PKR else userPreferredCurrency
                    val posted = MonthlySummaryNotificationManager.postItemReminderNotification(
                        context = appContext,
                        subscription = bill,
                        displayCurrency = billCurrency
                    )

                    if (posted) {
                        alertsSentCount++
                        eventLogDao.insert(
                            EventLogEntity(
                                relatedSubscriptionId = bill.id,
                                eventType = "WORKMANAGER_REMINDER_SENT",
                                title = "WorkManager Alert: ${bill.name}",
                                description = "Background WorkManager triggered 3-day due alert for ${bill.name} (Due in $daysUntilDue days on ${bill.nextDueDate})."
                            )
                        )
                    }
                }
            }

            Log.d(TAG, "WorkManager bill check complete. Dispatched $alertsSentCount notifications.")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error executing BillDueReminderWorker", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "BillDueReminderWorker"
    }
}
