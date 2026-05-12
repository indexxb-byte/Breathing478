package com.example.breathing478.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.breathing478.data.AppDatabase
import com.example.breathing478.data.ReminderEntity
import com.example.breathing478.ui.components.TimeScroller
import com.example.breathing478.utils.cancelReminder
import com.example.breathing478.utils.scheduleReminder
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    database: AppDatabase,
    requestExactAlarm: () -> Unit = {},
    requestNotifications: () -> Unit = {},
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val reminders by database.sessionDao().getAllReminders().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val daysOfWeek = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

    var showTimePicker by remember { mutableStateOf(false) }
    var selectedHour by remember { mutableIntStateOf(8) }
    var selectedMinute by remember { mutableIntStateOf(0) }
    var selectedDays by remember { mutableIntStateOf(0b1111111) }

    // Запрос разрешений при первом входе на экран
    LaunchedEffect(Unit) {
        requestExactAlarm()
        requestNotifications()
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Назад", tint = Color.White) }
            Text("Напоминания", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(reminders) { reminder ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Switch(checked = reminder.enabled, onCheckedChange = { enabled ->
                            scope.launch {
                                val updated = reminder.copy(enabled = enabled)
                                database.sessionDao().updateReminder(updated)
                                val rc = (reminder.hour * 100 + reminder.minute) * 100 + reminder.id.toInt()
                                if (enabled) scheduleReminder(context, reminder.hour, reminder.minute, reminder.days, rc)
                                else cancelReminder(context, rc)
                            }
                        }, colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF81C784)))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("${reminder.hour.toString().padStart(2, '0')}:${reminder.minute.toString().padStart(2, '0')}",
                            fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(daysOfWeek.filterIndexed { i, _ -> (reminder.days shr i) and 1 == 1 }.joinToString(", "),
                            fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = {
                            scope.launch {
                                val rc = (reminder.hour * 100 + reminder.minute) * 100 + reminder.id.toInt()
                                cancelReminder(context, rc)
                                database.sessionDao().deleteReminder(reminder)
                            }
                        }) {
                            Icon(Icons.Default.Delete, "Удалить", tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
            item {
                TextButton(
                    onClick = {
                        requestExactAlarm()
                        requestNotifications()
                        showTimePicker = true
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF81C784))
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Добавить время", fontSize = 16.sp)
                }
            }
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("Новое напоминание", color = Color.White) },
            text = {
                Column {
                    Text("Часы", fontSize = 14.sp, color = Color.White.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(4.dp))
                    TimeScroller(
                        vibrator = null,
                        value = selectedHour,
                        min = 0, max = 23,
                        formatValue = { it.toString().padStart(2, '0') },
                        onValueChange = { selectedHour = it }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Минуты", fontSize = 14.sp, color = Color.White.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(4.dp))
                    TimeScroller(
                        vibrator = null,
                        value = selectedMinute,
                        min = 0, max = 55,
                        formatValue = { (it - it % 5).toString().padStart(2, '0') },
                        onValueChange = { selectedMinute = it - it % 5 }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Дни:", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                        daysOfWeek.forEachIndexed { index, dayName ->
                            val mask = 1 shl index
                            val isSelected = (selectedDays and mask) != 0
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedDays = selectedDays xor mask },
                                label = { Text(dayName, fontSize = 12.sp, color = if (isSelected) Color(0xFF1A1A1A) else Color.White) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF81C784),
                                    containerColor = Color.White.copy(alpha = 0.08f)
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val reminder = ReminderEntity(hour = selectedHour, minute = selectedMinute, days = selectedDays)
                        database.sessionDao().insertReminder(reminder)
                        val rc = (selectedHour * 100 + selectedMinute) * 100
                        scheduleReminder(context, selectedHour, selectedMinute, selectedDays, rc)
                    }
                    showTimePicker = false
                }) { Text("Сохранить", color = Color(0xFF81C784)) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Отмена", color = Color.White.copy(alpha = 0.5f))
                }
            }
        )
    }
}
