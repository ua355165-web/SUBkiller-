package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.data.model.CurrencyCode
import com.example.data.model.CurrencyManager
import com.example.data.model.SubscriptionEntity

data class MonthlySavingsReport(
    val currentMonthSavings: Double,
    val previousMonthSavings: Double,
    val savingsDifference: Double,
    val activeMonthlyBillsTotal: Double,
    val cancelledServicesCount: Int,
    val topCancelledService: String?,
    val currency: CurrencyCode = CurrencyCode.USD
)

object MonthlySummaryNotificationManager {
    const val CHANNEL_ID = "subkiller_monthly_summary_channel"
    const val CHANNEL_NAME = "Monthly Bill Savings Summary"
    const val NOTIFICATION_ID = 2001

    const val REMINDER_CHANNEL_ID = "subkiller_reminders_channel"
    const val REMINDER_CHANNEL_NAME = "Bill Due Date Reminders"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Monthly summary notifications comparing bill savings with previous months"
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun postMonthlySummaryNotification(context: Context, report: MonthlySavingsReport): Boolean {
        createNotificationChannel(context)

        val currentFormatted = CurrencyManager.format(report.currentMonthSavings, report.currency)
        val prevFormatted = CurrencyManager.format(report.previousMonthSavings, report.currency)
        val diffFormatted = CurrencyManager.format(Math.abs(report.savingsDifference), report.currency)
        val activeFormatted = CurrencyManager.format(report.activeMonthlyBillsTotal, report.currency)

        val hasSavedMore = report.savingsDifference > 0.0
        val isFirstRecordedMonth = report.previousMonthSavings == 0.0 && report.currentMonthSavings > 0.0

        val title = when {
            hasSavedMore && !isFirstRecordedMonth -> "🎉 Saved $diffFormatted MORE on Bills This Month!"
            isFirstRecordedMonth -> "🎉 Monthly Savings Summary: $currentFormatted Saved!"
            report.currentMonthSavings > 0.0 -> "📊 Monthly Savings Report: $currentFormatted Total"
            else -> "📊 Monthly Bill Summary: $activeFormatted Active Spend"
        }

        val shortSummary = when {
            hasSavedMore && !isFirstRecordedMonth -> "You cut recurring costs by $currentFormatted ($diffFormatted ahead of last month)!"
            isFirstRecordedMonth -> "Your Room Database records $currentFormatted saved across ${report.cancelledServicesCount} optimized bills."
            report.currentMonthSavings > 0.0 -> "Total monthly bill savings: $currentFormatted across ${report.cancelledServicesCount} cancelled services."
            else -> "Tracking $activeFormatted active monthly utilities & subscriptions in Room DB."
        }

        val bigText = buildString {
            appendLine("SubKiller Monthly Savings Comparison:")
            appendLine("• This Month's Savings: $currentFormatted")
            appendLine("• Previous Month Savings: $prevFormatted")
            if (!isFirstRecordedMonth) {
                appendLine("• Month-Over-Month Net: ${if (report.savingsDifference >= 0) "+$diffFormatted" else "-$diffFormatted"}")
            }
            appendLine("• Active Recurring Commitments: $activeFormatted/mo")
            if (!report.topCancelledService.isNullOrBlank()) {
                appendLine("• Key Optimization: ${report.topCancelledService}")
            } else {
                appendLine("• Real-time Room Database tracking active")
            }
            appendLine("Tap to inspect spend analytics & savings graphs!")
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(shortSummary)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        return try {
            val manager = NotificationManagerCompat.from(context)
            manager.notify(NOTIFICATION_ID, builder.build())
            true
        } catch (e: SecurityException) {
            // Handled when POST_NOTIFICATIONS is not yet granted by user
            false
        } catch (e: Exception) {
            false
        }
    }

    fun postItemReminderNotification(context: Context, subscription: SubscriptionEntity, displayCurrency: CurrencyCode): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                REMINDER_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Due date reminders for active bills and subscriptions"
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val formattedAmount = CurrencyManager.formatConverted(
            amount = subscription.amount,
            sourceCurrency = if (subscription.currency.equals("PKR", ignoreCase = true)) CurrencyCode.PKR else CurrencyCode.USD,
            displayCurrency = displayCurrency
        )

        val title = "⏰ Due Alert: ${subscription.name} ($formattedAmount)"
        val text = "Next due date: ${subscription.nextDueDate}. Review or AI Cancel before auto-renewal."

        val bigText = buildString {
            appendLine("SubKiller Due Date Alert:")
            appendLine("• Service: ${subscription.name}")
            appendLine("• Provider: ${subscription.provider}")
            appendLine("• Amount: $formattedAmount (${subscription.billingCycle})")
            appendLine("• Next Due Date: ${subscription.nextDueDate}")
            if (!subscription.consumerNumber.isNullOrBlank()) {
                appendLine("• Consumer ID: ${subscription.consumerNumber}")
            }
            appendLine("Tap to open SubKiller and inspect details or trigger AI cancellation.")
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            subscription.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        return try {
            val manager = NotificationManagerCompat.from(context)
            val notifId = (subscription.id.hashCode() and 0x7FFFFFFF) % 10000 + 3000
            manager.notify(notifId, builder.build())
            true
        } catch (e: SecurityException) {
            false
        } catch (e: Exception) {
            false
        }
    }
}

