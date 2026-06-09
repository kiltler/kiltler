package com.bodyquest.app.domain

import com.bodyquest.app.domain.seed.AchievementDef
import java.time.LocalDate

/** Результат завершённой тренировки — для анимаций XP и окна Level Up. */
data class WorkoutOutcome(
    val xpByAttr: Map<AttributeType, Int>,
    val totalXp: Int,
    val oldLevel: Int,
    val newLevel: Int,
    val newStreak: Int,
    val streakMultiplier: Float,
    val unlocked: List<AchievementDef>,
    val progressed: List<String>,
) {
    val leveledUp: Boolean get() = newLevel > oldLevel
}

/** Результат записи замеров тела. */
data class MeasurementOutcome(
    val compositionXp: Int,
    val oldLevel: Int,
    val newLevel: Int,
    val unlocked: List<AchievementDef>,
) {
    val leveledUp: Boolean get() = newLevel > oldLevel
}

object Dates {
    fun todayEpochDay(): Long = LocalDate.now().toEpochDay()
}
