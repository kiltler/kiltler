package com.bodyquest.app.ui

import com.bodyquest.app.data.AchievementEntity
import com.bodyquest.app.data.ExercisePrEntity
import com.bodyquest.app.data.MeasurementEntity
import com.bodyquest.app.data.SetEntity
import com.bodyquest.app.data.StreakEntity
import com.bodyquest.app.data.UserProfileEntity
import com.bodyquest.app.domain.AttributeType
import com.bodyquest.app.domain.LevelProgress
import com.bodyquest.app.domain.NutritionPlan
import com.bodyquest.app.domain.Program
import com.bodyquest.app.domain.Rank
import com.bodyquest.app.domain.WorkoutDay
import com.bodyquest.app.domain.seed.AchievementDef

/** Состояние персонажа («лист героя»). */
data class CharacterState(
    val name: String,
    val overall: LevelProgress,
    val rank: Rank,
    val attributes: Map<AttributeType, LevelProgress>,
)

data class AchievementUi(
    val def: AchievementDef,
    val unlocked: Boolean,
    val unlockedAtMillis: Long,
)

/** Полное состояние приложения для экранов. */
data class AppUiState(
    val loading: Boolean = true,
    val onboarded: Boolean = false,
    val profile: UserProfileEntity? = null,
    val character: CharacterState? = null,
    val latest: MeasurementEntity? = null,
    val first: MeasurementEntity? = null,
    val measurements: List<MeasurementEntity> = emptyList(),
    val streak: StreakEntity? = null,
    val program: Program? = null,
    val todayQuest: WorkoutDay? = null,
    val isRestDay: Boolean = false,
    val nutrition: NutritionPlan? = null,
    val waterMl: Int = 0,
    val achievements: List<AchievementUi> = emptyList(),
    val sets: List<SetEntity> = emptyList(),
    val prs: Map<String, ExercisePrEntity> = emptyMap(),
) {
    val unlockedCount: Int get() = achievements.count { it.unlocked }
}
