package com.bodyquest.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bodyquest.app.domain.Analytics
import com.bodyquest.app.domain.AttributeType
import com.bodyquest.app.domain.AvatarMapper
import com.bodyquest.app.domain.ModifierEngine
import com.bodyquest.app.domain.WeakLinkAnalyzer
import com.bodyquest.app.domain.seed.ChallengeCatalog
import com.bodyquest.app.domain.seed.ChallengeKind
import com.bodyquest.app.domain.seed.ExerciseCatalog
import com.bodyquest.app.domain.seed.QuickWorkout
import com.bodyquest.app.ui.AppUiState
import com.bodyquest.app.ui.CharacterState
import com.bodyquest.app.ui.art.AssetImageOr
import com.bodyquest.app.ui.art.LivingAvatar
import com.bodyquest.app.ui.components.BqCard
import com.bodyquest.app.ui.theme.BqPrimary
import com.bodyquest.app.ui.theme.BqSurfaceVariant
import com.bodyquest.app.ui.components.RadarChart
import com.bodyquest.app.ui.components.RankSystemDialog
import com.bodyquest.app.ui.components.SectionTitle
import com.bodyquest.app.ui.components.StatPill
import com.bodyquest.app.ui.components.XpBar
import com.bodyquest.app.ui.theme.BqSecondary
import com.bodyquest.app.ui.theme.BqTertiary

@Composable
fun CharacterCard(character: CharacterState, rankDates: Map<Int, Long>) {
    var showRanks by remember { mutableStateOf(false) }
    if (showRanks) RankSystemDialog(
        currentRank = character.rank,
        currentLevel = character.overall.level,
        achievedDates = rankDates,
        onDismiss = { showRanks = false },
    )

    BqCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().clickable { showRanks = true },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column {
                Text(character.rank.title.uppercase() + "  ›", color = BqTertiary, fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium)
                Text(character.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            }
            Box(contentAlignment = Alignment.Center) {
                Text("ур. ${character.overall.level}", style = MaterialTheme.typography.titleLarge,
                    color = BqTertiary, fontWeight = FontWeight.Black)
            }
        }

        Column(Modifier.padding(top = 8.dp)) {
            XpBar(character.overall.fraction, color = BqTertiary)
            Text(
                "${character.overall.xpIntoLevel} / ${character.overall.xpForNext} XP до следующего уровня",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        // Радар
        val levels = character.attributes.mapValues { it.value.level }
        // Знаменатель с «полом», чтобы на низких уровнях радар был маленьким,
        // а не полным пятиугольником уже на 1 уровне.
        val maxCont = character.attributes.values.maxOf { it.level + it.fraction }
        val denom = maxCont.coerceAtLeast(6f)
        val values = character.attributes.mapValues { (it.value.level + it.value.fraction) / denom }
        RadarChart(values = values, levels = levels, modifier = Modifier.padding(top = 12.dp),
            rankIndex = character.rank.index)

        // Полосы характеристик
        Column(Modifier.padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AttributeType.entries.forEach { attr ->
                val lp = character.attributes[attr] ?: return@forEach
                Column {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${attr.emoji} ${attr.title}", style = MaterialTheme.typography.bodyMedium)
                        Text("ур. ${lp.level}", style = MaterialTheme.typography.labelLarge,
                            color = Color(attr.colorArgb))
                    }
                    XpBar(lp.fraction, color = Color(attr.colorArgb), height = 8.dp)
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(
    state: AppUiState,
    onStartQuest: (String) -> Unit,
    onOpenAchievements: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenCodex: () -> Unit,
    onClaimChallenge: (Int) -> Unit,
    onFreezeDay: () -> Unit,
) {
    val character = state.character ?: return
    val today = java.time.LocalDate.now().toEpochDay()
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CharacterCard(character, state.rankDates)

        // Модификатор дня
        val mod = ModifierEngine.forDay(today)
        Surface(color = BqPrimary.copy(alpha = 0.14f), shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()) {
            Text("${mod.emoji}  ${mod.title}", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(12.dp))
        }

        // Живой аватар по реальным замерам + призрак цели
        val base = state.first
        val cur = state.latest
        if (base != null && cur != null) {
            val shape = AvatarMapper.shape(
                base.shoulders, base.waist, base.belly, base.weightKg,
                cur.shoulders, cur.waist, cur.belly, cur.weightKg,
            )
            val ghost = AvatarMapper.target(
                base.shoulders, base.waist, base.belly, base.weightKg,
                cur.shoulders, cur.weightKg, state.targetWaist, state.targetBelly,
            )
            val vRatio = if (cur.waist > 0) cur.shoulders / cur.waist else 0.0
            val goalWaist = if (state.targetWaist > 0) state.targetWaist else base.waist * 0.88
            SectionTitle("Твой силуэт")
            BqCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LivingAvatar(shape = shape, ghost = ghost,
                        modifier = Modifier.size(110.dp).height(150.dp))
                    Column(Modifier.padding(start = 12.dp).weight(1f)) {
                        Text("V-силуэт (плечи/талия)", style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${(vRatio * 100).toInt()}%", style = MaterialTheme.typography.headlineMedium,
                            color = BqTertiary, fontWeight = FontWeight.Black)
                        val toGoal = cur.waist - goalWaist
                        Text(
                            if (toGoal > 0.5) "До цели по талии: −${toGoal.toInt()} см"
                            else "Цель по талии достигнута! 🎯",
                            style = MaterialTheme.typography.labelLarge, color = BqSecondary,
                        )
                        Text("Полупрозрачный силуэт — цель.", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Наджадж по слабому звену
        run {
            var push = 0.0; var pull = 0.0
            state.sets.forEach { s ->
                val ex = ExerciseCatalog.get(s.exerciseId) ?: return@forEach
                val vol = s.reps.toDouble() * maxOf(s.weightKg, 1.0) + s.timeSeconds
                if (WeakLinkAnalyzer.isPush(ex.muscles)) push += vol
                if (WeakLinkAnalyzer.isPull(ex.muscles)) pull += vol
            }
            if (state.attributeXp.isNotEmpty()) {
                val weak = WeakLinkAnalyzer.analyze(state.attributeXp, push, pull)
                Surface(color = BqSurfaceVariant, shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()) {
                    Text("🎯  ${weak.advice}", style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp))
                }
            }
        }

        // Квест дня
        SectionTitle("Квест дня")
        BqCard(Modifier.fillMaxWidth()) {
            val quest = state.todayQuest
            if (quest == null) {
                Text("День отдыха 🌙", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Восстановление — часть прогресса. Можно сделать лёгкую мобильность.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedButton(onClick = { onStartQuest("mobility_only") }, modifier = Modifier.padding(top = 8.dp).fillMaxWidth()) {
                    Text("Лёгкая мобильность")
                }
            } else {
                if (quest.isBoss) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (quest.portrait.isNotBlank()) {
                            AssetImageOr(
                                name = quest.id,
                                modifier = Modifier.size(44.dp).padding(end = 8.dp),
                            ) {
                                Text(quest.portrait, style = MaterialTheme.typography.headlineLarge,
                                    modifier = Modifier.padding(end = 8.dp))
                            }
                        }
                        Text("👑 БОСС НЕДЕЛИ", color = BqTertiary, fontWeight = FontWeight.Black)
                    }
                }
                Text(quest.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(quest.focus, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                Text("${quest.exercises.size} упражнений", style = MaterialTheme.typography.labelLarge,
                    color = BqSecondary, modifier = Modifier.padding(top = 6.dp))
                Button(onClick = { onStartQuest(quest.id) }, modifier = Modifier.padding(top = 10.dp).fillMaxWidth()) {
                    Text("Начать тренировку", fontWeight = FontWeight.Bold)
                }
            }
            OutlinedButton(
                onClick = { onStartQuest(QuickWorkout.ID) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) { Text("⏱️ Мало времени (≈10 мин)") }
        }

        // Заморозка серии
        Surface(color = BqSurfaceVariant, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("❄️ Заморозки серии: ${state.freezeTokens}/3", style = MaterialTheme.typography.bodyMedium)
                OutlinedButton(onClick = onFreezeDay, enabled = state.freezeTokens > 0) {
                    Text("Заморозить день")
                }
            }
        }

        // Недельная сводка + deload
        run {
            val today = java.time.LocalDate.now().toEpochDay()
            val done = Analytics.sessionsThisWeek(state.sessions.map { it.dateEpochDay }, today)
            val goal = (state.profile?.daysPerWeek ?: 3).coerceAtLeast(1)
            val startDay = state.first?.dateEpochDay ?: today
            val weeks = Analytics.weeksSince(startDay, today)
            val deload = Analytics.isDeloadWeek(weeks)
            SectionTitle("Неделя ${weeks + 1}")
            BqCard(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Тренировки недели", style = MaterialTheme.typography.bodyMedium)
                    Text("$done / $goal", fontWeight = FontWeight.Black, color = BqSecondary)
                }
                XpBar((done.toFloat() / goal).coerceIn(0f, 1f), color = BqSecondary,
                    modifier = Modifier.padding(top = 8.dp))
                when {
                    deload -> Text("🪶 Неделя разгрузки: снизь объём на ~40%, веса полегче — это часть прогресса.",
                        style = MaterialTheme.typography.bodySmall, color = BqTertiary,
                        modifier = Modifier.padding(top = 8.dp))
                    done >= goal -> Text("✅ Недельная цель выполнена! Самое время на Босса.",
                        style = MaterialTheme.typography.bodySmall, color = BqSecondary,
                        modifier = Modifier.padding(top = 8.dp))
                    else -> Text("Осталось ${goal - done} до недельной цели.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp))
                }
            }
        }

        // Испытание дня
        run {
            val today = java.time.LocalDate.now().toEpochDay()
            val ch = ChallengeCatalog.forDay(today)
            val done = when (ch.kind) {
                ChallengeKind.WORKOUT_TODAY -> state.sessions.any { it.dateEpochDay == today }
                ChallengeKind.WATER_GOAL -> state.waterMl >= (state.nutrition?.waterMlGoal ?: Int.MAX_VALUE)
                ChallengeKind.LOG_MEASUREMENT -> state.latest?.dateEpochDay == today
                ChallengeKind.SLEEP_8H -> state.sleepHours >= 8.0
                ChallengeKind.BEAT_BOSS -> state.sessions.any { it.dateEpochDay == today && it.isBoss }
            }
            val bonus = 25
            SectionTitle("Испытание дня")
            BqCard(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("${ch.emoji}  ${ch.title}", style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f))
                    Text(
                        "+$bonus XP",
                        style = MaterialTheme.typography.labelLarge,
                        color = BqTertiary,
                        fontWeight = FontWeight.Bold,
                    )
                }
                when {
                    state.challengeClaimedToday -> Text("🏆 Награда получена",
                        style = MaterialTheme.typography.labelLarge, color = BqSecondary,
                        modifier = Modifier.padding(top = 8.dp))
                    done -> Button(onClick = { onClaimChallenge(bonus) },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Text("Получить награду +$bonus XP", fontWeight = FontWeight.Bold)
                    }
                    else -> Text("В процессе…", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }

        // Быстрые показатели
        SectionTitle("Сводка")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatPill("Серия", "${state.streak?.current ?: 0} дн.", Modifier.weight(1f), BqTertiary)
            StatPill("Вес", "${(state.latest?.weightKg ?: 0.0)} кг", Modifier.weight(1f), BqSecondary)
            StatPill("Талия", "${(state.latest?.waist ?: 0.0)} см", Modifier.weight(1f))
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatPill("Достижения", "${state.unlockedCount}/${state.achievements.size}", Modifier.weight(1f), BqTertiary)
            val cal = state.nutrition?.targetCalories ?: 0
            StatPill("Энергия дня", "$cal ккал", Modifier.weight(1f), BqSecondary)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onOpenAchievements, modifier = Modifier.weight(1f)) {
                Text("Достижения")
            }
            OutlinedButton(onClick = onOpenLibrary, modifier = Modifier.weight(1f)) {
                Text("Упражнения")
            }
        }
        OutlinedButton(onClick = onOpenCodex, modifier = Modifier.fillMaxWidth()) {
            Text("📚 Кодекс знаний")
        }
    }
}
