package com.bodyquest.app.ui.art

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.util.lerp
import com.bodyquest.app.domain.AvatarShape

/** Параметры тела в пикселях. */
private class BodyPx(
    val shoulderHalf: Float,
    val waistHalf: Float,
    val hipHalf: Float,
    val armW: Float,
    val legW: Float,
    val neckW: Float,
    val deltaR: Float,
    val bellyBulge: Float, // 0..1
    val crown: Boolean,
)

/** Единый рендер силуэта (переиспользуется ранговой фигурой и живым аватаром). */
private fun DrawScope.drawBody(p: BodyPx, color: Color, accent: Color) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val headR = h * 0.085f
    val headY = h * 0.15f
    val shoulderY = h * 0.30f
    val waistY = h * 0.52f
    val hipY = h * 0.60f
    val footY = h * 0.95f

    drawLine(color, Offset(cx, headY + headR), Offset(cx, shoulderY), strokeWidth = p.neckW, cap = StrokeCap.Round)

    val torso = Path().apply {
        moveTo(cx - p.shoulderHalf, shoulderY)
        lineTo(cx + p.shoulderHalf, shoulderY)
        lineTo(cx + p.waistHalf, waistY)
        lineTo(cx + p.hipHalf, hipY)
        lineTo(cx - p.hipHalf, hipY)
        lineTo(cx - p.waistHalf, waistY)
        close()
    }
    drawPath(torso, color)

    if (p.bellyBulge > 0.02f) {
        drawOval(
            color = color,
            topLeft = Offset(cx - p.waistHalf * 1.02f, waistY - h * 0.02f),
            size = Size(p.waistHalf * 2.04f, (hipY - waistY) + h * 0.12f * p.bellyBulge),
        )
    }

    drawCircle(color, p.deltaR, Offset(cx - p.shoulderHalf, shoulderY))
    drawCircle(color, p.deltaR, Offset(cx + p.shoulderHalf, shoulderY))

    drawLine(color, Offset(cx - p.shoulderHalf, shoulderY), Offset(cx - p.shoulderHalf - w * 0.04f, hipY),
        strokeWidth = p.armW, cap = StrokeCap.Round)
    drawLine(color, Offset(cx + p.shoulderHalf, shoulderY), Offset(cx + p.shoulderHalf + w * 0.04f, hipY),
        strokeWidth = p.armW, cap = StrokeCap.Round)

    drawLine(color, Offset(cx - p.hipHalf * 0.6f, hipY), Offset(cx - p.hipHalf * 0.7f, footY),
        strokeWidth = p.legW, cap = StrokeCap.Round)
    drawLine(color, Offset(cx + p.hipHalf * 0.6f, hipY), Offset(cx + p.hipHalf * 0.7f, footY),
        strokeWidth = p.legW, cap = StrokeCap.Round)

    drawCircle(color, headR, Offset(cx, headY))

    if (p.crown) {
        val crown = Path().apply {
            moveTo(cx - headR, headY - headR * 0.6f)
            lineTo(cx - headR * 0.4f, headY - headR * 1.5f)
            lineTo(cx, headY - headR * 0.7f)
            lineTo(cx + headR * 0.4f, headY - headR * 1.5f)
            lineTo(cx + headR, headY - headR * 0.6f)
            close()
        }
        drawPath(crown, accent)
    }
}

private fun DrawScope.bodyFromT(t: Float): BodyPx {
    val w = size.width; val h = size.height
    val armW = h * lerp(0.055f, 0.10f, t)
    return BodyPx(
        shoulderHalf = w * lerp(0.16f, 0.32f, t),
        waistHalf = w * lerp(0.19f, 0.11f, t),
        hipHalf = w * lerp(0.16f, 0.13f, t),
        armW = armW,
        legW = h * lerp(0.065f, 0.095f, t),
        neckW = h * lerp(0.05f, 0.085f, t),
        deltaR = lerp(armW * 0.5f, armW * 0.85f, t),
        bellyBulge = if (t < 0.55f) (0.55f - t) / 0.55f else 0f,
        crown = t > 0.85f,
    )
}

private fun DrawScope.bodyFromShape(s: AvatarShape): BodyPx {
    val w = size.width; val h = size.height
    val armW = h * lerp(0.05f, 0.10f, s.shoulder * 0.6f + s.mass * 0.4f)
    return BodyPx(
        shoulderHalf = w * lerp(0.16f, 0.34f, s.shoulder),
        waistHalf = w * lerp(0.20f, 0.10f, s.waist),
        hipHalf = w * lerp(0.17f, 0.13f, s.shoulder),
        armW = armW,
        legW = h * lerp(0.06f, 0.095f, s.mass),
        neckW = h * lerp(0.05f, 0.085f, s.shoulder),
        deltaR = lerp(armW * 0.5f, armW * 0.9f, s.shoulder),
        bellyBulge = (1f - s.leanness).coerceIn(0f, 1f) * 0.9f,
        crown = false,
    )
}

/** Совместимость: фигура по «t» (ранг). */
fun DrawScope.drawHero(color: Color, t: Float, accent: Color) = drawBody(bodyFromT(t), color, accent)

@Composable
fun RankFigure(
    rankIndex: Int,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFE6E9F2),
    accent: Color = Color(0xFFFFB454),
) {
    val t = (rankIndex / 5f).coerceIn(0f, 1f)
    Canvas(modifier) { drawHero(color, t, accent) }
}

/**
 * Живой аватар по реальным замерам с «призраком цели».
 * Параметры анимируются при новых замерах.
 */
@Composable
fun LivingAvatar(
    shape: AvatarShape,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFE6E9F2),
    accent: Color = Color(0xFFFFB454),
    ghost: AvatarShape? = null,
) {
    val sh by animateFloatAsState(shape.shoulder, tween(700), label = "sh")
    val wa by animateFloatAsState(shape.waist, tween(700), label = "wa")
    val ma by animateFloatAsState(shape.mass, tween(700), label = "ma")
    val le by animateFloatAsState(shape.leanness, tween(700), label = "le")
    Canvas(modifier) {
        ghost?.let { drawBody(bodyFromShape(it), color.copy(alpha = 0.22f), accent.copy(alpha = 0.22f)) }
        drawBody(bodyFromShape(AvatarShape(sh, wa, ma, le)), color, accent)
    }
}
