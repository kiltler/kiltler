package com.bodyquest.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class StreaksTest {
    @Test fun firstEver() {
        assertEquals(1, Streaks.nextStreak(lastWorkoutEpochDay = -1, currentStreak = 0, today = 100))
    }

    @Test fun consecutiveDay_increments() {
        assertEquals(4, Streaks.nextStreak(lastWorkoutEpochDay = 99, currentStreak = 3, today = 100))
    }

    @Test fun sameDay_doesNotGrow() {
        assertEquals(3, Streaks.nextStreak(lastWorkoutEpochDay = 100, currentStreak = 3, today = 100))
        assertEquals(1, Streaks.nextStreak(lastWorkoutEpochDay = 100, currentStreak = 0, today = 100))
    }

    @Test fun gap_resets() {
        assertEquals(1, Streaks.nextStreak(lastWorkoutEpochDay = 97, currentStreak = 10, today = 100))
    }
}
