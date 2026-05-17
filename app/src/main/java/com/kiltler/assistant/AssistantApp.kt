package com.kiltler.assistant

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.yandex.mapkit.MapKitFactory

class AssistantApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        MapKitFactory.setApiKey(MAPKIT_API_KEY)
    }

    private fun createNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_REMINDERS,
            "Напоминания",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Напоминания о заказах и работах"
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_REMINDERS = "reminders"
        private const val MAPKIT_API_KEY = "9ea78a5d-ed0b-4575-8256-4095f5c8243f"
    }
}
