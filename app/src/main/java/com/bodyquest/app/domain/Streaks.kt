package com.bodyquest.app.domain

/** Чистая логика серии дней — вынесена из репозитория для тестируемости. */
object Streaks {
    /**
     * Новое значение серии при завершении тренировки сегодня.
     * Пропущенные дни между прошлой тренировкой и сегодня НЕ рвут серию,
     * если они «заморожены» ([frozen]).
     * - нет прошлых тренировок → 1
     * - последняя была сегодня → серия не растёт (минимум 1)
     * - все пропущенные дни заморожены → +1
     * - иначе (реальный пропуск) → сброс на 1
     */
    fun nextStreak(
        lastWorkoutEpochDay: Long,
        currentStreak: Int,
        today: Long,
        frozen: Set<Long> = emptySet(),
    ): Int {
        if (lastWorkoutEpochDay < 0) return 1
        if (lastWorkoutEpochDay == today) return currentStreak.coerceAtLeast(1)
        var day = lastWorkoutEpochDay + 1
        var allFrozen = true
        while (day < today) {
            if (day !in frozen) { allFrozen = false; break }
            day++
        }
        return if (allFrozen) currentStreak + 1 else 1
    }
}

/** Правила «заморозок серии» — конечный расходник, который зарабатывается. */
object FreezeRules {
    const val MAX_TOKENS = 3

    /** Начисление: одна заморозка за каждые полные 7 дней серии, но не выше лимита. */
    fun grantAfter(currentTokens: Int, newStreak: Int): Int =
        if (newStreak > 0 && newStreak % 7 == 0) (currentTokens + 1).coerceAtMost(MAX_TOKENS)
        else currentTokens.coerceAtMost(MAX_TOKENS)
}
