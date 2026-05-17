package com.kiltler.assistant.ui

import android.content.Context
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

/**
 * Превращает текстовый адрес в координаты через системный геокодер.
 * Возвращает null, если адрес не найден или геокодер недоступен.
 */
suspend fun geocodeAddress(context: Context, query: String): GeocodeResult? =
    withContext(Dispatchers.IO) {
        if (query.isBlank() || !Geocoder.isPresent()) return@withContext null
        try {
            val geocoder = Geocoder(context, Locale("ru"))
            @Suppress("DEPRECATION")
            val matches = geocoder.getFromLocationName(query, 1)
            val address = matches?.firstOrNull() ?: return@withContext null
            val name = (0..address.maxAddressLineIndex)
                .joinToString(", ") { address.getAddressLine(it) }
                .ifBlank { query.trim() }
            GeocodeResult(name, address.latitude, address.longitude)
        } catch (e: Exception) {
            null
        }
    }
