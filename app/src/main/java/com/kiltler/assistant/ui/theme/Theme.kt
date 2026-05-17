package com.kiltler.assistant.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Brand = Color(0xFF0A6CCC)
private val BrandDark = Color(0xFF1F82E0)

private val LightColors = lightColorScheme(
    primary = Brand,
    onPrimary = Color.White,
    secondary = Color(0xFF2E9E5B),
    background = Color(0xFFF4F7FB),
    surface = Color.White,
    surfaceVariant = Color(0xFFE9EEF5)
)

private val DarkColors = darkColorScheme(
    primary = BrandDark,
    onPrimary = Color.White,
    secondary = Color(0xFF54C887),
    background = Color(0xFF11151A),
    surface = Color(0xFF1A2027)
)

@Composable
fun AssistantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
