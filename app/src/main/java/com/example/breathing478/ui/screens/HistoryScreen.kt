package com.example.breathing478.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.breathing478.data.AppDatabase
import com.example.breathing478.data.SessionEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(database: AppDatabase, onBack: () -> Unit) {
    val sessions by database.sessionDao().getAllSessions().collectAsState(initial = emptyList())
    val grouped = remember(sessions) { groupSessionsByDate(sessions) }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
        // Верхняя панель
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Назад", tint = Color.White)
            }
            Text("История", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        // Статистика
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("Всего", "${sessions.size}")
                StatItem("Заверш.", "${sessions.count { it.completed }}")
                StatItem("Минут", "${sessions.sumOf { it.durationMinutes }}")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Список по дням
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            grouped.forEach { (date, daySessions) ->
                item {
                    Text(
                        date,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(daySessions) { session ->
                    SessionCard(session)
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(label, fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
    }
}

@Composable
fun SessionCard(session: SessionEntity) {
    val dateFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(10.dp).clip(CircleShape)
                    .background(if (session.completed) Color(0xFF81C784) else Color(0xFFE53935))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(session.modeName, fontSize = 15.sp, color = Color.White)
                Text("${dateFormat.format(Date(session.timestamp))} • ${session.durationMinutes} мин",
                    fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
            }
            Text(
                if (session.completed) "✓" else "✕",
                fontSize = 18.sp, color = if (session.completed) Color(0xFF81C784) else Color(0xFFE53935)
            )
        }
    }
}

fun groupSessionsByDate(sessions: List<SessionEntity>): List<Pair<String, List<SessionEntity>>> {
    val dateFormat = SimpleDateFormat("d MMMM, EEEE", Locale("ru"))
    return sessions.groupBy { dateFormat.format(Date(it.timestamp)) }.toList().sortedByDescending { it.second.first().timestamp }
}