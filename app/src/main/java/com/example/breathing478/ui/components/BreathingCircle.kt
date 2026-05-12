import com.example.breathing478.BreathingPhase
package com.example.breathing478.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.breathing478.BreathingPhase

@Composable
fun BreathingCircle(
    phase: BreathingPhase,
    timerText: String,
    phaseLabel: String,
    phaseProgress: Float,
    themeColor: Color,
    isRunning: Boolean,
    modifier: Modifier = Modifier
) {
    // Анимация масштаба
    val targetScale = when {
        !isRunning -> 0.35f
        phase == BreathingPhase.INHALE -> 1f
        phase == BreathingPhase.HOLD_IN -> 1f
        phase == BreathingPhase.EXHALE || phase == BreathingPhase.HOLD_OUT -> 0.35f
        else -> 0.35f
    }

    val duration = when (phase) {
        BreathingPhase.INHALE -> 4000
        BreathingPhase.EXHALE -> 8000
        BreathingPhase.HOLD_OUT -> 8000
        else -> 500
    }

    val animatedScale = remember { Animatable(0.35f) }

    LaunchedEffect(targetScale, isRunning) {
        if (!isRunning) {
            // Медленное дыхание в меню
            while (!isRunning) {
                animatedScale.animateTo(0.42f, tween(3000, easing = FastOutSlowInEasing))
                animatedScale.animateTo(0.35f, tween(3000, easing = FastOutSlowInEasing))
            }
        } else {
            animatedScale.animateTo(targetScale, tween(duration, easing = FastOutSlowInEasing))
        }
    }

    Box(contentAlignment = Alignment.Center, modifier = modifier.size(240.dp)) {
        Canvas(modifier = Modifier.size(240.dp)) {
            val cx = size.width / 2
            val cy = size.height / 2
            val baseRadius = size.width / 2 * animatedScale.value

            // Свечение
            for (i in 3 downTo 0) {
                drawCircle(
                    color = themeColor.copy(alpha = 0.03f),
                    radius = baseRadius + i * 20f + 10f,
                    center = Offset(cx, cy)
                )
            }

            // Кольцо прогресса
            if (phase != BreathingPhase.IDLE && isRunning) {
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

            drawCircle(Color.White.copy(alpha = 0.05f), baseRadius, Offset(cx, cy))
            drawCircle(Color.White.copy(alpha = 0.1f), baseRadius * 0.9f, Offset(cx, cy))
            drawCircle(Color.White.copy(alpha = 0.2f), baseRadius * 0.75f, Offset(cx, cy))
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(timerText, fontSize = 72.sp, fontWeight = FontWeight.Bold, color = Color.White)
            if (isRunning) {
                Text(
                    phaseLabel,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = themeColor,
                    letterSpacing = 4.sp
                )
            }
        }
    }
}
