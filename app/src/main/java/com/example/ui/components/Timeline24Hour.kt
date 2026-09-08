package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Phase
import com.example.ui.neumorphic.NeumorphicCard
import com.example.ui.neumorphic.NeumorphicSunkenCard
import com.example.ui.theme.LocalNeumorphicColors
import java.util.Calendar

/**
 * Minimal monochrome 24-hour timeline bar.
 */
@Composable
fun Timeline24Hour(
    phases: List<Phase>,
    selectedPhaseId: Long?,
    onPhaseClick: (Phase) -> Unit,
    modifier: Modifier = Modifier
) {
    val neuColors = LocalNeumorphicColors.current

    val cal = Calendar.getInstance()
    val nowMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
    val nowProgress = nowMinutes / 1440f

    NeumorphicCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("timeline_24_hour"),
        cornerRadius = 20.dp,
        elevationOffset = 3.dp,
        blurRadius = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Minimal clean labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TIMELINE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = neuColors.textTertiary,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "${phases.size} phases  •  24h planned",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = neuColors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 24-Hour Proportional Track
            NeumorphicSunkenCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                cornerRadius = 18.dp,
                depth = 2.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(2.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    Row(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
                        phases.forEachIndexed { index, phase ->
                            val duration = phase.durationMinutes()
                            val weight = (duration / 1440f).coerceAtLeast(0.01f)
                            val isSelected = phase.id == selectedPhaseId

                            // Subtle alternating monochrome tone
                            val blockAlpha = if (isSelected) 0.85f else if (index % 2 == 0) 0.35f else 0.22f

                            Box(
                                modifier = Modifier
                                    .weight(weight)
                                    .fillMaxHeight()
                                    .padding(horizontal = 0.5.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(neuColors.textPrimary.copy(alpha = blockAlpha))
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(1.5.dp, neuColors.textPrimary, RoundedCornerShape(6.dp))
                                        } else Modifier
                                    )
                                    .clickable { onPhaseClick(phase) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (duration >= 90) {
                                    Icon(
                                        imageVector = phase.getIcon(),
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = if (isSelected) neuColors.surface else neuColors.textPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Current Time Cursor Needle
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(nowProgress)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .fillMaxHeight()
                                .background(neuColors.textPrimary)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Clean hour labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("12a", "4a", "8a", "12p", "4p", "8p", "12a").forEach { label ->
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        color = neuColors.textTertiary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
