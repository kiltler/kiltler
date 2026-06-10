package com.bodyquest.app.domain

import com.bodyquest.app.domain.seed.ChallengeCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChallengeCatalogTest {

    @Test fun forDay_isDeterministicAndInRange() {
        for (d in 0L..40L) {
            val c = ChallengeCatalog.forDay(d)
            assertTrue(ChallengeCatalog.all.contains(c))
        }
        // одна и та же дата — одно испытание
        assertEquals(ChallengeCatalog.forDay(100), ChallengeCatalog.forDay(100))
    }

    @Test fun forDay_handlesNegativeEpochDay() {
        assertNotNull(ChallengeCatalog.forDay(-3))
    }

    @Test fun forDay_rotatesAcrossList() {
        val seen = (0L until ChallengeCatalog.all.size).map { ChallengeCatalog.forDay(it) }.toSet()
        assertEquals(ChallengeCatalog.all.size, seen.size) // за N дней увидим все
    }
}
