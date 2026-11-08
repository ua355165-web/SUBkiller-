package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SubscriptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions ORDER BY updatedAt DESC")
    fun getAllSubscriptionsFlow(): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions WHERE isUtility = 1 ORDER BY updatedAt DESC")
    fun getUtilityBillsFlow(): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions WHERE isUtility = 0 ORDER BY updatedAt DESC")
    fun getNonUtilitySubscriptionsFlow(): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions WHERE id = :id")
    suspend fun getSubscriptionById(id: String): SubscriptionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subscription: SubscriptionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(subscriptions: List<SubscriptionEntity>)

    @Update
    suspend fun update(subscription: SubscriptionEntity)

    @Delete
    suspend fun delete(subscription: SubscriptionEntity)

    @Query("SELECT * FROM subscriptions WHERE status = :status ORDER BY updatedAt DESC")
    fun getSubscriptionsByStatusFlow(status: String): Flow<List<SubscriptionEntity>>

    @Query("UPDATE subscriptions SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE subscriptions SET isPaidThisMonth = :isPaid, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updatePaidStatus(id: String, isPaid: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE subscriptions SET name = :name, amount = :amount, billingCycle = :billingCycle, nextDueDate = :nextDueDate, supportEmail = :supportEmail, notes = :notes, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateDetails(
        id: String,
        name: String,
        amount: Double,
        billingCycle: String,
        nextDueDate: String,
        supportEmail: String,
        notes: String?,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("SELECT COUNT(*) FROM subscriptions")
    suspend fun getSubscriptionCount(): Int

    @Query("SELECT * FROM subscriptions WHERE provider = :provider ORDER BY updatedAt DESC")
    fun getSubscriptionsByProviderFlow(provider: String): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions WHERE category = :category ORDER BY updatedAt DESC")
    fun getSubscriptionsByCategoryFlow(category: String): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions WHERE name LIKE '%' || :query || '%' OR provider LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchSubscriptionsFlow(query: String): Flow<List<SubscriptionEntity>>

    @Query("DELETE FROM subscriptions")
    suspend fun clearAll()

    @Query("DELETE FROM subscriptions WHERE id LIKE 'util-%' OR id LIKE 'sub-%' OR id LIKE 'user-%'")
    suspend fun clearSampleData(): Int
}
