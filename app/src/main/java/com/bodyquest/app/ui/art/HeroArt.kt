package com.bodyquest.app.ui.art

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.util.lerp

/**
 * Силуэт героя, который меняется с рангом: от пухлого новобранца (t=0)
 * до горы мышц с V-силуэтом (t=1). Полностью на Canvas, без картинок извне.
 */
fun DrawScope.drawHero(color: Color, t: Float, accent: Color) {
    val w = size.width
    val h = size.height
    val cx = w / 2f

    val shoulderHalf = w * lerp(0.16f, 0.32f, t)
    val waistHalf = w * lerp(0.19f, 0.11f, t)
    val hipHalf = w * lerp(0.16f, 0.13f, t)
    val armW = h * lerp(0.055f, 0.10f, t)
    val legW = h * lerp(0.065f, 0.095f, t)
    val neckW = h * lerp(0.05f, 0.085f, t)

    val headR = h * 0.085f
    val headY = h * 0.15f
    val shoulderY = h * 0.30f
    val waistY = h * 0.52f
    val hipY = h * 0.60f
    val footY = h * 0.95f

    // Шея
    drawLine(color, Offset(cx, headY + headR), Offset(cx, shoulderY),
        strokeWidth = neckW, cap = StrokeCap.Round)

    // Торс (трапеция плечи → талия → бёдра)
    val torso = Path().apply {
        moveTo(cx - shoulderHalf, shoulderY)
        lineTo(cx + shoulderHalf, shoulderY)
        lineTo(cx + waistHalf, waistY)
        lineTo(cx + hipHalf, hipY)
        lineTo(cx - hipHalf, hipY)
        lineTo(cx - waistHalf, waistY)
        close()
    }
    drawPath(torso, color)

    // Живот для низких рангов
    if (t < 0.55f) {
        val bulge = (0.55f - t) / 0.55f
        drawOval(
            color = color,
            topLeft = Offset(cx - waistHalf * 1.02f, waistY - h * 0.02f),
            size = androidx.compose.ui.geometry.Size(waistHalf * 2.04f, (hipY - waistY) + h * 0.12f * bulge),
        )
    }

    // Плечи-«дельты» — кружки, заметнее на высоких рангах
    val deltaR = lerp(armW * 0.5f, armW * 0.85f, t)
    drawCircle(color, deltaR, Offset(cx - shoulderHalf, shoulderY))
    drawCircle(color, deltaR, Offset(cx + shoulderHalf, shoulderY))

    // Руки
    drawLine(color, Offset(cx - shoulderHalf, shoulderY), Offset(cx - shoulderHalf - w * 0.04f, hipY),
        strokeWidth = armW, cap = StrokeCap.Round)
    drawLine(color, Offset(cx + shoulderHalf, shoulderY), Offset(cx + shoulderHalf + w * 0.04f, hipY),
        strokeWidth = armW, cap = StrokeCap.Round)

    // Ноги
    drawLine(color, Offset(cx - hipHalf * 0.6f, hipY), Offset(cx - hipHalf * 0.7f, footY),
        strokeWidth = legW, cap = StrokeCap.Round)
    drawLine(color, Offset(cx + hipHalf * 0.6f, hipY), Offset(cx + hipHalf * 0.7f, footY),
        strokeWidth = legW, cap = StrokeCap.Round)

    // Голова
    drawCircle(color, headR, Offset(cx, headY))

    // Корона/блик для легенды (t≈1)
    if (t > 0.85f) {
        val cap = Path().apply {
            moveTo(cx - headR, headY - headR * 0.6f)
            lineTo(cx - headR * 0.4f, headY - headR * 1.5f)
            lineTo(cx, headY - headR * 0.7f)
            lineTo(cx + headR * 0.4f, headY - headR * 1.5f)
            lineTo(cx + headR, headY - headR * 0.6f)
            close()
        }
        drawPath(cap, accent)
    }
}

@Composable
fun RankFigure(
    rankIndex: Int,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFE6E9F2),
    accent: Color = Color(0xFFFFB454),
) {
    val maxIndex = 5f
    val t = (rankIndex / maxIndex).coerceIn(0f, 1f)
    Canvas(modifier) { drawHero(color, t, accent) }
}
