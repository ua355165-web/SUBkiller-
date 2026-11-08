package com.example.data.ai

import android.util.Log
import com.example.data.model.SubscriptionEntity
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object GeminiCancellationService {
    private const val TAG = "GeminiCancellation"

    private val model by lazy {
        try {
            Firebase.ai.generativeModel("gemini-3.8-flash")
        } catch (e: Exception) {
            Log.w(TAG, "Firebase AI Logic model init fallback", e)
            null
        }
    }

    suspend fun generateCancellationEmail(
        subscription: SubscriptionEntity,
        userEmail: String,
        userName: String,
        tone: String,
        reason: String
    ): String {
        val dateStr = SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(Date())
        val prompt = """
            Act as an AI legal assistant and consumer advocate.
            Draft a highly professional, clear, and unambiguous subscription cancellation request email for the following service:

            Service Name: ${subscription.name}
            Provider/Company: ${subscription.provider}
            Category: ${subscription.category}
            Account / Reference ID: ${subscription.consumerNumber ?: subscription.referenceNumber ?: "Linked to $userEmail"}
            Registered Subscriber Email: $userEmail
            Subscriber Full Name: $userName
            Effective Date: $dateStr
            Target Tone: $tone (e.g. Firm & Legal, Price Sensitive / Retention, Courteous Direct)
            Stated Reason for Cancellation: $reason

            Guidelines:
            1. Output ONLY the raw email body text. Do NOT include email headers like "Subject:" or "To:".
            2. State explicitly that all automatic payment authorizations and recurring card debits are revoked immediately.
            3. Request written confirmation of termination sent to $userEmail with a transaction confirmation code.
            4. If tone is "Price Sensitive", politely invite a retention offer while explicitly stating termination stands if no offer is provided.
            5. Ensure the email is formatted with clear paragraphs and ends with "$userName".
        """.trimIndent()

        return try {
            val aiModel = model
            if (aiModel != null) {
                withTimeout(25_000L) {
                    val response = aiModel.generateContent(prompt)
                    val text = response.text
                    if (!text.isNullOrBlank()) {
                        text.trim()
                    } else {
                        buildFallbackTemplate(subscription, userEmail, userName, tone, reason)
                    }
                }
            } else {
                buildFallbackTemplate(subscription, userEmail, userName, tone, reason)
            }
        } catch (e: TimeoutCancellationException) {
            Log.w(TAG, "Gemini generation timed out, using fallback template", e)
            buildFallbackTemplate(subscription, userEmail, userName, tone, reason)
        } catch (e: Exception) {
            Log.e(TAG, "Gemini AI generation failed, using fallback template", e)
            buildFallbackTemplate(subscription, userEmail, userName, tone, reason)
        }
    }

    fun buildFallbackTemplate(
        subscription: SubscriptionEntity,
        userEmail: String,
        userName: String,
        tone: String,
        reason: String
    ): String {
        val dateStr = SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(Date())
        val refId = subscription.consumerNumber ?: subscription.referenceNumber ?: "Account linked to $userEmail"

        return when (tone) {
            "Firm & Legal" -> """
Dear ${subscription.name} Support & Billing Department,

I am writing to formally request immediate cancellation of my account and subscription (${subscription.name}), effective $dateStr.

Account / Reference ID: $refId
Registered Email: $userEmail
Subscriber: $userName
Primary Reason: $reason

Per consumer protection standards and electronic funds transfer terms, I hereby explicitly revoke authorization for any future automatic recurring debits or charges to my payment method associated with this service.

Please confirm in writing to $userEmail that:
1. This cancellation has been executed without fees or penalties.
2. Recurring billing has been terminated immediately.
3. Confirmation reference number has been issued.

Sincerely,
$userName
            """.trimIndent()

            "Price Sensitive" -> """
Dear ${subscription.name} Retention Team,

I am writing regarding my ongoing account for ${subscription.name}. Due to budget adjustments, I am considering cancelling this service as of $dateStr.

Account / Reference ID: $refId
Stated Reason: $reason

Before finalizing complete termination, please notify me if any loyalty discounts, lowered retention plans, or pauses are available. Otherwise, please treat this message as my official cancellation notice.

Best regards,
$userName
            """.trimIndent()

            else -> """
Hello ${subscription.name} Customer Support,

Please cancel my subscription for ${subscription.name} starting from $dateStr.

Subscriber: $userName
Registered Email: $userEmail
Account Ref: $refId
Reason: $reason

Kindly confirm once processed with no further billing cycles.

Thank you,
$userName
            """.trimIndent()
        }
    }
}
