package com.bodyquest.app.domain.seed

/** Тип ежедневного испытания — по нему определяется, выполнено ли оно (из данных дня). */
enum class ChallengeKind { WORKOUT_TODAY, WATER_GOAL, LOG_MEASUREMENT, SLEEP_8H, BEAT_BOSS }

data class DailyChallenge(val title: String, val emoji: String, val kind: ChallengeKind)

/** Ежедневное испытание — ротация по дню. Выполнение засчитывается автоматически. */
object ChallengeCatalog {
    val all = listOf(
        DailyChallenge("Заверши тренировку дня", "⚔️", ChallengeKind.WORKOUT_TODAY),
        DailyChallenge("Выпей дневную норму воды", "💧", ChallengeKind.WATER_GOAL),
        DailyChallenge("Запиши свежие замеры", "📏", ChallengeKind.LOG_MEASUREMENT),
        DailyChallenge("Поспи 8+ часов", "😴", ChallengeKind.SLEEP_8H),
        DailyChallenge("Повергни Босса недели", "👑", ChallengeKind.BEAT_BOSS),
    )

    fun forDay(epochDay: Long): DailyChallenge {
        val i = ((epochDay % all.size) + all.size) % all.size
        return all[i.toInt()]
    }
}
