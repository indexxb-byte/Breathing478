package com.example.breathing478.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE timestamp BETWEEN :startOfDay AND :endOfDay ORDER BY timestamp DESC")
    fun getSessionsForDay(startOfDay: Long, endOfDay: Long): Flow<List<SessionEntity>>

    @Query("SELECT DISTINCT date(timestamp / 1000, 'unixepoch') as day FROM sessions")
    fun getDaysWithSessions(): Flow<List<String>>

    @Insert
    suspend fun insert(session: SessionEntity)

    @Delete
    suspend fun delete(session: SessionEntity)

    // Custom modes
    @Query("SELECT * FROM custom_modes ORDER BY id ASC")
    fun getAllCustomModes(): Flow<List<CustomModeEntity>>

    @Insert
    suspend fun insertMode(mode: CustomModeEntity)

    @Update
    suspend fun updateMode(mode: CustomModeEntity)

    @Delete
    suspend fun deleteMode(mode: CustomModeEntity)

    // Reminders
    @Query("SELECT * FROM reminders ORDER BY hour, minute")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Insert
    suspend fun insertReminder(reminder: ReminderEntity)

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)
}