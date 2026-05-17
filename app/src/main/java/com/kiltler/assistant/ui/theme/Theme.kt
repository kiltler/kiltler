package com.kiltler.assistant.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val Brand = Color(0xFF0A6CCC)

private val LightColors = lightColorScheme(
    primary = Brand,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE9FB),
    onPrimaryContainer = Color(0xFF06376B),
    secondary = Color(0xFF2E9E5B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCDEBD8),
    onSecondaryContainer = Color(0xFF11512E),
    background = Color(0xFFF4F7FB),
    onBackground = Color(0xFF1A1C1E),
    surface = Color.White,
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE9EEF5),
    onSurfaceVariant = Color(0xFF5A6470),
    outline = Color(0xFFC2CAD4)
)

/** Скруглённые формы для карточек, диалогов и кнопок. */
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/** Светлая тема приложения (всегда белая, независимо от системной темы). */
@Composable
fun AssistantTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        shapes = AppShapes,
        content = content
    )
}

