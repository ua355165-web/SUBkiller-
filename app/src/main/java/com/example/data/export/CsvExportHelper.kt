package com.example.data.export

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.example.data.model.SubscriptionEntity
import java.io.File
import java.io.FileWriter

object CsvExportHelper {
    private const val TAG = "CsvExportHelper"

    fun generateCsvString(subscriptions: List<SubscriptionEntity>): String {
        val sb = StringBuilder()
        // Standard Financial CSV Headers
        sb.append("ID,Name,Category,Provider,Is Utility,Utility Type,Consumer Number,Amount,Currency,Billing Cycle,Next Due Date,Status,Paid This Month,Units Consumed,Support Email,Notes\n")

        for (sub in subscriptions) {
            val fields = listOf(
                escapeCsv(sub.id),
                escapeCsv(sub.name),
                escapeCsv(sub.category),
                escapeCsv(sub.provider),
                sub.isUtility.toString(),
                escapeCsv(sub.utilityType ?: ""),
                escapeCsv(sub.consumerNumber ?: ""),
                sub.amount.toString(),
                escapeCsv(sub.currency),
                escapeCsv(sub.billingCycle),
                escapeCsv(sub.nextDueDate),
                escapeCsv(sub.status),
                sub.isPaidThisMonth.toString(),
                (sub.unitsConsumed ?: 0).toString(),
                escapeCsv(sub.supportEmail),
                escapeCsv(sub.notes ?: "")
            )
            sb.append(fields.joinToString(",")).append("\n")
        }
        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        val containsSpecial = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")
        val escaped = value.replace("\"", "\"\"")
        return if (containsSpecial) "\"$escaped\"" else escaped
    }

    fun exportAndShareCsv(context: Context, subscriptions: List<SubscriptionEntity>): String {
        val csvContent = generateCsvString(subscriptions)

        // 1. Copy CSV content to Clipboard as an immediate accessible backup
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = ClipData.newPlainText("SubKiller Subscriptions CSV", csvContent)
            clipboard?.setPrimaryClip(clip)
        } catch (e: Exception) {
            Log.w(TAG, "Clipboard copy error", e)
        }

        // 2. Write CSV file and open system Share / Export Intent
        return try {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val csvFile = File(exportDir, "subkiller_financial_export.csv")
            FileWriter(csvFile, false).use { writer ->
                writer.write(csvContent)
            }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                csvFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "SubKiller Financial Export (.csv)")
                putExtra(Intent.EXTRA_TEXT, "Attached is your exported Room Database CSV file (${subscriptions.size} records) for personal financial tracking.")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "Export Financial CSV File").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)

            "📊 Exported ${subscriptions.size} records to CSV file & copied to clipboard!"
        } catch (e: Exception) {
            Log.e(TAG, "CSV file export error", e)
            "📋 Exported ${subscriptions.size} records to CSV format on Clipboard!"
        }
    }
}
