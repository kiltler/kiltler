package com.bodyquest.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bodyquest.app.domain.ExerciseType
import com.bodyquest.app.domain.Rank
import com.bodyquest.app.domain.seed.ExerciseCatalog
import com.bodyquest.app.ui.AppUiState
import com.bodyquest.app.ui.art.RankArt
import com.bodyquest.app.ui.components.BqCard
import com.bodyquest.app.ui.components.SectionTitle
import com.bodyquest.app.ui.components.epochMillisLabel
import com.bodyquest.app.ui.theme.BqSecondary
import com.bodyquest.app.ui.theme.BqTertiary

@Composable
fun HistoryScreen(state: AppUiState) {
    val level = state.character?.overall?.level ?: 1
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Зал славы", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)

        BqCard(Modifier.fillMaxWidth()) {
            SectionTitle("Достигнутые ранги")
            val achieved = Rank.entries.filter { level >= it.minLevel }
            achieved.forEach { rank ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    RankArt(rankIndex = rank.index, modifier = Modifier.size(40.dp).height(48.dp), color = BqTertiary)
                    Column(Modifier.weight(1f)) {
                        Text(rank.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Уровни ${rank.minLevel}+", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    val date = state.rankDates[rank.index]
                    Text(
                        if (date != null) epochMillisLabel(date) else "—",
                        style = MaterialTheme.typography.labelLarge, color = BqTertiary,
                    )
                }
            }
        }

        BqCard(Modifier.fillMaxWidth()) {
            SectionTitle("Личные рекорды")
            val records = state.prs.values
                .filter { it.bestWeight > 0 || it.bestReps > 0 || it.bestTimeSeconds > 0 }
                .sortedBy { ExerciseCatalog.get(it.exerciseId)?.name ?: it.exerciseId }
            if (records.isEmpty()) {
                Text("Рекорды появятся после первых тренировок.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                records.forEach { pr ->
                    val ex = ExerciseCatalog.get(pr.exerciseId)
                    val value = when (ex?.type) {
                        ExerciseType.WEIGHTED_REPS -> "${pr.bestWeight.toInt()} кг × ${pr.bestReps}"
                        ExerciseType.TIMED, ExerciseType.MOBILITY -> "${pr.bestTimeSeconds} сек"
                        else -> "${pr.bestReps} повт."
                    }
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(ex?.name ?: pr.exerciseId, style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f))
                        Text(value, fontWeight = FontWeight.Bold, color = BqSecondary)
                    }
                }
            }
        }
    }
}
