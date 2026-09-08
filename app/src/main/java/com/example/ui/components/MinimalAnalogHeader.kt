package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Phase
import com.example.ui.neumorphic.NeumorphicCard
import com.example.ui.neumorphic.NeumorphicIconButton
import com.example.ui.theme.GroteskFontFamily
import com.example.ui.theme.HelveticaFontFamily
import com.example.ui.theme.LocalNeumorphicColors
import com.example.ui.theme.TimelineNowBadgeBg
import java.util.Calendar

/**
 * Minimalist Time & Day Header directly modeled from Image 2:
 * - Day ("Monday", "28 May") with subtle chevron
 * - Giant 12:31 Grotesk display clock
 * - Constellation dots indicating phase markers
 * - Minimalist 24h timeline bar with solid, hatched, and light blocks + "NOW" badge
 * - "Today" section row with minimal '+' button
 */
@Composable
fun MinimalAnalogHeader(
    dayName: String,
    dateString: String,
    currentTimeString: String,
    currentPhase: Phase?,
    phases: List<Phase>,
    onDateClick: () -> Unit,
    onAddPhaseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val neuColors = LocalNeumorphicColors.current

    val cal = Calendar.getInstance()
    val nowMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
    val nowRatio = (nowMinutes / 1440f).coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("minimal_analog_header")
    ) {
        // 1. Top Day & Date Row with subtle Chevron (Image 2 top-left)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDateClick() }
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = dayName,
                    fontFamily = GroteskFontFamily,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = neuColors.textPrimary,
                    letterSpacing = (-0.5).sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dateString,
                        fontFamily = HelveticaFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = neuColors.textSecondary,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Change Date",
                tint = neuColors.textTertiary,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 2. Giant Clean Digital Clock (Image 2: "12:31")
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentTimeString,
                fontFamily = GroteskFontFamily,
                fontSize = 58.sp,
                fontWeight = FontWeight.Bold,
                color = neuColors.textPrimary,
                letterSpacing = (-1.5).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Constellation / Dot Pattern under Clock (Image 2)
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 12) {
                    val dotActive = (i.toFloat() / 12f) <= nowRatio
                    Box(
                        modifier = Modifier
                            .size(if (i % 3 == 0) 5.dp else 3.5.dp)
                            .clip(CircleShape)
                            .background(
                                if (dotActive) neuColors.textPrimary else neuColors.textTertiary.copy(alpha = 0.45f)
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // 3. Minimalist Timeline Bar with Hatched Pattern and "NOW" Badge (Image 2)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
        ) {
            // Background track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(7.dp))
                    .background(neuColors.sunken)
                    .border(1.dp, neuColors.border, RoundedCornerShape(7.dp))
            ) {
                // Canvas drawing solid, hatched, and greige sections
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val currentX = nowRatio * w

                    // Past / Completed block: Solid Deep Charcoal/Black
                    drawRect(
                        color = Color(0xFF141416),
                        topLeft = Offset(0f, 0f),
                        size = androidx.compose.ui.geometry.Size(currentX, h)
                    )

                    // Active session block: Diagonal Hatching if current phase is active
                    val activeStart = currentPhase?.let { (it.startMinutes / 1440f) * w } ?: 0f
                    val activeEnd = currentPhase?.let { (it.endMinutes / 1440f) * w } ?: 0f
                    if (activeEnd > activeStart) {
                        val activeWidth = activeEnd - activeStart
                        // Draw hatched diagonal lines
                        val step = 8f
                        var lineX = activeStart
                        while (lineX <= activeEnd) {
                            drawLine(
                                color = Color(0xFFFFFFFF).copy(alpha = 0.6f),
                                start = Offset(lineX, h),
                                end = Offset(lineX + h, 0f),
                                strokeWidth = 2f
                            )
                            lineX += step
                        }
                    }
                }
            }

            // Floating "NOW" Pill Badge directly positioned on the current time cursor!
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = (nowRatio * 260).dp.coerceIn(0.dp, 260.dp))
                    .clip(RoundedCornerShape(6.dp))
                    .background(TimelineNowBadgeBg)
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "NOW",
                    fontFamily = HelveticaFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.8.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. "Today" Section Header with Minimal "+" Button (Image 2)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today",
                fontFamily = GroteskFontFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = neuColors.textPrimary,
                letterSpacing = (-0.3).sp
            )

            NeumorphicIconButton(
                onClick = onAddPhaseClick,
                icon = Icons.Default.Add,
                contentDescription = "Add Phase",
                size = 32.dp,
                iconSize = 16.dp
            )
        }
    }
}
