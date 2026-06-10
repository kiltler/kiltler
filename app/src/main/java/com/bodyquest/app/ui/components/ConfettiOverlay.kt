package com.bodyquest.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import com.bodyquest.app.ui.theme.AttrComposition
import com.bodyquest.app.ui.theme.AttrEndurance
import com.bodyquest.app.ui.theme.AttrStrength
import com.bodyquest.app.ui.theme.BqPrimary
import com.bodyquest.app.ui.theme.BqSecondary
import com.bodyquest.app.ui.theme.BqTertiary
import kotlin.math.sin
import kotlin.random.Random

private class Confetto(
    val xFrac: Float,
    val color: Color,
    val sizePx: Float,
    val drift: Float,
    val rotSpeed: Float,
    val delay: Float,
    val fallScale: Float,
)

/**
 * Кратковременный «салют» из конфетти на весь экран. Не перехватывает нажатия.
 * Сам завершается через ~2.2 c и зовёт [onDone].
 */
@Composable
fun ConfettiOverlay(onDone: () -> Unit) {
    val palette = listOf(BqPrimary, BqSecondary, BqTertiary, AttrStrength, AttrComposition, AttrEndurance)
    val confetti = remember {
        List(90) {
            Confetto(
                xFrac = Random.nextFloat(),
                color = palette[Random.nextInt(palette.size)],
                sizePx = 8f + Random.nextFloat() * 12f,
                drift = (Random.nextFloat() - 0.5f) * 0.35f,
                rotSpeed = (Random.nextFloat() - 0.5f) * 900f,
                delay = Random.nextFloat() * 0.25f,
                fallScale = 0.85f + Random.nextFloat() * 0.4f,
            )
        }
    }
    var t by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        val durationMs = 2200f
        while (true) {
            val now = withFrameNanos { it }
            t = ((now - start) / 1_000_000f / durationMs).coerceIn(0f, 1f)
            if (t >= 1f) break
        }
        onDone()
    }

    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        confetti.forEach { c ->
            val local = ((t - c.delay) / (1f - c.delay)).coerceIn(0f, 1f)
            if (local <= 0f) return@forEach
            val y = -20f + local * (h + 40f) * c.fallScale
            val x = c.xFrac * w + sin(local * 6f) * c.drift * w
            val alpha = if (local > 0.85f) (1f - (local - 0.85f) / 0.15f).coerceIn(0f, 1f) else 1f
            rotate(degrees = local * c.rotSpeed, pivot = Offset(x, y)) {
                drawRect(
                    color = c.color.copy(alpha = alpha),
                    topLeft = Offset(x - c.sizePx / 2f, y - c.sizePx / 2f),
                    size = Size(c.sizePx, c.sizePx * 0.6f),
                )
            }
        }
    }
}
