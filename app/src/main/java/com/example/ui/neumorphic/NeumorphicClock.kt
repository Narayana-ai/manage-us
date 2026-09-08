package com.example.ui.neumorphic

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalNeumorphicColors
import kotlinx.coroutines.delay
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

/**
 * A tactile recessed neumorphic clock inspired by the reference design.
 * Features an inset bevel, subtle tick marks at 12, 03, 06, 09, needle hands,
 * and an optional active phase arc highlight around the dial perimeter.
 */
@Composable
fun NeumorphicClock(
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    currentPhaseStartAngle: Float? = null,
    currentPhaseSweepAngle: Float? = null,
    phaseColor: Color = Color(0xFF3B82F6)
) {
    val neuColors = LocalNeumorphicColors.current

    var currentHour by remember { mutableStateOf(Calendar.getInstance().get(Calendar.HOUR)) }
    var currentMinute by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MINUTE)) }
    var currentSecond by remember { mutableStateOf(Calendar.getInstance().get(Calendar.SECOND)) }

    LaunchedEffect(Unit) {
        while (true) {
            val cal = Calendar.getInstance()
            currentHour = cal.get(Calendar.HOUR)
            currentMinute = cal.get(Calendar.MINUTE)
            currentSecond = cal.get(Calendar.SECOND)
            delay(1000L)
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .neumorphicSunken(
                cornerRadius = size / 2,
                surfaceColor = neuColors.surface,
                innerShadowColor = neuColors.shadow.copy(alpha = 0.75f),
                innerHighlightColor = neuColors.highlight.copy(alpha = 0.9f),
                depth = 6.dp
            )
            .clip(CircleShape)
            .testTag("neumorphic_clock"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = (this.size.minDimension / 2f) * 0.88f

            // Draw outer subtle rim circle
            drawCircle(
                color = neuColors.shadow.copy(alpha = 0.25f),
                radius = radius,
                center = center,
                style = Stroke(width = 1.5f)
            )

            // Optional Phase Sector Arc (Highlighting the active phase on 12h/24h dial)
            if (currentPhaseStartAngle != null && currentPhaseSweepAngle != null) {
                drawArc(
                    color = phaseColor.copy(alpha = 0.85f),
                    startAngle = currentPhaseStartAngle - 90f,
                    sweepAngle = currentPhaseSweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Draw 12 tick marks
            for (i in 0 until 12) {
                val angleRad = Math.toRadians((i * 30).toDouble()).toFloat()
                val isMajor = i % 3 == 0
                val tickLength = if (isMajor) 10.dp.toPx() else 5.dp.toPx()
                val tickWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx()
                val tickColor = if (isMajor) neuColors.textSecondary else neuColors.textTertiary.copy(alpha = 0.5f)

                val start = Offset(
                    x = center.x + (radius - tickLength) * sin(angleRad),
                    y = center.y - (radius - tickLength) * cos(angleRad)
                )
                val end = Offset(
                    x = center.x + radius * sin(angleRad),
                    y = center.y - radius * cos(angleRad)
                )
                drawLine(
                    color = tickColor,
                    start = start,
                    end = end,
                    strokeWidth = tickWidth,
                    cap = StrokeCap.Round
                )
            }

            // Draw "09" and "03" labels as in the reference design
            val paint = android.graphics.Paint().apply {
                color = neuColors.textTertiary.toArgb()
                textSize = 9.dp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.NORMAL)
            }
            drawContext.canvas.nativeCanvas.drawText("09", center.x - radius * 0.65f, center.y + 3.dp.toPx(), paint)
            drawContext.canvas.nativeCanvas.drawText("03", center.x + radius * 0.65f, center.y + 3.dp.toPx(), paint)

            // Hour Hand
            val hourAngle = (currentHour % 12 + currentMinute / 60f) * 30f
            rotate(degrees = hourAngle, pivot = center) {
                drawLine(
                    color = neuColors.textPrimary,
                    start = center,
                    end = Offset(center.x, center.y - radius * 0.52f),
                    strokeWidth = 3.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Minute Hand
            val minuteAngle = (currentMinute + currentSecond / 60f) * 6f
            rotate(degrees = minuteAngle, pivot = center) {
                drawLine(
                    color = neuColors.textPrimary.copy(alpha = 0.85f),
                    start = center,
                    end = Offset(center.x, center.y - radius * 0.78f),
                    strokeWidth = 2.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Second Hand
            val secondAngle = currentSecond * 6f
            rotate(degrees = secondAngle, pivot = center) {
                drawLine(
                    color = phaseColor,
                    start = Offset(center.x, center.y + radius * 0.15f),
                    end = Offset(center.x, center.y - radius * 0.85f),
                    strokeWidth = 1.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Center Pin
            drawCircle(
                color = neuColors.textPrimary,
                radius = 3.5.dp.toPx(),
                center = center
            )
            drawCircle(
                color = neuColors.surface,
                radius = 1.5.dp.toPx(),
                center = center
            )
        }
    }
}
