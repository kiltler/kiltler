package com.bodyquest.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.bodyquest.app.domain.AttributeType
import com.bodyquest.app.ui.theme.BqOutline
import com.bodyquest.app.ui.theme.BqPrimary
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Радарная диаграмма 5 характеристик. Полностью на Compose Canvas.
 * [values] — доля 0..1 по каждой характеристике. [levels] — числа уровней для подписей.
 */
@Composable
fun RadarChart(
    values: Map<AttributeType, Float>,
    levels: Map<AttributeType, Int>,
    modifier: Modifier = Modifier,
    fill: Color = BqPrimary,
) {
    val order = AttributeType.entries.toList()
    Box(modifier.fillMaxWidth().aspectRatio(1f)) {
        Canvas(Modifier.fillMaxWidth().aspectRatio(1f)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val radius = min(cx, cy) * 0.66f
            val n = order.size
            val angleStep = (2.0 * Math.PI / n)
            val startAngle = -Math.PI / 2.0 // вверх

            fun point(index: Int, r: Float): Offset {
                val a = startAngle + index * angleStep
                return Offset(
                    x = cx + (r * cos(a)).toFloat(),
                    y = cy + (r * sin(a)).toFloat(),
                )
            }

            // Сетка-кольца (4 уровня)
            val rings = 4
            for (ring in 1..rings) {
                val r = radius * ring / rings
                val path = Path()
                for (i in 0 until n) {
                    val p = point(i, r)
                    if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
                }
                path.close()
                drawPath(path, color = BqOutline.copy(alpha = 0.5f), style = Stroke(width = 1.5f))
            }

            // Оси
            for (i in 0 until n) {
                drawLine(
                    color = BqOutline.copy(alpha = 0.6f),
                    start = Offset(cx, cy),
                    end = point(i, radius),
                    strokeWidth = 1.5f,
                )
            }

            // Полигон значений
            val valuePath = Path()
            order.forEachIndexed { i, attr ->
                val v = (values[attr] ?: 0f).coerceIn(0.06f, 1f)
                val p = point(i, radius * v)
                if (i == 0) valuePath.moveTo(p.x, p.y) else valuePath.lineTo(p.x, p.y)
            }
            valuePath.close()
            drawPath(valuePath, color = fill.copy(alpha = 0.28f))
            drawPath(valuePath, color = fill, style = Stroke(width = 3f))

            order.forEachIndexed { i, attr ->
                val v = (values[attr] ?: 0f).coerceIn(0.06f, 1f)
                val p = point(i, radius * v)
                drawCircle(fill, radius = 5f, center = p)
            }

            // Подписи (emoji + уровень) нативным canvas
            val paint = android.graphics.Paint().apply {
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = radius * 0.16f
                color = android.graphics.Color.rgb(0xE6, 0xE9, 0xF2)
            }
            order.forEachIndexed { i, attr ->
                val labelPoint = point(i, radius * 1.28f)
                val lvl = levels[attr] ?: 1
                drawContext.canvas.nativeCanvas.apply {
                    drawText(attr.emoji, labelPoint.x, labelPoint.y, paint)
                    drawText("ур.$lvl", labelPoint.x, labelPoint.y + paint.textSize * 1.05f, paint)
                }
            }
        }
    }
}
