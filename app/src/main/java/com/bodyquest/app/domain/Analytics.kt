package com.bodyquest.app.domain

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/**
 * Чистые функции аналитики прогресса (epochDay-основанные) — легко тестируются без Android.
 */
object Analytics {

    /** Понедельник недели, в которую попадает [epochDay]. */
    fun startOfIsoWeek(epochDay: Long): Long =
        LocalDate.ofEpochDay(epochDay)
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .toEpochDay()

    /** Сколько тренировок попало в текущую ISO-неделю. */
    fun sessionsThisWeek(sessionEpochDays: List<Long>, today: Long): Int {
        val weekStart = startOfIsoWeek(today)
        return sessionEpochDays.count { startOfIsoWeek(it) == weekStart }
    }

    /** Полных недель прошло от [startEpochDay] до [today] (по границам недель). */
    fun weeksSince(startEpochDay: Long, today: Long): Int {
        val a = startOfIsoWeek(startEpochDay)
        val b = startOfIsoWeek(today)
        return (((b - a) / 7).toInt()).coerceAtLeast(0)
    }

    /** Неделя разгрузки — каждая 6-я (индексация с 0: недели 5, 11, 17 …). */
    fun isDeloadWeek(weeksSinceStart: Int): Boolean =
        weeksSinceStart >= 0 && weeksSinceStart % 6 == 5

    /**
     * Изменение метрики за последние [days] дней: (последнее значение) − (значение ~[days] назад).
     * Отрицательное = метрика уменьшилась. null — если данных меньше двух точек.
     * [points] — пары (epochDay, value), не обязательно отсортированы.
     */
    fun deltaOverDays(points: List<Pair<Long, Double>>, today: Long, days: Int): Double? {
        if (points.size < 2) return null
        val sorted = points.sortedBy { it.first }
        val latest = sorted.last().second
        val cutoff = today - days
        // ближайшая точка не позже cutoff, иначе — самая ранняя
        val baseline = sorted.lastOrNull { it.first <= cutoff } ?: sorted.first()
        return latest - baseline.second
    }
}
