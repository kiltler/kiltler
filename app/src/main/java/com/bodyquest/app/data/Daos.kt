package com.bodyquest.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** Суммарный XP по характеристикам из тренировок. */
data class WorkoutXpTotals(
    val s: Int,
    val e: Int,
    val m: Int,
    val d: Int,
)

@Dao
interface ProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun flow(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun get(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: UserProfileEntity)

    @Query("DELETE FROM user_profile")
    suspend fun clear()
}

@Dao
interface MeasurementDao {
    @Query("SELECT * FROM measurement_log ORDER BY dateMillis ASC")
    fun flowAll(): Flow<List<MeasurementEntity>>

    @Query("SELECT * FROM measurement_log ORDER BY dateMillis DESC LIMIT 1")
    fun flowLatest(): Flow<MeasurementEntity?>

    @Query("SELECT * FROM measurement_log ORDER BY dateMillis DESC LIMIT 1")
    suspend fun latest(): MeasurementEntity?

    @Query("SELECT * FROM measurement_log ORDER BY dateMillis ASC LIMIT 1")
    suspend fun first(): MeasurementEntity?

    @Query("SELECT COALESCE(SUM(xpComposition), 0) FROM measurement_log")
    fun compositionXpFlow(): Flow<Int>

    @Query("SELECT COALESCE(SUM(xpComposition), 0) FROM measurement_log")
    suspend fun compositionXp(): Int

    @Query("SELECT * FROM measurement_log")
    suspend fun allOnce(): List<MeasurementEntity>

    @Insert
    suspend fun insert(measurement: MeasurementEntity): Long

    @Query("DELETE FROM measurement_log WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM measurement_log")
    suspend fun count(): Int

    @Query("DELETE FROM measurement_log")
    suspend fun clear()
}

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_session ORDER BY dateMillis DESC")
    fun flowSessions(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT COUNT(*) FROM workout_session")
    fun countFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM workout_session")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM workout_session WHERE isBoss = 1")
    suspend fun bossCount(): Int

    @Query(
        "SELECT COALESCE(SUM(xpStrength),0) AS s, COALESCE(SUM(xpEndurance),0) AS e, " +
            "COALESCE(SUM(xpMobility),0) AS m, COALESCE(SUM(xpDiscipline),0) AS d FROM workout_session"
    )
    fun xpTotalsFlow(): Flow<WorkoutXpTotals>

    @Query(
        "SELECT COALESCE(SUM(xpStrength),0) AS s, COALESCE(SUM(xpEndurance),0) AS e, " +
            "COALESCE(SUM(xpMobility),0) AS m, COALESCE(SUM(xpDiscipline),0) AS d FROM workout_session"
    )
    suspend fun xpTotals(): WorkoutXpTotals

    @Query("SELECT * FROM workout_session")
    suspend fun allSessionsOnce(): List<WorkoutSessionEntity>

    @Query("SELECT * FROM set_log")
    suspend fun allSetsOnce(): List<SetEntity>

    @Insert
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionRestore(session: WorkoutSessionEntity)

    @Insert
    suspend fun insertSets(sets: List<SetEntity>)

    @Query("SELECT * FROM set_log ORDER BY dateMillis ASC")
    fun flowAllSets(): Flow<List<SetEntity>>

    @Query("SELECT * FROM set_log WHERE exerciseId = :exerciseId ORDER BY dateMillis ASC")
    fun flowSetsFor(exerciseId: String): Flow<List<SetEntity>>

    @Query("DELETE FROM workout_session WHERE id = :id")
    suspend fun deleteSession(id: Long)

    @Query("DELETE FROM workout_session")
    suspend fun clearSessions()
}

@Dao
interface PrDao {
    @Query("SELECT * FROM exercise_pr")
    fun flowAll(): Flow<List<ExercisePrEntity>>

    @Query("SELECT * FROM exercise_pr WHERE exerciseId = :id")
    suspend fun get(id: String): ExercisePrEntity?

    @Query("SELECT * FROM exercise_pr")
    suspend fun allOnce(): List<ExercisePrEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(pr: ExercisePrEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(prs: List<ExercisePrEntity>)

    @Query("DELETE FROM exercise_pr")
    suspend fun clear()
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievement")
    fun flowAll(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievement WHERE unlocked = 0")
    suspend fun locked(): List<AchievementEntity>

    @Query("SELECT * FROM achievement")
    suspend fun allOnce(): List<AchievementEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<AchievementEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<AchievementEntity>)

    @Update
    suspend fun update(item: AchievementEntity)

    @Query("UPDATE achievement SET unlocked = 0, unlockedAtMillis = 0")
    suspend fun relockAll()
}

@Dao
interface StreakDao {
    @Query("SELECT * FROM streak WHERE id = 1")
    fun flow(): Flow<StreakEntity?>

    @Query("SELECT * FROM streak WHERE id = 1")
    suspend fun get(): StreakEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(streak: StreakEntity)
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 1")
    fun flow(): Flow<SettingsEntity?>

    @Query("SELECT * FROM settings WHERE id = 1")
    suspend fun get(): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: SettingsEntity)
}

@Dao
interface WaterDao {
    @Query("SELECT * FROM water_log WHERE dateEpochDay = :day")
    fun flowForDay(day: Long): Flow<WaterEntity?>

    @Query("SELECT * FROM water_log WHERE dateEpochDay = :day")
    suspend fun forDay(day: Long): WaterEntity?

    @Query("SELECT * FROM water_log")
    suspend fun allOnce(): List<WaterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(water: WaterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<WaterEntity>)

    @Query("DELETE FROM water_log")
    suspend fun clear()
}

@Dao
interface SleepDao {
    @Query("SELECT * FROM sleep_log WHERE dateEpochDay = :day")
    fun flowForDay(day: Long): Flow<SleepEntity?>

    @Query("SELECT * FROM sleep_log")
    suspend fun allOnce(): List<SleepEntity>

    @Query("SELECT COALESCE(MAX(hours), 0) FROM sleep_log")
    suspend fun maxHours(): Double

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(sleep: SleepEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<SleepEntity>)

    @Query("DELETE FROM sleep_log")
    suspend fun clear()
}
