package com.bodyquest.app.notifications

import android.Manifest
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.bodyquest.app.R
import com.bodyquest.app.data.AppDatabase
import com.bodyquest.app.domain.Dates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val kind = intent.getStringExtra(ReminderScheduler.EXTRA_KIND) ?: ReminderScheduler.KIND_WORKOUT

        // Страж серии: уведомляем только если сегодня не было тренировки и серия > 0.
        if (kind == ReminderScheduler.KIND_STREAK) {
            val pending = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.get(context)
                    val today = Dates.todayEpochDay()
                    val doneToday = db.workoutDao().countOnDay(today)
                    val streak = db.streakDao().get()?.current ?: 0
                    if (doneToday == 0 && streak > 0) {
                        notify(
                            context, id = 3,
                            title = "🔥 Серия под угрозой!",
                            text = "Серия $streak дн. оборвётся в полночь. Успей тренировку!",
                        )
                    }
                } finally {
                    pending.finish()
                }
            }
            return
        }

        val (title, text, id) = when (kind) {
            ReminderScheduler.KIND_WATER ->
                Triple("💧 Энергия дня", "Пора пополнить запас воды. К цели ~2.5–3 л!", 2)
            else ->
                Triple("⚔️ Квест дня ждёт", "Время прокачать персонажа. Открой BodyQuest!", 1)
        }
        notify(context, id, title, text)
    }

    private fun notify(context: Context, id: Int, title: String, text: String) {
        val notification = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val allowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (allowed) {
            context.getSystemService(NotificationManager::class.java)?.notify(id, notification)
        }
    }
}
