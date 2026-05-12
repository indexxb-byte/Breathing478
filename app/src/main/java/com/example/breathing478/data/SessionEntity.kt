package com.example.breathing478.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val modeName: String = "4-7-8",
    val durationMinutes: Int = 3,
    val elapsedSeconds: Int = 0,
    val completed: Boolean = false
)