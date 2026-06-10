package com.bodyquest.app.domain

data class WeakLink(
    val weakest: AttributeType,
    val pushPullWarning: Boolean,
    val advice: String,
)

/**
 * Находит отстающую характеристику и перекос тяги/жима.
 * Для V-силуэта тяги (спина) не должны отставать от жимов.
 */
object WeakLinkAnalyzer {

    /** pull отстаёт, если его объём < 70% от push. */
    private const val PULL_LAG_RATIO = 0.7

    fun analyze(
        attrXp: Map<AttributeType, Int>,
        pushVolume: Double,
        pullVolume: Double,
    ): WeakLink {
        val weakest = AttributeType.entries.minByOrNull { attrXp[it] ?: 0 } ?: AttributeType.STRENGTH
        val pushPullWarning = push_lagging(pushVolume, pullVolume)
        val advice = when {
            pushPullWarning ->
                "Тяги (спина) отстают от жимов — добавь подтягивания/тяги. Это ключ к V-силуэту."
            else ->
                "Слабое звено: ${weakest.title}. Сегодня сделай акцент на нём — будет бонус-XP."
        }
        return WeakLink(weakest, pushPullWarning, advice)
    }

    private fun push_lagging(pushVolume: Double, pullVolume: Double): Boolean =
        pushVolume > 0 && pullVolume < pushVolume * PULL_LAG_RATIO

    /** push-мышцы: грудь/плечи/трицепс. */
    fun isPush(muscles: List<Muscle>): Boolean =
        muscles.any { it == Muscle.CHEST || it == Muscle.SHOULDERS || it == Muscle.TRICEPS }

    /** pull-мышцы: спина/бицепс. */
    fun isPull(muscles: List<Muscle>): Boolean =
        muscles.any { it == Muscle.BACK || it == Muscle.BICEPS }
}
