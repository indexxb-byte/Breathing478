package com.example.breathing478.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, onIntensityChanged: (Float) -> Unit) {
    var intensity by remember { mutableFloatStateOf(1f) }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Назад", tint = Color.White)
            }
            Text("Настройки", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Интенсивность вибрации", fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = intensity,
                        onValueChange = { intensity = it },
                        onValueChangeFinished = { onIntensityChanged(intensity) },
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color(0xFF81C784),
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("${(intensity * 100).toInt()}%", fontSize = 14.sp, color = Color.White.copy(alpha = 0.5f))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("О приложении", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.White.copy(alpha = 0.7f))
            Text("Версия 1.0", fontSize = 13.sp, color = Color.White.copy(alpha = 0.4f))
            Text("Дыши. Расслабься — дыхательные практики 4-7-8 и 4-4-4-4",
                fontSize = 13.sp, color = Color.White.copy(alpha = 0.4f))
        }
    }
}