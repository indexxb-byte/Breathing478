package com.example.breathing478

import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BreathingApp()
        }
    }
}

enum class BreathingPhase {
    INHALE, HOLD, EXHALE, IDLE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreathingApp() {
    var phase by remember { mutableStateOf(BreathingPhase.IDLE) }
    var timerText by remember { mutableStateOf("4") }
    var isRunning by remember { mutableStateOf(false) }
    var roundCount by remember { mutableStateOf(0) }
    var totalRounds by remember { mutableStateOf(4) }
    var phaseLabel by remember { mutableStateOf("Готовы?") }
    val context = LocalContext.current
    val vibrator = context.getSystemService(Vibrator::class.java)

    val animatedScale = remember { Animatable(0.3f) }

    LaunchedEffect(phase) {
        when (phase) {
            BreathingPhase.INHALE -> {
                animatedScale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 4000, easing = FastOutSlowInEasing)
                )
            }
            BreathingPhase.EXHALE -> {
                animatedScale.animateTo(
                    targetValue = 0.3f,
                    animationSpec = tween(durationMillis = 8000, easing = FastOutSlowInEasing)
                )
            }
            BreathingPhase.HOLD -> { /* держим размер */ }
            BreathingPhase.IDLE -> {
                animatedScale.snapTo(0.3f)
            }
        }
    }

    LaunchedEffect(isRunning) {
        if (!isRunning) {
            phase = BreathingPhase.IDLE
            timerText = "4"
            phaseLabel = "Готовы?"
            return@LaunchedEffect
        }

        var currentRound = 0
        while (currentRound < totalRounds && isRunning) {
            // Вдох 4 сек
            phase = BreathingPhase.INHALE
            phaseLabel = "Вдох"
            vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            for (i in 4 downTo 1) {
                timerText = i.toString()
                delay(1000)
            }

            // Задержка 7 сек
            phase = BreathingPhase.HOLD
            phaseLabel = "Задержка"
            vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            for (i in 7 downTo 1) {
                timerText = i.toString()
                delay(1000)
            }

            // Выдох 8 сек
            phase = BreathingPhase.EXHALE
            phaseLabel = "Выдох"
            vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            for (i in 8 downTo 1) {
                timerText = i.toString()
                delay(1000)
            }

            currentRound++
            roundCount = currentRound
        }

        isRunning = false
        phase = BreathingPhase.IDLE
        timerText = "✓"
        phaseLabel = "Готово!"
        roundCount = 0
    }

    // UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = when (phase) {
                        BreathingPhase.INHALE -> listOf(Color(0xFF0D47A1), Color(0xFF1565C0))
                        BreathingPhase.HOLD -> listOf(Color(0xFFE65100), Color(0xFFEF6C00))
                        BreathingPhase.EXHALE -> listOf(Color(0xFF1B5E20), Color(0xFF2E7D32))
                        BreathingPhase.IDLE -> listOf(Color(0xFF263238), Color(0xFF37474F))
                    }
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "4-7-8 Дыхание",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Анимированный круг
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(220.dp)
            ) {
                Canvas(modifier = Modifier.size(220.dp)) {
                    val centerX = size.width / 2
                    val centerY = size.height / 2
                    val radius = size.width / 2 * animatedScale.value

                    drawCircle(
                        color = Color.White.copy(alpha = 0.1f),
                        radius = radius * 1.2f,
                        center = Offset(centerX, centerY)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.3f),
                        radius = radius,
                        center = Offset(centerX, centerY)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.6f),
                        radius = radius * 0.8f,
                        center = Offset(centerX, centerY)
                    )
                }

                Text(
                    text = timerText,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = phaseLabel,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (isRunning) {
                Text(
                    text = "Раунд $roundCount из $totalRounds",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(2, 4, 6, 8).forEach { rounds ->
                    FilterChip(
                        selected = totalRounds == rounds,
                        onClick = { if (!isRunning) totalRounds = rounds },
                        label = { Text("${rounds}р", color = Color.White) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color.White.copy(alpha = 0.3f),
                            containerColor = Color.White.copy(alpha = 0.1f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (isRunning) {
                        isRunning = false
                        phase = BreathingPhase.IDLE
                        timerText = "4"
                        phaseLabel = "Готовы?"
                        roundCount = 0
                    } else {
                        roundCount = 0
                        isRunning = true
                    }
                },
                modifier = Modifier
                    .width(200.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) Color(0xFFE53935) else Color.White,
                    contentColor = if (isRunning) Color.White else Color(0xFF1A237E)
                )
            ) {
                Text(
                    text = if (isRunning) "СТОП" else "СТАРТ",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
