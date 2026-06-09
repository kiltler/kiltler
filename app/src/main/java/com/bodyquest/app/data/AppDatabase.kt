package com.bodyquest.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

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
    ],
    version = 1,
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

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bodyquest.db",
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}
