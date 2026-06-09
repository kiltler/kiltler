package com.bodyquest.app.domain.seed

/** Определение достижения (справочник в коде). Состояние «открыто» хранится в Room. */
data class AchievementDef(
    val id: String,
    val name: String,
    val description: String,
    val emoji: String,
)

object AchievementCatalog {
    const val FIRST_WORKOUT = "first_workout"
    const val TEN_WORKOUTS = "ten_workouts"
    const val FIFTY_WORKOUTS = "fifty_workouts"
    const val STREAK_7 = "streak_7"
    const val STREAK_30 = "streak_30"
    const val WAIST_MINUS_1 = "waist_minus_1"
    const val WAIST_MINUS_5 = "waist_minus_5"
    const val PULLUP_10 = "pullup_10"
    const val BOSS_SLAIN = "boss_slain"
    const val LEVEL_10 = "level_10"
    const val WEIGHT_MINUS_3 = "weight_minus_3"
    const val SHOULDERS_PRIORITY = "shoulders_5k"

    val all = listOf(
        AchievementDef(FIRST_WORKOUT, "Первая тренировка", "Завершите первую тренировку", "🎯"),
        AchievementDef(TEN_WORKOUTS, "Десятка", "10 завершённых тренировок", "🔟"),
        AchievementDef(FIFTY_WORKOUTS, "Машина", "50 завершённых тренировок", "⚙️"),
        AchievementDef(STREAK_7, "Серия 7 дней", "Тренируйтесь 7 дней серии", "🔥"),
        AchievementDef(STREAK_30, "Несгибаемый", "Серия 30 дней", "🌋"),
        AchievementDef(WAIST_MINUS_1, "Минус 1 см на талии", "Талия уменьшилась на 1 см", "📉"),
        AchievementDef(WAIST_MINUS_5, "Узкая талия", "Талия уменьшилась на 5 см", "🗡️"),
        AchievementDef(WEIGHT_MINUS_3, "Легче на 3 кг", "Вес снизился на 3 кг", "⚖️"),
        AchievementDef(PULLUP_10, "Первое подтягивание ×10", "10 подтягиваний в подходе", "🦾"),
        AchievementDef(SHOULDERS_PRIORITY, "Кузнец плеч", "Накопите 1500 XP Силы", "🛠️"),
        AchievementDef(BOSS_SLAIN, "Босс повержен", "Завершите Босса недели", "👑"),
        AchievementDef(LEVEL_10, "Атлет", "Достигните 10 уровня", "🏅"),
    )

    val byId = all.associateBy { it.id }
}
