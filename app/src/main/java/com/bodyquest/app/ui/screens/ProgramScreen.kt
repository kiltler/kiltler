package com.bodyquest.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bodyquest.app.domain.WorkoutDay
import com.bodyquest.app.domain.seed.ExerciseCatalog
import com.bodyquest.app.ui.AppUiState
import com.bodyquest.app.ui.components.BqCard
import com.bodyquest.app.ui.components.SectionTitle
import com.bodyquest.app.ui.theme.BqTertiary

@Composable
fun ProgramScreen(state: AppUiState, onStartQuest: (String) -> Unit) {
    val program = state.program ?: return
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(program.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Text("${program.daysPerWeek} тренировки в неделю + Босс",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        SectionTitle("Недельный план")
        program.days.forEach { day -> DayCard(day, onStartQuest) }

        SectionTitle("Босс недели")
        DayCard(program.boss, onStartQuest, accentBoss = true)

        SectionTitle("Принципы")
        BqCard(Modifier.fillMaxWidth()) {
            program.notes.forEach {
                Text("• $it", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 3.dp))
            }
        }
    }
}

@Composable
private fun DayCard(day: WorkoutDay, onStart: (String) -> Unit, accentBoss: Boolean = false) {
    BqCard(Modifier.fillMaxWidth()) {
        Text(day.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
            color = if (accentBoss) BqTertiary else MaterialTheme.colorScheme.onSurface)
        Text(day.focus, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(Modifier.padding(top = 8.dp)) {
            day.exercises.forEach { p ->
                val name = ExerciseCatalog.get(p.exerciseId)?.name ?: p.exerciseId
                Text("• $name — ${p.sets}×${p.targetReps}", style = MaterialTheme.typography.bodyMedium)
            }
        }
        Button(onClick = { onStart(day.id) }, modifier = Modifier.padding(top = 10.dp).fillMaxWidth()) {
            Text(if (accentBoss) "Бросить вызов Боссу" else "Начать", fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}
