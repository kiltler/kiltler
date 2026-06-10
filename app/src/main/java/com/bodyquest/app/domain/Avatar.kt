package com.bodyquest.app.domain

import kotlin.math.max

/**
 * Параметры силуэта героя, 0..1. Нейтраль (== baseline) ≈ 0.5 по каждому полю,
 * чтобы фигура могла как «расти», так и «сушиться». Чистые данные без Android.
 */
data class AvatarShape(
    val shoulder: Float,  // ширина плеч/торса (1 = широкие)
    val waist: Float,     // узость талии (1 = узкая)
    val mass: Float,      // общая масса (1 = крупнее)
    val leanness: Float,  // сухость середины (1 = без живота)
)

object AvatarMapper {

    private fun ratio(a: Double, b: Double): Double = if (b <= 0.0) 1.0 else a / b

    /** Центрирование: x==1 → 0.5; x==1+halfRange → 1.0; x==1−halfRange → 0.0. */
    private fun centered(x: Double, halfRange: Double): Float =
        (0.5 + (x - 1.0) / (2.0 * halfRange)).toFloat().coerceIn(0f, 1f)

    /**
     * Силуэт по дельте baseline→текущее.
     * Плечи — от изменения V-ratio (плечи/талия); узость талии — от падения талии;
     * сухость — от падения живота; масса — от изменения веса.
     */
    fun shape(
        baselineShoulders: Double, baselineWaist: Double, baselineBelly: Double, baselineWeight: Double,
        shoulders: Double, waist: Double, belly: Double, weight: Double,
    ): AvatarShape {
        val vBase = ratio(baselineShoulders, baselineWaist)
        val vCur = ratio(shoulders, waist)
        val shoulder = centered(ratio(vCur, vBase), halfRange = 0.20)
        val waistN = centered(ratio(baselineWaist, max(waist, 1.0)), halfRange = 0.20)
        val lean = centered(ratio(baselineBelly, max(belly, 1.0)), halfRange = 0.20)
        val mass = centered(ratio(weight, max(baselineWeight, 1.0)), halfRange = 0.15)
        return AvatarShape(shoulder, waistN, mass, lean)
    }

    /** «Призрак цели»: широкие плечи + узкая талия + сухая середина. */
    fun target(
        baselineShoulders: Double, baselineWaist: Double, baselineBelly: Double, baselineWeight: Double,
        shoulders: Double, weight: Double,
        targetWaist: Double, targetBelly: Double,
    ): AvatarShape {
        // если цель не задана (<=0) — берём амбициозную, но реалистичную (−12% талия/живот)
        val tWaist = if (targetWaist > 0) targetWaist else baselineWaist * 0.88
        val tBelly = if (targetBelly > 0) targetBelly else baselineBelly * 0.88
        return shape(
            baselineShoulders, baselineWaist, baselineBelly, baselineWeight,
            shoulders = shoulders, waist = tWaist, belly = tBelly, weight = weight,
        )
    }
}
