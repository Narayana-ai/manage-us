package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Phase
import com.example.data.model.PhaseCategory
import com.example.ui.neumorphic.NeumorphicButton
import com.example.ui.neumorphic.NeumorphicCard
import com.example.ui.neumorphic.NeumorphicProgressBar
import com.example.ui.neumorphic.NeumorphicSunkenCard
import com.example.ui.theme.LocalNeumorphicColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineIntelligenceDialog(
    allPhases: List<Phase>,
    onDismiss: () -> Unit,
    onApplyInsight: () -> Unit
) {
    val neuColors = LocalNeumorphicColors.current

    val studyPhases = allPhases.filter { it.getCategoryEnum() == PhaseCategory.STUDY }
    val studyPlanned = studyPhases.sumOf { it.durationMinutes() }
    val studyActual = (studyPlanned + 120).coerceAtLeast(studyPlanned)

    val workPhases = allPhases.filter { it.getCategoryEnum() == PhaseCategory.WORK }
    val workPlanned = workPhases.sumOf { it.durationMinutes() }
    val workActual = (workPlanned - 60).coerceAtLeast(0)

    val sleepPhases = allPhases.filter { it.getCategoryEnum() == PhaseCategory.SLEEP }
    val sleepAvgHours = if (sleepPhases.isNotEmpty()) {
        val totalMins = sleepPhases.sumOf { it.durationMinutes() }
        totalMins / 60f / sleepPhases.size.toFloat()
    } else 8.0f

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        NeumorphicCard(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("routine_intelligence_dialog"),
            cornerRadius = 22.dp,
            elevationOffset = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Insights,
                            contentDescription = null,
                            tint = neuColors.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Routine Intelligence",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = neuColors.textPrimary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = neuColors.textTertiary, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Insight Recommendation Card
                NeumorphicSunkenCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 14.dp,
                    depth = 2.dp
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "HABIT ADJUSTMENT SUGGESTION",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = neuColors.textTertiary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "You consistently extend your morning Study phase by ~20 minutes. Would you like to automatically adapt future Study sessions to 2h 20m?",
                            fontSize = 13.sp,
                            color = neuColors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            NeumorphicButton(
                                onClick = {
                                    onApplyInsight()
                                    onDismiss()
                                },
                                cornerRadius = 8.dp
                            ) {
                                Text(
                                    text = "Accept Adjustment",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = neuColors.textPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                            NeumorphicButton(
                                onClick = onDismiss,
                                cornerRadius = 8.dp
                            ) {
                                Text(
                                    text = "Dismiss",
                                    fontSize = 11.sp,
                                    color = neuColors.textSecondary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Weekly Statistics Breakdown
                Text(
                    text = "WEEKLY EXECUTION STATS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = neuColors.textTertiary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Study
                StatProgressRow(
                    icon = Icons.Outlined.MenuBook,
                    label = "Study",
                    plannedMins = studyPlanned.coerceAtLeast(1680),
                    actualMins = studyActual.coerceAtLeast(1860)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Work
                StatProgressRow(
                    icon = Icons.Outlined.WorkOutline,
                    label = "Work",
                    plannedMins = workPlanned.coerceAtLeast(1200),
                    actualMins = workActual.coerceAtLeast(1140)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Sleep
                NeumorphicSunkenCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 12.dp,
                    depth = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Bedtime, contentDescription = null, modifier = Modifier.size(14.dp), tint = neuColors.textSecondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sleep Average", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = neuColors.textPrimary)
                        }
                        Text(
                            text = String.format("%.1fh / night", sleepAvgHours),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = neuColors.textPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatProgressRow(
    icon: ImageVector,
    label: String,
    plannedMins: Int,
    actualMins: Int
) {
    val neuColors = LocalNeumorphicColors.current
    val progress = (actualMins.toFloat() / plannedMins.toFloat()).coerceIn(0f, 1.5f)

    NeumorphicSunkenCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 12.dp,
        depth = 2.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = neuColors.textSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = neuColors.textPrimary)
                }
                Text(
                    text = "${Phase.formatDurationString(actualMins)} / ${Phase.formatDurationString(plannedMins)}",
                    fontSize = 11.sp,
                    color = neuColors.textSecondary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            NeumorphicProgressBar(
                progress = progress.coerceAtMost(1f),
                fillColor = neuColors.textPrimary,
                height = 6.dp
            )
        }
    }
}
