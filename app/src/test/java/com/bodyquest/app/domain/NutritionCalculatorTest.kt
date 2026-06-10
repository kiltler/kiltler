package com.bodyquest.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NutritionCalculatorTest {

    @Test fun mifflin_andDeficit_forDefaultUser() {
        val plan = NutritionCalculator.calculate(97.0, 189, 30, ActivityLevel.LIGHT)
        assertEquals(2006, plan.bmr)            // 10*97 + 6.25*189 - 5*30 + 5
        assertEquals(2758, plan.tdee)           // bmr * 1.375
        assertTrue(plan.targetCalories < plan.tdee)        // дефицит применён
        assertTrue(plan.targetCalories >= plan.bmr)        // но не ниже BMR
        assertTrue(plan.targetCalories >= NutritionCalculator.MIN_CALORIE_FLOOR)
    }

    @Test fun macros_followGramsPerKg() {
        val plan = NutritionCalculator.calculate(97.0, 189, 30, ActivityLevel.LIGHT)
        assertEquals(194, plan.macros.proteinG) // 2.0 г/кг
        assertEquals(78, plan.macros.fatG)      // 0.8 г/кг
        assertTrue(plan.macros.carbsG >= 0)
    }

    @Test fun neverBelow1700_forSmallPerson() {
        val plan = NutritionCalculator.calculate(40.0, 150, 25, ActivityLevel.LIGHT)
        assertEquals(1700, plan.targetCalories)            // упёрлись в порог
        assertTrue(plan.flooredToFloor)
    }

    @Test fun floor1700_isHardLimit_acrossInputs() {
        for (w in intArrayOf(35, 50, 70, 120, 200)) {
            val plan = NutritionCalculator.calculate(w.toDouble(), 170, 40, ActivityLevel.LIGHT)
            assertTrue("w=$w дал ${plan.targetCalories}", plan.targetCalories >= 1700)
        }
    }
}
