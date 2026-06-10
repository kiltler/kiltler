package com.bodyquest.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RetroFreezeTest {
    @Test fun missedOneDayWithTokenOffersThatDay() {
        val offer = RetroFreeze.offer(98, 5, 100, frozen = emptySet(), tokens = 1)
        assertEquals(listOf(99L), offer)
    }

    @Test fun noTokenNoOffer() {
        assertTrue(RetroFreeze.offer(98, 5, 100, emptySet(), tokens = 0).isEmpty())
    }

    @Test fun gapDeeperThanLimitNoOffer() {
        // пропущены 97,98,99 — это 3 дня > MAX_GAP_DAYS(2)
        assertTrue(RetroFreeze.offer(96, 5, 100, emptySet(), tokens = 5).isEmpty())
    }

    @Test fun alreadyFrozenDayNotOfferedAgain() {
        assertTrue(RetroFreeze.offer(98, 5, 100, frozen = setOf(99L), tokens = 2).isEmpty())
    }

    @Test fun noStreakOrNoGapNoOffer() {
        assertTrue(RetroFreeze.offer(98, 0, 100, emptySet(), 2).isEmpty())   // серия неактивна
        assertTrue(RetroFreeze.offer(99, 5, 100, emptySet(), 2).isEmpty())   // тренировался вчера
    }
}

class WeightTrendPlateauTest {
    private val today = 100L
    @Test fun flatTrendIsPlateau() {
        val w = listOf(80L to 90.1, 86L to 89.95, 92L to 90.0, 99L to 90.05)
        assertTrue(PlateauDetector.isWeightStall(w, today))
    }
    @Test fun noisyButDecliningIsNotPlateau() {
        val w = listOf(80L to 90.6, 86L to 90.1, 92L to 89.7, 99L to 89.0)
        assertFalse(PlateauDetector.isWeightStall(w, today))
    }
    @Test fun tooFewPointsStaysSilent() {
        val w = listOf(80L to 90.1, 92L to 90.0, 99L to 90.05) // 3 < minPoints
        assertFalse(PlateauDetector.isWeightStall(w, today))
    }
}

class ShortSessionXpTest {
    @Test fun shortSessionGivesLessThanFull() {
        val base = mapOf(AttributeType.STRENGTH to 20, AttributeType.DISCIPLINE to 30)
        val none = ModifierEngine.all.first { it.id == "none" }
        val full = XpPipeline.apply(base, none, 1f, sessionFactor = 1f).values.sum()
        val short = XpPipeline.apply(base, none, 1f, sessionFactor = XpPipeline.SHORT_SESSION_FACTOR).values.sum()
        assertTrue("короткая должна давать меньше: short=$short full=$full", short < full)
    }
}

class StreakDecoupledFromXpTest {
    @Test fun zeroXpDayStillKeepsStreak() {
        // день модификатора «кардио не считается», сессия только из кардио → итоговый XP = 0
        val base = mapOf(AttributeType.ENDURANCE to 40)
        val noCardio = ModifierEngine.all.first { it.id == "nocardio" }
        val xp = XpPipeline.apply(base, noCardio, 1.2f).values.sum()
        assertEquals(0, xp)
        // но серия зависит ТОЛЬКО от факта сессии (последняя была вчера) → растёт независимо от XP
        assertEquals(6, Streaks.nextStreak(lastWorkoutEpochDay = 99, currentStreak = 5, today = 100))
    }
}

class SchemaContractTest {
    @Test fun sqlDefaultMatchesEntityDefault() {
        assertEquals("0", SchemaContract.SETTINGS_DEFAULT)
        // SQL DEFAULT берётся из той же константы, что и @ColumnInfo(defaultValue) → совпадают
        assertTrue(SchemaContract.ALTER_FREEZE_TOKENS.contains("DEFAULT ${SchemaContract.SETTINGS_DEFAULT}"))
        assertTrue(SchemaContract.ALTER_TARGET_WAIST.contains("DEFAULT ${SchemaContract.SETTINGS_DEFAULT}"))
        assertTrue(SchemaContract.ALTER_TARGET_BELLY.contains("DEFAULT ${SchemaContract.SETTINGS_DEFAULT}"))
    }

    @Test fun columnTypesAndTableShape() {
        assertTrue(SchemaContract.ALTER_FREEZE_TOKENS.contains("`freezeTokens` INTEGER NOT NULL"))
        assertTrue(SchemaContract.ALTER_TARGET_WAIST.contains("`targetWaist` REAL NOT NULL"))
        assertTrue(SchemaContract.ALTER_TARGET_BELLY.contains("`targetBelly` REAL NOT NULL"))
        assertTrue(SchemaContract.CREATE_FROZEN_DAY.contains("`frozen_day`"))
        assertTrue(SchemaContract.CREATE_FROZEN_DAY.contains("PRIMARY KEY(`dateEpochDay`)"))
    }
}
