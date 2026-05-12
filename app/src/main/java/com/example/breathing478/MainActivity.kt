package com.example.breathing478

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.breathing478.data.AppDatabase
import com.example.breathing478.ui.screens.*
import com.example.breathing478.utils.VibrationManager

class MainActivity : ComponentActivity() {
    private lateinit var vibrator: Vibrator
    private lateinit var database: AppDatabase
    private var breathingServiceIntent: Intent? = null
    private var vibeOnLockEnabled by mutableStateOf(false)

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startBreathingService()
            vibeOnLockEnabled = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vibrator = getSystemService(Vibrator::class.java)
        database = (application as BreathingApp).database
        breathingServiceIntent = Intent(this, BreathingService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(false)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        }

        VibrationManager.setIntensity(
            getSharedPreferences("settings", MODE_PRIVATE).getFloat("vibration_intensity", 1f)
        )

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(
                                vibrator = vibrator,
                                database = database,
                                vibeOnLockEnabled = vibeOnLockEnabled,
                                onToggleVibeOnLock = { enable ->
                                    if (enable) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            if (ContextCompat.checkSelfPermission(
                                                    this@MainActivity, Manifest.permission.POST_NOTIFICATIONS
                                                ) != PackageManager.PERMISSION_GRANTED
                                            ) {
                                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                            } else {
                                                startBreathingService()
                                                vibeOnLockEnabled = true
                                            }
                                        } else {
                                            startBreathingService()
                                            vibeOnLockEnabled = true
                                        }
                                    } else {
                                        stopBreathingService()
                                        vibeOnLockEnabled = false
                                    }
                                },
                                onNavigateToHistory = { navController.navigate("history") },
                                onNavigateToReminders = { navController.navigate("reminders") },
                                onNavigateToConstructor = { navController.navigate("constructor") },
                                onNavigateToSettings = { navController.navigate("settings") }
                            )
                        }
                        composable("history") {
                            HistoryScreen(
                                database = database,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("reminders") {
                            RemindersScreen(
                                database = database,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("constructor") {
                            ConstructorScreen(
                                database = database,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                onBack = { navController.popBackStack() },
                                onIntensityChanged = { intensity ->
                                    getSharedPreferences("settings", MODE_PRIVATE)
                                        .edit().putFloat("vibration_intensity", intensity).apply()
                                    VibrationManager.setIntensity(intensity)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun startBreathingService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(breathingServiceIntent)
        } else {
            @Suppress("DEPRECATION")
            startService(breathingServiceIntent)
        }
    }

    private fun stopBreathingService() {
        stopService(breathingServiceIntent)
    }

    override fun onDestroy() {
        stopBreathingService()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        super.onDestroy()
    }
}
