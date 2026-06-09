package com.bodyquest.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.bodyquest.app.ui.theme.BqOutline
import com.bodyquest.app.ui.theme.BqPrimary
import kotlin.math.abs

data class LineSeries(val label: String, val color: Color, val points: List<Float>)

/**
 * Линейный график на Compose Canvas (без сторонних библиотек).
 * Несколько серий с общей осью Y, нормализованных по глобальному min/max.
 */
@Composable
fun MultiLineChart(
    series: List<LineSeries>,
    modifier: Modifier = Modifier,
    unit: String = "",
) {
    val allValues = series.flatMap { it.points }
    if (allValues.size < 2) {
        Box(modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
            Text(
                "Недостаточно данных для графика",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }
    var minV = allValues.min()
    var maxV = allValues.max()
    if (abs(maxV - minV) < 0.001f) {
        minV -= 1f
        maxV += 1f
    }
    val pad = (maxV - minV) * 0.12f
    minV -= pad
    maxV += pad

    Box(modifier.fillMaxWidth().height(200.dp)) {
        Canvas(Modifier.fillMaxWidth().height(200.dp)) {
            val left = 8f
            val right = size.width - 8f
            val top = 14f
            val bottom = size.height - 14f
            val w = right - left
            val h = bottom - top

            // Горизонтальные направляющие
            val gridLines = 4
            val gridPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                textSize = 26f
                color = android.graphics.Color.rgb(0x9A, 0xA3, 0xBF)
            }
            for (g in 0..gridLines) {
                val y = top + h * g / gridLines
                drawLine(
                    color = BqOutline.copy(alpha = 0.4f),
                    start = Offset(left, y),
                    end = Offset(right, y),
                    strokeWidth = 1f,
                )
                val value = maxV - (maxV - minV) * g / gridLines
                drawContext.canvas.nativeCanvas.drawText(
                    String.format("%.0f", value), left + 4f, y - 4f, gridPaint,
                )
            }

            fun yFor(v: Float): Float = bottom - (v - minV) / (maxV - minV) * h

            series.forEach { s ->
                if (s.points.size < 2) return@forEach
                val stepX = if (s.points.size == 1) 0f else w / (s.points.size - 1)
                val path = Path()
                s.points.forEachIndexed { i, v ->
                    val x = left + stepX * i
                    val y = yFor(v)
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, color = s.color, style = Stroke(width = 4f))
                s.points.forEachIndexed { i, v ->
                    drawCircle(s.color, radius = 5f, center = Offset(left + stepX * i, yFor(v)))
                }
            }
        }
    }
}

@Composable
fun SingleLineChart(
    points: List<Float>,
    color: Color = BqPrimary,
    label: String = "",
    modifier: Modifier = Modifier,
) = MultiLineChart(listOf(LineSeries(label, color, points)), modifier)
