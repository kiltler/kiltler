package com.bodyquest.app.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bodyquest.app.domain.Equipment
import com.bodyquest.app.domain.seed.ExerciseCatalog
import com.bodyquest.app.ui.art.ExercisePoseView
import com.bodyquest.app.ui.components.BqCard
import com.bodyquest.app.ui.components.SectionTitle
import com.bodyquest.app.ui.openTechniqueVideo
import com.bodyquest.app.ui.theme.BqSurfaceVariant

@Composable
fun LibraryScreen() {
    val context = LocalContext.current
    var filter by remember { mutableStateOf<Equipment?>(null) }
    val exercises = remember(filter) {
        filter?.let { ExerciseCatalog.forEquipment(it) } ?: ExerciseCatalog.all
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("Библиотека упражнений", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        SectionTitle("Фильтр по инвентарю")
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = filter == null, onClick = { filter = null }, label = { Text("Все") })
            Equipment.entries
                .filter { it != Equipment.BODYWEIGHT && it != Equipment.SCALE }
                .forEach { eq ->
                    FilterChip(selected = filter == eq, onClick = { filter = eq }, label = { Text(eq.title) })
                }
        }

        exercises.forEach { ex ->
            BqCard(Modifier.fillMaxWidth()) {
                Surface(
                    color = BqSurfaceVariant,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                ) {
                    ExercisePoseView(
                        exerciseId = ex.id,
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        accent = Color(ex.attribute.colorArgb),
                    )
                }
                Text(ex.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp))
                Text("${ex.attribute.emoji} ${ex.attribute.title} · ${ex.muscles.joinToString { it.title }}",
                    style = MaterialTheme.typography.labelMedium, color = Color(ex.attribute.colorArgb))
                Text(ex.instructions, style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 6.dp))
                Column(Modifier.padding(top = 6.dp)) {
                    ex.formTips.forEach {
                        Text("💡 $it", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text("Инвентарь: ${ex.equipment.joinToString { it.title }}",
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp))
                OutlinedButton(
                    onClick = { openTechniqueVideo(context, ex.name) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                ) {
                    Icon(Icons.Filled.PlayCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("  Смотреть технику")
                }
            }
        }
    }
}
