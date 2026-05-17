package com.kiltler.assistant.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Планирование точных будильников для напоминаний и заказов.
 *
 * Каждой записи соответствует свой requestCode, чтобы будильники
 * не перетирали друг друга. Заказы смещены, чтобы их id не пересекались
 * с id напоминаний.
 */
object ReminderScheduler {

    const val EXTRA_TITLE = "extra_title"
    const val EXTRA_TEXT = "extra_text"
    const val EXTRA_ID = "extra_id"

    private const val ORDER_OFFSET = 5_000_000

    fun reminderRequestCode(id: Long): Int = id.toInt()
    fun orderRequestCode(id: Long): Int = (ORDER_OFFSET + id).toInt()

    fun schedule(context: Context, requestCode: Int, triggerAtMillis: Long, title: String, text: String) {
        if (triggerAtMillis <= System.currentTimeMillis()) {
            cancel(context, requestCode)
            return
        }
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = buildPendingIntent(context, requestCode, title, text)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    fun cancel(context: Context, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(buildPendingIntent(context, requestCode, "", ""))
    }

    private fun buildPendingIntent(context: Context, requestCode: Int, title: String, text: String): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_ID, requestCode)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_TEXT, text)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
