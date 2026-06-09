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
    const val TWENTYFIVE_WORKOUTS = "twentyfive_workouts"
    const val STREAK_14 = "streak_14"
    const val LEVEL_20 = "level_20"
    const val LEVEL_35 = "level_35"
    const val LEVEL_50 = "level_50"
    const val PULLUP_15 = "pullup_15"
    const val BOSS_3 = "boss_3"
    const val SLEEP_8 = "sleep_8"
    const val ENDURANCE_2000 = "endurance_2000"
    const val MOBILITY_1000 = "mobility_1000"
    const val WAIST_MINUS_10 = "waist_minus_10"

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
        AchievementDef(BOSS_3, "Охотник на боссов", "Повергните 3 Босса", "⚔️"),
        AchievementDef(LEVEL_10, "Атлет", "Достигните 10 уровня", "🏅"),
        AchievementDef(LEVEL_20, "Воин", "Достигните 20 уровня", "🛡️"),
        AchievementDef(LEVEL_35, "Чемпион", "Достигните 35 уровня", "🏆"),
        AchievementDef(LEVEL_50, "Легенда", "Достигните 50 уровня", "🐉"),
        AchievementDef(TWENTYFIVE_WORKOUTS, "Ветеран", "25 завершённых тренировок", "🎖️"),
        AchievementDef(STREAK_14, "Две недели огня", "Серия 14 дней", "☄️"),
        AchievementDef(PULLUP_15, "Король турника", "15 подтягиваний в подходе", "👑"),
        AchievementDef(ENDURANCE_2000, "Двигатель", "Накопите 2000 XP Выносливости", "🫁"),
        AchievementDef(MOBILITY_1000, "Гибкий как лоза", "Накопите 1000 XP Мобильности", "🤸"),
        AchievementDef(SLEEP_8, "Сон чемпиона", "Запишите ночь с 8+ часами сна", "😴"),
        AchievementDef(WAIST_MINUS_10, "Осиная талия", "Талия уменьшилась на 10 см", "🏵️"),
    )

    val byId = all.associateBy { it.id }
}
