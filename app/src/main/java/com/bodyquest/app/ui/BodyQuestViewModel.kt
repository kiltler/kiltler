package com.bodyquest.app.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bodyquest.app.data.AchievementEntity
import com.bodyquest.app.data.ExercisePrEntity
import com.bodyquest.app.data.MeasurementEntity
import com.bodyquest.app.data.RankUpEntity
import com.bodyquest.app.data.Repository
import com.bodyquest.app.data.SettingsEntity
import com.bodyquest.app.data.SleepEntity
import com.bodyquest.app.data.StreakEntity
import com.bodyquest.app.data.UserProfileEntity
import com.bodyquest.app.data.WaterEntity
import com.bodyquest.app.data.WorkoutSessionEntity
import com.bodyquest.app.data.WorkoutXpTotals
import com.bodyquest.app.domain.ActivityLevel
import com.bodyquest.app.domain.AttributeType
import com.bodyquest.app.domain.Leveling
import com.bodyquest.app.domain.LoggedSet
import com.bodyquest.app.domain.MeasurementOutcome
import com.bodyquest.app.domain.NutritionCalculator
import com.bodyquest.app.domain.Program
import com.bodyquest.app.domain.Rank
import com.bodyquest.app.domain.WorkoutDay
import com.bodyquest.app.domain.WorkoutOutcome
import com.bodyquest.app.domain.seed.AchievementCatalog
import com.bodyquest.app.domain.seed.ProgramSeed
import com.bodyquest.app.notifications.ReminderScheduler
import com.bodyquest.app.share.ShareImage
import com.bodyquest.app.share.ShareSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate

class BodyQuestViewModel(
    private val repo: Repository,
    private val scheduler: ReminderScheduler,
    private val appContext: Context,
) : ViewModel() {

    private data class CoreBundle(
        val profile: UserProfileEntity?,
        val xp: WorkoutXpTotals,
        val compositionXp: Int,
        val challengeXp: Int,
        val rankUps: List<RankUpEntity>,
    )

    private data class BodyBundle(
        val latest: MeasurementEntity?,
        val all: List<MeasurementEntity>,
        val streak: StreakEntity?,
        val sessions: List<WorkoutSessionEntity>,
        val challengeClaimedToday: Boolean,
    )

    private data class ProgressBundle(
        val achievements: List<AchievementEntity>,
        val prs: List<ExercisePrEntity>,
        val water: WaterEntity?,
        val sleep: SleepEntity?,
        val settings: SettingsEntity?,
    )

    private val core = combine(
        repo.profile, repo.xpTotals, repo.compositionXp, repo.challengeXp, repo.rankUps, ::CoreBundle
    )
    private val body = combine(
        repo.latestMeasurement, repo.measurements, repo.streak, repo.sessions,
        repo.challengeClaimedTodayFlow(), ::BodyBundle
    )
    private val progress = combine(
        repo.achievements, repo.prs, repo.waterTodayFlow(), repo.sleepTodayFlow(), repo.settings,
        ::ProgressBundle
    )

    val state: StateFlow<AppUiState> =
        combine(core, body, progress) { c, b, p -> buildState(c, b, p) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppUiState())

    // События для анимаций/диалогов
    private val _workoutOutcome = MutableStateFlow<WorkoutOutcome?>(null)
    val workoutOutcome: StateFlow<WorkoutOutcome?> = _workoutOutcome

    private val _measurementOutcome = MutableStateFlow<MeasurementOutcome?>(null)
    val measurementOutcome: StateFlow<MeasurementOutcome?> = _measurementOutcome

    // Сообщения для тостов (бэкап и т.п.)
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message
    fun clearMessage() { _message.value = null }

    private fun buildState(c: CoreBundle, b: BodyBundle, p: ProgressBundle): AppUiState {
        val profile = c.profile
        val attrXp = mapOf(
            AttributeType.STRENGTH to c.xp.s,
            AttributeType.ENDURANCE to c.xp.e,
            AttributeType.MOBILITY to c.xp.m,
            AttributeType.DISCIPLINE to c.xp.d,
            AttributeType.COMPOSITION to c.compositionXp,
        )
        // Общий XP = сумма характеристик + бонус за испытания (в радар не идёт).
        val totalXp = attrXp.values.sum() + c.challengeXp
        val overall = Leveling.progressFor(totalXp)
        val character = CharacterState(
            name = profile?.name ?: "Герой",
            overall = overall,
            rank = Rank.forLevel(overall.level),
            attributes = attrXp.mapValues { Leveling.progressFor(it.value) },
        )

        // Даты достижения рангов: самый ранний rank-up на каждый ранг.
        val rankDates: Map<Int, Long> = c.rankUps
            .groupBy { Rank.forLevel(it.level).index }
            .mapValues { (_, list) -> list.minOf { it.atMillis } }

        val program: Program = ProgramSeed.programFor(profile?.daysPerWeek ?: 3)
        val (todayQuest, rest) = questForToday(program)

        val weight = b.latest?.weightKg ?: 97.0
        val activity = runCatching { ActivityLevel.valueOf(profile?.activity ?: "LIGHT") }
            .getOrDefault(ActivityLevel.LIGHT)
        val nutrition = NutritionCalculator.calculate(
            weightKg = weight,
            heightCm = profile?.heightCm ?: 189,
            age = profile?.age ?: 30,
            activity = activity,
        )

        val achMap = p.achievements.associateBy { it.id }
        val achievements = AchievementCatalog.all.map { def ->
            val e = achMap[def.id]
            AchievementUi(def, e?.unlocked == true, e?.unlockedAtMillis ?: 0L)
        }

        return AppUiState(
            loading = false,
            onboarded = profile?.onboarded == true,
            profile = profile,
            character = character,
            latest = b.latest,
            first = b.all.firstOrNull(),
            measurements = b.all,
            sessions = b.sessions,
            streak = b.streak,
            program = program,
            todayQuest = todayQuest,
            isRestDay = rest,
            nutrition = nutrition,
            waterMl = p.water?.amountMl ?: 0,
            sleepHours = p.sleep?.hours ?: 0.0,
            achievements = achievements,
            prs = p.prs.associateBy { it.exerciseId },
            attributeXp = attrXp,
            rankDates = rankDates,
            challengeClaimedToday = b.challengeClaimedToday,
            freezeTokens = p.settings?.freezeTokens ?: 0,
            targetWaist = p.settings?.targetWaist ?: 0.0,
            targetBelly = p.settings?.targetBelly ?: 0.0,
        )
    }

    private fun questForToday(program: Program): Pair<WorkoutDay?, Boolean> {
        val dow = LocalDate.now().dayOfWeek
        if (dow == DayOfWeek.SATURDAY) return program.bossForWeek(LocalDate.now().dayOfYear / 7) to false
        val map = if (program.daysPerWeek == 4) {
            mapOf(
                DayOfWeek.MONDAY to 0,
                DayOfWeek.TUESDAY to 1,
                DayOfWeek.THURSDAY to 2,
                DayOfWeek.FRIDAY to 3,
            )
        } else {
            mapOf(
                DayOfWeek.MONDAY to 0,
                DayOfWeek.WEDNESDAY to 1,
                DayOfWeek.FRIDAY to 2,
            )
        }
        val idx = map[dow]
        return if (idx != null && idx < program.days.size) program.days[idx] to false else null to true
    }

    // ─────────────────────────── Actions ───────────────────────────
    fun completeOnboarding(
        name: String, heightCm: Int, age: Int, daysPerWeek: Int, activity: ActivityLevel,
        weightKg: Double, chest: Double, shoulders: Double, belly: Double, waist: Double,
        thigh: Double, hips: Double, inseam: Double, foot: Double,
    ) {
        viewModelScope.launch {
            repo.completeOnboarding(
                name, heightCm, age, daysPerWeek, activity.name,
                weightKg, chest, shoulders, belly, waist, thigh, hips, inseam, foot,
            )
        }
    }

    fun finishWorkout(day: WorkoutDay, logged: List<LoggedSet>, durationSeconds: Int) {
        viewModelScope.launch {
            _workoutOutcome.value = repo.finishWorkout(day, logged, durationSeconds)
        }
    }

    fun clearWorkoutOutcome() { _workoutOutcome.value = null }

    fun logMeasurement(
        weightKg: Double, chest: Double, shoulders: Double, belly: Double, waist: Double,
        thigh: Double, hips: Double, inseam: Double, foot: Double,
    ) {
        viewModelScope.launch {
            _measurementOutcome.value = repo.logMeasurement(
                weightKg, chest, shoulders, belly, waist, thigh, hips, inseam, foot,
            )
        }
    }

    fun clearMeasurementOutcome() { _measurementOutcome.value = null }

    fun updateProfile(profile: UserProfileEntity) {
        viewModelScope.launch { repo.updateProfile(profile) }
    }

    fun addWater(ml: Int) {
        viewModelScope.launch { repo.addWater(ml) }
    }

    fun setWater(ml: Int) {
        viewModelScope.launch { repo.setWaterToday(ml) }
    }

    fun setSleep(hours: Double) {
        viewModelScope.launch { repo.setSleepToday(hours) }
    }

    fun setWorkoutReminder(enabled: Boolean, hour: Int, minute: Int) {
        viewModelScope.launch {
            val s = repo.settings.first() ?: SettingsEntity()
            repo.updateSettings(s.copy(remindersEnabled = enabled, reminderHour = hour, reminderMinute = minute))
            if (enabled) scheduler.scheduleWorkout(hour, minute) else scheduler.cancelWorkout()
        }
    }

    fun setWaterReminder(enabled: Boolean) {
        viewModelScope.launch {
            val s = repo.settings.first() ?: SettingsEntity()
            repo.updateSettings(s.copy(waterRemindersEnabled = enabled))
            scheduler.scheduleWater(enabled)
        }
    }

    val settings: StateFlow<SettingsEntity?> =
        repo.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun resetProgress() {
        viewModelScope.launch { repo.resetProgress() }
    }

    fun deleteSession(id: Long) {
        viewModelScope.launch {
            repo.deleteSession(id)
            _message.value = "Тренировка удалена"
        }
    }

    fun deleteMeasurement(id: Long) {
        viewModelScope.launch {
            _message.value =
                if (repo.deleteMeasurement(id)) "Замер удалён"
                else "Нельзя удалить единственный замер"
        }
    }

    fun claimDailyChallenge(bonusXp: Int) {
        viewModelScope.launch {
            if (repo.claimDailyChallenge(bonusXp)) _message.value = "Испытание выполнено: +$bonusXp XP"
        }
    }

    fun freezeToday() {
        viewModelScope.launch {
            _message.value =
                if (repo.freezeToday()) "День заморожен — серия в безопасности ❄️"
                else "Нет заморозок: тренируйся 7 дней подряд, чтобы заработать"
        }
    }

    fun setTargetMeasurements(waist: Double, belly: Double) {
        viewModelScope.launch { repo.setTargetMeasurements(waist, belly) }
    }

    fun shareProgress() {
        val s = state.value
        val c = s.character ?: return
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    ShareImage.share(
                        appContext,
                        ShareSnapshot(
                            name = c.name,
                            rankTitle = c.rank.title,
                            level = c.overall.level,
                            streak = s.streak?.current ?: 0,
                            weightKg = s.latest?.weightKg ?: 0.0,
                            waistCm = s.latest?.waist ?: 0.0,
                            unlocked = s.unlockedCount,
                            totalAch = s.achievements.size,
                        ),
                    )
                }
            } catch (e: Exception) {
                _message.value = "Не удалось поделиться: ${e.message}"
            }
        }
    }

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            _message.value = try {
                withContext(Dispatchers.IO) {
                    val json = repo.exportJson()
                    appContext.contentResolver.openOutputStream(uri)?.use {
                        it.write(json.toByteArray(Charsets.UTF_8))
                    } ?: error("нет доступа к файлу")
                }
                "Прогресс сохранён в файл ✅"
            } catch (e: Exception) {
                "Ошибка экспорта: ${e.message}"
            }
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            _message.value = try {
                withContext(Dispatchers.IO) {
                    val json = appContext.contentResolver.openInputStream(uri)
                        ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                        ?: error("не удалось прочитать файл")
                    repo.importJson(json)
                }
                "Прогресс восстановлен ✅"
            } catch (e: Exception) {
                "Ошибка импорта: ${e.message}"
            }
        }
    }

    class Factory(
        private val repo: Repository,
        private val scheduler: ReminderScheduler,
        private val appContext: Context,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            BodyQuestViewModel(repo, scheduler, appContext) as T
    }
}
