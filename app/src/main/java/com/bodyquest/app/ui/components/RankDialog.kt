package com.bodyquest.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bodyquest.app.domain.Rank
import com.bodyquest.app.ui.art.RankArt
import com.bodyquest.app.ui.theme.BqOutline
import com.bodyquest.app.ui.theme.BqSurfaceVariant
import com.bodyquest.app.ui.theme.BqTertiary

@Composable
fun RankSystemDialog(currentRank: Rank, currentLevel: Int, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } },
        title = { Text("Система рангов", fontWeight = FontWeight.Black) },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Rank.entries.forEach { rank ->
                    val isCurrent = rank == currentRank
                    Surface(
                        color = if (isCurrent) BqTertiary.copy(alpha = 0.16f) else BqSurfaceVariant,
                        shape = RoundedCornerShape(14.dp),
                        border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.dp, BqTertiary)
                        else androidx.compose.foundation.BorderStroke(1.dp, BqOutline),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            RankArt(
                                rankIndex = rank.index,
                                modifier = Modifier.size(54.dp).height(64.dp),
                                color = if (isCurrent) BqTertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        rank.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrent) BqTertiary else MaterialTheme.colorScheme.onSurface,
                                    )
                                    if (isCurrent) {
                                        Text("  • ты здесь", style = MaterialTheme.typography.labelSmall, color = BqTertiary)
                                    } else if (currentLevel >= rank.minLevel) {
                                        Text("  ✓ пройден", style = MaterialTheme.typography.labelSmall, color = BqTertiary)
                                    }
                                }
                                Text("Уровни ${rank.minLevel}+", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(rank.flavor, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        },
    )
}
