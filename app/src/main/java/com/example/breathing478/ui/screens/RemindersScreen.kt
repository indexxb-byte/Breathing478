package com.example.breathing478.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.breathing478.utils.cancelReminder
import com.example.breathing478.utils.scheduleReminder
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(database: AppDatabase, onBack: () -> Unit) {
    val context = LocalContext.current
    val reminders by database.sessionDao().getAllReminders().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val daysOfWeek = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

    var showTimePicker by remember { mutableStateOf(false) }
    var selectedHour by remember { mutableIntStateOf(8) }
    var selectedMinute by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Назад", tint = Color.White)
            }
            Text("Напоминания", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(reminders) { reminder ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = reminder.enabled,
                            onCheckedChange = { enabled ->
                                scope.launch {
                                    val updated = reminder.copy(enabled = enabled)
                                    database.sessionDao().updateReminder(updated)
                                    if (enabled) scheduleReminder(context, reminder.hour, reminder.minute, reminder.days, reminder.id.toInt())
                                    else cancelReminder(context, reminder.id.toInt())
                                }
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF81C784))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "${reminder.hour.toString().padStart(2, '0')}:${reminder.minute.toString().padStart(2, '0')}",
                            fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            daysOfWeek.filterIndexed { i, _ -> (reminder.days shr i) and 1 == 1 }.joinToString(", "),
                            fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = {
                            scope.launch {
                                cancelReminder(context, reminder.id.toInt())
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
                    onClick = { showTimePicker = true },
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

    // TimePicker диалог
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("Выберите время", color = Color.White) },
            text = {
                Column {
                    TimeScrollerHour(vibrator = null, value = selectedHour, onValueChange = { selectedHour = it })
                    Spacer(modifier = Modifier.height(8.dp))
                    TimeScrollerHour(vibrator = null, value = selectedMinute, min = 0, max = 59, formatValue = { it.toString().padStart(2, '0') }, onValueChange = { selectedMinute = it })
                }
            },
            confirmButton = {
    TextButton(onClick = {
        scope.launch {
            val reminder = ReminderEntity(hour = selectedHour, minute = selectedMinute)
            val newId = database.sessionDao().insertReminder(reminder)
            val newId = database.sessionDao().insertReminder(reminder)
scheduleReminder(context, selectedHour, selectedMinute, 0b1111111, newId.toInt())
        }
        showTimePicker = false
    }) { Text("Сохранить", color = Color(0xFF81C784)) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Отмена", color = Color.White.copy(alpha = 0.5f)) }
            }
        )
    }
}

@Composable
fun TimeScrollerHour(
    vibrator: android.os.Vibrator? = null,
    value: Int,
    min: Int = 0,
    max: Int = 23,
    formatValue: (Int) -> String = { it.toString().padStart(2, '0') },
    onValueChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onValueChange((value - 1).coerceIn(min, max)) }) {
            Text("−", fontSize = 24.sp, color = Color.White)
        }
        Text(formatValue(value), fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 24.dp))
        IconButton(onClick = { onValueChange((value + 1).coerceIn(min, max)) }) {
            Text("+", fontSize = 24.sp, color = Color.White)
        }
    }
}
