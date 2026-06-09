package com.bodyquest.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bodyquest.app.ui.theme.BqOutline
import com.bodyquest.app.ui.theme.BqPrimary
import com.bodyquest.app.ui.theme.BqSurface

@Composable
fun BqCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        color = BqSurface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 2.dp,
    ) {
        Column(Modifier.padding(16.dp)) { content() }
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(start = 4.dp, bottom = 8.dp, top = 4.dp),
    )
}

/** Анимированная полоса прогресса XP. */
@Composable
fun XpBar(
    fraction: Float,
    modifier: Modifier = Modifier,
    color: Color = BqPrimary,
    height: Dp = 12.dp,
    track: Color = BqOutline,
) {
    val animated by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = tween(700),
        label = "xpbar",
    )
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height))
            .background(track),
    ) {
        Box(
            Modifier
                .fillMaxWidth(animated)
                .height(height)
                .clip(RoundedCornerShape(height))
                .background(
                    Brush.horizontalGradient(
                        listOf(color.copy(alpha = 0.85f), color),
                    )
                ),
        )
    }
}

@Composable
fun StatPill(label: String, value: String, modifier: Modifier = Modifier, accent: Color = BqPrimary) {
    Surface(
        modifier = modifier,
        color = accent.copy(alpha = 0.14f),
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Black,
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun KeyValueRow(key: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(key, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}
