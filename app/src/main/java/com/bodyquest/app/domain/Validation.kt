package com.bodyquest.app.domain

/**
 * Безопасные диапазоны для пользовательского ввода. Защищают расчёты (питание, графики)
 * от нулей и нереалистичных значений. Чистая функция — легко тестируется.
 */
object BodyInput {
    fun height(cm: Int): Int = cm.coerceIn(100, 250)
    fun age(years: Int): Int = years.coerceIn(10, 100)
    fun weight(kg: Double): Double = kg.coerceIn(30.0, 300.0)
    fun circumference(cm: Double): Double = cm.coerceIn(20.0, 200.0)
    fun shoulders(cm: Double): Double = cm.coerceIn(20.0, 80.0)
    fun thigh(cm: Double): Double = cm.coerceIn(20.0, 120.0)
    fun inseam(cm: Double): Double = cm.coerceIn(40.0, 130.0)
    fun foot(cm: Double): Double = cm.coerceIn(10.0, 40.0)
}
