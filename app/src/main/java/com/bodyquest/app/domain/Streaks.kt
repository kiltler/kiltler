package com.bodyquest.app.domain

/** Чистая логика серии дней — вынесена из репозитория для тестируемости. */
object Streaks {
    /**
     * Новое значение серии при завершении тренировки сегодня.
     * - нет прошлых тренировок → 1
     * - последняя была сегодня → серия не растёт (минимум 1)
     * - последняя была вчера → +1
     * - иначе (пропуск) → сброс на 1
     */
    fun nextStreak(lastWorkoutEpochDay: Long, currentStreak: Int, today: Long): Int = when {
        lastWorkoutEpochDay < 0 -> 1
        lastWorkoutEpochDay == today -> currentStreak.coerceAtLeast(1)
        lastWorkoutEpochDay == today - 1 -> currentStreak + 1
        else -> 1
    }
}
