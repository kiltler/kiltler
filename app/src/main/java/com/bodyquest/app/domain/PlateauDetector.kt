package com.bodyquest.app.domain

import kotlin.math.abs

object PlateauDetector {

    /**
     * Плато по упражнению: рекорд (лучшее значение в сессии) не обновлялся ≥ [window] последних сессий.
     * [bestPerSession] — лучшие значения по сессиям в хронологическом порядке.
     */
    fun isLiftPlateau(bestPerSession: List<Double>, window: Int = 4): Boolean {
        if (bestPerSession.size < window) return false
        // индекс последнего улучшения (строго больше предыдущего максимума)
        var runningMax = bestPerSession.first()
        var lastImprovement = 0
        for (i in 1 until bestPerSession.size) {
            if (bestPerSession[i] > runningMax) {
                runningMax = bestPerSession[i]
                lastImprovement = i
            }
        }
        val sessionsSince = (bestPerSession.size - 1) - lastImprovement
        return sessionsSince >= window
    }

    /**
     * Застой веса тела: за последние [weeks] недель разброс ≤ [tolerance] кг.
     * [weights] — (epochDay, weightKg). Нужно ≥ 2 точек в окне.
     */
    fun isWeightStall(
        weights: List<Pair<Long, Double>>,
        today: Long,
        weeks: Int = 3,
        tolerance: Double = 0.5,
    ): Boolean {
        val cutoff = today - weeks * 7L
        val window = weights.filter { it.first >= cutoff }
        if (window.size < 2) return false
        val values = window.map { it.second }
        return abs(values.max() - values.min()) <= tolerance
    }
}
