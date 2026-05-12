package com.example.breathing478.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hour: Int = 8,
    val minute: Int = 0,
    val days: Int = 0b1111111, // битовая маска: Пн=1,Вт=2,...Вс=64
    val enabled: Boolean = true
)