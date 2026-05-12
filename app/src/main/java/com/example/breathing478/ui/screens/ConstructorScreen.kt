package com.example.breathing478.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.breathing478.data.AppDatabase
import com.example.breathing478.data.CustomModeEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConstructorScreen(database: AppDatabase, onBack: () -> Unit) {
    val customModes by database.sessionDao().getAllCustomModes().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var editingMode by remember { mutableStateOf<CustomModeEntity?>(null) }

    var name by remember { mutableStateOf("") }
    var inhale by remember { mutableIntStateOf(4) }
    var holdIn by remember { mutableIntStateOf(7) }
    var exhale by remember { mutableIntStateOf(8) }
    var holdOut by remember { mutableIntStateOf(0) }

    fun loadMode(mode: CustomModeEntity?) {
        editingMode = mode
        name = mode?.name ?: ""
        inhale = mode?.inhale ?: 4
        holdIn = mode?.holdIn ?: 7
        exhale = mode?.exhale ?: 8
        holdOut = mode?.holdOut ?: 0
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onBack() }) {
                Icon(Icons.Default.ArrowBack, "Назад", tint = Color.White)
            }
            Text("Конструктор", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        LazyColumn(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            // Поле названия
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название режима", color = Color.White.copy(alpha = 0.5f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White.copy(alpha = 0.5f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Скроллы
            item {
                ParamScroller("Вдох", inhale, 1, 30, "сек") { inhale = it }
                ParamScroller("Задержка", holdIn, 0, 60, "сек") { holdIn = it }
                ParamScroller("Выдох", exhale, 1, 30, "сек") { exhale = it }
                ParamScroller("Пауза", holdOut, 0, 60, "сек") { holdOut = it }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Предпросмотр
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Схема: ${inhale}-${holdIn}-${exhale}" + if (holdOut > 0) "-${holdOut}" else "",
                        fontSize = 16.sp, color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Кнопки
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            scope.launch {
                                if (editingMode != null) {
                                    database.sessionDao().updateMode(editingMode!!.copy(
                                        name = name, inhale = inhale, holdIn = holdIn, exhale = exhale, holdOut = holdOut
                                    ))
                                } else if (name.isNotBlank()) {
                                    database.sessionDao().insertMode(
                                        CustomModeEntity(name = name, inhale = inhale, holdIn = holdIn, exhale = exhale, holdOut = holdOut)
                                    )
                                }
                                loadMode(null)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = name.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784))
                    ) { Text(if (editingMode != null) "Обновить" else "Сохранить", color = Color.White) }

                    if (editingMode != null) {
                        Button(
                            onClick = {
                                scope.launch {
                                    database.sessionDao().deleteMode(editingMode!!)
                                    loadMode(null)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                        ) { Icon(Icons.Default.Delete, "Удалить", tint = Color.White) }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Список сохранённых
            if (customModes.isNotEmpty()) {
                item {
                    Text("Сохранённые режимы", fontSize = 14.sp, color = Color.White.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(customModes) { mode ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(mode.name, fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.Medium)
                                Text("${mode.inhale}-${mode.holdIn}-${mode.exhale}" + if(mode.holdOut>0)"-${mode.holdOut}" else "",
                                    fontSize = 12.sp, color = Color.White.copy(alpha = 0.4f))
                            }
                            IconButton(onClick = { loadMode(mode) }) {
                                Icon(Icons.Default.Edit, "Ред.", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ParamScroller(label: String, value: Int, min: Int, max: Int, suffix: String, onValueChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.width(80.dp))
        IconButton(onClick = { onValueChange((value - 1).coerceIn(min, max)) }) {
            Text("−", fontSize = 20.sp, color = Color.White)
        }
        Text("$value $suffix", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White,
            modifier = Modifier.width(100.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        IconButton(onClick = { onValueChange((value + 1).coerceIn(min, max)) }) {
            Text("+", fontSize = 20.sp, color = Color.White)
        }
    }
}