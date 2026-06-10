package com.bodyquest.app.domain

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalyticsTest {

    private fun day(s: String) = LocalDate.parse(s).toEpochDay()

    @Test fun startOfIsoWeek_isMonday() {
        // 2024-06-12 — среда; понедельник этой недели — 2024-06-10
        assertEquals(day("2024-06-10"), Analytics.startOfIsoWeek(day("2024-06-12")))
        assertEquals(day("2024-06-10"), Analytics.startOfIsoWeek(day("2024-06-10")))
        assertEquals(day("2024-06-10"), Analytics.startOfIsoWeek(day("2024-06-16"))) // воскресенье
    }

    @Test fun sessionsThisWeek_countsOnlyCurrentWeek() {
        val today = day("2024-06-13")
        val sessions = listOf(
            day("2024-06-10"), day("2024-06-12"), day("2024-06-13"), // эта неделя
            day("2024-06-09"), day("2024-06-03"),                     // прошлые недели
        )
        assertEquals(3, Analytics.sessionsThisWeek(sessions, today))
    }

    @Test fun weeksSince_andDeload() {
        val start = day("2024-01-01") // понедельник
        assertEquals(0, Analytics.weeksSince(start, day("2024-01-03")))
        assertEquals(5, Analytics.weeksSince(start, day("2024-02-05")))
        assertTrue(Analytics.isDeloadWeek(5))   // 6-я неделя
        assertTrue(Analytics.isDeloadWeek(11))
        assertFalse(Analytics.isDeloadWeek(0))
        assertFalse(Analytics.isDeloadWeek(4))
    }

    @Test fun deltaOverDays_negativeWhenDecreased() {
        val today = day("2024-02-01")
        val pts = listOf(
            day("2024-01-01") to 100.0,
            day("2024-01-25") to 97.0,
            day("2024-02-01") to 95.0,
        )
        // ~30 дней назад ближайшая точка ≤ (today-30=2024-01-02) — это 2024-01-01 (100)
        val d = Analytics.deltaOverDays(pts, today, 30)!!
        assertEquals(-5.0, d, 0.0001)
    }

    @Test fun deltaOverDays_nullWhenNotEnough() {
        assertNull(Analytics.deltaOverDays(listOf(day("2024-01-01") to 90.0), day("2024-02-01"), 30))
    }
}
