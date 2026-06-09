package com.bodyquest.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bodyquest.app.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Перепланирует напоминания после перезагрузки устройства. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        val scheduler = ReminderScheduler(context.applicationContext)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = AppDatabase.get(context).settingsDao().get()
                if (settings?.remindersEnabled == true) {
                    scheduler.scheduleWorkout(settings.reminderHour, settings.reminderMinute)
                }
                if (settings?.waterRemindersEnabled == true) {
                    scheduler.scheduleWater(true)
                }
            } finally {
                pending.finish()
            }
        }
    }
}
