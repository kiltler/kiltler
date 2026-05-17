package com.kiltler.assistant.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kiltler.assistant.data.AppDatabase
import com.kiltler.assistant.data.OrderStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** После перезагрузки телефона заново планирует все будильники. */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        val appContext = context.applicationContext

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.get(appContext)

                db.reminderDao().getAll()
                    .filter { !it.isDone && it.timeMillis > System.currentTimeMillis() }
                    .forEach {
                        ReminderScheduler.schedule(
                            appContext,
                            ReminderScheduler.reminderRequestCode(it.id),
                            it.timeMillis,
                            it.title,
                            it.description
                        )
                    }

                db.orderDao().getAll()
                    .filter {
                        it.reminderEnabled &&
                            it.scheduledMillis != null &&
                            it.scheduledMillis > System.currentTimeMillis() &&
                            OrderStatus.from(it.status) != OrderStatus.DONE &&
                            OrderStatus.from(it.status) != OrderStatus.CANCELLED
                    }
                    .forEach {
                        ReminderScheduler.schedule(
                            appContext,
                            ReminderScheduler.orderRequestCode(it.id),
                            it.scheduledMillis!!,
                            "Заказ: ${it.clientName}",
                            listOfNotNull(
                                it.address.ifBlank { null },
                                it.description.ifBlank { null }
                            ).joinToString(" — ")
                        )
                    }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
