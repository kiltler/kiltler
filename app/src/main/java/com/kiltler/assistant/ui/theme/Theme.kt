package com.kiltler.assistant.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val Brand = Color(0xFF0A6CCC)
private val BrandDark = Color(0xFF4DA3F0)

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

private val DarkColors = darkColorScheme(
    primary = BrandDark,
    onPrimary = Color(0xFF06243F),
    primaryContainer = Color(0xFF1F4E7E),
    onPrimaryContainer = Color(0xFFDCE9FB),
    secondary = Color(0xFF54C887),
    onSecondary = Color(0xFF06311B),
    secondaryContainer = Color(0xFF1F5638),
    onSecondaryContainer = Color(0xFFCDEBD8),
    background = Color(0xFF11151A),
    onBackground = Color(0xFFE3E6EA),
    surface = Color(0xFF1A2027),
    onSurface = Color(0xFFE3E6EA),
    surfaceVariant = Color(0xFF2A323C),
    onSurfaceVariant = Color(0xFFAEB6C0),
    outline = Color(0xFF3C4650)
)

/** Скруглённые формы для карточек, диалогов и кнопок. */
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun AssistantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        shapes = AppShapes,
        content = content
    )
}
