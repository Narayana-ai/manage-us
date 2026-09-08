package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Phase
import com.example.ui.neumorphic.NeumorphicButton
import com.example.ui.neumorphic.NeumorphicCard
import com.example.ui.neumorphic.NeumorphicProgressBar
import com.example.ui.theme.LocalNeumorphicColors
import com.example.viewmodel.ScheduleConflict

/**
 * Clean monochrome 24-hour mathematical validation breakdown.
 */
@Composable
fun Summary24Validation(
    phases: List<Phase>,
    totalPlannedMinutes: Int,
    unallocatedMinutes: Int,
    conflicts: List<ScheduleConflict>,
    todayProgressPair: Pair<Int, Int>,
    onFixConflictsAutomatically: () -> Unit,
    onFillGap: (Int) -> Unit,
    onAddNewPhase: () -> Unit,
    modifier: Modifier = Modifier
) {
    val neuColors = LocalNeumorphicColors.current

    val (plannedMins, completedMins) = todayProgressPair
    val progressPct = if (plannedMins > 0) {
        ((completedMins.toFloat() / plannedMins.toFloat()) * 100).toInt().coerceIn(0, 100)
    } else 0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("summary_24_validation"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Conflict Warning (if any)
        if (conflicts.isNotEmpty()) {
            val firstConflict = conflicts.first()
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 16.dp,
                elevationOffset = 3.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.WarningAmber,
                            contentDescription = null,
                            tint = neuColors.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Schedule Conflict",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = neuColors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${firstConflict.phase1.title} overlaps with ${firstConflict.phase2.title} by ${firstConflict.overlapMinutes} min.",
                        fontSize = 12.sp,
                        color = neuColors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    NeumorphicButton(
                        onClick = onFixConflictsAutomatically,
                        cornerRadius = 8.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.AutoFixHigh, contentDescription = null, modifier = Modifier.size(13.dp), tint = neuColors.textPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Resolve Automatically", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = neuColors.textPrimary)
                        }
                    }
                }
            }
        }

        // 2. Unallocated Time (if any)
        if (unallocatedMinutes > 0) {
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 16.dp,
                elevationOffset = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = neuColors.textSecondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "${Phase.formatDurationString(unallocatedMinutes)} unallocated",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = neuColors.textPrimary
                            )
                            Text(
                                text = "24-hour total not yet filled",
                                fontSize = 11.sp,
                                color = neuColors.textTertiary
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        NeumorphicButton(
                            onClick = { onFillGap(unallocatedMinutes) },
                            cornerRadius = 8.dp
                        ) {
                            Text(
                                text = "Fill gap",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = neuColors.textPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                        NeumorphicButton(
                            onClick = onAddNewPhase,
                            cornerRadius = 8.dp
                        ) {
                            Text(
                                text = "+ Phase",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = neuColors.textPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. Category Allocation Table
        NeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 20.dp,
            elevationOffset = 3.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ALLOCATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = neuColors.textTertiary,
                        letterSpacing = 1.2.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = neuColors.textSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (totalPlannedMinutes == 1440) "24h Balanced" else "${Phase.formatDurationString(totalPlannedMinutes)} / 24h",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = neuColors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val categoryGroups = phases.groupBy { it.getCategoryEnum() }
                    .mapValues { (_, pList) -> pList.sumOf { it.durationMinutes() } }
                    .toList()
                    .sortedByDescending { it.second }

                categoryGroups.forEach { (cat, minutes) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = cat.getIcon(),
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = neuColors.textSecondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = cat.displayName.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = neuColors.textPrimary
                            )
                        }
                        Text(
                            text = Phase.formatDurationString(minutes),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            color = neuColors.textSecondary
                        )
                    }
                }

                HorizontalDivider(
                    color = neuColors.shadow.copy(alpha = 0.2f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "TOTAL",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = neuColors.textPrimary
                    )
                    Text(
                        text = Phase.formatDurationString(totalPlannedMinutes),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = neuColors.textPrimary
                    )
                }
            }
        }

        // 4. Progress Card
        NeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 20.dp,
            elevationOffset = 3.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Day Progress",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = neuColors.textPrimary
                    )
                    Text(
                        text = "$progressPct%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = neuColors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                NeumorphicProgressBar(
                    progress = progressPct / 100f,
                    fillColor = neuColors.textPrimary,
                    height = 8.dp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Planned: ${Phase.formatDurationString(plannedMins)}",
                        fontSize = 11.sp,
                        color = neuColors.textTertiary
                    )
                    Text(
                        text = "Completed: ${Phase.formatDurationString(completedMins)}",
                        fontSize = 11.sp,
                        color = neuColors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
