package com.bodyquest.app.domain

import kotlin.math.roundToInt

/** Суточный модификатор XP. perAttr — множитель характеристики; zeroAttrs — обнуляется сегодня. */
data class DayModifier(
    val id: String,
    val title: String,
    val emoji: String,
    val perAttr: Map<AttributeType, Float> = emptyMap(),
    val zeroAttrs: Set<AttributeType> = emptySet(),
) {
    fun mult(attr: AttributeType): Float = perAttr[attr] ?: 1f
}

/** Детерминированный по дате модификатор дня (офлайн, одинаков для всех запусков в этот день). */
object ModifierEngine {
    val all: List<DayModifier> = listOf(
        DayModifier("none", "Обычный день", "☀️"),
        DayModifier("strength", "День силы: ×2 к Силе", "💪", perAttr = mapOf(AttributeType.STRENGTH to 2f)),
        DayModifier("endurance", "День выносливости: +50%", "🫁", perAttr = mapOf(AttributeType.ENDURANCE to 1.5f)),
        DayModifier("mobility", "День гибкости: +50%", "🤸", perAttr = mapOf(AttributeType.MOBILITY to 1.5f)),
        DayModifier("nocardio", "Силовой режим: кардио не считается", "🚫", zeroAttrs = setOf(AttributeType.ENDURANCE)),
        DayModifier("surge", "Прилив сил: +25% ко всему", "✨",
            perAttr = AttributeType.entries.associateWith { 1.25f }),
    )

    fun forDay(epochDay: Long): DayModifier {
        val i = ((epochDay % all.size) + all.size) % all.size
        return all[i.toInt()]
    }
}

/**
 * Явный порядок XP-пайплайна: базовый XP → модификатор дня → множитель серии.
 * Обнулённые модификатором характеристики дают 0 в этот день.
 */
object XpPipeline {
    fun apply(
        base: Map<AttributeType, Int>,
        modifier: DayModifier,
        streakMultiplier: Float,
    ): Map<AttributeType, Int> = base.mapValues { (attr, v) ->
        if (attr in modifier.zeroAttrs) 0
        else (v * modifier.mult(attr) * streakMultiplier).roundToInt()
    }
}
