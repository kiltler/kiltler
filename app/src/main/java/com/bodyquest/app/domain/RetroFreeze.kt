package com.bodyquest.app.domain

/**
 * Ретроспективная заморозка: при заходе в приложение после пропуска предлагаем закрыть
 * пропущенные дни токенами, чтобы серия дожила до сегодня.
 *
 * Правило глубины: восстановление доступно, пока число НЕзамороженных пропущенных дней
 * не больше [MAX_GAP_DAYS] и не больше, чем есть токенов. Иначе — поздно/некем закрывать.
 */
object RetroFreeze {
    const val MAX_GAP_DAYS = 2

    /**
     * Список дней, которые нужно заморозить, чтобы серия не порвалась к [today].
     * Пусто/нет предложения, если: серия не активна, пропуска нет (тренировался сегодня/вчера),
     * пропуск глубже лимита, или токенов не хватает (нужно закрыть ВСЕ пропущенные дни).
     */
    fun offer(
        lastWorkoutEpochDay: Long,
        currentStreak: Int,
        today: Long,
        frozen: Set<Long>,
        tokens: Int,
    ): List<Long> {
        if (lastWorkoutEpochDay < 0 || currentStreak <= 0) return emptyList()
        if (today - lastWorkoutEpochDay <= 1) return emptyList() // нет пропущенного дня
        val gap = ((lastWorkoutEpochDay + 1) until today)
        val missing = gap.filter { it !in frozen }
        if (missing.isEmpty()) return emptyList()       // всё уже заморожено
        if (missing.size > MAX_GAP_DAYS) return emptyList() // слишком глубоко
        if (missing.size > tokens) return emptyList()   // токенов не хватает закрыть весь разрыв
        return missing
    }
}
