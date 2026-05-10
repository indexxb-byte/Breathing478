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
    INHALE, HOLD_IN, EXHALE, HOLD_OUT, IDLE
}

enum class BreathingMode(val label: String, val inhale: Int, val holdIn: Int, val exhale: Int, val holdOut: Int) {
    MODE_478("4-7-8", 4, 7, 8, 0),
    MODE_4444("4-4-4-4", 4, 4, 4, 4)
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
    var breathingMode by remember { mutableStateOf(BreathingMode.MODE_478) }

    val context = LocalContext.current
    val view = LocalView.current
    val vibrator = context.getSystemService(Vibrator::class.java)

    val animatedScale = remember { Animatable(0.35f) }
    val glowAlpha = remember { Animatable(0.3f) }
    val particles = remember { mutableStateListOf<Particle>() }

    // Плавный скролл
    var displayMinutes by remember { mutableFloatStateOf(totalMinutes.toFloat()) }
    val scrollAnimatable = remember { Animatable(totalMinutes.toFloat()) }

    val themeColor = when (phase) {
        BreathingPhase.INHALE -> Color(0xFF64B5F6)
        BreathingPhase.HOLD_IN -> Color(0xFFFFD54F)
        BreathingPhase.EXHALE -> Color(0xFF81C784)
        BreathingPhase.HOLD_OUT -> Color(0xFFCE93D8)
        BreathingPhase.IDLE -> Color(0xFF90A4AE)
    }

    val bgColor = when (phase) {
        BreathingPhase.INHALE -> Color(0xFF0D1B2A)
        BreathingPhase.HOLD_IN -> Color(0xFF1A0A00)
        BreathingPhase.EXHALE -> Color(0xFF0A1A0A)
        BreathingPhase.HOLD_OUT -> Color(0xFF1A0A1A)
        BreathingPhase.IDLE -> Color(0xFF121212)
    }

    LaunchedEffect(phase) {
        when (phase) {
            BreathingPhase.INHALE -> {
                animatedScale.animateTo(1f, animationSpec = tween(4000, easing = FastOutSlowInEasing))
                glowAlpha.animateTo(0.8f, animationSpec = tween(4000))
            }
            BreathingPhase.EXHALE, BreathingPhase.HOLD_OUT -> {
                animatedScale.animateTo(0.35f, animationSpec = tween(8000, easing = FastOutSlowInEasing))
                glowAlpha.animateTo(0.2f, animationSpec = tween(8000))
            }
            BreathingPhase.HOLD_IN -> {
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

        val mode = breathingMode
        val cycleDuration = mode.inhale + mode.holdIn + mode.exhale + mode.holdOut

        while (isRunning) {
            // Проверяем, хватит ли времени на полный цикл
            if (elapsedSeconds + cycleDuration > totalSeconds) break

            // Вдох
            phase = BreathingPhase.INHALE
            phaseLabel = "Вдох"
            vibrateTick(vibrator)
            for (i in 1..mode.inhale) {
                if (!isRunning) return@LaunchedEffect
                timerText = (mode.inhale + 1 - i).toString()
                phaseProgress = i.toFloat() / mode.inhale
                delay(1000)
                elapsedSeconds++
            }

            // Задержка на вдохе
            if (mode.holdIn > 0) {
                phase = BreathingPhase.HOLD_IN
                phaseLabel = "Задержка"
                vibrateHold(vibrator)
                for (i in 1..mode.holdIn) {
                    if (!isRunning) return@LaunchedEffect
                    timerText = (mode.holdIn + 1 - i).toString()
                    phaseProgress = i.toFloat() / mode.holdIn
                    delay(1000)
                    elapsedSeconds++
                }
            }

            // Выдох
            phase = BreathingPhase.EXHALE
            phaseLabel = "Выдох"
            vibrateTick(vibrator)
            for (i in 1..mode.exhale) {
                if (!isRunning) return@LaunchedEffect
                timerText = (mode.exhale + 1 - i).toString()
                phaseProgress = i.toFloat() / mode.exhale
                delay(1000)
                elapsedSeconds++
            }

            // Задержка на выдохе (только для 4-4-4-4)
            if (mode.holdOut > 0) {
                phase = BreathingPhase.HOLD_OUT
                phaseLabel = "Пауза"
                vibrateHold(vibrator)
                for (i in 1..mode.holdOut) {
                    if (!isRunning) return@LaunchedEffect
                    timerText = (mode.holdOut + 1 - i).toString()
                    phaseProgress = i.toFloat() / mode.holdOut
                    delay(1000)
                    elapsedSeconds++
                }
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
                    text = breathingMode.label,
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
                Spacer(modifier = Modifier.height(32.dp))

                // Переключатель режима
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BreathingMode.entries.forEach { mode ->
                        FilterChip(
                            selected = breathingMode == mode,
                            onClick = { breathingMode = mode },
                            label = { Text(mode.label, color = Color.White, fontSize = 14.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color.White.copy(alpha = 0.2f),
                                containerColor = Color.White.copy(alpha = 0.05f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
            }

            // Круг
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

                    drawCircle(color = Color.White.copy(alpha = 0.05f), radius = baseRadius, center = Offset(cx, cy))
                    drawCircle(color = Color.White.copy(alpha = 0.1f), radius = baseRadius * 0.9f, center = Offset(cx, cy))
                    drawCircle(color = Color.White.copy(alpha = 0.2f), radius = baseRadius * 0.75f, center = Offset(cx, cy))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = timerText, fontSize = 72.sp, fontWeight = FontWeight.Bold, color = Color.White)
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

                // Плавный скролл с прилипанием
                val targetMinutes by remember { derivedStateOf { kotlin.math.roundToInt(scrollAnimatable.value).toFloat() } }

                Box(
                    modifier = Modifier
                        .height(60.dp)
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onDrag = { _, dragAmount ->
                                    scrollAnimatable.snapTo(
                                        (scrollAnimatable.value - dragAmount / 50f).coerceIn(1f, 10f)
                                    )
                                },
                                onDragEnd = {
                                    val snapped = kotlin.math.roundToInt(scrollAnimatable.value).coerceIn(1, 10)
                                    scrollAnimatable.animateTo(snapped.toFloat(), animationSpec = spring(dampingRatio = 0.6f))
                                    totalMinutes = snapped
                                    vibrateScroll()
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "▼", fontSize = 12.sp, color = Color.White.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "${kotlin.math.roundToInt(scrollAnimatable.value)}",
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when {
                                kotlin.math.roundToInt(scrollAnimatable.value) == 1 -> "минута"
                                kotlin.math.roundToInt(scrollAnimatable.value) in 2..4 -> "минуты"
                                else -> "минут"
                            },
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
                    Text(text = formatTime(elapsedSeconds, totalSeconds), fontSize = 14.sp, color = Color.White.copy(alpha = 0.5f))
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
                        totalMinutes = kotlin.math.roundToInt(scrollAnimatable.value).coerceIn(1, 10)
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
                Text(text = if (isRunning) "ЗАВЕРШИТЬ" else "НАЧАТЬ", fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
            }
        }
    }
}

data class Particle(val angle: Float, val distance: Float, val alpha: Float, val size: Float)

fun vibrateTick(vibrator: Vibrator?) {
    vibrator?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
}

fun vibrateHold(vibrator: Vibrator?) {
    vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 50, 50, 50), intArrayOf(0, VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE), -1))
}

fun vibrateLong(vibrator: Vibrator?) {
    vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 100, 200), intArrayOf(0, VibrationEffect.DEFAULT_AMPLITUDE, 80, VibrationEffect.DEFAULT_AMPLITUDE), -1))
}

fun formatTime(elapsed: Int, total: Int): String {
    val remain = (total - elapsed).coerceAtLeast(0)
    val min = remain / 60
    val sec = remain % 60
    return "Осталось ${min}:${sec.toString().padStart(2, '0')}"
}
