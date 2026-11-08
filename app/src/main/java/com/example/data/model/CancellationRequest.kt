package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "cancellation_requests")
data class CancellationRequestEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val subscriptionId: String,
    val serviceName: String,
    val recipientEmail: String,
    val tone: String = "Firm & Legal", // "Firm & Legal", "Polite & Formal", "Price Sensitive"
    val userReason: String,
    val emailSubject: String,
    val emailBody: String,
    val status: String = "requested", // "requested", "contacted", "negotiating", "cancelled"
    val monthlySavings: Double = 0.0,
    val currency: String = "USD",
    val sentAt: Long? = null,
    val lastUpdateNotes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
