package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Phase
import com.example.ui.neumorphic.NeumorphicButton
import com.example.ui.neumorphic.NeumorphicCard
import com.example.ui.neumorphic.NeumorphicClock
import com.example.ui.neumorphic.NeumorphicIconButton
import com.example.ui.neumorphic.NeumorphicSunkenCard
import com.example.ui.theme.LocalNeumorphicColors

/**
 * Minimal, monochrome hero dashboard displaying the active phase and recessed clock.
 */
@Composable
fun CurrentPhaseHero(
    greeting: String,
    userName: String,
    currentPhase: Phase?,
    remainingMinutes: Int,
    dateDisplay: String,
    currentTimeDisplay: String,
    onOpenPhase: (Phase) -> Unit,
    modifier: Modifier = Modifier
) {
    val neuColors = LocalNeumorphicColors.current

    val clockPhaseStartAngle = currentPhase?.let {
        ((it.startMinutes % 720) / 720f) * 360f
    }
    val clockPhaseSweepAngle = currentPhase?.let {
        (it.durationMinutes().coerceAtMost(720) / 720f) * 360f
    }

    NeumorphicCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("current_phase_hero"),
        cornerRadius = 24.dp,
        elevationOffset = 4.dp,
        blurRadius = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row: Minimal Typography
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = currentTimeDisplay,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = neuColors.textPrimary,
                        letterSpacing = (-0.8).sp
                    )
                    Text(
                        text = dateDisplay,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = neuColors.textSecondary
                    )
                }

                // Clean monochrome pill badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(neuColors.pillBg)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (currentPhase != null) "IN PHASE" else "IDLE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = neuColors.textSecondary,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Active Phase Tray
            NeumorphicSunkenCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp,
                depth = 2.5.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentPhase != null) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(neuColors.surface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = currentPhase.getIcon(),
                                    contentDescription = currentPhase.category,
                                    modifier = Modifier.size(20.dp),
                                    tint = neuColors.textPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = currentPhase.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = neuColors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${currentPhase.formattedStartTime()} – ${currentPhase.formattedEndTime()}  •  ${Phase.formatDurationString(remainingMinutes)} left",
                                    fontSize = 12.sp,
                                    color = neuColors.textSecondary
                                )
                            }
                        }

                        NeumorphicButton(
                            onClick = { onOpenPhase(currentPhase) },
                            cornerRadius = 12.dp,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Details",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = neuColors.textPrimary
                                )
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = neuColors.textSecondary
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "No active phase currently scheduled",
                            fontSize = 13.sp,
                            color = neuColors.textTertiary,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Lower Section: Clock (Left) + Minimal Summary (Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeumorphicClock(
                    size = 125.dp,
                    currentPhaseStartAngle = clockPhaseStartAngle,
                    currentPhaseSweepAngle = clockPhaseSweepAngle,
                    phaseColor = neuColors.textPrimary.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (currentPhase != null && currentPhase.goal.isNotBlank()) {
                        NeumorphicSunkenCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 14.dp,
                            depth = 2.dp
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Target Goal",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = neuColors.textTertiary
                                )
                                Text(
                                    text = currentPhase.goal,
                                    fontSize = 12.sp,
                                    color = neuColors.textPrimary,
                                    maxLines = 2
                                )
                            }
                        }
                    } else {
                        NeumorphicSunkenCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 14.dp,
                            depth = 2.dp
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Phase Mode",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = neuColors.textTertiary
                                )
                                Text(
                                    text = if (currentPhase?.isLocked == true) "Locked Time Block" else "Flexible Schedule",
                                    fontSize = 12.sp,
                                    color = neuColors.textPrimary
                                )
                            }
                        }
                    }

                    // Minimal Control Pill Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NeumorphicButton(
                            onClick = {
                                if (currentPhase != null) onOpenPhase(currentPhase)
                            },
                            modifier = Modifier.weight(1f),
                            cornerRadius = 10.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Tune,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                    tint = neuColors.textSecondary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Tune",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = neuColors.textPrimary
                                )
                            }
                        }

                        if (currentPhase != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(neuColors.surface)
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (currentPhase.startAlarmEnabled) Icons.Outlined.Alarm else Icons.Outlined.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                    tint = neuColors.textSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
