package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Phase
import com.example.data.model.PhaseCategory
import com.example.ui.neumorphic.NeumorphicButton
import com.example.ui.neumorphic.NeumorphicCard
import com.example.ui.neumorphic.NeumorphicPill
import com.example.ui.neumorphic.NeumorphicSunkenCard
import com.example.ui.neumorphic.NeumorphicSwitch
import com.example.ui.neumorphic.NeumorphicTextField
import com.example.ui.theme.LocalNeumorphicColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PhaseEditDialog(
    phaseToEdit: Phase?,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        category: PhaseCategory,
        startMinutes: Int,
        endMinutes: Int,
        isLocked: Boolean,
        notes: String,
        subject: String,
        topic: String,
        goal: String,
        startAlarm: Boolean,
        endAlarm: Boolean,
        preReminder: Int
    ) -> Unit
) {
    val neuColors = LocalNeumorphicColors.current

    var title by remember { mutableStateOf(phaseToEdit?.title ?: "New Phase") }
    var category by remember { mutableStateOf(phaseToEdit?.getCategoryEnum() ?: PhaseCategory.STUDY) }
    var startMins by remember { mutableIntStateOf(phaseToEdit?.startMinutes ?: 480) }
    var endMins by remember { mutableIntStateOf(phaseToEdit?.endMinutes ?: 600) }
    var isLocked by remember { mutableStateOf(phaseToEdit?.isLocked ?: false) }
    var startAlarm by remember { mutableStateOf(phaseToEdit?.startAlarmEnabled ?: true) }
    var endAlarm by remember { mutableStateOf(phaseToEdit?.endAlarmEnabled ?: false) }
    var preReminder by remember { mutableIntStateOf(phaseToEdit?.preReminderMinutes ?: 10) }
    var notes by remember { mutableStateOf(phaseToEdit?.notes ?: "") }
    var subject by remember { mutableStateOf(phaseToEdit?.subject ?: "") }
    var topic by remember { mutableStateOf(phaseToEdit?.topic ?: "") }
    var goal by remember { mutableStateOf(phaseToEdit?.goal ?: "") }

    val currentDuration = if (endMins >= startMins) endMins - startMins else (1440 - startMins) + endMins

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        NeumorphicCard(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("phase_edit_dialog"),
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
                    Text(
                        text = if (phaseToEdit != null) "Edit Phase" else "Add Phase",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = neuColors.textPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = neuColors.textTertiary, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Phase Title
                Text("TITLE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = neuColors.textTertiary, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(4.dp))
                NeumorphicTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = "Phase title",
                    testTag = "phase_title_input"
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Category Selector
                Text("CATEGORY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = neuColors.textTertiary, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PhaseCategory.values().forEach { cat ->
                        val isSelected = category == cat
                        NeumorphicPill(
                            text = cat.displayName,
                            isSelected = isSelected,
                            accentColor = neuColors.textPrimary,
                            onClick = { category = cat }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Time Pickers
                NeumorphicSunkenCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 14.dp,
                    depth = 2.dp
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("SCHEDULE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = neuColors.textTertiary, letterSpacing = 1.sp)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(neuColors.surface)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = Phase.formatDurationString(currentDuration),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = neuColors.textPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        TimeStepper(label = "Start", minutes = startMins, onMinutesChange = { startMins = it })
                        Spacer(modifier = Modifier.height(8.dp))
                        TimeStepper(label = "End", minutes = endMins, onMinutesChange = { endMins = it })
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Lock Phase
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Lock, contentDescription = null, modifier = Modifier.size(15.dp), tint = neuColors.textSecondary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Lock Phase Time", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = neuColors.textPrimary)
                    }
                    NeumorphicSwitch(
                        checked = isLocked,
                        onCheckedChange = { isLocked = it },
                        activeColor = neuColors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Smart Alarms Section
                Text("NOTIFICATIONS & ALARMS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = neuColors.textTertiary, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Notify when phase starts", fontSize = 12.sp, color = neuColors.textSecondary)
                    NeumorphicSwitch(
                        checked = startAlarm,
                        onCheckedChange = { startAlarm = it },
                        activeColor = neuColors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Notify when phase ends", fontSize = 12.sp, color = neuColors.textSecondary)
                    NeumorphicSwitch(
                        checked = endAlarm,
                        onCheckedChange = { endAlarm = it },
                        activeColor = neuColors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Pre-phase reminder:", fontSize = 12.sp, color = neuColors.textSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(0, 5, 10, 15, 30).forEach { mins ->
                        val label = if (mins == 0) "At start" else "${mins}m"
                        NeumorphicPill(
                            text = label,
                            isSelected = preReminder == mins,
                            onClick = { preReminder = mins }
                        )
                    }
                }

                // If Category is Study: Study Mode fields
                if (category == PhaseCategory.STUDY) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("STUDY DETAILS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = neuColors.textTertiary, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    NeumorphicTextField(value = subject, onValueChange = { subject = it }, placeholder = "Subject (e.g. Physics, Law)")
                    Spacer(modifier = Modifier.height(6.dp))
                    NeumorphicTextField(value = topic, onValueChange = { topic = it }, placeholder = "Topic (e.g. Thermodynamics)")
                    Spacer(modifier = Modifier.height(6.dp))
                    NeumorphicTextField(value = goal, onValueChange = { goal = it }, placeholder = "Target Goal (e.g. Complete 20 problems)")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("NOTES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = neuColors.textTertiary, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(4.dp))
                NeumorphicTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = "Phase notes or context...",
                    singleLine = false
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NeumorphicButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = neuColors.textSecondary,
                            modifier = Modifier.padding(vertical = 10.dp).align(Alignment.Center)
                        )
                    }
                    NeumorphicButton(
                        onClick = {
                            onSave(
                                title,
                                category,
                                startMins,
                                endMins,
                                isLocked,
                                notes,
                                subject,
                                topic,
                                goal,
                                startAlarm,
                                endAlarm,
                                preReminder
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Save",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = neuColors.textPrimary,
                            modifier = Modifier.padding(vertical = 10.dp).align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimeStepper(
    label: String,
    minutes: Int,
    onMinutesChange: (Int) -> Unit
) {
    val neuColors = LocalNeumorphicColors.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = label, fontSize = 11.sp, color = neuColors.textTertiary)
            Text(
                text = Phase.formatMinutesToTimeString(minutes),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = neuColors.textPrimary
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            NeumorphicButton(
                onClick = { onMinutesChange((minutes - 15 + 1440) % 1440) },
                cornerRadius = 8.dp
            ) {
                Text("-15m", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = neuColors.textSecondary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
            }
            Spacer(modifier = Modifier.width(4.dp))
            NeumorphicButton(
                onClick = { onMinutesChange((minutes - 60 + 1440) % 1440) },
                cornerRadius = 8.dp
            ) {
                Text("-1h", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = neuColors.textSecondary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
            }
            Spacer(modifier = Modifier.width(4.dp))
            NeumorphicButton(
                onClick = { onMinutesChange((minutes + 60) % 1440) },
                cornerRadius = 8.dp
            ) {
                Text("+1h", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = neuColors.textSecondary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
            }
            Spacer(modifier = Modifier.width(4.dp))
            NeumorphicButton(
                onClick = { onMinutesChange((minutes + 15) % 1440) },
                cornerRadius = 8.dp
            ) {
                Text("+15m", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = neuColors.textSecondary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
            }
        }
    }
}
