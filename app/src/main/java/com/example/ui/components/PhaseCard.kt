package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.CallSplit
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Merge
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Phase
import com.example.data.model.PhaseCategory
import com.example.data.model.TaskItem
import com.example.ui.neumorphic.NeumorphicButton
import com.example.ui.neumorphic.NeumorphicCard
import com.example.ui.neumorphic.NeumorphicIconButton
import com.example.ui.neumorphic.NeumorphicPill
import com.example.ui.neumorphic.NeumorphicSunkenCard
import com.example.ui.neumorphic.NeumorphicTextField
import com.example.ui.theme.GroteskFontFamily
import com.example.ui.theme.HelveticaFontFamily
import com.example.ui.theme.LocalNeumorphicColors

/**
 * Clean, monochrome phase card without emojis.
 * Adheres to 60% Helvetica (with kerning) and 40% Grotesk (important elements)
 * and 70% off-white / 30% black color volume.
 */
@Composable
fun PhaseCard(
    phase: Phase,
    isCurrentPhase: Boolean,
    onEdit: () -> Unit,
    onToggleLock: () -> Unit,
    onToggleStartAlarm: () -> Unit,
    onToggleEndAlarm: () -> Unit,
    onResize: (Int) -> Unit,
    onSplit: () -> Unit,
    onMergeNext: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onAddTask: (String) -> Unit,
    onToggleTask: (String) -> Unit,
    onDeleteTask: (String) -> Unit,
    onSetStudyGoalStatus: (String) -> Unit,
    taskList: List<TaskItem>,
    modifier: Modifier = Modifier
) {
    val neuColors = LocalNeumorphicColors.current
    var isExpanded by remember { mutableStateOf(false) }
    var newTaskInput by remember { mutableStateOf("") }

    NeumorphicCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("phase_card_${phase.id}"),
        cornerRadius = 18.dp,
        elevationOffset = if (isCurrentPhase) 4.dp else 2.dp,
        blurRadius = if (isCurrentPhase) 8.dp else 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Main Top Row (Image 2 style)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Icon
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(neuColors.sunken),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = phase.getIcon(),
                        contentDescription = phase.category,
                        modifier = Modifier.size(18.dp),
                        tint = neuColors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Phase Title and Time Range
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = phase.title,
                            fontFamily = GroteskFontFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = neuColors.textPrimary,
                            letterSpacing = (-0.2).sp
                        )

                        // Outline Category Pill (Image 2 style: `(Work)`)
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(neuColors.sunken)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "(${phase.category.lowercase().replaceFirstChar { it.uppercase() }})",
                                fontFamily = HelveticaFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = neuColors.textSecondary,
                                letterSpacing = 0.4.sp
                            )
                        }

                        if (phase.isLocked) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = "Locked",
                                modifier = Modifier.size(12.dp),
                                tint = neuColors.textTertiary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${phase.formattedStartTime()} – ${phase.formattedEndTime()}",
                        fontFamily = HelveticaFontFamily,
                        fontSize = 12.sp,
                        color = neuColors.textSecondary,
                        letterSpacing = 0.5.sp
                    )
                }

                // Duration Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isCurrentPhase) neuColors.textPrimary else neuColors.sunken)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = phase.formattedDuration(),
                        fontFamily = GroteskFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCurrentPhase) Color.White else neuColors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = neuColors.textTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Quick Actions Bar
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Toggles: Start Alarm, End Alarm, Lock
                Row(verticalAlignment = Alignment.CenterVertically) {
                    NeumorphicIconButton(
                        onClick = onToggleStartAlarm,
                        icon = if (phase.startAlarmEnabled) Icons.Outlined.Notifications else Icons.Outlined.NotificationsNone,
                        contentDescription = "Start alarm",
                        size = 30.dp,
                        iconSize = 14.dp,
                        tint = if (phase.startAlarmEnabled) neuColors.textPrimary else neuColors.textTertiary,
                        isActive = phase.startAlarmEnabled
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    NeumorphicIconButton(
                        onClick = onToggleEndAlarm,
                        icon = Icons.Outlined.Alarm,
                        contentDescription = "End alarm",
                        size = 30.dp,
                        iconSize = 14.dp,
                        tint = if (phase.endAlarmEnabled) neuColors.textPrimary else neuColors.textTertiary,
                        isActive = phase.endAlarmEnabled
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    NeumorphicIconButton(
                        onClick = onToggleLock,
                        icon = if (phase.isLocked) Icons.Outlined.Lock else Icons.Outlined.LockOpen,
                        contentDescription = "Lock",
                        size = 30.dp,
                        iconSize = 14.dp,
                        tint = if (phase.isLocked) neuColors.textPrimary else neuColors.textTertiary,
                        isActive = phase.isLocked
                    )
                }

                // Resize buttons: -15m and +15m
                Row(verticalAlignment = Alignment.CenterVertically) {
                    NeumorphicButton(
                        onClick = { onResize(-15) },
                        cornerRadius = 8.dp,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(
                            text = "-15m",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = neuColors.textSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                    NeumorphicButton(
                        onClick = { onResize(15) },
                        cornerRadius = 8.dp
                    ) {
                        Text(
                            text = "+15m",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = neuColors.textSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Expandable details
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                    HorizontalDivider(
                        color = neuColors.shadow.copy(alpha = 0.2f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    // If category is STUDY: Study Mode Details
                    if (phase.getCategoryEnum() == PhaseCategory.STUDY) {
                        NeumorphicSunkenCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 12.dp,
                            depth = 2.dp
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "STUDY FOCUS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = neuColors.textTertiary,
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                if (phase.subject.isNotEmpty()) {
                                    Text("Subject: ${phase.subject}", fontSize = 12.sp, color = neuColors.textPrimary)
                                }
                                if (phase.topic.isNotEmpty()) {
                                    Text("Topic: ${phase.topic}", fontSize = 12.sp, color = neuColors.textSecondary)
                                }
                                if (phase.goal.isNotEmpty()) {
                                    Text("Goal: ${phase.goal}", fontSize = 12.sp, color = neuColors.textSecondary)
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Goal status:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = neuColors.textTertiary
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("YES", "PARTIALLY", "NO").forEach { status ->
                                        val isSelected = phase.goalCompletedStatus == status
                                        NeumorphicPill(
                                            text = status,
                                            isSelected = isSelected,
                                            onClick = { onSetStudyGoalStatus(status) }
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Notes
                    if (phase.notes.isNotEmpty()) {
                        Text(
                            text = phase.notes,
                            fontSize = 12.sp,
                            color = neuColors.textSecondary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    // Tasks checklist
                    if (taskList.isNotEmpty()) {
                        Text(
                            text = "Tasks (${taskList.count { it.completed }}/${taskList.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = neuColors.textTertiary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        taskList.forEach { task ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = task.completed,
                                    onCheckedChange = { onToggleTask(task.id) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = neuColors.textPrimary,
                                        uncheckedColor = neuColors.textTertiary
                                    ),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = task.text,
                                    fontSize = 12.sp,
                                    color = if (task.completed) neuColors.textTertiary else neuColors.textPrimary,
                                    textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { onDeleteTask(task.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Delete",
                                        tint = neuColors.textTertiary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Add task input
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NeumorphicTextField(
                            value = newTaskInput,
                            onValueChange = { newTaskInput = it },
                            placeholder = "Add task...",
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        NeumorphicIconButton(
                            onClick = {
                                if (newTaskInput.isNotBlank()) {
                                    onAddTask(newTaskInput)
                                    newTaskInput = ""
                                }
                            },
                            icon = Icons.Default.Add,
                            contentDescription = "Add",
                            size = 36.dp,
                            iconSize = 16.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Action buttons: Edit, Split, Merge, Copy, Delete
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        NeumorphicButton(onClick = onEdit, cornerRadius = 10.dp) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(12.dp), tint = neuColors.textSecondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit", fontSize = 11.sp, color = neuColors.textSecondary)
                            }
                        }
                        NeumorphicButton(onClick = onSplit, cornerRadius = 10.dp) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.CallSplit, contentDescription = null, modifier = Modifier.size(12.dp), tint = neuColors.textSecondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Split", fontSize = 11.sp, color = neuColors.textSecondary)
                            }
                        }
                        NeumorphicButton(onClick = onMergeNext, cornerRadius = 10.dp) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.Merge, contentDescription = null, modifier = Modifier.size(12.dp), tint = neuColors.textSecondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Merge", fontSize = 11.sp, color = neuColors.textSecondary)
                            }
                        }
                        NeumorphicButton(onClick = onDuplicate, cornerRadius = 10.dp) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp), tint = neuColors.textSecondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy", fontSize = 11.sp, color = neuColors.textSecondary)
                            }
                        }
                        NeumorphicButton(onClick = onDelete, cornerRadius = 10.dp) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.DeleteOutline, contentDescription = null, modifier = Modifier.size(12.dp), tint = neuColors.textSecondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Delete", fontSize = 11.sp, color = neuColors.textSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}
