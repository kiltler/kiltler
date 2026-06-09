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

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val kind = intent.getStringExtra(ReminderScheduler.EXTRA_KIND) ?: ReminderScheduler.KIND_WORKOUT
        val (title, text, id) = when (kind) {
            ReminderScheduler.KIND_WATER ->
                Triple("💧 Энергия дня", "Пора пополнить запас воды. К цели ~2.5–3 л!", 2)
            else ->
                Triple("⚔️ Квест дня ждёт", "Время прокачать персонажа. Открой BodyQuest!", 1)
        }

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
