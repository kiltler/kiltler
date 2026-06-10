package com.bodyquest.app.data

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.bodyquest.app.domain.SchemaContract
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Реальный SQLite-прогон миграции v3 → v4 на устройстве/эмуляторе.
 * Схема v3 не экспортировалась, поэтому строим базу v3 вручную на «голом» SQLite,
 * затем выполняем MIGRATION_3_4 напрямую и проверяем: миграция не падает, данные целы,
 * новые поля получили DEFAULT из SchemaContract, таблица frozen_day создана.
 *
 * Запуск: ./gradlew connectedDebugAndroidTest (нужен эмулятор/устройство).
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val testDb = "migration-3to4-test.db"

    @Test
    fun migrate3To4_keepsDataAndAddsDefaults() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        ctx.deleteDatabase(testDb)

        // --- v3 «как в реальной базе»: создаём settings со СТАРЫМИ колонками + кладём данные ---
        val v3Config = SupportSQLiteOpenHelper.Configuration.builder(ctx)
            .name(testDb)
            .callback(object : SupportSQLiteOpenHelper.Callback(3) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS `settings` (`id` INTEGER NOT NULL, " +
                            "`remindersEnabled` INTEGER NOT NULL, `reminderHour` INTEGER NOT NULL, " +
                            "`reminderMinute` INTEGER NOT NULL, `waterRemindersEnabled` INTEGER NOT NULL, " +
                            "PRIMARY KEY(`id`))"
                    )
                    db.execSQL(
                        "INSERT INTO settings (id, remindersEnabled, reminderHour, reminderMinute, " +
                            "waterRemindersEnabled) VALUES (1, 1, 7, 45, 1)"
                    )
                }
                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()
        val v3Helper = FrameworkSQLiteOpenHelperFactory().create(v3Config)
        v3Helper.writableDatabase.use { db ->
            // --- выполняем реальную миграцию ---
            AppDatabase.MIGRATION_3_4.migrate(db)

            // старые данные на месте
            db.query("SELECT reminderHour, reminderMinute, waterRemindersEnabled FROM settings WHERE id=1").use { c ->
                assertTrue(c.moveToFirst())
                assertEquals(7, c.getInt(0))
                assertEquals(45, c.getInt(1))
                assertEquals(1, c.getInt(2))
            }
            // новые поля получили DEFAULT из SchemaContract (== @ColumnInfo(defaultValue))
            db.query("SELECT freezeTokens, targetWaist, targetBelly FROM settings WHERE id=1").use { c ->
                assertTrue(c.moveToFirst())
                assertEquals(SchemaContract.SETTINGS_DEFAULT.toInt(), c.getInt(0))
                assertEquals(0.0, c.getDouble(1), 0.0001)
                assertEquals(0.0, c.getDouble(2), 0.0001)
            }
            // таблица frozen_day создана и пишется
            db.execSQL("INSERT INTO frozen_day (dateEpochDay) VALUES (20001)")
            db.query("SELECT COUNT(*) FROM frozen_day").use { c ->
                assertTrue(c.moveToFirst())
                assertEquals(1, c.getInt(0))
            }
        }
        ctx.deleteDatabase(testDb)
    }
}
