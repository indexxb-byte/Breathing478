package com.example.breathing478.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_modes")
data class CustomModeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val inhale: Int = 4,
    val holdIn: Int = 7,
    val exhale: Int = 8,
    val holdOut: Int = 0
)