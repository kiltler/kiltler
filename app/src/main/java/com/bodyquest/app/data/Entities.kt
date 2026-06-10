package com.bodyquest.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "Герой",
    val heightCm: Int = 189,
    val age: Int = 30,
    val sexMale: Boolean = true,
    val daysPerWeek: Int = 3,
    val activity: String = "LIGHT",   // ActivityLevel.name
    val onboarded: Boolean = false,
    val createdAtMillis: Long = 0L,
)

@Entity(tableName = "measurement_log")
data class MeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,
    val dateMillis: Long,
    val weightKg: Double,
    val chest: Double,
    val shoulders: Double,
    val belly: Double,
    val waist: Double,
    val thigh: Double,
    val hips: Double,
    val inseam: Double,
    val foot: Double,
    val xpComposition: Int = 0,
)

@Entity(tableName = "workout_session")
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayId: String,
    val title: String,
    val dateMillis: Long,
    val dateEpochDay: Long,
    val durationSeconds: Int,
    val isBoss: Boolean,
    val xpStrength: Int,
    val xpEndurance: Int,
    val xpMobility: Int,
    val xpDiscipline: Int,
    val totalXp: Int,
    val streakMultiplierX100: Int,
)

@Entity(
    tableName = "set_log",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("sessionId"), Index("exerciseId")],
)
data class SetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val exerciseId: String,
    val exerciseName: String,
    val dateMillis: Long,
    val setIndex: Int,
    val reps: Int,
    val weightKg: Double,
    val timeSeconds: Int,
)

@Entity(tableName = "exercise_pr")
data class ExercisePrEntity(
    @PrimaryKey val exerciseId: String,
    val bestWeight: Double = 0.0,
    val bestReps: Int = 0,
    val bestTimeSeconds: Int = 0,
    val lastWeight: Double = 0.0,
    val lastReps: Int = 0,
    val lastTimeSeconds: Int = 0,
    val updatedAtMillis: Long = 0L,
)

@Entity(tableName = "achievement")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val unlocked: Boolean = false,
    val unlockedAtMillis: Long = 0L,
)

@Entity(tableName = "streak")
data class StreakEntity(
    @PrimaryKey val id: Int = 1,
    val current: Int = 0,
    val longest: Int = 0,
    val lastWorkoutEpochDay: Long = -1L,
)

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val remindersEnabled: Boolean = false,
    val reminderHour: Int = 18,
    val reminderMinute: Int = 30,
    val waterRemindersEnabled: Boolean = false,
)

@Entity(tableName = "water_log")
data class WaterEntity(
    @PrimaryKey val dateEpochDay: Long,
    val amountMl: Int = 0,
)

@Entity(tableName = "sleep_log")
data class SleepEntity(
    @PrimaryKey val dateEpochDay: Long,
    val hours: Double = 0.0,
)

@Entity(tableName = "rank_up")
data class RankUpEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val level: Int,
    val rankTitle: String,
    val atMillis: Long,
)

@Entity(tableName = "challenge_log")
data class ChallengeLogEntity(
    @PrimaryKey val dateEpochDay: Long,
    val bonusXp: Int,
)
