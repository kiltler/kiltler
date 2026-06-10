package com.bodyquest.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelingTest {

    @Test fun xpToNext_formula() {
        assertEquals(100, Leveling.xpToNext(1))          // round(100 * 1^1.5)
        assertEquals(283, Leveling.xpToNext(2))          // round(100 * 2.828)
        assertEquals(800, Leveling.xpToNext(4))          // round(100 * 8)
    }

    @Test fun xpToNext_clampsBelowOne() {
        assertEquals(100, Leveling.xpToNext(0))
        assertEquals(100, Leveling.xpToNext(-5))
    }

    @Test fun progress_zeroXp_isLevelOne() {
        val p = Leveling.progressFor(0)
        assertEquals(1, p.level)
        assertEquals(0, p.xpIntoLevel)
        assertEquals(100, p.xpForNext)
        assertEquals(0f, p.fraction, 0.0001f)
    }

    @Test fun progress_negativeXp_isSafe() {
        assertEquals(1, Leveling.levelFor(-9999))
    }

    @Test fun progress_exactlyFillsLevel() {
        val p = Leveling.progressFor(100)   // ровно закрывает 1-й уровень
        assertEquals(2, p.level)
        assertEquals(0, p.xpIntoLevel)
    }

    @Test fun progress_partwayThroughLevelTwo() {
        val p = Leveling.progressFor(150)   // 100 на ур.1, 50 внутри ур.2
        assertEquals(2, p.level)
        assertEquals(50, p.xpIntoLevel)
        assertEquals(283, p.xpForNext)
    }

    @Test fun streakMultiplier_capsAtFiftyPercent() {
        assertEquals(1.0f, Leveling.streakMultiplier(0), 0.0001f)
        assertEquals(1.15f, Leveling.streakMultiplier(3), 0.0001f)
        assertEquals(1.5f, Leveling.streakMultiplier(10), 0.0001f)
        assertEquals(1.5f, Leveling.streakMultiplier(50), 0.0001f) // не выше +50%
        assertTrue(Leveling.streakMultiplier(-3) >= 1.0f)
    }
}
