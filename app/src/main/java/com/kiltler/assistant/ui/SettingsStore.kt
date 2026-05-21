package com.kiltler.assistant.ui

import android.content.Context

/** Пользовательские настройки приложения (цены и проценты отчислений). */
data class AppSettings(
    val cleaningPrice: Double = 3500.0,
    val installPrice: Double = 11500.0,
    val materialsPct: Int = 30,
    val adsPct: Int = 10
)

/** Чтение и запись настроек в SharedPreferences. */
object SettingsStore {
    private const val NAME = "settings"
    private const val LEGACY_FINANCE = "finance"

    fun load(context: Context): AppSettings {
        val s = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
        val legacy = context.getSharedPreferences(LEGACY_FINANCE, Context.MODE_PRIVATE)
        return AppSettings(
            cleaningPrice = s.getFloat("price_cleaning", 3500f).toDouble(),
            installPrice = s.getFloat("price_install", 11500f).toDouble(),
            materialsPct = s.getInt("pct_materials", legacy.getInt("mat", 30)),
            adsPct = s.getInt("pct_ads", legacy.getInt("ads", 10))
        )
    }

    fun save(context: Context, settings: AppSettings) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
            .putFloat("price_cleaning", settings.cleaningPrice.toFloat())
            .putFloat("price_install", settings.installPrice.toFloat())
            .putInt("pct_materials", settings.materialsPct)
            .putInt("pct_ads", settings.adsPct)
            .apply()
    }
}
