package com.bodyquest.app.domain

import kotlin.math.abs
import kotlin.math.sqrt

/** Результат прогноза по линейной регрессии. */
data class Forecast(
    val slopePerDay: Double,   // изменение метрики за день
    val direction: Int,        // -1 вниз, 0 без тренда, +1 вверх
    val ratePerWeek: Double,   // slopePerDay * 7
    val band: Double,          // полуширина полосы доверия (станд. ошибка остатков)
    val lastValue: Double,
    val lastDay: Long,
) {
    val hasTrend: Boolean get() = direction != 0
}

/**
 * Линейная регрессия (МНК) по истории замеров. Чистый Kotlin, без матбиблиотек.
 * Точки — пары (epochDay, value).
 */
object ForecastEngine {

    /** Порог «плоского» тренда по умолчанию: < ~0.03 единицы в неделю считаем удержанием. */
    fun fit(points: List<Pair<Long, Double>>, flatPerWeek: Double = 0.03): Forecast? {
        if (points.size < 2) return null
        val pts = points.sortedBy { it.first }
        val x0 = pts.first().first
        val xs = pts.map { (it.first - x0).toDouble() }
        val ys = pts.map { it.second }
        val n = xs.size
        val mx = xs.average()
        val my = ys.average()
        var sxx = 0.0; var sxy = 0.0
        for (i in 0 until n) {
            sxx += (xs[i] - mx) * (xs[i] - mx)
            sxy += (xs[i] - mx) * (ys[i] - my)
        }
        if (sxx == 0.0) return null
        val slope = sxy / sxx
        val intercept = my - slope * mx
        // станд. ошибка остатков
        var ssRes = 0.0
        for (i in 0 until n) {
            val pred = intercept + slope * xs[i]
            ssRes += (ys[i] - pred) * (ys[i] - pred)
        }
        val band = if (n > 2) sqrt(ssRes / (n - 2)) else abs(ys.last() - ys.first()) / 2.0
        val ratePerWeek = slope * 7.0
        val direction = when {
            abs(ratePerWeek) < flatPerWeek -> 0
            ratePerWeek < 0 -> -1
            else -> 1
        }
        return Forecast(slope, direction, ratePerWeek, band, pts.last().second, pts.last().first)
    }

    /** Через сколько дней метрика достигнет [target] при текущем темпе; null — если не приближается. */
    fun daysToReach(forecast: Forecast, target: Double): Int? {
        if (forecast.direction == 0) return null
        val needed = target - forecast.lastValue
        if (needed == 0.0) return 0
        // движемся ли в сторону цели
        if (needed > 0 && forecast.slopePerDay <= 0) return null
        if (needed < 0 && forecast.slopePerDay >= 0) return null
        val days = needed / forecast.slopePerDay
        if (days <= 0 || days.isInfinite() || days.isNaN()) return null
        return days.toInt()
    }
}
