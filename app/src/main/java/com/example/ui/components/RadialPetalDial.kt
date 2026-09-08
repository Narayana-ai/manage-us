package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Phase
import com.example.data.model.PhaseCategory
import com.example.ui.neumorphic.NeumorphicCard
import com.example.ui.theme.GroteskFontFamily
import com.example.ui.theme.HelveticaFontFamily
import com.example.ui.theme.LocalNeumorphicColors
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Radial Petal Dial directly inspired by Image 1:
 * Circular flower/petal diagram showing 24-hour balance with rounded petals,
 * inner warm greige petals, outer charcoal petal frames, bold numbers, and category labels.
 */
@Composable
fun RadialPetalDial(
    phases: List<Phase>,
    onPhaseClick: (Phase) -> Unit,
    modifier: Modifier = Modifier
) {
    val neuColors = LocalNeumorphicColors.current
    var selectedCategoryIndex by remember { mutableStateOf<Int?>(null) }

    // Group phases by category or take up to 8-10 major slices
    val categoryGroups: Map<String, Int> = remember(phases) {
        val map = LinkedHashMap<String, Int>()
        for (phase in phases) {
            val cat = phase.category
            map[cat] = (map[cat] ?: 0) + phase.durationMinutes()
        }
        if (map.isEmpty()) {
            // Default 24h sample distribution if blank
            linkedMapOf<String, Int>(
                PhaseCategory.SLEEP.name to 480,
                PhaseCategory.STUDY.name to 240,
                PhaseCategory.WORK.name to 240,
                PhaseCategory.FOOD.name to 120,
                PhaseCategory.EXERCISE.name to 60,
                PhaseCategory.SOCIAL.name to 120,
                PhaseCategory.PERSONAL.name to 180
            )
        } else {
            map
        }
    }

    val totalMinutes = categoryGroups.values.sum().coerceAtLeast(1)
    val entries: List<Map.Entry<String, Int>> = categoryGroups.entries.toList()

    NeumorphicCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("radial_petal_dial"),
        cornerRadius = 24.dp,
        elevationOffset = 3.dp,
        blurRadius = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "24-HOUR RADIAL BALANCE",
                        fontFamily = GroteskFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = neuColors.textTertiary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Time Partition Dial",
                        fontFamily = HelveticaFontFamily,
                        fontSize = 13.sp,
                        color = neuColors.textSecondary,
                        letterSpacing = 0.4.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(neuColors.sunken)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${totalMinutes / 60}h allocated",
                        fontFamily = HelveticaFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = neuColors.textPrimary,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // The Canvas Petal Flower
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(entries) {
                            detectTapGestures { offset ->
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val dx = offset.x - center.x
                                val dy = offset.y - center.y
                                val dist = sqrt(dx * dx + dy * dy)
                                val radius = size.width / 2f

                                if (dist >= radius * 0.2f && dist <= radius * 0.98f) {
                                    var angle = (atan2(dy, dx) * 180f / PI).toFloat()
                                    if (angle < 0) angle += 360f

                                    var curAngle = 0f
                                    entries.forEachIndexed { idx, entry ->
                                        val sweep = (entry.value.toFloat() / totalMinutes.toFloat()) * 360f
                                        if (angle >= curAngle && angle < curAngle + sweep) {
                                            selectedCategoryIndex = idx
                                            val matchedPhase = phases.firstOrNull { it.category == entry.key }
                                            if (matchedPhase != null) {
                                                onPhaseClick(matchedPhase)
                                            }
                                        }
                                        curAngle += sweep
                                    }
                                } else {
                                    selectedCategoryIndex = null
                                }
                            }
                        }
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val center = Offset(canvasWidth / 2f, canvasHeight / 2f)
                    val outerRadius = canvasWidth * 0.46f
                    val innerCenterHole = canvasWidth * 0.16f

                    var startAngle = -90f // Start at top 12 o'clock

                    entries.forEachIndexed { index, entry ->
                        val sweepAngle = (entry.value.toFloat() / totalMinutes.toFloat()) * 360f
                        val isSelected = selectedCategoryIndex == index
                        val hours = (entry.value / 60f)
                        val hoursInt = (entry.value + 30) / 60

                        // 1. Draw Outer Petal (Dark Slate/Charcoal - Image 1 outer petals)
                        drawPetalWedge(
                            center = center,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            innerRadius = innerCenterHole,
                            outerRadius = outerRadius,
                            fillColor = if (isSelected) Color(0xFF1E2128) else Color(0xFF2C2F36),
                            strokeColor = Color(0xFF1B1D22),
                            strokeWidth = 2.dp.toPx()
                        )

                        // 2. Draw Inner Petal (Warm Off-White Greige - Image 1 inner petals)
                        // Inner petal depth varies slightly with duration to give that organic flower depth!
                        val innerPetalDepth = outerRadius * (0.55f + (hours / 12f).coerceIn(0f, 0.35f))
                        val greigePetalColor = if (isSelected) Color(0xFFFFFFFF) else Color(0xFFF2EFE9)

                        drawPetalWedge(
                            center = center,
                            startAngle = startAngle + 2f,
                            sweepAngle = sweepAngle - 4f,
                            innerRadius = innerCenterHole + 4.dp.toPx(),
                            outerRadius = innerPetalDepth,
                            fillColor = greigePetalColor,
                            strokeColor = Color(0xFFE4E0D6),
                            strokeWidth = 1.5.dp.toPx()
                        )

                        // 3. Draw Text: Number and Category inside the inner petal
                        val midAngleRad = ((startAngle + sweepAngle / 2f) * PI / 180f).toFloat()
                        val textRadius = innerCenterHole + (innerPetalDepth - innerCenterHole) * 0.55f
                        val textX = center.x + cos(midAngleRad) * textRadius
                        val textY = center.y + sin(midAngleRad) * textRadius

                        drawIntoCanvas { canvas ->
                            val native = canvas.nativeCanvas

                            // Bold Grotesk Number (40% volume font)
                            val numPaint = android.graphics.Paint().apply {
                                color = android.graphics.Color.parseColor("#141416")
                                textSize = if (sweepAngle > 30f) 36f else 28f
                                isFakeBoldText = true
                                textAlign = android.graphics.Paint.Align.CENTER
                                isAntiAlias = true
                            }

                            // Helvetica Label (60% volume font)
                            val labelPaint = android.graphics.Paint().apply {
                                color = android.graphics.Color.parseColor("#5E5C56")
                                textSize = if (sweepAngle > 30f) 22f else 18f
                                textAlign = android.graphics.Paint.Align.CENTER
                                isAntiAlias = true
                                letterSpacing = 0.05f
                            }

                            val cleanCategoryName = entry.key.lowercase().replaceFirstChar { it.uppercase() }
                            native.drawText("${hoursInt}h", textX, textY - 4f, numPaint)
                            if (sweepAngle > 25f) {
                                native.drawText(cleanCategoryName, textX, textY + 22f, labelPaint)
                            }
                        }

                        startAngle += sweepAngle
                    }

                    // Center Organic Hole (Star/Circle as in Image 1)
                    drawCircle(
                        color = Color(0xFF141416),
                        radius = innerCenterHole - 2.dp.toPx(),
                        center = center
                    )
                    drawCircle(
                        color = Color(0xFF28282D),
                        radius = innerCenterHole - 6.dp.toPx(),
                        center = center
                    )
                }

                // Center Icon/Text inside the black circular cutout
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "24",
                        fontFamily = GroteskFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF2F1EB)
                    )
                    Text(
                        text = "HRS",
                        fontFamily = HelveticaFontFamily,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA09E96),
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Category Legend Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tap any petal to filter & inspect phases",
                    fontFamily = HelveticaFontFamily,
                    fontSize = 11.sp,
                    color = neuColors.textTertiary,
                    letterSpacing = 0.4.sp
                )
            }
        }
    }
}

/**
 * Helper to construct and draw a curved wedge/petal path.
 */
private fun DrawScope.drawPetalWedge(
    center: Offset,
    startAngle: Float,
    sweepAngle: Float,
    innerRadius: Float,
    outerRadius: Float,
    fillColor: Color,
    strokeColor: Color,
    strokeWidth: Float
) {
    if (sweepAngle <= 0f) return

    val path = Path().apply {
        val rectOuter = Rect(
            center.x - outerRadius,
            center.y - outerRadius,
            center.x + outerRadius,
            center.y + outerRadius
        )
        val rectInner = Rect(
            center.x - innerRadius,
            center.y - innerRadius,
            center.x + innerRadius,
            center.y + innerRadius
        )

        arcTo(rectOuter, startAngle, sweepAngle, false)
        arcTo(rectInner, startAngle + sweepAngle, -sweepAngle, false)
        close()
    }

    drawPath(path = path, color = fillColor, style = Fill)
    drawPath(path = path, color = strokeColor, style = Stroke(width = strokeWidth))
}
