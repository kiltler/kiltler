package com.kiltler.assistant.ui

import android.content.Context

/** Пользовательские настройки приложения. */
data class AppSettings(
    val cleaningPrice: Double = 3500.0,
    val refillPrice: Double = 0.0,
    val installPrice: Double = 11500.0,
    val preInstallPrice: Double = 0.0,
    val demountPrice: Double = 0.0,
    val materialsPct: Int = 30,
    val adsPct: Int = 10,
    val acPriceMdv7: Double = 0.0,
    val acPriceMdv9: Double = 0.0,
    val acPriceMdv12: Double = 0.0,
    val acPriceMdv24: Double = 0.0,
    val acPriceMulti: Double = 0.0
) {
    /** Закупочная цена кондиционера выбранной модели. */
    fun acPriceFor(code: String): Double = when (code) {
        "MDV7" -> acPriceMdv7
        "MDV9" -> acPriceMdv9
        "MDV12" -> acPriceMdv12
        "MDV24" -> acPriceMdv24
        "MULTI" -> acPriceMulti
        else -> 0.0
    }
}

/** Чтение и запись настроек в SharedPreferences. */
object SettingsStore {
    private const val NAME = "settings"
    private const val LEGACY_FINANCE = "finance"

    fun load(context: Context): AppSettings {
        val s = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
        val legacy = context.getSharedPreferences(LEGACY_FINANCE, Context.MODE_PRIVATE)
        return AppSettings(
            cleaningPrice = s.getFloat("price_cleaning", 3500f).toDouble(),
            refillPrice = s.getFloat("price_refill", 0f).toDouble(),
            installPrice = s.getFloat("price_install", 11500f).toDouble(),
            preInstallPrice = s.getFloat("price_pre_install", 0f).toDouble(),
            demountPrice = s.getFloat("price_demount", 0f).toDouble(),
            materialsPct = s.getInt("pct_materials", legacy.getInt("mat", 30)),
            adsPct = s.getInt("pct_ads", legacy.getInt("ads", 10)),
            acPriceMdv7 = s.getFloat("ac_mdv7", 0f).toDouble(),
            acPriceMdv9 = s.getFloat("ac_mdv9", 0f).toDouble(),
            acPriceMdv12 = s.getFloat("ac_mdv12", 0f).toDouble(),
            acPriceMdv24 = s.getFloat("ac_mdv24", 0f).toDouble(),
            acPriceMulti = s.getFloat("ac_multi", 0f).toDouble()
        )
    }

    fun save(context: Context, settings: AppSettings) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
            .putFloat("price_cleaning", settings.cleaningPrice.toFloat())
            .putFloat("price_refill", settings.refillPrice.toFloat())
            .putFloat("price_install", settings.installPrice.toFloat())
            .putFloat("price_pre_install", settings.preInstallPrice.toFloat())
            .putFloat("price_demount", settings.demountPrice.toFloat())
            .putInt("pct_materials", settings.materialsPct)
            .putInt("pct_ads", settings.adsPct)
            .putFloat("ac_mdv7", settings.acPriceMdv7.toFloat())
            .putFloat("ac_mdv9", settings.acPriceMdv9.toFloat())
            .putFloat("ac_mdv12", settings.acPriceMdv12.toFloat())
            .putFloat("ac_mdv24", settings.acPriceMdv24.toFloat())
            .putFloat("ac_multi", settings.acPriceMulti.toFloat())
            .apply()
    }
}
