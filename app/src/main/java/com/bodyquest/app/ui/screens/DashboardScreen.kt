package com.bodyquest.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.bodyquest.app.domain.AttributeType
import com.bodyquest.app.ui.AppUiState
import com.bodyquest.app.ui.CharacterState
import com.bodyquest.app.ui.components.BqCard
import com.bodyquest.app.ui.components.RadarChart
import com.bodyquest.app.ui.components.RankSystemDialog
import com.bodyquest.app.ui.components.SectionTitle
import com.bodyquest.app.ui.components.StatPill
import com.bodyquest.app.ui.components.XpBar
import com.bodyquest.app.ui.theme.BqSecondary
import com.bodyquest.app.ui.theme.BqTertiary

@Composable
fun CharacterCard(character: CharacterState) {
    var showRanks by remember { mutableStateOf(false) }
    if (showRanks) RankSystemDialog(currentRank = character.rank, onDismiss = { showRanks = false })

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
        val maxCont = character.attributes.values.maxOf { it.level + it.fraction }.coerceAtLeast(1f)
        val values = character.attributes.mapValues { (it.value.level + it.value.fraction) / maxCont }
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
) {
    val character = state.character ?: return
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CharacterCard(character)

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
                            Text(quest.portrait, style = MaterialTheme.typography.headlineLarge,
                                modifier = Modifier.padding(end = 8.dp))
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
    }
}
