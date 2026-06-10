package com.bodyquest.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bodyquest.app.domain.AchStats
import com.bodyquest.app.domain.AchievementProgress
import com.bodyquest.app.domain.AttributeType
import com.bodyquest.app.ui.AchievementUi
import com.bodyquest.app.ui.AppUiState
import com.bodyquest.app.ui.art.AssetImageOr
import com.bodyquest.app.ui.theme.BqOutline
import com.bodyquest.app.ui.theme.BqSurface
import com.bodyquest.app.ui.theme.BqTertiary

@Composable
fun AchievementsScreen(state: AppUiState) {
    val stats = AchStats(
        workouts = state.sessions.size,
        bossCount = state.sessions.count { it.isBoss },
        longestStreak = state.streak?.longest ?: 0,
        waistDrop = (state.first?.waist ?: 0.0) - (state.latest?.waist ?: 0.0),
        weightDrop = (state.first?.weightKg ?: 0.0) - (state.latest?.weightKg ?: 0.0),
        pullupBest = state.prs["pullup"]?.bestReps ?: 0,
        strengthXp = state.attributeXp[AttributeType.STRENGTH] ?: 0,
        enduranceXp = state.attributeXp[AttributeType.ENDURANCE] ?: 0,
        mobilityXp = state.attributeXp[AttributeType.MOBILITY] ?: 0,
        level = state.character?.overall?.level ?: 1,
    )
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
            items(state.achievements) { a ->
                val progress = if (a.unlocked) null else AchievementProgress.forId(a.def.id, stats)
                Badge(a, progress)
            }
        }
    }
}

@Composable
private fun Badge(a: AchievementUi, progress: Pair<Int, Int>?) {
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
                // Открытые — цветной бейдж; закрытые — тот же бейдж, но приглушённый.
                val badgeMod = Modifier.size(48.dp).then(if (unlocked) Modifier else Modifier.alpha(0.3f))
                AssetImageOr(name = "ach_${a.def.id}", modifier = badgeMod) {
                    Text(if (unlocked) a.def.emoji else "🔒", style = MaterialTheme.typography.headlineLarge)
                }
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
                if (!unlocked && progress != null) {
                    val (cur, target) = progress
                    Text(
                        "${cur.coerceIn(0, target)} / $target",
                        style = MaterialTheme.typography.labelLarge,
                        color = BqTertiary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}
