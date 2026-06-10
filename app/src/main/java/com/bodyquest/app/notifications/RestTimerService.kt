package com.bodyquest.app.notifications

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.bodyquest.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Таймер отдыха как foreground-service: продолжает отсчёт, даже если приложение свёрнуто,
 * показывает уведомление с обратным отсчётом и вибрирует по окончании.
 */
class RestTimerService : Service() {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var job: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopEverything()
            return START_NOT_STICKY
        }
        val seconds = intent?.getIntExtra(EXTRA_SECONDS, 0) ?: 0
        if (seconds <= 0) {
            stopEverything()
            return START_NOT_STICKY
        }

        ReminderScheduler(this).ensureChannel()
        startForegroundCompat(buildNotification(seconds, ongoing = true))
        RestTimerController.set(seconds)

        job?.cancel()
        val endAt = SystemClock.elapsedRealtime() + seconds * 1000L
        job = scope.launch {
            while (true) {
                val left = endAt - SystemClock.elapsedRealtime()
                val sec = ((left + 999L) / 1000L).toInt()
                if (sec <= 0) break
                RestTimerController.set(sec)
                notify(buildNotification(sec, ongoing = true))
                delay(250)
            }
            RestTimerController.set(0)
            vibrate()
            notify(buildNotification(0, ongoing = false))
            stopForegroundCompat()
            stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        job?.cancel()
        RestTimerController.set(0)
        super.onDestroy()
    }

    private fun stopEverything() {
        job?.cancel()
        RestTimerController.set(0)
        stopForegroundCompat()
        stopSelf()
    }

    private fun buildNotification(seconds: Int, ongoing: Boolean): android.app.Notification {
        val (title, text) = if (seconds > 0) {
            "⏱️ Отдых" to "Осталось $seconds сек"
        } else {
            "Готово!" to "Отдых окончен — следующий подход"
        }
        return NotificationCompat.Builder(this, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setOngoing(ongoing)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun notify(n: android.app.Notification) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        nm.notify(NOTIF_ID, n)
    }

    private fun startForegroundCompat(n: android.app.Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIF_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE)
        } else {
            startForeground(NOTIF_ID, n)
        }
    }

    private fun stopForegroundCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }

    private fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        runCatching {
            vibrator?.vibrate(VibrationEffect.createOneShot(450, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    companion object {
        private const val NOTIF_ID = 7
        const val EXTRA_SECONDS = "seconds"
        const val ACTION_STOP = "com.bodyquest.app.REST_STOP"

        fun start(context: Context, seconds: Int) {
            if (seconds <= 0) return
            val intent = Intent(context, RestTimerService::class.java).putExtra(EXTRA_SECONDS, seconds)
            androidx.core.content.ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, RestTimerService::class.java).setAction(ACTION_STOP)
            context.startService(intent)
        }
    }
}
