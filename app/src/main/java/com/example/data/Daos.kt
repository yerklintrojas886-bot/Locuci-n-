package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class UserCostSummary(
    val username: String,
    val totalCredits: Int,
    val totalCostUsd: Double,
    val totalCostPen: Double,
    val totalOps: Int
)

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE username = :username")
    suspend fun deleteUserByUsername(username: String)

    @Query("UPDATE users SET isActive = :isActive WHERE username = :username")
    suspend fun setUserActiveStatus(username: String, isActive: Boolean)

    @Query("UPDATE users SET passwordHash = :newHash WHERE username = :username")
    suspend fun resetPassword(username: String, newHash: String)

    @Query("UPDATE users SET maxDevices = :maxDevices WHERE username = :username")
    suspend fun updateMaxDevices(username: String, maxDevices: Int)
}

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects WHERE ownerUsername = :username ORDER BY updatedAt DESC")
    fun getProjectsForUser(username: String): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: String): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProject(project: ProjectEntity)

    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: String)
}

@Dao
interface UsageDao {
    @Query("SELECT * FROM usage_records WHERE username = :username ORDER BY timestamp DESC")
    fun getUsageForUser(username: String): Flow<List<UsageRecordEntity>>

    @Query("SELECT * FROM usage_records ORDER BY timestamp DESC LIMIT 200")
    fun getAllUsage(): Flow<List<UsageRecordEntity>>

    @Query("SELECT COUNT(*) FROM usage_records WHERE username = :username AND timestamp >= :sinceTimestamp")
    suspend fun getDailyUsageCount(username: String, sinceTimestamp: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun logUsage(record: UsageRecordEntity)

    @Query("SELECT SUM(costCredits) FROM usage_records")
    suspend fun getTotalCreditsConsumed(): Int?

    @Query("SELECT SUM(estimatedCostUsd) FROM usage_records")
    suspend fun getTotalCostUsd(): Double?

    @Query("SELECT SUM(estimatedCostPen) FROM usage_records")
    suspend fun getTotalCostPen(): Double?
}

@Dao
interface DeviceSessionDao {
    @Query("SELECT * FROM device_sessions WHERE username = :username AND isActive = 1")
    suspend fun getActiveSessions(username: String): List<DeviceSessionEntity>

    @Query("SELECT * FROM device_sessions WHERE username = :username AND isActive = 1")
    fun getActiveSessionsFlow(username: String): Flow<List<DeviceSessionEntity>>

    @Query("SELECT * FROM device_sessions ORDER BY loginTimestamp DESC")
    fun getAllSessions(): Flow<List<DeviceSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: DeviceSessionEntity)

    @Query("UPDATE device_sessions SET isActive = 0 WHERE id = :sessionId")
    suspend fun revokeSession(sessionId: String)

    @Query("UPDATE device_sessions SET isActive = 0 WHERE username = :username")
    suspend fun revokeAllSessionsForUser(username: String)

    @Query("UPDATE device_sessions SET lastActiveTimestamp = :now WHERE id = :sessionId")
    suspend fun updateLastActive(sessionId: String, now: Long)
}

@Dao
interface AuthorizedVoiceDao {
    @Query("SELECT * FROM authorized_voices ORDER BY createdAt DESC")
    fun getAllAuthorizedVoices(): Flow<List<AuthorizedVoiceEntity>>

    @Query("SELECT * FROM authorized_voices WHERE id = :id LIMIT 1")
    suspend fun getVoiceById(id: String): AuthorizedVoiceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoice(voice: AuthorizedVoiceEntity)

    @Delete
    suspend fun deleteVoice(voice: AuthorizedVoiceEntity)
}
