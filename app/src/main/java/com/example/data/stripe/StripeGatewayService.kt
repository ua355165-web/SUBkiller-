package com.example.data.stripe

import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class StripePaymentType(val title: String, val isRecurring: Boolean) {
    SMART_SCAN_ONE_TIME("Smart Scan Pro (Lifetime)", false),
    AI_CANCELLATION_SUCCESS_FEE("AI Cancellation 10% Success Fee", false),
    ADVANCED_REMINDERS_SUBSCRIPTION("Advanced Reminders (Recurring Monthly)", true)
}

data class StripeCardInput(
    val cardNumber: String,
    val expiry: String, // "MM/YY"
    val cvc: String,
    val cardholderName: String,
    val zipCode: String = "10001"
) {
    val cleanNumber: String get() = cardNumber.replace("\\s|-".toRegex(), "")
    val last4: String get() = if (cleanNumber.length >= 4) cleanNumber.takeLast(4) else "4242"
    val brand: String get() = when {
        cleanNumber.startsWith("4") -> "Visa"
        cleanNumber.startsWith("5") || cleanNumber.startsWith("2") -> "Mastercard"
        cleanNumber.startsWith("3") -> "American Express"
        else -> "Stripe Card"
    }

    fun isValid(): Boolean {
        return cleanNumber.length in 13..19 && cvc.length in 3..4 && expiry.contains("/")
    }
}

data class StripePaymentResult(
    val isSuccess: Boolean,
    val paymentIntentId: String,
    val chargeId: String,
    val amountCharged: Double,
    val currency: String,
    val paymentType: StripePaymentType,
    val destinationBankAccount: String,
    val receiptId: String,
    val cardBrand: String,
    val cardLast4: String,
    val timestamp: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
)

object StripeGatewayService {
    // Production connected backend merchant bank payout destination
    const val BACKEND_MERCHANT_BANK_ID = StripePaymentManager.BACKEND_MERCHANT_BANK_ID
    const val STRIPE_ACCOUNT_NAME = StripePaymentManager.STRIPE_MERCHANT_NAME

    suspend fun processPayment(
        cardInput: StripeCardInput,
        amount: Double,
        currency: String = "USD",
        paymentType: StripePaymentType,
        serviceItemName: String? = null
    ): StripePaymentResult {
        return when (paymentType) {
            StripePaymentType.SMART_SCAN_ONE_TIME -> {
                StripePaymentManager.processSmartScanOneTimePayment(cardInput)
            }
            StripePaymentType.AI_CANCELLATION_SUCCESS_FEE -> {
                StripePaymentManager.processCancellationSuccessFeePayment(
                    cardInput = cardInput,
                    subscriptionName = serviceItemName ?: "Service",
                    annualSavings = amount * 10.0,
                    feeAmount = amount
                )
            }
            StripePaymentType.ADVANCED_REMINDERS_SUBSCRIPTION -> {
                StripePaymentManager.processAdvancedRemindersSubscription(cardInput)
            }
        }
    }
}

