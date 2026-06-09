package com.bodyquest.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bodyquest.app.domain.ExerciseType
import com.bodyquest.app.domain.LoggedSet
import com.bodyquest.app.domain.PlannedExercise
import com.bodyquest.app.domain.WorkoutDay
import com.bodyquest.app.domain.seed.ExerciseCatalog
import com.bodyquest.app.ui.AppUiState
import com.bodyquest.app.ui.openTechniqueVideo
import com.bodyquest.app.ui.components.BqCard
import com.bodyquest.app.ui.components.SectionTitle
import com.bodyquest.app.ui.theme.BqSecondary
import com.bodyquest.app.ui.theme.BqTertiary
import kotlinx.coroutines.delay

private class SetRow(reps: String, weight: String, time: String) {
    var reps by mutableStateOf(reps)
    var weight by mutableStateOf(weight)
    var time by mutableStateOf(time)
}

private fun upperRepBound(target: String): Int =
    Regex("\\d+").findAll(target).map { it.value.toInt() }.maxOrNull() ?: Int.MAX_VALUE

@Composable
fun WorkoutScreen(
    day: WorkoutDay,
    state: AppUiState,
    onFinish: (WorkoutDay, List<LoggedSet>, Int) -> Unit,
    onCancel: () -> Unit,
) {
    val startMillis = remember(day.id) { System.currentTimeMillis() }

    // Локальное состояние подходов по упражнениям
    val sheets = remember(day.id) {
        day.exercises.map { planned ->
            val pr = state.prs[planned.exerciseId]
            val defWeight = pr?.lastWeight?.takeIf { it > 0 }?.let { it.toString() } ?: ""
            val rows = (0 until planned.sets).map { SetRow("", defWeight, "") }.toMutableStateList()
            planned to rows
        }
    }

    // Таймер отдыха
    var restRemaining by remember { mutableIntStateOf(0) }
    LaunchedEffect(restRemaining) {
        if (restRemaining > 0) {
            delay(1000)
            restRemaining -= 1
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(day.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Text(day.focus, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            BqCard(Modifier.fillMaxWidth()) {
                SectionTitle("Разминка")
                day.warmup.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
            }

            sheets.forEach { (planned, rows) ->
                ExerciseBlock(
                    planned = planned,
                    rows = rows,
                    lastTopReps = state.prs[planned.exerciseId]?.lastReps ?: 0,
                    onRest = { restRemaining = planned.restSeconds.coerceAtLeast(0) },
                    onAddSet = { rows.add(SetRow("", rows.lastOrNull()?.weight ?: "", "")) },
                )
            }

            Button(
                onClick = {
                    val logged = mutableListOf<LoggedSet>()
                    sheets.forEach { (planned, rows) ->
                        val ex = ExerciseCatalog.get(planned.exerciseId)
                        val name = ex?.name ?: planned.exerciseId
                        rows.forEach { r ->
                            val reps = r.reps.toIntOrNull() ?: 0
                            val weight = r.weight.toDoubleOrNull() ?: 0.0
                            val time = r.time.toIntOrNull() ?: 0
                            if (reps > 0 || time > 0) {
                                logged.add(LoggedSet(planned.exerciseId, name, reps, weight, time))
                            }
                        }
                    }
                    val duration = ((System.currentTimeMillis() - startMillis) / 1000).toInt()
                    onFinish(day, logged, duration)
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Завершить тренировку", fontWeight = FontWeight.Bold) }

            TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Отмена") }
        }

        if (restRemaining > 0) {
            Surface(
                color = BqTertiary,
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp),
                shape = MaterialTheme.shapes.large,
            ) {
                Row(
                    Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("⏱️ Отдых: $restRemaining сек", color = MaterialTheme.colorScheme.onTertiary,
                        fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge)
                    TextButton(onClick = { restRemaining = 0 }) { Text("Пропустить") }
                }
            }
        }
    }
}

@Composable
private fun ExerciseBlock(
    planned: PlannedExercise,
    rows: SnapshotStateList<SetRow>,
    lastTopReps: Int,
    onRest: () -> Unit,
    onAddSet: () -> Unit,
) {
    val ex = ExerciseCatalog.get(planned.exerciseId)
    val context = LocalContext.current
    BqCard(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text(ex?.name ?: planned.exerciseId, style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = { openTechniqueVideo(context, ex?.name ?: planned.exerciseId) }) {
                Icon(Icons.Filled.PlayCircle, contentDescription = "Видео техники", tint = BqSecondary)
            }
        }
        Text("${planned.sets} × ${planned.targetReps}", style = MaterialTheme.typography.bodyMedium, color = BqSecondary)
        if (planned.note.isNotBlank()) {
            Text(planned.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (lastTopReps in 1 until Int.MAX_VALUE && lastTopReps >= upperRepBound(planned.targetReps)) {
            Text("⬆️ В прошлый раз — $lastTopReps повт. Готов к прокачке: добавь вес или повтор!",
                style = MaterialTheme.typography.labelLarge, color = BqTertiary, modifier = Modifier.padding(top = 4.dp))
        }

        val type = ex?.type ?: ExerciseType.BODYWEIGHT_REPS
        Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            rows.forEachIndexed { i, row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("#${i + 1}", modifier = Modifier.padding(end = 2.dp))
                    when (type) {
                        ExerciseType.WEIGHTED_REPS -> {
                            NumberField("Повт.", row.reps, { row.reps = it }, Modifier.weight(1f))
                            NumberField("Вес, кг", row.weight, { row.weight = it }, Modifier.weight(1f))
                        }
                        ExerciseType.BODYWEIGHT_REPS -> {
                            NumberField("Повт.", row.reps, { row.reps = it }, Modifier.weight(1f))
                        }
                        ExerciseType.TIMED, ExerciseType.MOBILITY -> {
                            NumberField("Время, сек", row.time, { row.time = it }, Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onAddSet, modifier = Modifier.weight(1f)) { Text("+ Подход") }
            if (planned.restSeconds > 0) {
                OutlinedButton(onClick = onRest, modifier = Modifier.weight(1f)) {
                    Text("Отдых ${planned.restSeconds}с")
                }
            }
        }
    }
}
