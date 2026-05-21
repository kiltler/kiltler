package com.kiltler.assistant.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.yandex.mapkit.GeoObject
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManager
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.SearchType
import com.yandex.mapkit.search.Session
import com.yandex.runtime.Error
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/** Открывает Яндекс Карты с автомобильным маршрутом до координат. */
fun openYandexDrivingRoute(context: Context, latitude: Double, longitude: Double) {
    val point = "$latitude,$longitude"
    val targets = listOf(
        "yandexmaps://maps.yandex.ru/?rtext=~$point&rtt=auto",
        "https://yandex.ru/maps/?rtext=~$point&rtt=auto"
    )
    for (uri in targets) {
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            return
        } catch (e: Exception) {
            // приложение не найдено — пробуем следующий вариант
        }
    }
}

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
 * Геокодирует адрес через Yandex MapKit Search SDK — те же результаты,
 * что и в самих Яндекс Картах. Системный Android Geocoder под капотом
 * использует Google и иногда расходится с Яндексом на дальневосточных
 * адресах, что и приводило к промахам при копировании адреса из карт.
 *
 * Поиск идёт с приоритетом Хабаровского края: сначала с ограничительной
 * рамкой региона, и только если ничего не нашлось — без неё.
 */
suspend fun geocodeAddress(context: Context, query: String): GeocodeResult? {
    val trimmed = query.trim()
    if (trimmed.isBlank()) return null
    // Несколько форм одного запроса по убыванию приоритета.
    // Префикс «улица»/«ул.» иногда сбивает фуззи-матчинг Яндекс Search SDK,
    // поэтому пробуем и с ним, и без.
    val withoutStreet = trimmed.replace(Regex("(?i)^\\s*(ул\\.?|улица)\\s+"), "")
    val queries = listOf(
        qualifyAddress(trimmed),
        qualifyAddress(withoutStreet),
        trimmed,
        withoutStreet,
    ).distinct()

    return withContext(Dispatchers.Main) {
        val manager = searchManager(context)
        // 1. С приоритетом региона.
        for (q in queries) {
            yandexGeocode(manager, q, regionBias = true)?.let { return@withContext it }
        }
        // 2. Запасной вариант — поиск без географической рамки.
        for (q in queries) {
            yandexGeocode(manager, q, regionBias = false)?.let { return@withContext it }
        }
        null
    }
}

private val KNOWN_CITIES = listOf("хабаровск", "комсомольск", "амурск")

/**
 * Дополняет адрес городом Хабаровск, если город не указан явно.
 * Резко повышает точность для коротких адресов вида «Ворошилова 4».
 */
fun qualifyAddress(query: String): String {
    val trimmed = query.trim()
    val lower = trimmed.lowercase()
    return if (KNOWN_CITIES.any { it in lower }) trimmed
    else "Хабаровск, $trimmed"
}

// --- Yandex Search SDK ---

@Volatile
private var cachedManager: SearchManager? = null

/** Лениво создаёт менеджер поиска на главном потоке и кэширует его.
 *  В MapKit 4.x Search SDK инициализируется автоматически вместе с
 *  `MapKitFactory.initialize(...)` (вызывается в MainActivity), отдельный
 *  `SearchFactory.initialize(...)` в этой версии SDK отсутствует. */
private fun searchManager(@Suppress("UNUSED_PARAMETER") context: Context): SearchManager {
    cachedManager?.let { return it }
    synchronized(Geocoding) {
        cachedManager?.let { return it }
        // ONLINE даёт точные актуальные ответы по API Яндекса.
        // COMBINED иногда выбирает offline-индекс с фуззи-матчингом, что и
        // приводило к подмене на похоже звучащий, но географически далёкий
        // адрес.
        return SearchFactory.getInstance()
            .createSearchManager(SearchManagerType.ONLINE)
            .also { cachedManager = it }
    }
}

/** Узкая обёртка над `SearchManager.submit` в виде suspend-функции. */
private suspend fun yandexGeocode(
    manager: SearchManager,
    query: String,
    regionBias: Boolean
): GeocodeResult? = suspendCancellableCoroutine { cont ->
    val options = SearchOptions()
        .setSearchTypes(SearchType.GEO.value)
        .setResultPageSize(5)
    val geometry: Geometry = if (regionBias) {
        Geometry.fromBoundingBox(
            BoundingBox(
                Point(KhabarovskRegion.SOUTH, KhabarovskRegion.WEST),
                Point(KhabarovskRegion.NORTH, KhabarovskRegion.EAST)
            )
        )
    } else {
        // Точка-затравка: центр Хабаровска — Search всё равно вернёт глобальные результаты,
        // но при равных весах выберет ближайшие к нам.
        Geometry.fromPoint(Point(KhabarovskRegion.CENTER_LAT, KhabarovskRegion.CENTER_LON))
    }

    val listener = object : Session.SearchListener {
        override fun onSearchResponse(response: Response) {
            val candidates = response.collection.children
                .mapNotNull { it.obj }
                .mapNotNull(::toResult)

            val best = if (regionBias) {
                candidates.firstOrNull { KhabarovskRegion.contains(it.latitude, it.longitude) }
                    ?: candidates.firstOrNull()
            } else {
                candidates.firstOrNull()
            }
            if (cont.isActive) cont.resume(best)
        }

        override fun onSearchError(error: Error) {
            if (cont.isActive) cont.resume(null)
        }
    }

    val session: Session = manager.submit(query, geometry, options, listener)
    cont.invokeOnCancellation { session.cancel() }
}

private fun toResult(obj: GeoObject): GeocodeResult? {
    val point = obj.geometry.firstOrNull()?.point ?: return null
    val name = (obj.name ?: obj.descriptionText)?.takeIf { it.isNotBlank() }
        ?: "${point.latitude}, ${point.longitude}"
    return GeocodeResult(name, point.latitude, point.longitude)
}

/** Маркер для синхронизации ленивой инициализации. */
private object Geocoding
