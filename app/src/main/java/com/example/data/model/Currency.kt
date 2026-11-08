package com.example.data.model

import java.text.DecimalFormat

enum class CurrencyCode(val symbol: String, val displayName: String) {
    USD("$", "US Dollar ($)"),
    PKR("Rs ", "Pakistani Rupee (PKR)")
}

object CurrencyManager {
    // Current fixed benchmark conversion rate: 1 USD = 278.0 PKR
    const val USD_TO_PKR_RATE = 278.0

    fun convert(amount: Double, from: CurrencyCode, to: CurrencyCode): Double {
        if (from == to) return amount
        return if (from == CurrencyCode.USD && to == CurrencyCode.PKR) {
            amount * USD_TO_PKR_RATE
        } else {
            amount / USD_TO_PKR_RATE
        }
    }

    fun format(amount: Double, currency: CurrencyCode): String {
        return when (currency) {
            CurrencyCode.USD -> {
                val df = DecimalFormat("$#,##0.00")
                df.format(amount)
            }
            CurrencyCode.PKR -> {
                val df = DecimalFormat("Rs #,##0")
                df.format(amount)
            }
        }
    }

    fun formatConverted(amount: Double, sourceCurrency: CurrencyCode, displayCurrency: CurrencyCode): String {
        val converted = convert(amount, sourceCurrency, displayCurrency)
        return format(converted, displayCurrency)
    }
}
