package com.kiltler.assistant.ui

import android.content.Context
import android.location.Address
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/** Результат геокодирования адреса в координаты. */
data class GeocodeResult(
    val displayName: String,
    val latitude: Double,
    val longitude: Double
)

/** Географические рамки Хабаровского края — для центрирования карты и приоритета адресов. */
object KhabarovskRegion {
    /** Центр — город Хабаровск. */
    const val CENTER_LAT = 48.4827
    const val CENTER_LON = 135.0838

    /** Ограничивающий прямоугольник края. */
    const val SOUTH = 46.0
    const val WEST = 129.0
    const val NORTH = 62.5
    const val EAST = 142.5

    fun contains(lat: Double, lon: Double): Boolean =
        lat in SOUTH..NORTH && lon in WEST..EAST
}

/**
 * Превращает текстовый адрес в координаты через системный геокодер.
 *
 * Адреса Хабаровского края имеют приоритет: сначала идёт поиск строго
 * в границах региона, и лишь затем — обычный поиск как запасной вариант.
 * Возвращает null, если адрес не найден или геокодер недоступен.
 */
suspend fun geocodeAddress(context: Context, query: String): GeocodeResult? =
    withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isBlank() || !Geocoder.isPresent()) return@withContext null
        val geocoder = Geocoder(context, Locale("ru"))

        // Запросы по убыванию приоритета: вариант с привязкой к краю — первым.
        val queries = listOf(withRegionSuffix(trimmed), trimmed).distinct()

        // 1. Совпадение строго в границах Хабаровского края.
        for (q in queries) {
            regionMatch(geocoder, q)?.let { return@withContext it }
        }
        // 2. Запасной вариант — поиск без географических ограничений.
        for (q in queries) {
            plainMatch(geocoder, q)?.let { return@withContext it }
        }
        null
    }

/** Добавляет «, Хабаровский край», если регион ещё не упомянут в запросе. */
private fun withRegionSuffix(query: String): String {
    val lower = query.lowercase()
    return if (lower.contains("хабаров") || lower.contains("край")) query
    else "$query, Хабаровский край"
}

/** Ищет адрес с привязкой к краю и оставляет только попавшие в его границы. */
private fun regionMatch(geocoder: Geocoder, query: String): GeocodeResult? = try {
    @Suppress("DEPRECATION")
    val matches = geocoder.getFromLocationName(
        query, 5,
        KhabarovskRegion.SOUTH, KhabarovskRegion.WEST,
        KhabarovskRegion.NORTH, KhabarovskRegion.EAST
    ).orEmpty()
    matches.firstOrNull { KhabarovskRegion.contains(it.latitude, it.longitude) }
        ?.let { toResult(it, query) }
} catch (e: Exception) {
    null
}

/** Обычный поиск без ограничений — запасной вариант. */
private fun plainMatch(geocoder: Geocoder, query: String): GeocodeResult? = try {
    @Suppress("DEPRECATION")
    val matches = geocoder.getFromLocationName(query, 1).orEmpty()
    matches.firstOrNull()?.let { toResult(it, query) }
} catch (e: Exception) {
    null
}

private fun toResult(address: Address, fallback: String): GeocodeResult {
    val name = (0..address.maxAddressLineIndex)
        .joinToString(", ") { address.getAddressLine(it) }
        .ifBlank { fallback }
    return GeocodeResult(name, address.latitude, address.longitude)
}
