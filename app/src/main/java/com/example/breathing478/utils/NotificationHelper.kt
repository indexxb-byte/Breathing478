package com.example.breathing478.utils

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.util.Calendar

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val soundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, "breathing_reminder_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Дыши. Расслабься")
            .setContentText("Время подышать! 🧘")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 300, 200, 300, 200, 500))
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                NotificationManagerCompat.from(context).notify(
                    System.currentTimeMillis().toInt() % Int.MAX_VALUE,
                    notification
                )
            }
        } else {
            NotificationManagerCompat.from(context).notify(
                System.currentTimeMillis().toInt() % Int.MAX_VALUE,
                notification
            )
        }

        // Перепланирование для следующего раза
        val requestCode = intent?.getIntExtra("requestCode", 0) ?: 0
        if (requestCode != 0) {
            rescheduleAlarm(context, requestCode)
        }
    }

    private fun rescheduleAlarm(context: Context, requestCode: Int) {
        // Если это ежедневный — он сам повторится через INTERVAL_DAY
        // Если по дням — тоже через INTERVAL_DAY * 7
        // setRepeating делает это автоматически
    }
}

fun createReminderChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val soundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val channel = NotificationChannel(
            "breathing_reminder_channel",
            "Напоминания о дыхании",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Уведомления о времени дыхательной практики"
            setSound(soundUri, audioAttributes)
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 300, 200, 300, 200, 500)
            setShowBadge(true)
            enableLights(true)
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}

fun scheduleReminder(context: Context, hour: Int, minute: Int, daysBitmask: Int, requestCode: Int) {
    createReminderChannel(context)

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    cancelReminder(context, requestCode)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (!alarmManager.canScheduleExactAlarms()) return
    }

    val intent = Intent(context, ReminderReceiver::class.java).apply {
        putExtra("requestCode", requestCode)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context, requestCode, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    if (daysBitmask == 0b1111111) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_MONTH, 1)
        }
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    } else if (daysBitmask != 0) {
        for (dayIndex in 0..6) {
            if ((daysBitmask shr dayIndex) and 1 == 1) {
                val dayOfWeek = when (dayIndex) {
                    0 -> Calendar.MONDAY; 1 -> Calendar.TUESDAY; 2 -> Calendar.WEDNESDAY
                    3 -> Calendar.THURSDAY; 4 -> Calendar.FRIDAY; 5 -> Calendar.SATURDAY
                    6 -> Calendar.SUNDAY; else -> Calendar.MONDAY
                }
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_WEEK, dayOfWeek)
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    if (before(Calendar.getInstance())) add(Calendar.WEEK_OF_MONTH, 1)
                }
                val dayRequestCode = requestCode + dayIndex + 1
                val dayIntent = Intent(context, ReminderReceiver::class.java).apply {
                    putExtra("requestCode", dayRequestCode)
                }
                val dayPendingIntent = PendingIntent.getBroadcast(
                    context, dayRequestCode, dayIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY * 7,
                    dayPendingIntent
                )
            }
        }
    }
}

fun cancelReminder(context: Context, requestCode: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, ReminderReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context, requestCode, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    alarmManager.cancel(pendingIntent)
    for (i in 1..7) {
        val dayIntent = Intent(context, ReminderReceiver::class.java)
        val dayPendingIntent = PendingIntent.getBroadcast(
            context, requestCode + i, dayIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(dayPendingIntent)
    }
}
