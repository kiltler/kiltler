package com.kiltler.assistant

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import org.osmdroid.config.Configuration

class AssistantApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        configureOsmdroid()
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

    private fun configureOsmdroid() {
        val prefs = getSharedPreferences("osmdroid", MODE_PRIVATE)
        Configuration.getInstance().apply {
            load(this@AssistantApp, prefs)
            userAgentValue = packageName
            osmdroidBasePath = cacheDir
            osmdroidTileCache = cacheDir.resolve("osm_tiles")
        }
    }

    companion object {
        const val CHANNEL_REMINDERS = "reminders"
    }
}
