package com.bodyquest.app.domain

import com.bodyquest.app.domain.seed.AchievementCatalog
import kotlin.math.floor

/** Снимок статистики для расчёта прогресса к достижениям. */
data class AchStats(
    val workouts: Int,
    val bossCount: Int,
    val longestStreak: Int,
    val waistDrop: Double,
    val weightDrop: Double,
    val pullupBest: Int,
    val strengthXp: Int,
    val enduranceXp: Int,
    val mobilityXp: Int,
    val level: Int,
)

object AchievementProgress {
    /** (текущее, цель) для измеримых достижений, либо null. */
    fun forId(id: String, s: AchStats): Pair<Int, Int>? {
        fun drop(v: Double) = floor(v.coerceAtLeast(0.0)).toInt()
        return when (id) {
            AchievementCatalog.FIRST_WORKOUT -> s.workouts to 1
            AchievementCatalog.TEN_WORKOUTS -> s.workouts to 10
            AchievementCatalog.TWENTYFIVE_WORKOUTS -> s.workouts to 25
            AchievementCatalog.FIFTY_WORKOUTS -> s.workouts to 50
            AchievementCatalog.STREAK_7 -> s.longestStreak to 7
            AchievementCatalog.STREAK_14 -> s.longestStreak to 14
            AchievementCatalog.STREAK_30 -> s.longestStreak to 30
            AchievementCatalog.WAIST_MINUS_1 -> drop(s.waistDrop) to 1
            AchievementCatalog.WAIST_MINUS_5 -> drop(s.waistDrop) to 5
            AchievementCatalog.WAIST_MINUS_10 -> drop(s.waistDrop) to 10
            AchievementCatalog.WEIGHT_MINUS_3 -> drop(s.weightDrop) to 3
            AchievementCatalog.PULLUP_10 -> s.pullupBest to 10
            AchievementCatalog.PULLUP_15 -> s.pullupBest to 15
            AchievementCatalog.SHOULDERS_PRIORITY -> s.strengthXp to 1500
            AchievementCatalog.ENDURANCE_2000 -> s.enduranceXp to 2000
            AchievementCatalog.MOBILITY_1000 -> s.mobilityXp to 1000
            AchievementCatalog.BOSS_SLAIN -> s.bossCount to 1
            AchievementCatalog.BOSS_3 -> s.bossCount to 3
            AchievementCatalog.LEVEL_10 -> s.level to 10
            AchievementCatalog.LEVEL_20 -> s.level to 20
            AchievementCatalog.LEVEL_35 -> s.level to 35
            AchievementCatalog.LEVEL_50 -> s.level to 50
            else -> null // SLEEP_8 и пр. — без шкалы
        }
    }
}
