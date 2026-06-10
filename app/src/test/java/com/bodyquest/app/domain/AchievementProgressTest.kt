package com.bodyquest.app.domain

import com.bodyquest.app.domain.seed.AchievementCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AchievementProgressTest {

    private val stats = AchStats(
        workouts = 12, bossCount = 1, longestStreak = 9,
        waistDrop = 2.7, weightDrop = 1.4, pullupBest = 8,
        strengthXp = 900, enduranceXp = 500, mobilityXp = 300, level = 7,
    )

    @Test fun counts() {
        assertEquals(12 to 25, AchievementProgress.forId(AchievementCatalog.TWENTYFIVE_WORKOUTS, stats))
        assertEquals(9 to 14, AchievementProgress.forId(AchievementCatalog.STREAK_14, stats))
        assertEquals(8 to 10, AchievementProgress.forId(AchievementCatalog.PULLUP_10, stats))
        assertEquals(7 to 20, AchievementProgress.forId(AchievementCatalog.LEVEL_20, stats))
    }

    @Test fun dropsFlooredToCm() {
        assertEquals(2 to 5, AchievementProgress.forId(AchievementCatalog.WAIST_MINUS_5, stats))
        assertEquals(1 to 3, AchievementProgress.forId(AchievementCatalog.WEIGHT_MINUS_3, stats))
    }

    @Test fun unmeasurableIsNull() {
        assertNull(AchievementProgress.forId(AchievementCatalog.SLEEP_8, stats))
    }
}
