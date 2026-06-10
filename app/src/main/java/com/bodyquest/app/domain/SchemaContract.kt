package com.bodyquest.app.domain

/**
 * Единый источник правды для миграции v3→v4. И SQL-DEFAULT, и @ColumnInfo(defaultValue=...)
 * берут значение ОТСЮДА — они не могут разойтись по построению. Чистый Kotlin (тестируется на JVM).
 */
object SchemaContract {
    /** Должно совпадать в SQL `DEFAULT` и в `@ColumnInfo(defaultValue=...)`. */
    const val SETTINGS_DEFAULT = "0"

    val ALTER_FREEZE_TOKENS =
        "ALTER TABLE `settings` ADD COLUMN `freezeTokens` INTEGER NOT NULL DEFAULT $SETTINGS_DEFAULT"
    val ALTER_TARGET_WAIST =
        "ALTER TABLE `settings` ADD COLUMN `targetWaist` REAL NOT NULL DEFAULT $SETTINGS_DEFAULT"
    val ALTER_TARGET_BELLY =
        "ALTER TABLE `settings` ADD COLUMN `targetBelly` REAL NOT NULL DEFAULT $SETTINGS_DEFAULT"
    val CREATE_FROZEN_DAY =
        "CREATE TABLE IF NOT EXISTS `frozen_day` (`dateEpochDay` INTEGER NOT NULL, PRIMARY KEY(`dateEpochDay`))"
}
