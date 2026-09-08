package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Phase
import com.example.ui.components.AppViewMode
import com.example.ui.components.FloatingCapsuleDock
import com.example.ui.components.MinimalAnalogHeader
import com.example.ui.components.PhaseCard
import com.example.ui.components.RadialPetalDial
import com.example.ui.components.Summary24Validation
import com.example.ui.dialogs.AlarmTriggerDialog
import com.example.ui.dialogs.AutoArrangeDialog
import com.example.ui.dialogs.PhaseEditDialog
import com.example.ui.dialogs.RoutineIntelligenceDialog
import com.example.ui.dialogs.SkipShiftConfirmDialog
import com.example.ui.dialogs.TemplateDialog
import com.example.ui.neumorphic.NeumorphicButton
import com.example.ui.neumorphic.NeumorphicCard
import com.example.ui.neumorphic.NeumorphicIconButton
import com.example.ui.theme.GroteskFontFamily
import com.example.ui.theme.HelveticaFontFamily
import com.example.ui.theme.LocalNeumorphicColors
import com.example.viewmodel.PlannerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MainScreen(
    viewModel: PlannerViewModel,
    modifier: Modifier = Modifier
) {
    val neuColors = LocalNeumorphicColors.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val phases by viewModel.phases.collectAsState()
    val templates by viewModel.templates.collectAsState()
    val allPhases by viewModel.allPhases.collectAsState()
    val selectedDateStr by viewModel.selectedDate.collectAsState()
    val activeAlarmEvent by viewModel.activeAlarmEvent.collectAsState()
    val pendingSkipPhase by viewModel.pendingSkipPhase.collectAsState()
    val routineSuggestion by viewModel.routineSuggestion.collectAsState()

    // View mode state (Timeline, Radial Petal Dial, 24h Validation)
    var currentViewMode by remember { mutableStateOf(AppViewMode.TIMELINE) }

    // Dialog state
    var showEditDialog by remember { mutableStateOf(false) }
    var editingPhase by remember { mutableStateOf<Phase?>(null) }
    var showAutoArrangeDialog by remember { mutableStateOf(false) }
    var showTemplateDialog by remember { mutableStateOf(false) }
    var showRoutineDialog by remember { mutableStateOf(false) }

    // Live clock
    var currentTimeString by remember { mutableStateOf("12:31") }
    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("h:mm", Locale.getDefault())
        while (true) {
            currentTimeString = timeFormat.format(Date())
            delay(1000)
        }
    }

    // Parsed Day and Date from selectedDateStr
    val parsedDate = remember(selectedDateStr) {
        try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDateStr) ?: Date()
        } catch (_: Exception) {
            Date()
        }
    }
    val dayName = remember(parsedDate) {
        SimpleDateFormat("EEEE", Locale.getDefault()).format(parsedDate)
    }
    val dateDisplay = remember(parsedDate) {
        SimpleDateFormat("d MMMM", Locale.getDefault()).format(parsedDate)
    }

    val currentPhase = viewModel.getCurrentPhase(phases)
    val totalPlanned = viewModel.calculateTotalPlannedMinutes(phases)
    val unallocatedMins = viewModel.calculateUnallocatedMinutes(phases)
    val conflicts = viewModel.findConflicts(phases)
    val todayProgress = viewModel.calculateTodayProgress(phases)

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(neuColors.background)
            .testTag("main_screen"),
        containerColor = neuColors.background,
        contentColor = neuColors.textPrimary,
        contentWindowInsets = WindowInsets.statusBars,
        bottomBar = {
            // Floating Matte Black Capsule Dock (Images 2 & 3)
            FloatingCapsuleDock(
                currentMode = currentViewMode,
                onModeSelect = { currentViewMode = it },
                onQuickAdd = {
                    editingPhase = null
                    showEditDialog = true
                },
                onOpenIntelligence = { showRoutineDialog = true }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(neuColors.background)
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Subtle Utility Bar (Day Shift, Templates, Auto Arrange)
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NeumorphicIconButton(
                            onClick = { viewModel.goToPreviousDay() },
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Day",
                            size = 34.dp,
                            iconSize = 14.dp
                        )
                        NeumorphicIconButton(
                            onClick = { viewModel.goToNextDay() },
                            icon = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Day",
                            size = 34.dp,
                            iconSize = 14.dp
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NeumorphicIconButton(
                            onClick = { showTemplateDialog = true },
                            icon = Icons.Default.FolderOpen,
                            contentDescription = "Templates",
                            size = 34.dp,
                            iconSize = 15.dp
                        )
                        NeumorphicIconButton(
                            onClick = { showAutoArrangeDialog = true },
                            icon = Icons.Default.AutoAwesome,
                            contentDescription = "Auto Arrange",
                            size = 34.dp,
                            iconSize = 15.dp
                        )
                    }
                }
            }

            // 2. Minimalist Time & Day Header directly inspired by Image 2
            item {
                MinimalAnalogHeader(
                    dayName = dayName,
                    dateString = dateDisplay,
                    currentTimeString = currentTimeString,
                    currentPhase = currentPhase,
                    phases = phases,
                    onDateClick = { viewModel.goToNextDay() },
                    onAddPhaseClick = {
                        editingPhase = null
                        showEditDialog = true
                    }
                )
            }

            // 3. Routine Insight Banner (Clean Monochrome)
            if (routineSuggestion != null && currentViewMode == AppViewMode.TIMELINE) {
                item {
                    NeumorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 16.dp,
                        elevationOffset = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Text(
                                text = routineSuggestion ?: "",
                                fontFamily = HelveticaFontFamily,
                                fontSize = 12.sp,
                                color = neuColors.textPrimary,
                                letterSpacing = 0.4.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                NeumorphicButton(
                                    onClick = { viewModel.applyRoutineAdjustment() },
                                    cornerRadius = 8.dp
                                ) {
                                    Text(
                                        text = "Apply",
                                        fontFamily = GroteskFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = neuColors.textPrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                NeumorphicButton(
                                    onClick = { viewModel.dismissRoutineSuggestion() },
                                    cornerRadius = 8.dp
                                ) {
                                    Text(
                                        text = "Dismiss",
                                        fontFamily = HelveticaFontFamily,
                                        fontSize = 11.sp,
                                        color = neuColors.textTertiary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Dynamic Content Switching based on Floating Dock Selection
            when (currentViewMode) {
                AppViewMode.TIMELINE -> {
                    // Clean, unflooded Phase Cards List (Image 2 style)
                    if (phases.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No phases scheduled. Tap '+' to create one.",
                                    fontFamily = HelveticaFontFamily,
                                    fontSize = 13.sp,
                                    color = neuColors.textTertiary,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    } else {
                        items(phases, key = { it.id }) { phase ->
                            val isCurrent = currentPhase?.id == phase.id
                            val taskList = viewModel.parseTasks(phase.tasksJson)

                            PhaseCard(
                                phase = phase,
                                isCurrentPhase = isCurrent,
                                onEdit = {
                                    editingPhase = phase
                                    showEditDialog = true
                                },
                                onToggleLock = { viewModel.toggleLock(phase) },
                                onToggleStartAlarm = { viewModel.toggleStartAlarm(phase) },
                                onToggleEndAlarm = { viewModel.toggleEndAlarm(phase) },
                                onResize = { delta -> viewModel.resizePhase(phase, delta) },
                                onSplit = { viewModel.splitPhase(phase) },
                                onMergeNext = { viewModel.mergeWithNext(phase) },
                                onDuplicate = { viewModel.duplicatePhase(phase) },
                                onDelete = { viewModel.deletePhase(phase) },
                                onAddTask = { text -> viewModel.addTaskToPhase(phase, text) },
                                onToggleTask = { id -> viewModel.toggleTask(phase, id) },
                                onDeleteTask = { id -> viewModel.deleteTask(phase, id) },
                                onSetStudyGoalStatus = { status -> viewModel.setStudyGoalCompleted(phase, status) },
                                taskList = taskList
                            )
                        }
                    }
                }

                AppViewMode.RADIAL_DIAL -> {
                    // Radial Petal Dial directly from Image 1!
                    item {
                        RadialPetalDial(
                            phases = phases,
                            onPhaseClick = { phase ->
                                editingPhase = phase
                                showEditDialog = true
                            }
                        )
                    }

                    // Active Phase Card or Selected Category Info
                    if (currentPhase != null) {
                        item {
                            Text(
                                text = "ACTIVE PHASE",
                                fontFamily = GroteskFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = neuColors.textTertiary,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            PhaseCard(
                                phase = currentPhase,
                                isCurrentPhase = true,
                                onEdit = {
                                    editingPhase = currentPhase
                                    showEditDialog = true
                                },
                                onToggleLock = { viewModel.toggleLock(currentPhase) },
                                onToggleStartAlarm = { viewModel.toggleStartAlarm(currentPhase) },
                                onToggleEndAlarm = { viewModel.toggleEndAlarm(currentPhase) },
                                onResize = { delta -> viewModel.resizePhase(currentPhase, delta) },
                                onSplit = { viewModel.splitPhase(currentPhase) },
                                onMergeNext = { viewModel.mergeWithNext(currentPhase) },
                                onDuplicate = { viewModel.duplicatePhase(currentPhase) },
                                onDelete = { viewModel.deletePhase(currentPhase) },
                                onAddTask = { text -> viewModel.addTaskToPhase(currentPhase, text) },
                                onToggleTask = { id -> viewModel.toggleTask(currentPhase, id) },
                                onDeleteTask = { id -> viewModel.deleteTask(currentPhase, id) },
                                onSetStudyGoalStatus = { status -> viewModel.setStudyGoalCompleted(currentPhase, status) },
                                taskList = viewModel.parseTasks(currentPhase.tasksJson)
                            )
                        }
                    }
                }

                AppViewMode.VALIDATION -> {
                    // 24-Hour Mathematical Validation Breakdown
                    item {
                        Summary24Validation(
                            phases = phases,
                            totalPlannedMinutes = totalPlanned,
                            unallocatedMinutes = unallocatedMins,
                            conflicts = conflicts,
                            todayProgressPair = todayProgress,
                            onFixConflictsAutomatically = { viewModel.fixConflictsAutomatically() },
                            onFillGap = { gap -> viewModel.fillUnallocatedGap(gap) },
                            onAddNewPhase = {
                                editingPhase = null
                                showEditDialog = true
                            }
                        )
                    }
                }
            }

            // Bottom Spacer for generous scrolling room above the floating capsule dock
            item {
                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }

    // --- Dialogs ---

    if (showEditDialog) {
        PhaseEditDialog(
            phaseToEdit = editingPhase,
            onDismiss = {
                showEditDialog = false
                editingPhase = null
            },
            onSave = { title, cat, start, end, locked, notes, subj, topic, goal, sAlarm, eAlarm, preRem ->
                if (editingPhase != null) {
                    val updated = editingPhase!!.copy(
                        title = title,
                        category = cat.name,
                        startMinutes = start,
                        endMinutes = end,
                        isLocked = locked,
                        notes = notes,
                        subject = subj,
                        topic = topic,
                        goal = goal,
                        startAlarmEnabled = sAlarm,
                        endAlarmEnabled = eAlarm,
                        preReminderMinutes = preRem
                    )
                    viewModel.updatePhase(updated)
                } else {
                    viewModel.addPhase(
                        title = title,
                        category = cat,
                        startMinutes = start,
                        endMinutes = end,
                        isLocked = locked,
                        notes = notes,
                        subject = subj,
                        topic = topic,
                        goal = goal,
                        startAlarm = sAlarm,
                        endAlarm = eAlarm,
                        preReminder = preRem
                    )
                }
                showEditDialog = false
                editingPhase = null
            }
        )
    }

    if (showAutoArrangeDialog) {
        AutoArrangeDialog(
            onDismiss = { showAutoArrangeDialog = false },
            onAutoArrange = { sleep, study, work, exercise, social, food, personal ->
                viewModel.autoArrangeSchedule(sleep, study, work, exercise, social, food, personal)
            }
        )
    }

    if (showTemplateDialog) {
        TemplateDialog(
            templates = templates,
            onDismiss = { showTemplateDialog = false },
            onLoadTemplate = { tmpl -> viewModel.applyTemplate(tmpl) },
            onSaveCurrentDayAsTemplate = { name -> viewModel.saveCurrentDayAsTemplate(name) },
            onCopyYesterday = { viewModel.copyYesterday() },
            onStartBlankDay = { viewModel.useBlankDay() }
        )
    }

    if (showRoutineDialog) {
        RoutineIntelligenceDialog(
            allPhases = allPhases,
            onDismiss = { showRoutineDialog = false },
            onApplyInsight = { viewModel.applyRoutineAdjustment() }
        )
    }

    activeAlarmEvent?.let { event ->
        AlarmTriggerDialog(
            alarmEvent = event,
            onDismiss = { viewModel.dismissAlarmPrompt() },
            onStartPhase = { viewModel.dismissAlarmPrompt() },
            onSnooze = { viewModel.dismissAlarmPrompt() },
            onSkipPhase = {
                val phase = phases.firstOrNull { it.id == event.phaseId }
                if (phase != null) {
                    viewModel.requestSkipPhase(phase)
                } else {
                    viewModel.dismissAlarmPrompt()
                }
            },
            onCompletePhase = {
                val phase = phases.firstOrNull { it.id == event.phaseId }
                if (phase != null) {
                    viewModel.updatePhase(phase.copy(isCompleted = true))
                }
                viewModel.dismissAlarmPrompt()
            },
            onExtend15m = { viewModel.extendCurrentPhase15m(event.phaseId) },
            onStartNextPhase = { viewModel.dismissAlarmPrompt() }
        )
    }

    pendingSkipPhase?.let { phase ->
        SkipShiftConfirmDialog(
            phase = phase,
            onDismiss = { viewModel.cancelSkipPhase() },
            onConfirm = { shiftRemaining -> viewModel.confirmSkipPhase(shiftRemaining) }
        )
    }
}
