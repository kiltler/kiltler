package com.bodyquest.app.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

/**
 * Локальные напоминания через AlarmManager. Без сети.
 */
class ReminderScheduler(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "bodyquest_reminders"
        const val WORKOUT_REQUEST = 1001
        const val WATER_REQUEST = 1002
        const val STREAK_REQUEST = 1003
        const val EXTRA_KIND = "kind"
        const val KIND_WORKOUT = "workout"
        const val KIND_WATER = "water"
        const val KIND_STREAK = "streak"
    }

    fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Напоминания BodyQuest",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = "Тренировки и вода" }
            nm.createNotificationChannel(channel)
        }
    }

    private fun pendingIntent(requestCode: Int, kind: String): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_KIND, kind)
        }
        var flags = PendingIntent.FLAG_UPDATE_CURRENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flags = flags or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, requestCode, intent, flags)
    }

    fun scheduleWorkout(hour: Int, minute: Int) {
        ensureChannel()
        scheduleDaily(WORKOUT_REQUEST, KIND_WORKOUT, hour, minute)
        // Страж серии: вечером напомнит, только если сегодня не было тренировки.
        scheduleDaily(STREAK_REQUEST, KIND_STREAK, 20, 0)
    }

    fun cancelWorkout() {
        cancel(WORKOUT_REQUEST, KIND_WORKOUT)
        cancel(STREAK_REQUEST, KIND_STREAK)
    }

    /** Напоминание о воде — ежедневно в полдень (упрощённо, один раз в день). */
    fun scheduleWater(enabled: Boolean) {
        if (enabled) {
            ensureChannel()
            // Периодически в течение дня (каждые ~3 часа), а не один раз в полдень.
            val am = context.getSystemService(AlarmManager::class.java) ?: return
            val intervalMs = 3 * 60 * 60 * 1000L
            try {
                am.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + intervalMs,
                    intervalMs,
                    pendingIntent(WATER_REQUEST, KIND_WATER),
                )
            } catch (_: SecurityException) {
                // ignore
            }
        } else {
            cancel(WATER_REQUEST, KIND_WATER)
        }
    }

    private fun scheduleDaily(requestCode: Int, kind: String, hour: Int, minute: Int) {
        val am = context.getSystemService(AlarmManager::class.java) ?: return
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= now) add(Calendar.DAY_OF_MONTH, 1)
        }
        try {
            am.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                cal.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent(requestCode, kind),
            )
        } catch (_: SecurityException) {
            // На некоторых устройствах нужно разрешение точных будильников — игнорируем безопасно.
        }
    }

    private fun cancel(requestCode: Int, kind: String) {
        val am = context.getSystemService(AlarmManager::class.java) ?: return
        am.cancel(pendingIntent(requestCode, kind))
    }
}
