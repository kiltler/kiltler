package com.bodyquest.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bodyquest.app.R

// Manrope — основной UI-шрифт (titles/body/labels), веса 400–800.
private val Manrope = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold),
)

// Russo One — дисплейный «игровой» шрифт, только для крупных заголовков.
private val RussoOne = FontFamily(Font(R.font.russo_one))

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
    // Заголовки — Russo One
    headlineLarge = TextStyle(fontFamily = RussoOne, fontSize = 30.sp, lineHeight = 36.sp, letterSpacing = (-0.2).sp),
    headlineMedium = TextStyle(fontFamily = RussoOne, fontSize = 24.sp, lineHeight = 30.sp),
    headlineSmall = TextStyle(fontFamily = RussoOne, fontSize = 20.sp, lineHeight = 26.sp),
    // Остальной UI — Manrope
    titleLarge = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 24.sp),
    titleMedium = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = Manrope, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = Manrope, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = Manrope, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, letterSpacing = 0.3.sp),
    labelMedium = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Medium, fontSize = 12.sp, letterSpacing = 0.3.sp),
    labelSmall = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 0.8.sp),
)

private val BqShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun BodyQuestTheme(content: @Composable () -> Unit) {
    // Приложение всегда в тёмной игровой теме
    MaterialTheme(
        colorScheme = BqColorScheme,
        typography = BqTypography,
        shapes = BqShapes,
        content = content
    )
}
