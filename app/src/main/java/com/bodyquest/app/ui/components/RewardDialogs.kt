package com.bodyquest.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bodyquest.app.domain.AttributeType
import com.bodyquest.app.domain.MeasurementOutcome
import com.bodyquest.app.domain.Rank
import com.bodyquest.app.domain.WorkoutOutcome
import com.bodyquest.app.ui.art.AssetImageOr
import com.bodyquest.app.ui.theme.BqSecondary
import com.bodyquest.app.ui.theme.BqTertiary

@Composable
private fun LevelUpBanner(newLevel: Int) {
    val transition = rememberInfiniteTransition(label = "levelup")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "scale",
    )
    Surface(
        color = BqTertiary.copy(alpha = 0.18f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Золотой взрыв сзади (центр пустой специально), текст — поверх.
            AssetImageOr(name = "fx_levelup", modifier = Modifier.matchParentSize()) {}
            Column(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("⭐ НОВЫЙ УРОВЕНЬ ⭐", color = BqTertiary, fontWeight = FontWeight.Black)
            Text(
                "$newLevel",
                style = MaterialTheme.typography.headlineLarge,
                color = BqTertiary,
                fontWeight = FontWeight.Black,
                modifier = Modifier.scale(scale),
            )
                Text(
                    Rank.forLevel(newLevel).title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
fun WorkoutRewardDialog(outcome: WorkoutOutcome, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = onDismiss) { Text("Принять награду") } },
        title = { Text("⚔️ Квест завершён!", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (outcome.leveledUp) LevelUpBanner(outcome.newLevel)

                Text("+${outcome.totalXp} XP", style = MaterialTheme.typography.headlineMedium,
                    color = BqTertiary, fontWeight = FontWeight.Black)

                if (outcome.streakMultiplier > 1f) {
                    val pct = ((outcome.streakMultiplier - 1f) * 100).toInt()
                    Text("🔥 Серия ${outcome.newStreak} дн. · бонус +$pct%", color = BqSecondary)
                }

                outcome.xpByAttr.forEach { (attr, xp) ->
                    if (xp > 0) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${attr.emoji} ${attr.title}")
                            Text("+$xp XP", color = Color(attr.colorArgb), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                outcome.progressed.distinct().forEach {
                    Text("⬆️ Прокачка упражнения: $it!", color = BqSecondary, fontWeight = FontWeight.SemiBold)
                }

                outcome.unlocked.forEach {
                    Text("🏆 Достижение: ${it.emoji} ${it.name}", color = BqTertiary, fontWeight = FontWeight.SemiBold)
                }
            }
        },
    )
}

@Composable
fun MeasurementRewardDialog(outcome: MeasurementOutcome, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = onDismiss) { Text("Ок") } },
        title = { Text("🛡️ Замеры записаны", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (outcome.leveledUp) LevelUpBanner(outcome.newLevel)
                if (outcome.compositionXp > 0) {
                    Text("+${outcome.compositionXp} XP Композиции",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(AttributeType.COMPOSITION.colorArgb),
                        fontWeight = FontWeight.Black)
                    Text("Прогресс к цели засчитан. Так держать!")
                } else {
                    Text("Данные сохранены. XP начисляется за прогресс к цели (меньше талия/живот/вес).")
                }
                outcome.unlocked.forEach {
                    Text("🏆 Достижение: ${it.emoji} ${it.name}", color = BqTertiary, fontWeight = FontWeight.SemiBold)
                }
            }
        },
    )
}
