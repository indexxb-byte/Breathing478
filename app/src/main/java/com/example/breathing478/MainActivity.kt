package com.example.breathing478

import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

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
    var elapsedSeconds by remember { mutableStateOf(0) }
    var totalMinutes by remember { mutableStateOf(3) }
    var totalSeconds by remember { mutableStateOf(180) }
    var phaseLabel by remember { mutableStateOf("Готовы?") }
    var phaseProgress by remember { mutableStateOf(0f) }
    var sessionCompleted by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val view = LocalView.current
    val vibrator = context.getSystemService(Vibrator::class.java)

    val animatedScale = remember { Animatable(0.35f) }
    val glowAlpha = remember { Animatable(0.3f) }
    val particles = remember { mutableStateListOf<Particle>() }

    val themeColor = when (phase) {
        BreathingPhase.INHALE -> Color(0xFF64B5F6)
        BreathingPhase.HOLD -> Color(0xFFFFD54F)
        BreathingPhase.EXHALE -> Color(0xFF81C784)
        BreathingPhase.IDLE -> Color(0xFF90A4AE)
    }

    val bgColor = when (phase) {
        BreathingPhase.INHALE -> Color(0xFF0D1B2A)
        BreathingPhase.HOLD -> Color(0xFF1A0A00)
        BreathingPhase.EXHALE -> Color(0xFF0A1A0A)
        BreathingPhase.IDLE -> Color(0xFF121212)
    }

    LaunchedEffect(phase) {
        when (phase) {
            BreathingPhase.INHALE -> {
                animatedScale.animateTo(1f, animationSpec = tween(4000, easing = FastOutSlowInEasing))
                glowAlpha.animateTo(0.8f, animationSpec = tween(4000))
            }
            BreathingPhase.EXHALE -> {
                animatedScale.animateTo(0.35f, animationSpec = tween(8000, easing = FastOutSlowInEasing))
                glowAlpha.animateTo(0.2f, animationSpec = tween(8000))
            }
            BreathingPhase.HOLD -> {
                glowAlpha.animateTo(0.6f, animationSpec = tween(500))
            }
            BreathingPhase.IDLE -> {
                animatedScale.snapTo(0.35f)
                glowAlpha.snapTo(0.3f)
            }
        }
    }

    LaunchedEffect(isRunning, phase) {
        while (isRunning) {
            if (phase != BreathingPhase.IDLE) {
                particles.add(
                    Particle(
                        angle = Random.nextFloat() * 360f,
                        distance = Random.nextFloat() * 50f + 80f,
                        alpha = 1f,
                        size = Random.nextFloat() * 3f + 1f
                    )
                )
                if (particles.size > 30) particles.removeAt(0)
            }
            particles.forEachIndexed { i, p ->
                particles[i] = p.copy(
                    distance = p.distance + 2f,
                    alpha = (p.alpha - 0.02f).coerceAtLeast(0f)
                )
            }
            particles.removeAll { it.alpha <= 0f }
            delay(50)
        }
    }

    LaunchedEffect(isRunning) {
        if (!isRunning) {
            phase = BreathingPhase.IDLE
            timerText = "4"
            phaseLabel = "Готовы?"
            elapsedSeconds = 0
            sessionCompleted = false
            return@LaunchedEffect
        }

        elapsedSeconds = 0
        totalSeconds = totalMinutes * 60
        sessionCompleted = false

        while (elapsedSeconds < totalSeconds && isRunning) {
            // Вдох 4 сек
            phase = BreathingPhase.INHALE
            phaseLabel = "Вдох"
            vibrateTick(vibrator)
            for (i in 1..4) {
                if (!isRunning) return@LaunchedEffect
                timerText = (5 - i).toString()
                phaseProgress = i / 4f
                delay(1000)
                elapsedSeconds++
                if (elapsedSeconds >= totalSeconds) break
            }
            if (elapsedSeconds >= totalSeconds) break

            // Задержка 7 сек
            phase = BreathingPhase.HOLD
            phaseLabel = "Задержка"
            vibrateHold(vibrator)
            for (i in 1..7) {
                if (!isRunning) return@LaunchedEffect
                timerText = (8 - i).toString()
                phaseProgress = i / 7f
                delay(1000)
                elapsedSeconds++
                if (elapsedSeconds >= totalSeconds) break
            }
            if (elapsedSeconds >= totalSeconds) break

            // Выдох 8 сек
            phase = BreathingPhase.EXHALE
            phaseLabel = "Выдох"
            vibrateTick(vibrator)
            for (i in 1..8) {
                if (!isRunning) return@LaunchedEffect
                timerText = (9 - i).toString()
                phaseProgress = i / 8f
                delay(1000)
                elapsedSeconds++
                if (elapsedSeconds >= totalSeconds) break
            }
        }

        isRunning = false
        phase = BreathingPhase.IDLE
        timerText = "✓"
        phaseLabel = "Готово!"
        sessionCompleted = true
        vibrateLong(vibrator)
    }

    fun vibrateScroll() {
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { p ->
                val rad = Math.toRadians(p.angle.toDouble())
                val cx = center.x + cos(rad).toFloat() * p.distance
                val cy = center.y + sin(rad).toFloat() * p.distance
                drawCircle(
                    color = themeColor.copy(alpha = p.alpha * 0.3f),
                    radius = p.size,
                    center = Offset(cx, cy)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            if (!isRunning) {
                Text(
                    text = "4-7-8",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.9f),
                    letterSpacing = 8.sp
                )
                Text(
                    text = "ДЫХАНИЕ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White.copy(alpha = 0.5f),
                    letterSpacing = 12.sp
                )
                Spacer(modifier = Modifier.height(48.dp))
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {
                Canvas(modifier = Modifier.size(240.dp)) {
                    val cx = size.width / 2
                    val cy = size.height / 2
                    val baseRadius = size.width / 2 * animatedScale.value

                    for (i in 3 downTo 0) {
                        drawCircle(
                            color = themeColor.copy(alpha = glowAlpha.value * 0.05f),
                            radius = baseRadius + i * 20f + 10f,
                            center = Offset(cx, cy)
                        )
                    }

                    if (phase != BreathingPhase.IDLE) {
                        drawCircle(
                            color = themeColor.copy(alpha = 0.4f),
                            radius = baseRadius + 15f,
                            center = Offset(cx, cy),
                            style = Stroke(width = 3f)
                        )
                        drawArc(
                            color = themeColor,
                            startAngle = -90f,
                            sweepAngle = 360f * phaseProgress,
                            useCenter = false,
                            topLeft = Offset(cx - baseRadius - 15f, cy - baseRadius - 15f),
                            size = Size((baseRadius + 15f) * 2, (baseRadius + 15f) * 2),
                            style = Stroke(width = 3f)
                        )
                    }

                    drawCircle(
                        color = Color.White.copy(alpha = 0.05f),
                        radius = baseRadius,
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.1f),
                        radius = baseRadius * 0.9f,
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.2f),
                        radius = baseRadius * 0.75f,
                        center = Offset(cx, cy)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timerText,
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (isRunning) {
                        Text(
                            text = phaseLabel,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = themeColor,
                            letterSpacing = 4.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            if (!isRunning) {
                Text(
                    text = "Длительность",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    letterSpacing = 4.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .height(60.dp)
                        .pointerInput(Unit) {
                            detectVerticalDragGestures { _, dragAmount ->
                                val change = if (dragAmount < 0) 1 else -1
                                totalMinutes = (totalMinutes + change).coerceIn(1, 10)
                                vibrateScroll()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "▼", fontSize = 12.sp, color = Color.White.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "$totalMinutes", fontSize = 56.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (totalMinutes == 1) "минута"
                                   else if (totalMinutes in 2..4) "минуты"
                                   else "минут",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "▲", fontSize = 12.sp, color = Color.White.copy(alpha = 0.3f))
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (sessionCompleted) {
                    Text(
                        text = "Сеанс завершён!",
                        fontSize = 16.sp,
                        color = Color(0xFF81C784),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formatTime(elapsedSeconds, totalSeconds),
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.width(200.dp).height(2.dp).background(Color.White.copy(alpha = 0.1f))) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = elapsedSeconds.toFloat() / totalSeconds)
                                .background(themeColor)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (isRunning) {
                        isRunning = false
                        phase = BreathingPhase.IDLE
                        timerText = "4"
                        phaseLabel = "Готовы?"
                        elapsedSeconds = 0
                    } else {
                        elapsedSeconds = 0
                        isRunning = true
                    }
                },
                modifier = Modifier.width(200.dp).height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) Color(0xFFE53935) else Color.White,
                    contentColor = if (isRunning) Color.White else Color(0xFF0D1B2A)
                )
            ) {
                Text(
                    text = if (isRunning) "ЗАВЕРШИТЬ" else "НАЧАТЬ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                )
            }
        }
    }
}

data class Particle(val angle: Float, val distance: Float, val alpha: Float, val size: Float)

fun vibrateTick(vibrator: Vibrator?) {
    vibrator?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
}

fun vibrateHold(vibrator: Vibrator?) {
    vibrator?.vibrate(
        VibrationEffect.createWaveform(
            longArrayOf(0, 50, 50, 50),
            intArrayOf(0, VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE),
            -1
        )
    )
}

fun vibrateLong(vibrator: Vibrator?) {
    vibrator?.vibrate(
        VibrationEffect.createWaveform(
            longArrayOf(0, 100, 100, 200),
            intArrayOf(0, VibrationEffect.DEFAULT_AMPLITUDE, 80, VibrationEffect.DEFAULT_AMPLITUDE),
            -1
        )
    )
}

fun formatTime(elapsed: Int, total: Int): String {
    val remain = (total - elapsed).coerceAtLeast(0)
    val min = remain / 60
    val sec = remain % 60
    return "Осталось ${min}:${sec.toString().padStart(2, '0')}"
}
