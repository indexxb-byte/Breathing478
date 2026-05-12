package com.example.breathing478.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
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
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    var currentMonth by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var currentYear by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var selectedDate by remember { mutableStateOf<String?>(null) }

    // Дни с сессиями
    val daysWithSessions = remember(sessions) {
        sessions.map { dateFormat.format(Date(it.timestamp)) }.toSet()
    }

    // Сессии для выбранного дня
    val selectedSessions = remember(sessions, selectedDate) {
        if (selectedDate == null) emptyList()
        else sessions.filter { dateFormat.format(Date(it.timestamp)) == selectedDate }
    }

    // Дни в месяце
    val calendar = remember(currentYear, currentMonth) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOffset = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
    val monthNames = listOf("Январь","Февраль","Март","Апрель","Май","Июнь","Июль","Август","Сентябрь","Октябрь","Ноябрь","Декабрь")

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Назад", tint = Color.White) }
            Text("История", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        // Календарь
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Переключатель месяца
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = {
                        if (currentMonth == 0) { currentMonth = 11; currentYear-- } else currentMonth--
                    }) { Text("◀", color = Color.White, fontSize = 18.sp) }
                    Text("${monthNames[currentMonth]} $currentYear", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    IconButton(onClick = {
                        if (currentMonth == 11) { currentMonth = 0; currentYear++ } else currentMonth++
                    }) { Text("▶", color = Color.White, fontSize = 18.sp) }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Дни недели
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("Пн","Вт","Ср","Чт","Пт","Сб","Вс").forEach {
                        Text(it, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))

                // Сетка дней
                var day = 1
                for (row in 0 until 6) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 0 until 7) {
                            val cellModifier = Modifier.weight(1f).aspectRatio(1f)
                            if (row == 0 && col < firstDayOffset || day > daysInMonth) {
                                Box(cellModifier)
                            } else {
                                val currentDay = day
                                val dateStr = "$currentYear-${(currentMonth+1).toString().padStart(2,'0')}-${currentDay.toString().padStart(2,'0')}"
                                val hasSession = daysWithSessions.contains(dateStr)
                                val isSelected = selectedDate == dateStr

                                Box(cellModifier.clickable {
                                    selectedDate = if (isSelected) null else dateStr
                                }, contentAlignment = Alignment.Center) {
                                    Box(
                                        modifier = Modifier.size(30.dp)
                                            .then(if (isSelected) Modifier.border(2.dp, Color(0xFF81C784), CircleShape) else Modifier)
                                            .clip(CircleShape)
                                            .background(if (hasSession) Color(0xFF81C784).copy(alpha = 0.3f) else Color.Transparent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("$currentDay", fontSize = 14.sp, color = Color.White)
                                    }
                                }
                                day++
                            }
                        }
                    }
                    if (day > daysInMonth) break
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Статистика
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem("Всего", "${sessions.size}")
                StatItem("Заверш.", "${sessions.count{it.completed}}")
                StatItem("Минут", "${sessions.sumOf{it.durationMinutes}}")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Список сессий
        if (selectedDate != null) {
            Text("Сессии за $selectedDate", fontSize = 16.sp, fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(selectedSessions) { session -> SessionCard(session) }
            }
        } else {
            val grouped = groupSessionsByDate(sessions)
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                grouped.forEach { (date, daySessions) ->
                    item {
                        Text(date, fontSize = 16.sp, fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    }
                    items(daySessions) { session -> SessionCard(session) }
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
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)), shape = RoundedCornerShape(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(if(session.completed) Color(0xFF81C784) else Color(0xFFE53935)))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(session.modeName, fontSize = 15.sp, color = Color.White)
                Text("${dateFormat.format(Date(session.timestamp))} • ${session.durationMinutes} мин", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
            }
            Text(if(session.completed) "✓" else "✕", fontSize = 18.sp, color = if(session.completed) Color(0xFF81C784) else Color(0xFFE53935))
        }
    }
}

fun groupSessionsByDate(sessions: List<SessionEntity>): List<Pair<String, List<SessionEntity>>> {
    val dateFormat = SimpleDateFormat("d MMMM, EEEE", Locale("ru"))
    return sessions.groupBy { dateFormat.format(Date(it.timestamp)) }.toList().sortedByDescending { it.second.first().timestamp }
}
