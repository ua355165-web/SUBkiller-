package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CancellationRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CancellationDao {
    @Query("SELECT * FROM cancellation_requests ORDER BY createdAt DESC")
    fun getAllCancellationsFlow(): Flow<List<CancellationRequestEntity>>

    @Query("SELECT * FROM cancellation_requests WHERE subscriptionId = :subscriptionId LIMIT 1")
    suspend fun getBySubscriptionId(subscriptionId: String): CancellationRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(request: CancellationRequestEntity)

    @Update
    suspend fun update(request: CancellationRequestEntity)

    @Query("UPDATE cancellation_requests SET status = :status, lastUpdateNotes = :notes WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, notes: String?)

    @Delete
    suspend fun delete(request: CancellationRequestEntity)
}
