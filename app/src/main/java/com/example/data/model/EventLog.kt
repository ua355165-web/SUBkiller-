package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "event_logs")
data class EventLogEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val relatedSubscriptionId: String? = null,
    val eventType: String, // "AI_CANCELLATION_TRIGGERED", "STATUS_UPDATED", "BILL_PAID", "BILL_ADDED", "PREMIUM_PURCHASED", "SMART_SCAN_PERFORMED"
    val title: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)
