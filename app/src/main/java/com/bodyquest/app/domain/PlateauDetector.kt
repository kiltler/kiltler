package com.bodyquest.app.domain

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
     * Застой веса тела: оценивается по НАКЛОНУ тренда (МНК), а не по разнице двух точек —
     * так шум от воды/еды не даёт ложных срабатываний.
     * Плато = за последние [weeks] недель достаточно точек ([minPoints]) и наклон близок к нулю
     * (|кг/нед| < [flatPerWeek]). Мало точек → не флагуем (молчим, а не тревожим зря).
     */
    fun isWeightStall(
        weights: List<Pair<Long, Double>>,
        today: Long,
        weeks: Int = 3,
        flatPerWeek: Double = 0.07,
        minPoints: Int = 4,
    ): Boolean {
        val cutoff = today - weeks * 7L
        val window = weights.filter { it.first >= cutoff }
        if (window.size < minPoints) return false
        val forecast = ForecastEngine.fit(window, flatPerWeek) ?: return false
        return forecast.direction == 0 // близкий к нулю наклон при достаточном числе точек
    }
}
