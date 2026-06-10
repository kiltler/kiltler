package com.bodyquest.app.domain.seed

import com.bodyquest.app.domain.PlannedExercise
import com.bodyquest.app.domain.WorkoutDay

/**
 * Эквивалентные замены упражнений (реролл) и генератор короткой тренировки.
 * Всё на базе существующего справочника, инвентарь домашний (всегда доступен).
 */
object ExerciseEquivalence {

    /**
     * Равноценные замены: та же характеристика, тот же тип логирования (повторы/вес/время)
     * и пересечение по мышцам. Так XP не страдает при замене.
     */
    fun alternatives(exerciseId: String): List<String> {
        val ex = ExerciseCatalog.get(exerciseId) ?: return emptyList()
        return ExerciseCatalog.all
            .filter {
                it.id != ex.id &&
                    it.attribute == ex.attribute &&
                    it.type == ex.type &&
                    it.muscles.any { m -> m in ex.muscles }
            }
            .map { it.id }
    }
}

/** Короткая тренировка «мало времени»: спасает серию за ~10 минут. */
object QuickWorkout {
    const val ID = "quick_mode"

    fun generate(): WorkoutDay = WorkoutDay(
        id = ID,
        title = "Мало времени (≈10 мин)",
        focus = "Короткая сессия — серия сохраняется, XP меньше обычного.",
        emphasis = emptyList(),
        warmup = listOf("1–2 минуты суставной разминки"),
        exercises = listOf(
            PlannedExercise("pullup", 2, "макс.", 45),
            PlannedExercise("pushup", 2, "макс.", 45),
            PlannedExercise("kb_swing", 2, "20", 45),
            PlannedExercise("plank", 1, "45 сек", 0),
        ),
    )
}
