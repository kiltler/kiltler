package com.bodyquest.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class BodyInputTest {

    @Test fun weight_isClampedToSaneRange() {
        assertEquals(30.0, BodyInput.weight(0.0), 0.0)
        assertEquals(30.0, BodyInput.weight(-50.0), 0.0)
        assertEquals(300.0, BodyInput.weight(999.0), 0.0)
        assertEquals(97.0, BodyInput.weight(97.0), 0.0)
    }

    @Test fun height_age_clamped() {
        assertEquals(100, BodyInput.height(0))
        assertEquals(250, BodyInput.height(500))
        assertEquals(10, BodyInput.age(3))
        assertEquals(100, BodyInput.age(150))
    }

    @Test fun circumferences_clamped() {
        assertEquals(20.0, BodyInput.circumference(0.0), 0.0)
        assertEquals(200.0, BodyInput.circumference(999.0), 0.0)
        assertEquals(10.0, BodyInput.foot(5.0), 0.0)    // нижняя граница стопы
        assertEquals(40.0, BodyInput.foot(99.0), 0.0)   // верхняя граница стопы
    }
}
