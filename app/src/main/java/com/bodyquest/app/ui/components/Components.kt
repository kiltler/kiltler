package com.bodyquest.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bodyquest.app.ui.theme.BqHairline
import com.bodyquest.app.ui.theme.BqOutline
import com.bodyquest.app.ui.theme.BqPrimary
import com.bodyquest.app.ui.theme.BqSurfaceElevated

/** Карточка-поверхность: приподнятый фон, тонкая обводка и мягкий верхний блик. */
@Composable
fun BqCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        color = BqSurfaceElevated,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, BqHairline),
        tonalElevation = 3.dp,
        shadowElevation = 2.dp,
    ) {
        Box(
            Modifier.background(
                Brush.verticalGradient(
                    0f to Color.White.copy(alpha = 0.05f),
                    0.45f to Color.Transparent,
                )
            )
        ) {
            Column(Modifier.padding(16.dp)) { content() }
        }
    }
}

/** Заголовок секции в стиле «eyebrow»: акцентный штрих + капс. */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier, accent: Color = BqPrimary) {
    Row(
        modifier = modifier.padding(start = 4.dp, top = 4.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(width = 16.dp, height = 3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accent),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.2.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
        )
    }
}

/** Анимированная полоса прогресса XP с градиентом и верхним бликом. */
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
    val lighter = lerp(color, Color.White, 0.4f)
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
                .background(Brush.horizontalGradient(listOf(color, lighter))),
        ) {
            // мягкий блик сверху
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(height / 2)
                    .clip(RoundedCornerShape(height))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.20f), Color.Transparent),
                        )
                    ),
            )
        }
    }
}

/** Пилюля показателя: акцентная подложка + рамка, значение цветом акцента. */
@Composable
fun StatPill(label: String, value: String, modifier: Modifier = Modifier, accent: Color = BqPrimary) {
    Surface(
        modifier = modifier,
        color = accent.copy(alpha = 0.12f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.30f)),
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                color = accent,
                fontWeight = FontWeight.Black,
            )
            Text(
                label.uppercase(),
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
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(key, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}
