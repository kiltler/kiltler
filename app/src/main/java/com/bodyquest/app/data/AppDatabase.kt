package com.bodyquest.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UserProfileEntity::class,
        MeasurementEntity::class,
        WorkoutSessionEntity::class,
        SetEntity::class,
        ExercisePrEntity::class,
        AchievementEntity::class,
        StreakEntity::class,
        SettingsEntity::class,
        WaterEntity::class,
        SleepEntity::class,
        RankUpEntity::class,
        ChallengeLogEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun measurementDao(): MeasurementDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun prDao(): PrDao
    abstract fun achievementDao(): AchievementDao
    abstract fun streakDao(): StreakDao
    abstract fun settingsDao(): SettingsDao
    abstract fun waterDao(): WaterDao
    abstract fun sleepDao(): SleepDao
    abstract fun rankUpDao(): RankUpDao
    abstract fun challengeDao(): ChallengeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /** v1 → v2: добавлена таблица сна (без потери прогресса). */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `sleep_log` " +
                        "(`dateEpochDay` INTEGER NOT NULL, `hours` REAL NOT NULL, " +
                        "PRIMARY KEY(`dateEpochDay`))"
                )
            }
        }

        /** v2 → v3: история рангов и журнал бонус-XP за испытания. */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `rank_up` " +
                        "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `level` INTEGER NOT NULL, " +
                        "`rankTitle` TEXT NOT NULL, `atMillis` INTEGER NOT NULL)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `challenge_log` " +
                        "(`dateEpochDay` INTEGER NOT NULL, `bonusXp` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`dateEpochDay`))"
                )
            }
        }

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bodyquest.db",
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    // НЕ используем fallbackToDestructiveMigration: прогресс не должен
                    // теряться при обновлении. Каждое изменение схемы — отдельная Migration.
                    .build().also { INSTANCE = it }
            }
    }
}
