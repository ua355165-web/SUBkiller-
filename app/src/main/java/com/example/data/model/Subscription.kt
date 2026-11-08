package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String, // "Electricity & Power", "Streaming", "AI & Productivity", "Telecom & Internet", "Fitness", "Cloud"
    val provider: String, // "LESCO", "K_ELECTRIC", "NETFLIX", "SPOTIFY", "OPENAI", "PTCL", "SNGPL", "OTHER"
    val isUtility: Boolean = false,
    val utilityType: String? = null, // "LESCO", "K_ELECTRIC", "GAS", "INTERNET", null
    val consumerNumber: String? = null, // e.g. 14-digit reference for LESCO, 13-digit for K-Electric
    val referenceNumber: String? = null,
    val amount: Double,
    val currency: String = "USD", // "USD" or "PKR"
    val billingCycle: String = "Monthly", // "Monthly", "Quarterly", "Yearly"
    val nextDueDate: String, // "2026-11-20"
    val status: String = "active", // "active", "cancelled", "negotiating", "paused"
    val supportEmail: String = "support@service.com",
    val notes: String? = null,
    val unitsConsumed: Int? = null, // for LESCO / K-Electric meter units
    val isPaidThisMonth: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
