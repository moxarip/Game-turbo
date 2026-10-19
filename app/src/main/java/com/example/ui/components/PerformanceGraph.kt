package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun PerformanceGraph(
    title: String,
    dataPoints: List<Float>,
    lineColor: Color,
    unit: String = "",
    minExpected: Float = 0f,
    maxExpected: Float = 144f,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                if (dataPoints.isNotEmpty()) {
                    val latest = dataPoints.last()
                    Text(
                        text = "Current: %.1f %s".format(latest, unit),
                        color = lineColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
            ) {
                val w = size.width
                val h = size.height

                // Draw background horizontal grid guide lines
                val gridLines = 3
                for (i in 0..gridLines) {
                    val y = h * (i.toFloat() / gridLines)
                    drawLine(
                        color = Color(0xFF1E293B),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1f
                    )
                }

                if (dataPoints.size < 2) return@Canvas

                val minVal = dataPoints.minOrNull()?.coerceAtMost(minExpected) ?: minExpected
                val maxVal = dataPoints.maxOrNull()?.coerceAtLeast(maxExpected) ?: maxExpected
                val range = if (maxVal > minVal) maxVal - minVal else 1f

                val stepX = w / (dataPoints.size - 1).coerceAtLeast(1)

                val path = Path()
                val fillPath = Path()

                dataPoints.forEachIndexed { index, value ->
                    val x = index * stepX
                    val normY = (value - minVal) / range
                    val y = h - (normY * h)

                    if (index == 0) {
                        path.moveTo(x, y)
                        fillPath.moveTo(x, h)
                        fillPath.lineTo(x, y)
                    } else {
                        path.lineTo(x, y)
                        fillPath.lineTo(x, y)
                    }

                    if (index == dataPoints.size - 1) {
                        fillPath.lineTo(x, h)
                        fillPath.close()
                    }
                }

                // Draw subtle gradient fill beneath line
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(lineColor.copy(alpha = 0.25f), Color.Transparent),
                        startY = 0f,
                        endY = h
                    )
                )

                // Draw curve stroke
                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 2.5.dp.toPx())
                )

                // Draw point on the latest reading
                val lastIndex = dataPoints.size - 1
                val lastVal = dataPoints[lastIndex]
                val lastX = lastIndex * stepX
                val lastY = h - (((lastVal - minVal) / range) * h)

                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = Offset(lastX, lastY)
                )
                drawCircle(
                    color = lineColor,
                    radius = 5.dp.toPx(),
                    center = Offset(lastX, lastY),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "History: ${dataPoints.size} samples",
                    color = TextSecondary,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
