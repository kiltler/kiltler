package com.bodyquest.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bodyquest.app.ui.AchievementUi
import com.bodyquest.app.ui.AppUiState
import com.bodyquest.app.ui.theme.BqOutline
import com.bodyquest.app.ui.theme.BqSurface
import com.bodyquest.app.ui.theme.BqTertiary

@Composable
fun AchievementsScreen(state: AppUiState) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Зал достижений", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Text("Открыто ${state.unlockedCount} из ${state.achievements.size}",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(state.achievements) { Badge(it) }
        }
    }
}

@Composable
private fun Badge(a: AchievementUi) {
    val unlocked = a.unlocked
    Surface(
        color = if (unlocked) BqTertiary.copy(alpha = 0.16f) else BqSurface,
        shape = RoundedCornerShape(18.dp),
        border = if (unlocked) androidx.compose.foundation.BorderStroke(1.dp, BqTertiary)
        else androidx.compose.foundation.BorderStroke(1.dp, BqOutline),
        modifier = Modifier.fillMaxWidth().aspectRatio(1f),
    ) {
        Box(Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (unlocked) a.def.emoji else "🔒",
                    style = MaterialTheme.typography.headlineLarge,
                )
                Text(
                    a.def.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = if (unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )
                Text(
                    a.def.description,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}
