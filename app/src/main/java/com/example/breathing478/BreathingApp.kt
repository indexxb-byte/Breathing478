package com.example.breathing478

import android.app.Application
import com.example.breathing478.data.AppDatabase

class BreathingApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
}
