package com.bodyquest.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bodyquest.app.data.SetEntity
import com.bodyquest.app.domain.AttributeType
import com.bodyquest.app.ui.AppUiState
import com.bodyquest.app.ui.components.BqCard
import com.bodyquest.app.ui.components.DeletableHistoryRow
import com.bodyquest.app.ui.components.epochDayLabel
import com.bodyquest.app.ui.components.LineSeries
import com.bodyquest.app.ui.components.MultiLineChart
import com.bodyquest.app.ui.components.SectionTitle
import com.bodyquest.app.ui.components.SingleLineChart
import com.bodyquest.app.ui.components.XpBar
import com.bodyquest.app.ui.theme.AttrEndurance
import com.bodyquest.app.ui.theme.AttrStrength
import com.bodyquest.app.ui.theme.BqSecondary
import com.bodyquest.app.ui.theme.BqTertiary

private fun sessionMaxWeight(sets: List<SetEntity>, exerciseId: String): List<Float> =
    sets.filter { it.exerciseId == exerciseId }
        .groupBy { it.dateMillis }
        .toSortedMap()
        .map { (_, g) -> g.maxOf { it.weightKg }.toFloat() }

private fun sessionMaxReps(sets: List<SetEntity>, exerciseId: String): List<Float> =
    sets.filter { it.exerciseId == exerciseId }
        .groupBy { it.dateMillis }
        .toSortedMap()
        .map { (_, g) -> g.maxOf { it.reps }.toFloat() }

@Composable
fun ProgressScreen(state: AppUiState, onDeleteSession: (Long) -> Unit) {
    val m = state.measurements
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Прогресс", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)

        BqCard(Modifier.fillMaxWidth()) {
            SectionTitle("Вес по времени, кг")
            SingleLineChart(points = m.map { it.weightKg.toFloat() }, color = BqSecondary, label = "Вес")
        }

        BqCard(Modifier.fillMaxWidth()) {
            SectionTitle("Обхваты по времени, см")
            MultiLineChart(
                series = listOf(
                    LineSeries("Талия", AttrStrength, m.map { it.waist.toFloat() }),
                    LineSeries("Живот", BqTertiary, m.map { it.belly.toFloat() }),
                    LineSeries("Грудь", AttrEndurance, m.map { it.chest.toFloat() }),
                ),
            )
            Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Legend("Талия", AttrStrength)
                Legend("Живот", BqTertiary)
                Legend("Грудь", AttrEndurance)
            }
        }

        BqCard(Modifier.fillMaxWidth()) {
            SectionTitle("Силовые рекорды")
            val pressW = sessionMaxWeight(state.sets, "kb_press")
            val lateralW = sessionMaxWeight(state.sets, "db_lateral")
            val pullups = sessionMaxReps(state.sets, "pullup")
            val series = buildList {
                if (pressW.size >= 2) add(LineSeries("Жим гири, кг", AttrStrength, pressW))
                if (lateralW.size >= 2) add(LineSeries("Махи, кг", BqTertiary, lateralW))
                if (pullups.size >= 2) add(LineSeries("Подтягивания, повт.", BqSecondary, pullups))
            }
            if (series.isEmpty()) {
                Text("Завершите несколько тренировок, чтобы увидеть рост рекордов.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                MultiLineChart(series = series)
                Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (pressW.size >= 2) Legend("Жим", AttrStrength)
                    if (lateralW.size >= 2) Legend("Махи", BqTertiary)
                    if (pullups.size >= 2) Legend("Подтяг.", BqSecondary)
                }
            }
        }

        BqCard(Modifier.fillMaxWidth()) {
            SectionTitle("Уровни характеристик")
            val character = state.character
            AttributeType.entries.forEach { attr ->
                val lp = character?.attributes?.get(attr)
                Column(Modifier.padding(vertical = 4.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${attr.emoji} ${attr.title}", style = MaterialTheme.typography.bodyMedium)
                        Text("ур. ${lp?.level ?: 1}", color = Color(attr.colorArgb), fontWeight = FontWeight.Bold)
                    }
                    XpBar(lp?.fraction ?: 0f, color = Color(attr.colorArgb), height = 8.dp)
                }
            }
        }

        BqCard(Modifier.fillMaxWidth()) {
            SectionTitle("Журнал тренировок")
            if (state.sessions.isEmpty()) {
                Text("Пока нет завершённых тренировок.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                state.sessions.take(20).forEach { s ->
                    DeletableHistoryRow(
                        title = s.title,
                        subtitle = "${epochDayLabel(s.dateEpochDay)} · +${s.totalXp} XP" +
                            if (s.isBoss) " · 👑 Босс" else "",
                        onDelete = { onDeleteSession(s.id) },
                    )
                }
                Text("Удаление тренировки пересчитывает XP и уровни.",
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
private fun Legend(label: String, color: Color) {
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        androidx.compose.foundation.Canvas(Modifier.padding(end = 2.dp).size(10.dp)) {
            drawCircle(color)
        }
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
