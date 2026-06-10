package com.bodyquest.app.domain

import com.bodyquest.app.domain.seed.ExerciseEquivalence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AvatarMapperTest {
    @Test fun baselineGivesNeutral() {
        val s = AvatarMapper.shape(49.0, 97.0, 107.0, 97.0, 49.0, 97.0, 107.0, 97.0)
        assertEquals(0.5f, s.shoulder, 0.02f)
        assertEquals(0.5f, s.waist, 0.02f)
        assertEquals(0.5f, s.mass, 0.02f)
        assertEquals(0.5f, s.leanness, 0.02f)
    }

    @Test fun narrowerWaistRaisesWaistAndShoulder() {
        val s = AvatarMapper.shape(49.0, 97.0, 107.0, 97.0, 49.0, 87.0, 100.0, 93.0)
        assertTrue("узость талии растёт", s.waist > 0.5f)
        assertTrue("V-ratio растёт → плечи шире", s.shoulder > 0.5f)
        assertTrue("сухость растёт", s.leanness > 0.5f)
    }

    @Test fun extremesAreClamped() {
        val s = AvatarMapper.shape(49.0, 97.0, 107.0, 97.0, 90.0, 40.0, 40.0, 200.0)
        assertTrue(s.shoulder in 0f..1f)
        assertTrue(s.waist in 0f..1f)
        assertTrue(s.mass in 0f..1f)
        assertTrue(s.leanness in 0f..1f)
        assertEquals(1f, s.waist, 0.0001f)
    }
}

class ForecastEngineTest {
    private val downhill = listOf(0L to 100.0, 7L to 99.0, 14L to 98.0, 21L to 97.0)

    @Test fun knownSlope() {
        val f = ForecastEngine.fit(downhill)!!
        assertEquals(-1.0 / 7.0, f.slopePerDay, 1e-6)
        assertEquals(-1.0, f.ratePerWeek, 1e-6)
        assertEquals(-1, f.direction)
        assertEquals(0.0, f.band, 1e-6) // идеально линейно
    }

    @Test fun daysToReachTarget() {
        val f = ForecastEngine.fit(downhill)!!
        assertEquals(14, ForecastEngine.daysToReach(f, 95.0)) // с 97 до 95 при −1/нед = 14 дней
        assertNull("нельзя достичь, двигаемся в др. сторону", ForecastEngine.daysToReach(f, 99.0))
    }

    @Test fun flatHasNoTrend() {
        val f = ForecastEngine.fit(listOf(0L to 90.0, 7L to 90.0, 14L to 90.0))!!
        assertEquals(0, f.direction)
        assertFalse(f.hasTrend)
    }

    @Test fun tooFewPointsIsNull() {
        assertNull(ForecastEngine.fit(listOf(0L to 90.0)))
    }

    @Test fun scatterWidensBand() {
        val tight = ForecastEngine.fit(listOf(0L to 100.0, 7L to 99.0, 14L to 98.0))!!
        val noisy = ForecastEngine.fit(listOf(0L to 100.0, 7L to 95.0, 14L to 99.0))!!
        assertTrue(noisy.band > tight.band)
    }
}

class PlateauDetectorTest {
    @Test fun growingNoPlateau() {
        assertFalse(PlateauDetector.isLiftPlateau(listOf(10.0, 20.0, 30.0, 40.0, 50.0)))
    }

    @Test fun stalledRecordFlags() {
        assertTrue(PlateauDetector.isLiftPlateau(listOf(10.0, 20.0, 30.0, 30.0, 30.0, 30.0, 30.0)))
    }

    @Test fun bodyweightStall() {
        val w = listOf(80L to 90.0, 88L to 90.3, 99L to 90.1)
        assertTrue(PlateauDetector.isWeightStall(w, today = 100, weeks = 3, tolerance = 0.5))
    }
}

class WeakLinkAnalyzerTest {
    @Test fun returnsClearMinimum() {
        val xp = mapOf(
            AttributeType.STRENGTH to 500, AttributeType.ENDURANCE to 400,
            AttributeType.MOBILITY to 50, AttributeType.DISCIPLINE to 300,
            AttributeType.COMPOSITION to 200,
        )
        assertEquals(AttributeType.MOBILITY, WeakLinkAnalyzer.analyze(xp, 100.0, 100.0).weakest)
    }

    @Test fun pushHeavyWarns() {
        val xp = AttributeType.entries.associateWith { 100 }
        assertTrue(WeakLinkAnalyzer.analyze(xp, pushVolume = 100.0, pullVolume = 30.0).pushPullWarning)
    }

    @Test fun balancedNoWarning() {
        val xp = AttributeType.entries.associateWith { 100 }
        assertFalse(WeakLinkAnalyzer.analyze(xp, pushVolume = 100.0, pullVolume = 90.0).pushPullWarning)
    }
}

class ModifierAndPipelineTest {
    @Test fun deterministicByDate() {
        assertEquals(ModifierEngine.forDay(100), ModifierEngine.forDay(100))
        assertEquals(ModifierEngine.forDay(-3), ModifierEngine.forDay(-3))
    }

    @Test fun rangeCoversAll() {
        val seen = (0L until ModifierEngine.all.size).map { ModifierEngine.forDay(it).id }.toSet()
        assertEquals(ModifierEngine.all.size, seen.size)
    }

    @Test fun pipelineOrderBaseModStreak() {
        val base = mapOf(AttributeType.STRENGTH to 10, AttributeType.ENDURANCE to 10)
        val mod = ModifierEngine.all.first { it.id == "strength" } // ×2 к Силе
        val out = XpPipeline.apply(base, mod, streakMultiplier = 1.5f)
        assertEquals(30, out[AttributeType.STRENGTH]) // 10×2×1.5
        assertEquals(15, out[AttributeType.ENDURANCE]) // 10×1×1.5
    }

    @Test fun noCardioZeroesEndurance() {
        val base = mapOf(AttributeType.ENDURANCE to 40, AttributeType.STRENGTH to 10)
        val mod = ModifierEngine.all.first { it.id == "nocardio" }
        val out = XpPipeline.apply(base, mod, streakMultiplier = 1.2f)
        assertEquals(0, out[AttributeType.ENDURANCE])
        assertEquals(12, out[AttributeType.STRENGTH])
    }
}

class StreakFreezeTest {
    @Test fun frozenGapKeepsStreak() {
        assertEquals(6, Streaks.nextStreak(97, 5, 100, frozen = setOf(98L, 99L)))
    }

    @Test fun unfrozenGapResets() {
        assertEquals(1, Streaks.nextStreak(97, 5, 100, frozen = setOf(98L)))
    }

    @Test fun noFrozenStillWorksConsecutive() {
        assertEquals(6, Streaks.nextStreak(99, 5, 100))
    }

    @Test fun tokenGrantAndCap() {
        assertEquals(1, FreezeRules.grantAfter(0, 7))
        assertEquals(3, FreezeRules.grantAfter(3, 7))   // лимит
        assertEquals(2, FreezeRules.grantAfter(2, 8))   // не кратно 7 — без начисления
    }
}

class ExerciseEquivalenceTest {
    @Test fun alternativesShareAttributeTypeAndMuscleExcludingSelf() {
        val alts = ExerciseEquivalence.alternatives("pullup")
        assertTrue(alts.isNotEmpty())
        assertFalse(alts.contains("pullup"))
        val pullup = com.bodyquest.app.domain.seed.ExerciseCatalog.get("pullup")!!
        alts.forEach { id ->
            val e = com.bodyquest.app.domain.seed.ExerciseCatalog.get(id)!!
            assertEquals(pullup.attribute, e.attribute)
            assertEquals(pullup.type, e.type)
            assertTrue(e.muscles.any { it in pullup.muscles })
        }
    }
}
