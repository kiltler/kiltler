package com.bodyquest.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val BqColorScheme = darkColorScheme(
    primary = BqPrimary,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = BqPrimaryDark,
    onPrimaryContainer = BqOnSurface,
    secondary = BqSecondary,
    onSecondary = BqBackground,
    tertiary = BqTertiary,
    onTertiary = BqBackground,
    background = BqBackground,
    onBackground = BqOnSurface,
    surface = BqSurface,
    onSurface = BqOnSurface,
    surfaceVariant = BqSurfaceVariant,
    onSurfaceVariant = BqOnSurfaceMuted,
    outline = BqOutline,
    error = BqDanger,
)

private val BqTypography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.Black, fontSize = 34.sp, letterSpacing = 0.5.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 26.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    bodyLarge = TextStyle(fontSize = 16.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 0.5.sp),
)

@Composable
fun BodyQuestTheme(content: @Composable () -> Unit) {
    // Приложение всегда в тёмной игровой теме
    MaterialTheme(
        colorScheme = BqColorScheme,
        typography = BqTypography,
        content = content
    )
}
