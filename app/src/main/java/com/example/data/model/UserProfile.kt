package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: String = "primary_user",
    val email: String = "ua355165@gmail.com",
    val name: String = "Hamza R.",
    val isGoogleSignedIn: Boolean = false,
    val googleAccountId: String? = null,
    val preferredCurrency: String = "USD", // "USD" or "PKR"
    val isPremium: Boolean = false, // Unlocked via $2.99 one-time Stripe payment
    val isAdvancedRemindersActive: Boolean = false, // Unlocked via $2.99/month recurring Stripe subscription
    val remindersSubscriptionId: String? = null,
    val scansPerformed: Int = 0,
    val totalSaved: Double = 0.0,
    val previousMonthSaved: Double = 0.0,
    val lastMonthlyNotificationSent: Long = 0L,
    val freeCancellationTokens: Int = 0, // Earned via watching 2 rewarded video ads
    val stripeCustomerId: String = "cus_subkiller_primary",
    val stripeConnectedBankId: String = "ba_direct_payout_settlement",
    val avatarUri: String? = null, // URI of custom profile picture chosen by user
    val securityPin: String = "2580", // 4-digit security PIN for passcode lock
    val lastUpdated: Long = System.currentTimeMillis()
)
