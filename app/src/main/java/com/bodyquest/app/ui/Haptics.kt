package com.bodyquest.app.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/** Тактильная отдача. Уважает системные настройки: если вибро нет/выключено — тихо ничего. */
object Haptics {

    private fun vibrator(context: Context): Vibrator? {
        val v = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        return v?.takeIf { it.hasVibrator() }
    }

    /** Короткий тик — завершение подхода. */
    fun setComplete(context: Context) = oneShot(context, 25)

    /** Заметный двойной импульс — новый рекорд. */
    fun pr(context: Context) = pattern(context, longArrayOf(0, 40, 80, 80))

    /** Сочный паттерн — повышение уровня. */
    fun levelUp(context: Context) = pattern(context, longArrayOf(0, 60, 60, 60, 60, 140))

    private fun oneShot(context: Context, ms: Long) = runCatching {
        vibrator(context)?.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun pattern(context: Context, timings: LongArray) = runCatching {
        vibrator(context)?.vibrate(VibrationEffect.createWaveform(timings, -1))
    }
}
