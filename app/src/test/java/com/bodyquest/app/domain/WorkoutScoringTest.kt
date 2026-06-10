package com.bodyquest.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutScoringTest {

    @Test fun emptyWorkout_givesOnlyDiscipline() {
        val xp = WorkoutScoring.baseXp(emptyList(), isBoss = false)
        assertEquals(WorkoutScoring.DISCIPLINE_PER_WORKOUT, xp[AttributeType.DISCIPLINE])
        assertEquals(null, xp[AttributeType.STRENGTH])
    }

    @Test fun bossAddsDisciplineBonus() {
        val xp = WorkoutScoring.baseXp(emptyList(), isBoss = true)
        assertEquals(
            WorkoutScoring.DISCIPLINE_PER_WORKOUT + WorkoutScoring.BOSS_DISCIPLINE_BONUS,
            xp[AttributeType.DISCIPLINE],
        )
    }

    @Test fun weightedSet_addsToCorrectAttribute() {
        // kb_press -> STRENGTH. reps=6, weight=32: 14 + (6/3 + 32/8 + 0) = 14 + 6 = 20
        val xp = WorkoutScoring.baseXp(
            listOf(LoggedSet("kb_press", "Жим гири стоя", reps = 6, weightKg = 32.0, timeSeconds = 0)),
            isBoss = false,
        )
        assertEquals(20, xp[AttributeType.STRENGTH])
        assertEquals(WorkoutScoring.DISCIPLINE_PER_WORKOUT, xp[AttributeType.DISCIPLINE])
    }

    @Test fun incompleteSet_isIgnored() {
        val xp = WorkoutScoring.baseXp(
            listOf(LoggedSet("pushup", "Отжимания", reps = 0, weightKg = 0.0, timeSeconds = 0)),
            isBoss = false,
        )
        assertEquals(null, xp[AttributeType.STRENGTH])
    }

    @Test fun volumeBonus_isCapped() {
        // огромные значения не должны давать бесконечный XP за подход
        val xp = WorkoutScoring.baseXp(
            listOf(LoggedSet("kb_press", "Жим", reps = 999, weightKg = 999.0, timeSeconds = 999)),
            isBoss = false,
        )
        // 14 + cap(20) = 34
        assertEquals(34, xp[AttributeType.STRENGTH])
    }
}
