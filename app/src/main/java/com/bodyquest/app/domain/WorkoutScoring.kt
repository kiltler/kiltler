package com.bodyquest.app.domain

import com.bodyquest.app.domain.seed.ExerciseCatalog

/** Один зафиксированный подход во время тренировки. */
data class LoggedSet(
    val exerciseId: String,
    val exerciseName: String,
    val reps: Int,
    val weightKg: Double,
    val timeSeconds: Int,
) {
    val isCompleted: Boolean get() = reps > 0 || timeSeconds > 0
}

/** Расчёт XP за тренировку (до применения множителя серии). */
object WorkoutScoring {

    const val XP_PER_SET = 14
    const val DISCIPLINE_PER_WORKOUT = 30
    const val BOSS_DISCIPLINE_BONUS = 60

    /**
     * Базовый XP по характеристикам. Каждый завершённый подход даёт XP той характеристике,
     * к которой относится упражнение, плюс небольшой бонус за объём (повторы/вес/время).
     */
    fun baseXp(sets: List<LoggedSet>, isBoss: Boolean): Map<AttributeType, Int> {
        val result = linkedMapOf<AttributeType, Int>()
        for (set in sets) {
            if (!set.isCompleted) continue
            val ex = ExerciseCatalog.get(set.exerciseId) ?: continue
            val volumeBonus = set.reps / 3 + (set.weightKg / 8).toInt() + set.timeSeconds / 30
            val gained = XP_PER_SET + volumeBonus.coerceAtMost(20)
            result[ex.attribute] = (result[ex.attribute] ?: 0) + gained
        }
        // Дисциплина за сам факт выполнения квеста.
        val discipline = DISCIPLINE_PER_WORKOUT + if (isBoss) BOSS_DISCIPLINE_BONUS else 0
        result[AttributeType.DISCIPLINE] = (result[AttributeType.DISCIPLINE] ?: 0) + discipline
        return result
    }
}
