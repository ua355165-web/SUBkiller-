package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 'primary_user' LIMIT 1")
    fun getUserProfileFlow(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 'primary_user' LIMIT 1")
    suspend fun getUserProfile(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: UserProfileEntity)

    @Update
    suspend fun update(profile: UserProfileEntity)

    @Query("UPDATE user_profile SET preferredCurrency = :currency WHERE id = 'primary_user'")
    suspend fun updateCurrency(currency: String)

    @Query("UPDATE user_profile SET isPremium = :isPremium WHERE id = 'primary_user'")
    suspend fun updatePremium(isPremium: Boolean)

    @Query("UPDATE user_profile SET isAdvancedRemindersActive = :isActive, remindersSubscriptionId = :subId WHERE id = 'primary_user'")
    suspend fun updateRemindersSubscription(isActive: Boolean, subId: String?)

    @Query("UPDATE user_profile SET freeCancellationTokens = freeCancellationTokens + :count WHERE id = 'primary_user'")
    suspend fun addCancellationTokens(count: Int)

    @Query("UPDATE user_profile SET freeCancellationTokens = CASE WHEN freeCancellationTokens > 0 THEN freeCancellationTokens - 1 ELSE 0 END WHERE id = 'primary_user'")
    suspend fun consumeCancellationToken()

    @Query("UPDATE user_profile SET scansPerformed = scansPerformed + 1 WHERE id = 'primary_user'")
    suspend fun incrementScans()

    @Query("UPDATE user_profile SET email = :email, name = :name, isGoogleSignedIn = :isSignedIn, googleAccountId = :googleId WHERE id = 'primary_user'")
    suspend fun updateGoogleAuth(email: String, name: String, isSignedIn: Boolean, googleId: String?)

    @Query("UPDATE user_profile SET isGoogleSignedIn = 0 WHERE id = 'primary_user'")
    suspend fun signOutGoogle()

    @Query("UPDATE user_profile SET previousMonthSaved = :prevSaved, lastMonthlyNotificationSent = :timestamp WHERE id = 'primary_user'")
    suspend fun recordMonthlyNotificationSent(prevSaved: Double, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE user_profile SET avatarUri = :uri WHERE id = 'primary_user'")
    suspend fun updateAvatarUri(uri: String?)

    @Query("UPDATE user_profile SET securityPin = :pin WHERE id = 'primary_user'")
    suspend fun updateSecurityPin(pin: String)
}
