package com.bodyquest.app.domain

import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

/** Пять направлений прокачки персонажа. */
enum class AttributeType(val title: String, val emoji: String, val colorArgb: Long) {
    STRENGTH("Сила", "💪", 0xFFFF6B6B),
    ENDURANCE("Выносливость", "🫁", 0xFF4DA3FF),
    MOBILITY("Мобильность", "🤸", 0xFF22D3A6),
    DISCIPLINE("Дисциплина", "🔥", 0xFFFFB454),
    COMPOSITION("Композиция", "🛡️", 0xFFB57BFF);
}

/** Ранги по общему уровню персонажа. */
enum class Rank(val title: String, val minLevel: Int) {
    RECRUIT("Новобранец", 1),
    FIGHTER("Боец", 5),
    ATHLETE("Атлет", 10),
    WARRIOR("Воин", 20),
    CHAMPION("Чемпион", 35),
    LEGEND("Легенда", 50);

    companion object {
        fun forLevel(level: Int): Rank =
            entries.lastOrNull { level >= it.minLevel } ?: RECRUIT
    }
}

/** Прогресс по одному уровню: текущий уровень, накопленный XP внутри уровня и порог до следующего. */
data class LevelProgress(
    val level: Int,
    val totalXp: Int,
    val xpIntoLevel: Int,
    val xpForNext: Int,
) {
    val fraction: Float get() = if (xpForNext <= 0) 0f else (xpIntoLevel.toFloat() / xpForNext)
}

/**
 * Ядро RPG-прогрессии.
 * XP до следующего уровня: round(100 * level^1.5).
 */
object Leveling {

    /** Сколько XP нужно, чтобы перейти с [level] на [level] + 1. */
    fun xpToNext(level: Int): Int {
        val l = level.coerceAtLeast(1)
        return (100.0 * l.toDouble().pow(1.5)).roundToInt()
    }

    /** Разбор накопленного XP в уровень и прогресс внутри него. */
    fun progressFor(totalXp: Int): LevelProgress {
        var level = 1
        var remaining = totalXp.coerceAtLeast(0)
        while (true) {
            val need = xpToNext(level)
            if (remaining < need) {
                return LevelProgress(level, totalXp, remaining, need)
            }
            remaining -= need
            level++
            if (level > 999) return LevelProgress(level, totalXp, 0, xpToNext(level))
        }
    }

    fun levelFor(totalXp: Int): Int = progressFor(totalXp).level

    /**
     * Множитель XP за серию дней: +5% за каждый день серии, максимум +50%.
     */
    fun streakMultiplier(streakDays: Int): Float =
        1f + min(0.5f, 0.05f * streakDays.coerceAtLeast(0))
}
