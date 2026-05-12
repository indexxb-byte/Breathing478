package com.example.breathing478.ui.components

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun TimeScroller(
    label: String = "",
    value: Int,
    min: Int = 1,
    max: Int = 60,
    suffix: String = "",
    vibrator: Vibrator? = null,
    onValueChange: (Int) -> Unit,
    formatValue: (Int) -> String = { "$it" }
) {
    var scrollAccumulator by remember { mutableFloatStateOf(0f) }
    var lastCommitted by remember { mutableIntStateOf(value) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (label.isNotEmpty()) {
            Text(label, fontSize = 14.sp, color = Color.White.copy(alpha = 0.5f), letterSpacing = 4.sp)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Box(
            modifier = Modifier
                .height(60.dp)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { _, dragAmount ->
                            scrollAccumulator += dragAmount
                            val threshold = 30f
                            if (kotlin.math.abs(scrollAccumulator) >= threshold) {
                                val steps = (scrollAccumulator / threshold).roundToInt()
                                val newValue = (lastCommitted - steps).coerceIn(min, max)
                                if (newValue != lastCommitted) {
                                    lastCommitted = newValue
                                    onValueChange(newValue)
                                    vibrator?.let {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            it.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
                                        } else {
                                            @Suppress("DEPRECATION") it.vibrate(20)
                                        }
                                    }
                                }
                                scrollAccumulator -= steps * threshold
                            }
                        },
                        onDragEnd = {
                            scrollAccumulator = 0f
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("▼", fontSize = 12.sp, color = Color.White.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.width(12.dp))
                Text(formatValue(lastCommitted), fontSize = 56.sp, fontWeight = FontWeight.Bold, color = Color.White)
                if (suffix.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(suffix, fontSize = 16.sp, color = Color.White.copy(alpha = 0.6f))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("▲", fontSize = 12.sp, color = Color.White.copy(alpha = 0.3f))
            }
        }
    }
}
