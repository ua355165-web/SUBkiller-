package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.SubscriptionEntity
import com.example.data.model.UserProfileEntity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

private const val TAG = "FirestoreSyncRepo"

class FirestoreSyncRepository(private val context: Context) {
    private val db: FirebaseFirestore by lazy {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
    }

    private val auth = Firebase.auth

    fun currentUserId(): String? = auth.currentUser?.uid

    suspend fun syncSubscriptionToFirestore(subscription: SubscriptionEntity) {
        val uid = currentUserId() ?: return
        val docRef = db.collection("subscriptions").document(subscription.id)
        val payload = hashMapOf<String, Any?>(
            "userId" to uid,
            "name" to subscription.name,
            "category" to subscription.category,
            "provider" to subscription.provider,
            "isUtility" to subscription.isUtility,
            "utilityType" to subscription.utilityType,
            "consumerNumber" to subscription.consumerNumber,
            "amount" to subscription.amount,
            "currency" to subscription.currency,
            "billingCycle" to subscription.billingCycle,
            "nextDueDate" to subscription.nextDueDate,
            "supportEmail" to subscription.supportEmail,
            "notes" to subscription.notes,
            "unitsConsumed" to subscription.unitsConsumed,
            "status" to subscription.status,
            "isPaidThisMonth" to subscription.isPaidThisMonth,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            Log.d(TAG, "Synced subscription ${subscription.id} to Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync subscription to Firestore", e)
        }
    }

    suspend fun deleteSubscriptionFromFirestore(subscriptionId: String) {
        val uid = currentUserId() ?: return
        try {
            db.collection("subscriptions").document(subscriptionId).delete().await()
            Log.d(TAG, "Deleted subscription $subscriptionId from Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete subscription from Firestore", e)
        }
    }

    suspend fun syncUserProfileToFirestore(profile: UserProfileEntity) {
        val uid = currentUserId() ?: return
        val docRef = db.collection("user_profiles").document(uid)
        val payload = hashMapOf<String, Any?>(
            "userId" to uid,
            "email" to profile.email,
            "name" to profile.name,
            "totalSaved" to profile.totalSaved,
            "preferredCurrency" to profile.preferredCurrency,
            "isPremium" to profile.isPremium,
            "isAdvancedRemindersActive" to profile.isAdvancedRemindersActive,
            "securityPin" to profile.securityPin,
            "avatarUri" to profile.avatarUri,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            Log.d(TAG, "Synced user profile to Firestore for $uid")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync user profile to Firestore", e)
        }
    }

    fun observeRemoteSubscriptions(): Flow<List<SubscriptionEntity>> = flow {
        val uid = currentUserId() ?: return@flow
        val query = db.collection("subscriptions").whereEqualTo("userId", uid)
        
        query.snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        SubscriptionEntity(
                            id = doc.id,
                            name = doc.getString("name") ?: "Subscription",
                            category = doc.getString("category") ?: "General",
                            provider = doc.getString("provider") ?: "OTHER",
                            isUtility = doc.getBoolean("isUtility") ?: false,
                            utilityType = doc.getString("utilityType"),
                            consumerNumber = doc.getString("consumerNumber"),
                            amount = doc.getDouble("amount") ?: 0.0,
                            currency = doc.getString("currency") ?: "USD",
                            billingCycle = doc.getString("billingCycle") ?: "Monthly",
                            nextDueDate = doc.getString("nextDueDate") ?: "2026-12-01",
                            status = doc.getString("status") ?: "active",
                            supportEmail = doc.getString("supportEmail") ?: "support@service.com",
                            notes = doc.getString("notes"),
                            unitsConsumed = doc.getLong("unitsConsumed")?.toInt(),
                            isPaidThisMonth = doc.getBoolean("isPaidThisMonth") ?: false,
                            updatedAt = System.currentTimeMillis()
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
            }
            .catch { e ->
                Log.e(TAG, "Error observing Firestore subscriptions", e)
                emit(emptyList())
            }
            .collect { emit(it) }
    }
}
