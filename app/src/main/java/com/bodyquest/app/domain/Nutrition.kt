package com.bodyquest.app.domain

import kotlin.math.roundToInt

enum class ActivityLevel(val title: String, val factor: Double) {
    LIGHT("Лёгкая (3 трен./нед)", 1.375),
    MODERATE("Умеренная (4+ трен./нед)", 1.55);
}

data class Macros(
    val proteinG: Int,
    val fatG: Int,
    val carbsG: Int,
)

data class NutritionPlan(
    val bmr: Int,
    val tdee: Int,
    val targetCalories: Int,
    val macros: Macros,
    val deficitApplied: Boolean,
    val flooredToFloor: Boolean,   // целевые упёрлись в порог 1700
    val flooredToBmr: Boolean,     // целевые упёрлись в BMR
    val waterMlGoal: Int,
) {
    val deficitKcal: Int get() = (tdee - targetCalories).coerceAtLeast(0)
}

/**
 * Расчёт калорий и БЖУ. Здоровый, не экстремальный подход.
 * BMR — Миффлин–Сан Жеор (муж.). Дефицит 15–20%, но НИКОГДА ниже BMR и ниже 1700 ккал.
 */
object NutritionCalculator {

    const val MIN_CALORIE_FLOOR = 1700
    private const val DEFICIT = 0.18 // 18% — в рекомендуемом коридоре 15–20%

    fun calculate(
        weightKg: Double,
        heightCm: Int,
        age: Int,
        activity: ActivityLevel,
    ): NutritionPlan {
        val bmr = (10 * weightKg + 6.25 * heightCm - 5 * age + 5).roundToInt()
        val tdee = (bmr * activity.factor).roundToInt()

        val rawTarget = (tdee * (1 - DEFICIT)).roundToInt()

        // Безопасные пороги: не ниже BMR и не ниже 1700 ккал.
        var target = rawTarget
        val flooredToBmr = target < bmr
        if (flooredToBmr) target = bmr
        val flooredToFloor = target < MIN_CALORIE_FLOOR
        if (flooredToFloor) target = MIN_CALORIE_FLOOR

        // БЖУ. Белок ~2.0 г/кг, жиры ~0.8 г/кг, остаток — углеводы.
        val protein = (2.0 * weightKg).roundToInt()
        val fat = (0.8 * weightKg).roundToInt()
        val kcalFromPF = protein * 4 + fat * 9
        val carbs = ((target - kcalFromPF) / 4.0).roundToInt().coerceAtLeast(0)

        // Вода ~2.5–3 л, по массе ~35 мл/кг, в коридоре 2500–3000.
        val water = (weightKg * 35).roundToInt().coerceIn(2500, 3000)

        return NutritionPlan(
            bmr = bmr,
            tdee = tdee,
            targetCalories = target,
            macros = Macros(protein, fat, carbs),
            deficitApplied = target < tdee,
            flooredToFloor = flooredToFloor,
            flooredToBmr = flooredToBmr && !flooredToFloor,
            waterMlGoal = water,
        )
    }
}
