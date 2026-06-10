package com.bodyquest.app.data

import com.bodyquest.app.domain.AttributeType
import com.bodyquest.app.domain.Dates
import com.bodyquest.app.domain.Leveling
import com.bodyquest.app.domain.LoggedSet
import com.bodyquest.app.domain.MeasurementOutcome
import com.bodyquest.app.domain.FreezeRules
import com.bodyquest.app.domain.ModifierEngine
import com.bodyquest.app.domain.Rank
import com.bodyquest.app.domain.Streaks
import com.bodyquest.app.domain.WorkoutDay
import com.bodyquest.app.domain.XpPipeline
import com.bodyquest.app.domain.WorkoutOutcome
import com.bodyquest.app.domain.WorkoutScoring
import com.bodyquest.app.domain.seed.AchievementCatalog
import com.bodyquest.app.domain.seed.AchievementDef
import com.bodyquest.app.domain.seed.QuickWorkout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Единая точка доступа к данным и RPG-логике. Всё офлайн, через Room + Flow.
 */
class Repository(private val db: AppDatabase) {

    private val profileDao = db.profileDao()
    private val measurementDao = db.measurementDao()
    private val workoutDao = db.workoutDao()
    private val prDao = db.prDao()
    private val achievementDao = db.achievementDao()
    private val streakDao = db.streakDao()
    private val settingsDao = db.settingsDao()
    private val waterDao = db.waterDao()
    private val sleepDao = db.sleepDao()
    private val rankUpDao = db.rankUpDao()
    private val challengeDao = db.challengeDao()
    private val frozenDayDao = db.frozenDayDao()

    // ─────────────────────────── Flows ───────────────────────────
    val profile: Flow<UserProfileEntity?> = profileDao.flow()
    val latestMeasurement: Flow<MeasurementEntity?> = measurementDao.flowLatest()
    val measurements: Flow<List<MeasurementEntity>> = measurementDao.flowAll()
    val sessions: Flow<List<WorkoutSessionEntity>> = workoutDao.flowSessions()
    val workoutCount: Flow<Int> = workoutDao.countFlow()
    val xpTotals: Flow<WorkoutXpTotals> = workoutDao.xpTotalsFlow()
    val compositionXp: Flow<Int> = measurementDao.compositionXpFlow()
    val achievements: Flow<List<AchievementEntity>> = achievementDao.flowAll()
    val streak: Flow<StreakEntity?> = streakDao.flow()
    val settings: Flow<SettingsEntity?> = settingsDao.flow()
    val allSets: Flow<List<SetEntity>> = workoutDao.flowAllSets()
    val prs: Flow<List<ExercisePrEntity>> = prDao.flowAll()

    fun waterTodayFlow(): Flow<WaterEntity?> = waterDao.flowForDay(Dates.todayEpochDay())
    fun sleepTodayFlow(): Flow<SleepEntity?> = sleepDao.flowForDay(Dates.todayEpochDay())

    val frozenDays: Flow<List<Long>> = frozenDayDao.flowAll().map { list -> list.map { it.dateEpochDay } }
    val rankUps: Flow<List<RankUpEntity>> = rankUpDao.flowAll()
    val challengeXp: Flow<Int> = challengeDao.flowTotal()
    fun challengeClaimedTodayFlow(): Flow<Boolean> =
        challengeDao.flowForDay(Dates.todayEpochDay()).map { it != null }

    // ─────────────────────────── Seed ───────────────────────────
    suspend fun seedIfNeeded() {
        achievementDao.insertAll(AchievementCatalog.all.map { AchievementEntity(it.id) })
        if (streakDao.get() == null) streakDao.upsert(StreakEntity())
        if (settingsDao.get() == null) settingsDao.upsert(SettingsEntity())
    }

    // ─────────────────────────── Onboarding / profile ───────────────────────────
    suspend fun completeOnboarding(
        name: String,
        heightCm: Int,
        age: Int,
        daysPerWeek: Int,
        activity: String,
        weightKg: Double,
        chest: Double,
        shoulders: Double,
        belly: Double,
        waist: Double,
        thigh: Double,
        hips: Double,
        inseam: Double,
        foot: Double,
    ) {
        val now = System.currentTimeMillis()
        seedIfNeeded()
        profileDao.upsert(
            UserProfileEntity(
                name = name,
                heightCm = heightCm,
                age = age,
                daysPerWeek = daysPerWeek,
                activity = activity,
                onboarded = true,
                createdAtMillis = now,
            )
        )
        measurementDao.insert(
            MeasurementEntity(
                dateEpochDay = Dates.todayEpochDay(),
                dateMillis = now,
                weightKg = weightKg,
                chest = chest,
                shoulders = shoulders,
                belly = belly,
                waist = waist,
                thigh = thigh,
                hips = hips,
                inseam = inseam,
                foot = foot,
                xpComposition = 0,
            )
        )
        evaluateAchievements()
    }

    suspend fun updateProfile(profile: UserProfileEntity) = profileDao.upsert(profile)

    /** Удаление тренировки — XP/уровни/радар пересчитываются автоматически (через SUM). */
    suspend fun deleteSession(id: Long) = workoutDao.deleteSession(id)

    /**
     * Удаление замера. Не даём удалить последний (нужна хотя бы одна точка отсчёта),
     * чтобы не сломать расчёт питания и графики.
     */
    suspend fun deleteMeasurement(id: Long): Boolean {
        if (measurementDao.count() <= 1) return false
        measurementDao.deleteById(id)
        return true
    }

    // ─────────────────────────── Measurements ───────────────────────────
    suspend fun logMeasurement(
        weightKg: Double,
        chest: Double,
        shoulders: Double,
        belly: Double,
        waist: Double,
        thigh: Double,
        hips: Double,
        inseam: Double,
        foot: Double,
    ): MeasurementOutcome {
        val prev = measurementDao.latest()
        val now = System.currentTimeMillis()

        var xp = 0
        if (prev != null) {
            val waistDrop = (prev.waist - waist).coerceAtLeast(0.0)
            val bellyDrop = (prev.belly - belly).coerceAtLeast(0.0)
            val weightDrop = (prev.weightKg - weightKg).coerceAtLeast(0.0)
            xp += (waistDrop * 40).roundToInt()
            xp += (bellyDrop * 30).roundToInt()
            xp += (weightDrop * 20).roundToInt()
        }

        val oldTotal = totalXpAll()
        val oldLevel = Leveling.levelFor(oldTotal)

        measurementDao.insert(
            MeasurementEntity(
                dateEpochDay = Dates.todayEpochDay(),
                dateMillis = now,
                weightKg = weightKg,
                chest = chest,
                shoulders = shoulders,
                belly = belly,
                waist = waist,
                thigh = thigh,
                hips = hips,
                inseam = inseam,
                foot = foot,
                xpComposition = xp,
            )
        )

        val newLevel = Leveling.levelFor(oldTotal + xp)
        recordRankUps(oldLevel, newLevel, now)
        val unlocked = evaluateAchievements()
        return MeasurementOutcome(xp, oldLevel, newLevel, unlocked)
    }

    // ─────────────────────────── Workout ───────────────────────────
    suspend fun finishWorkout(
        day: WorkoutDay,
        logged: List<LoggedSet>,
        durationSeconds: Int,
    ): WorkoutOutcome {
        val completed = logged.filter { it.isCompleted }
        val today = Dates.todayEpochDay()
        val now = System.currentTimeMillis()

        // Серия дней (замороженные дни не рвут streak)
        val frozen = frozenDayDao.allDays().toSet()
        val streak = streakDao.get() ?: StreakEntity()
        val newStreak = Streaks.nextStreak(streak.lastWorkoutEpochDay, streak.current, today, frozen)
        val longest = max(streak.longest, newStreak)
        val multiplier = Leveling.streakMultiplier(newStreak)

        // XP: базовый → модификатор дня → множитель серии → множитель сессии
        val modifier = ModifierEngine.forDay(today)
        val base = WorkoutScoring.baseXp(completed, day.isBoss)
        val sessionFactor = if (day.id == QuickWorkout.ID) XpPipeline.SHORT_SESSION_FACTOR else 1f
        val xpByAttr = XpPipeline.apply(base, modifier, multiplier, sessionFactor)
        val total = xpByAttr.values.sum()

        val oldTotal = totalXpAll()
        val oldLevel = Leveling.levelFor(oldTotal)

        val sessionId = workoutDao.insertSession(
            WorkoutSessionEntity(
                dayId = day.id,
                title = day.title,
                dateMillis = now,
                dateEpochDay = today,
                durationSeconds = durationSeconds,
                isBoss = day.isBoss,
                xpStrength = xpByAttr[AttributeType.STRENGTH] ?: 0,
                xpEndurance = xpByAttr[AttributeType.ENDURANCE] ?: 0,
                xpMobility = xpByAttr[AttributeType.MOBILITY] ?: 0,
                xpDiscipline = xpByAttr[AttributeType.DISCIPLINE] ?: 0,
                totalXp = total,
                streakMultiplierX100 = (multiplier * 100).roundToInt(),
            )
        )

        workoutDao.insertSets(
            completed.mapIndexed { idx, s ->
                SetEntity(
                    sessionId = sessionId,
                    exerciseId = s.exerciseId,
                    exerciseName = s.exerciseName,
                    dateMillis = now,
                    setIndex = idx,
                    reps = s.reps,
                    weightKg = s.weightKg,
                    timeSeconds = s.timeSeconds,
                )
            }
        )

        val progressed = updatePrs(completed, now)
        streakDao.upsert(streak.copy(current = newStreak, longest = longest, lastWorkoutEpochDay = today))

        // Начисление токенов заморозки за каждые 7 дней серии (с лимитом)
        settingsDao.get()?.let { s ->
            val granted = FreezeRules.grantAfter(s.freezeTokens, newStreak)
            if (granted != s.freezeTokens) settingsDao.upsert(s.copy(freezeTokens = granted))
        }

        val newLevel = Leveling.levelFor(oldTotal + total)
        recordRankUps(oldLevel, newLevel, now)
        val unlocked = evaluateAchievements()

        return WorkoutOutcome(
            xpByAttr = xpByAttr,
            totalXp = total,
            oldLevel = oldLevel,
            newLevel = newLevel,
            newStreak = newStreak,
            streakMultiplier = multiplier,
            unlocked = unlocked,
            progressed = progressed,
        )
    }

    /** Обновляет рекорды и возвращает названия упражнений, где зафиксирован прогресс. */
    private suspend fun updatePrs(sets: List<LoggedSet>, now: Long): List<String> {
        val progressed = mutableListOf<String>()
        val byExercise = sets.groupBy { it.exerciseId }
        for ((exerciseId, group) in byExercise) {
            val maxReps = group.maxOf { it.reps }
            val maxWeight = group.maxOf { it.weightKg }
            val maxTime = group.maxOf { it.timeSeconds }
            val prev = prDao.get(exerciseId)
            val prevBestWeight = prev?.bestWeight ?: 0.0
            val prevBestReps = prev?.bestReps ?: 0
            val prevBestTime = prev?.bestTimeSeconds ?: 0

            val improved = (maxWeight > prevBestWeight && prevBestWeight > 0) ||
                (maxReps > prevBestReps && prevBestReps > 0) ||
                (maxTime > prevBestTime && prevBestTime > 0)
            if (improved) {
                group.first().exerciseName.let { progressed.add(it) }
            }

            prDao.upsert(
                ExercisePrEntity(
                    exerciseId = exerciseId,
                    bestWeight = max(prevBestWeight, maxWeight),
                    bestReps = max(prevBestReps, maxReps),
                    bestTimeSeconds = max(prevBestTime, maxTime),
                    lastWeight = maxWeight,
                    lastReps = maxReps,
                    lastTimeSeconds = maxTime,
                    updatedAtMillis = now,
                )
            )
        }
        return progressed
    }

    // ─────────────────────────── Achievements ───────────────────────────
    private suspend fun evaluateAchievements(): List<AchievementDef> {
        val locked = achievementDao.locked()
        if (locked.isEmpty()) return emptyList()

        val workoutCount = workoutDao.count()
        val bossCount = workoutDao.bossCount()
        val streak = streakDao.get()
        val longest = streak?.longest ?: 0
        val first = measurementDao.first()
        val latest = measurementDao.latest()
        val waistDelta = if (first != null && latest != null) first.waist - latest.waist else 0.0
        val weightDelta = if (first != null && latest != null) first.weightKg - latest.weightKg else 0.0
        val pullupBest = prDao.get("pullup")?.bestReps ?: 0
        val xp = workoutDao.xpTotals()
        val totalAll = xp.s + xp.e + xp.m + xp.d + measurementDao.compositionXp()
        val level = Leveling.levelFor(totalAll)
        val maxSleep = sleepDao.maxHours()
        val now = System.currentTimeMillis()

        val unlocked = mutableListOf<AchievementDef>()
        for (a in locked) {
            val ok = when (a.id) {
                AchievementCatalog.FIRST_WORKOUT -> workoutCount >= 1
                AchievementCatalog.TEN_WORKOUTS -> workoutCount >= 10
                AchievementCatalog.FIFTY_WORKOUTS -> workoutCount >= 50
                AchievementCatalog.STREAK_7 -> longest >= 7
                AchievementCatalog.STREAK_30 -> longest >= 30
                AchievementCatalog.WAIST_MINUS_1 -> waistDelta >= 1.0
                AchievementCatalog.WAIST_MINUS_5 -> waistDelta >= 5.0
                AchievementCatalog.WEIGHT_MINUS_3 -> weightDelta >= 3.0
                AchievementCatalog.PULLUP_10 -> pullupBest >= 10
                AchievementCatalog.SHOULDERS_PRIORITY -> xp.s >= 1500
                AchievementCatalog.BOSS_SLAIN -> bossCount >= 1
                AchievementCatalog.BOSS_3 -> bossCount >= 3
                AchievementCatalog.LEVEL_10 -> level >= 10
                AchievementCatalog.LEVEL_20 -> level >= 20
                AchievementCatalog.LEVEL_35 -> level >= 35
                AchievementCatalog.LEVEL_50 -> level >= 50
                AchievementCatalog.TWENTYFIVE_WORKOUTS -> workoutCount >= 25
                AchievementCatalog.STREAK_14 -> longest >= 14
                AchievementCatalog.PULLUP_15 -> pullupBest >= 15
                AchievementCatalog.ENDURANCE_2000 -> xp.e >= 2000
                AchievementCatalog.MOBILITY_1000 -> xp.m >= 1000
                AchievementCatalog.SLEEP_8 -> maxSleep >= 8.0
                AchievementCatalog.WAIST_MINUS_10 -> waistDelta >= 10.0
                else -> false
            }
            if (ok) {
                achievementDao.update(a.copy(unlocked = true, unlockedAtMillis = now))
                AchievementCatalog.byId[a.id]?.let { unlocked.add(it) }
            }
        }
        return unlocked
    }

    // ─────────────────────────── Water ───────────────────────────
    suspend fun addWater(ml: Int) {
        val day = Dates.todayEpochDay()
        val current = waterDao.forDay(day)?.amountMl ?: 0
        waterDao.upsert(WaterEntity(day, (current + ml).coerceAtLeast(0)))
    }

    suspend fun setWaterToday(ml: Int) {
        waterDao.upsert(WaterEntity(Dates.todayEpochDay(), ml.coerceAtLeast(0)))
    }

    /** Фиксирует переход в новый ранг (для истории рангов). */
    private suspend fun recordRankUps(oldLevel: Int, newLevel: Int, now: Long) {
        if (newLevel <= oldLevel) return
        for (level in (oldLevel + 1)..newLevel) {
            val rank = Rank.forLevel(level)
            if (rank != Rank.forLevel(level - 1)) {
                rankUpDao.insert(RankUpEntity(level = level, rankTitle = rank.title, atMillis = now))
            }
        }
    }

    /** Начисляет бонус-XP за выполненное испытание дня — один раз в день. */
    suspend fun claimDailyChallenge(bonusXp: Int): Boolean {
        val today = Dates.todayEpochDay()
        if (challengeDao.forDay(today) != null) return false
        challengeDao.insert(ChallengeLogEntity(today, bonusXp.coerceAtLeast(0)))
        return true
    }

    // ─────────────────────────── Sleep ───────────────────────────
    suspend fun setSleepToday(hours: Double): List<AchievementDef> {
        sleepDao.upsert(SleepEntity(Dates.todayEpochDay(), hours.coerceIn(0.0, 24.0)))
        return evaluateAchievements()
    }

    // ─────────────────────────── Settings ───────────────────────────
    suspend fun updateSettings(settings: SettingsEntity) = settingsDao.upsert(settings)

    /** Заморозить сегодняшний день: тратит токен, день перестаёт рвать серию. */
    suspend fun freezeToday(): Boolean {
        val s = settingsDao.get() ?: return false
        if (s.freezeTokens <= 0) return false
        frozenDayDao.insert(FrozenDayEntity(Dates.todayEpochDay()))
        settingsDao.upsert(s.copy(freezeTokens = s.freezeTokens - 1))
        return true
    }

    /**
     * Ретроспективно заморозить пропущенные дни [days]. Списывает по токену за каждый
     * НОВЫЙ замороженный день (повторный тот же день не списывает второй токен).
     */
    suspend fun applyRetroFreeze(days: List<Long>): Boolean {
        val s = settingsDao.get() ?: return false
        val alreadyFrozen = frozenDayDao.allDays().toSet()
        val toFreeze = days.filter { it !in alreadyFrozen }.distinct()
        if (toFreeze.isEmpty() || toFreeze.size > s.freezeTokens) return false
        toFreeze.forEach { frozenDayDao.insert(FrozenDayEntity(it)) }
        settingsDao.upsert(s.copy(freezeTokens = s.freezeTokens - toFreeze.size))
        return true
    }

    /** Цель «призрака»: целевые талия/живот (см). 0 = авто. */
    suspend fun setTargetMeasurements(waist: Double, belly: Double) {
        val s = settingsDao.get() ?: SettingsEntity()
        settingsDao.upsert(s.copy(targetWaist = waist.coerceAtLeast(0.0), targetBelly = belly.coerceAtLeast(0.0)))
    }

    // ─────────────────────────── Reset ───────────────────────────
    suspend fun resetProgress() {
        val latest = measurementDao.latest()
        val now = System.currentTimeMillis()
        workoutDao.clearSessions() // каскадно удалит подходы
        measurementDao.clear()
        prDao.clear()
        waterDao.clear()
        sleepDao.clear()
        rankUpDao.clear()
        challengeDao.clear()
        frozenDayDao.clear()
        settingsDao.get()?.let { settingsDao.upsert(it.copy(freezeTokens = 0)) }
        achievementDao.relockAll()
        streakDao.upsert(StreakEntity())
        // Сохраняем текущий замер как новую точку отсчёта
        if (latest != null) {
            measurementDao.insert(
                latest.copy(
                    id = 0,
                    dateEpochDay = Dates.todayEpochDay(),
                    dateMillis = now,
                    xpComposition = 0,
                )
            )
        }
    }

    private suspend fun totalXpAll(): Int {
        val xp = workoutDao.xpTotals()
        return xp.s + xp.e + xp.m + xp.d + measurementDao.compositionXp() + challengeDao.total()
    }

    // ─────────────────────────── Backup ───────────────────────────
    suspend fun exportJson(): String {
        val data = BackupData(
            profile = profileDao.get(),
            measurements = measurementDao.allOnce(),
            sessions = workoutDao.allSessionsOnce(),
            sets = workoutDao.allSetsOnce(),
            prs = prDao.allOnce(),
            achievements = achievementDao.allOnce(),
            streak = streakDao.get(),
            settings = settingsDao.get(),
            water = waterDao.allOnce(),
            sleep = sleepDao.allOnce(),
        )
        return BackupSerializer.export(data)
    }

    suspend fun importJson(json: String) {
        val data = BackupSerializer.parse(json)
        seedIfNeeded()

        // Полностью заменяем прогресс данными из бэкапа
        workoutDao.clearSessions() // каскадно удалит подходы
        measurementDao.clear()
        prDao.clear()
        waterDao.clear()
        sleepDao.clear()
        rankUpDao.clear()
        challengeDao.clear()
        frozenDayDao.clear()

        data.profile?.let { profileDao.upsert(it) }
        data.measurements.forEach { measurementDao.insert(it.copy(id = 0)) }
        // Сессии восстанавливаем с исходными id, чтобы совпали внешние ключи подходов
        data.sessions.forEach { workoutDao.insertSessionRestore(it) }
        if (data.sets.isNotEmpty()) workoutDao.insertSets(data.sets)
        if (data.prs.isNotEmpty()) prDao.upsertAll(data.prs)
        if (data.achievements.isNotEmpty()) achievementDao.upsertAll(data.achievements)
        if (data.water.isNotEmpty()) waterDao.upsertAll(data.water)
        if (data.sleep.isNotEmpty()) sleepDao.upsertAll(data.sleep)
        data.streak?.let { streakDao.upsert(it) }
        data.settings?.let { settingsDao.upsert(it) }
    }
}
