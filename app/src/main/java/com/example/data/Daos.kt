package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodLogDao {
    @Query("SELECT * FROM food_logs ORDER BY timestamp DESC")
    fun getAllFoodLogs(): Flow<List<FoodLogEntry>>

    @Query("SELECT * FROM food_logs WHERE timestamp >= :startOfDay AND timestamp < :endOfDay ORDER BY timestamp DESC")
    fun getFoodLogsForDay(startOfDay: Long, endOfDay: Long): Flow<List<FoodLogEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodLog(entry: FoodLogEntry)

    @Update
    suspend fun updateFoodLog(entry: FoodLogEntry)

    @Delete
    suspend fun deleteFoodLog(entry: FoodLogEntry)

    @Query("DELETE FROM food_logs WHERE id = :id")
    suspend fun deleteFoodLogById(id: Int)

    @Query("SELECT * FROM food_logs ORDER BY timestamp DESC LIMIT 50")
    suspend fun getRecentFoodLogs(): List<FoodLogEntry>
}

@Dao
interface WaterLogDao {
    @Query("SELECT * FROM water_logs ORDER BY timestamp DESC")
    fun getAllWaterLogs(): Flow<List<WaterLog>>

    @Query("SELECT * FROM water_logs WHERE timestamp >= :startOfDay AND timestamp < :endOfDay ORDER BY timestamp DESC")
    fun getWaterLogsForDay(startOfDay: Long, endOfDay: Long): Flow<List<WaterLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterLog(log: WaterLog)

    @Query("DELETE FROM water_logs WHERE id = :id")
    suspend fun deleteWaterLogById(id: Int)
}

@Dao
interface WeightLogDao {
    @Query("SELECT * FROM weight_logs ORDER BY timestamp DESC")
    fun getAllWeightLogs(): Flow<List<WeightLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightLog(log: WeightLog)

    @Query("DELETE FROM weight_logs WHERE id = :id")
    suspend fun deleteWeightLogById(id: Int)
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileSync(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)
}

@Dao
interface CoachMessageDao {
    @Query("SELECT * FROM coach_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<CoachMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: CoachMessage)

    @Query("DELETE FROM coach_messages")
    suspend fun clearChatHistory()
}

@Dao
interface UserAccountDao {
    @Query("SELECT * FROM user_accounts WHERE emailOrPhone = :emailOrPhone LIMIT 1")
    suspend fun getAccountSync(emailOrPhone: String): UserAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAccount(account: UserAccount)
}
