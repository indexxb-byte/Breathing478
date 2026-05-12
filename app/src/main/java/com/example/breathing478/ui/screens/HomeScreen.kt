package com.example.breathing478.ui.screens

import android.os.Vibrator
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.breathing478.BreathingMode
import com.example.breathing478.BreathingPhase
import com.example.breathing478.data.AppDatabase
import com.example.breathing478.data.SessionEntity
import com.example.breathing478.ui.components.BreathingCircle
import com.example.breathing478.ui.components.TimeScroller
import com.example.breathing478.utils.VibrationManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class Particle(val angle: Float, val distance: Float, val alpha: Float, val size: Float)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vibrator: Vibrator,
    database: AppDatabase,
    vibeOnLockEnabled: Boolean,
    onToggleVibeOnLock: (Boolean) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToConstructor: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    var isRunning by remember { mutableStateOf(false) }
    var phase by remember { mutableStateOf(BreathingPhase.IDLE) }
    var timerText by remember { mutableStateOf("4") }
    var phaseLabel by remember { mutableStateOf("Готовы?") }
    var phaseProgress by remember { mutableStateOf(0f) }
    var elapsedSeconds by remember { mutableStateOf(0) }
    var totalMinutes by remember { mutableStateOf(3) }
    var totalSeconds by remember { mutableStateOf(180) }
    var selectedModeLabel by remember { mutableStateOf("4-7-8") }
    var selectedModeInhale by remember { mutableIntStateOf(4) }
    var selectedModeHoldIn by remember { mutableIntStateOf(7) }
    var selectedModeExhale by remember { mutableIntStateOf(8) }
    var selectedModeHoldOut by remember { mutableIntStateOf(0) }

    val customModes by database.sessionDao().getAllCustomModes().collectAsState(initial = emptyList())
    val particles = remember { mutableStateListOf<Particle>() }
    val scope = rememberCoroutineScope()

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

    LaunchedEffect(isRunning, phase) {
        while (true) {
            if (isRunning || particles.size < 15) {
                if (particles.size < 30) {
                    particles.add(Particle(Random.nextFloat() * 360f, Random.nextFloat() * 200f, 0f, Random.nextFloat() * 2f + 1f))
                }
            }
            particles.forEachIndexed { i, p ->
                particles[i] = p.copy(
                    distance = p.distance + 0.5f,
                    alpha = when {
                        p.distance < 30f -> (p.alpha + 0.03f).coerceAtMost(0.5f)
                        p.distance > 180f -> (p.alpha - 0.02f).coerceAtLeast(0f)
                        else -> p.alpha
                    }
                )
            }
            particles.removeAll { it.distance > 200f || it.alpha <= 0f }
            delay(30)
        }
    }

    LaunchedEffect(isRunning) {
        if (!isRunning) {
            phase = BreathingPhase.IDLE; timerText = "4"; phaseLabel = "Готовы?"; elapsedSeconds = 0
            return@LaunchedEffect
        }

        elapsedSeconds = 0; totalSeconds = totalMinutes * 60
        val cycleDuration = selectedModeInhale + selectedModeHoldIn + selectedModeExhale + selectedModeHoldOut

        while (isRunning) {
            if (elapsedSeconds + cycleDuration > totalSeconds) break

            phase = BreathingPhase.INHALE; phaseLabel = "Вдох"
            VibrationManager.vibrateTick(vibrator)
            for (i in 1..selectedModeInhale) {
                if (!isRunning) return@LaunchedEffect
                timerText = (selectedModeInhale + 1 - i).toString(); phaseProgress = i.toFloat() / selectedModeInhale
                delay(1000); elapsedSeconds++
            }

            if (selectedModeHoldIn > 0) {
                phase = BreathingPhase.HOLD_IN; phaseLabel = "Задержка"
                VibrationManager.vibrateHold(vibrator)
                for (i in 1..selectedModeHoldIn) {
                    if (!isRunning) return@LaunchedEffect
                    timerText = (selectedModeHoldIn + 1 - i).toString(); phaseProgress = i.toFloat() / selectedModeHoldIn
                    delay(1000); elapsedSeconds++
                }
            }

            phase = BreathingPhase.EXHALE; phaseLabel = "Выдох"
            VibrationManager.vibrateTick(vibrator)
            for (i in 1..selectedModeExhale) {
                if (!isRunning) return@LaunchedEffect
                timerText = (selectedModeExhale + 1 - i).toString(); phaseProgress = i.toFloat() / selectedModeExhale
                delay(1000); elapsedSeconds++
            }

            if (selectedModeHoldOut > 0) {
                phase = BreathingPhase.HOLD_OUT; phaseLabel = "Пауза"
                VibrationManager.vibrateHold(vibrator)
                for (i in 1..selectedModeHoldOut) {
                    if (!isRunning) return@LaunchedEffect
                    timerText = (selectedModeHoldOut + 1 - i).toString(); phaseProgress = i.toFloat() / selectedModeHoldOut
                    delay(1000); elapsedSeconds++
                }
            }
        }

        scope.launch {
            database.sessionDao().insert(SessionEntity(
                timestamp = System.currentTimeMillis(),
                modeName = selectedModeLabel,
                durationMinutes = totalMinutes,
                elapsedSeconds = elapsedSeconds,
                completed = elapsedSeconds >= totalSeconds
            ))
        }

        isRunning = false; phase = BreathingPhase.IDLE
        timerText = "✓"; phaseLabel = "Готово!"
        VibrationManager.vibrateLong(vibrator)
        delay(2000)
        timerText = "4"; phaseLabel = "Готовы?"
    }

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { p ->
                val rad = Math.toRadians(p.angle.toDouble())
                drawCircle(color = themeColor.copy(alpha = p.alpha), radius = p.size,
                    center = Offset(center.x + cos(rad).toFloat() * p.distance, center.y + sin(rad).toFloat() * p.distance))
            }
        }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.weight(1f))

            if (!isRunning) {
                Text(selectedModeLabel, fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.9f), letterSpacing = 8.sp)
                Text("ДЫХАНИЕ", fontSize = 16.sp, fontWeight = FontWeight.Light, color = Color.White.copy(alpha = 0.5f), letterSpacing = 12.sp)
                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(BreathingMode.MODE_478, BreathingMode.MODE_4444).forEach { mode ->
                        FilterChip(
                            selected = selectedModeLabel == mode.label,
                            onClick = {
                                selectedModeLabel = mode.label; selectedModeInhale = mode.inhale
                                selectedModeHoldIn = mode.holdIn; selectedModeExhale = mode.exhale; selectedModeHoldOut = mode.holdOut
                            },
                            label = { Text(mode.label, fontSize = 13.sp, color = Color.White) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color.White.copy(alpha = 0.25f), containerColor = Color.White.copy(alpha = 0.08f))
                        )
                    }
                    customModes.forEach { mode ->
                        FilterChip(
                            selected = selectedModeLabel == mode.name,
                            onClick = {
                                selectedModeLabel = mode.name; selectedModeInhale = mode.inhale
                                selectedModeHoldIn = mode.holdIn; selectedModeExhale = mode.exhale; selectedModeHoldOut = mode.holdOut
                            },
                            label = { Text(mode.name, fontSize = 13.sp, color = Color.White) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color.White.copy(alpha = 0.25f), containerColor = Color.White.copy(alpha = 0.08f))
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            BreathingCircle(phase, timerText, phaseLabel, phaseProgress, themeColor, isRunning)
            Spacer(modifier = Modifier.height(24.dp))

            if (!isRunning) {
                TimeScroller(label = "Длительность", value = totalMinutes, min = 1, max = 10,
                    suffix = when(totalMinutes){1->"минута"; in 2..4->"минуты"; else->"минут"}, vibrator = vibrator, onValueChange = { totalMinutes = it })

                Spacer(modifier = Modifier.height(16.dp))

                // Тумблер вибрации на заблокированном
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onToggleVibeOnLock(!vibeOnLockEnabled) }.padding(8.dp)) {
                    Switch(checked = vibeOnLockEnabled, onCheckedChange = { onToggleVibeOnLock(it) },
                        colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF81C784)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Вибро на заблокированном", fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f))
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val remain = (totalSeconds - elapsedSeconds).coerceAtLeast(0)
                    Text("Осталось ${remain/60}:${(remain%60).toString().padStart(2,'0')}", fontSize = 14.sp, color = Color.White.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(Modifier.width(200.dp).height(2.dp).background(Color.White.copy(alpha = 0.1f))) {
                        Box(Modifier.fillMaxHeight().fillMaxWidth(elapsedSeconds.toFloat()/totalSeconds).background(themeColor))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (isRunning) { isRunning = false; phase = BreathingPhase.IDLE; timerText = "4"; phaseLabel = "Готовы?"; elapsedSeconds = 0 }
                    else { elapsedSeconds = 0; isRunning = true }
                },
                modifier = Modifier.width(200.dp).height(56.dp), shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if(isRunning) Color(0xFFE53935) else Color.White, contentColor = if(isRunning) Color.White else Color(0xFF0D1B2A))
            ) { Text(if(isRunning) "ЗАВЕРШИТЬ" else "НАЧАТЬ", fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp) }

            Spacer(modifier = Modifier.weight(1f))

            if (!isRunning) {
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    BottomIconButton(Icons.Default.CalendarMonth, "История", onNavigateToHistory)
                    BottomIconButton(Icons.Default.Notifications, "Напом.", onNavigateToReminders)
                    BottomIconButton(Icons.Default.Edit, "Констр.", onNavigateToConstructor)
                    BottomIconButton(Icons.Default.Settings, "Настр.", onNavigateToSettings)
                }
            }
        }
    }
}

@Composable
fun BottomIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick).padding(8.dp)) {
        Icon(icon, label, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(28.dp))
        Text(label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
    }
}
