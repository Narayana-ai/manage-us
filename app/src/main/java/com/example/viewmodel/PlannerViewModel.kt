package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.DailyTemplate
import com.example.data.model.Phase
import com.example.data.model.PhaseCategory
import com.example.data.model.TaskItem
import com.example.data.repository.PlannerRepository
import com.example.receiver.ActiveAlarmEvent
import com.example.receiver.AlarmReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

data class ScheduleConflict(
    val phase1: Phase,
    val phase2: Phase,
    val overlapMinutes: Int
)

data class WeeklyCategoryStat(
    val category: PhaseCategory,
    val plannedMinutes: Int,
    val actualMinutes: Int
)

class PlannerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PlannerRepository(application)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())

    private val _selectedDate = MutableStateFlow(dateFormat.format(Date()))
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _userName = MutableStateFlow("Alex")
    val userName: StateFlow<String> = _userName.asStateFlow()

    // Alarm prompt dialog state
    private val _activeAlarmEvent = MutableStateFlow<ActiveAlarmEvent?>(null)
    val activeAlarmEvent: StateFlow<ActiveAlarmEvent?> = _activeAlarmEvent.asStateFlow()

    // Skip shift confirmation dialog state
    private val _pendingSkipPhase = MutableStateFlow<Phase?>(null)
    val pendingSkipPhase: StateFlow<Phase?> = _pendingSkipPhase.asStateFlow()

    // Routine suggestion banner state
    private val _routineSuggestion = MutableStateFlow<String?>(
        "Insight: You frequently extend Study sessions by ~20m. Would you like to adjust Study phases to 2h 20m?"
    )
    val routineSuggestion: StateFlow<String?> = _routineSuggestion.asStateFlow()

    // All phases for the selected date
    val phases: StateFlow<List<Phase>> = _selectedDate
        .flatMapLatest { date -> repository.getPhasesForDay(date) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val templates: StateFlow<List<DailyTemplate>> = repository.getAllTemplates()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allPhases: StateFlow<List<Phase>> = repository.getAllPhasesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            repository.ensureDefaultSchedule(_selectedDate.value)
        }

        // Listen for alarm events emitted from AlarmReceiver
        viewModelScope.launch {
            AlarmReceiver.alarmEvents.collect { event ->
                _activeAlarmEvent.value = event
            }
        }
    }

    fun setUserName(name: String) {
        _userName.value = name
    }

    fun dismissAlarmPrompt() {
        _activeAlarmEvent.value = null
    }

    fun dismissRoutineSuggestion() {
        _routineSuggestion.value = null
    }

    fun applyRoutineAdjustment() {
        viewModelScope.launch {
            val currentList = phases.value
            val studyPhase = currentList.firstOrNull { it.getCategoryEnum() == PhaseCategory.STUDY && !it.isLocked }
            if (studyPhase != null) {
                repository.extendPhase(studyPhase, 20)
            }
            _routineSuggestion.value = null
        }
    }

    fun setDate(date: String) {
        _selectedDate.value = date
        viewModelScope.launch {
            repository.ensureDefaultSchedule(date)
        }
    }

    fun goToToday() {
        setDate(dateFormat.format(Date()))
    }

    fun goToPreviousDay() {
        changeDay(-1)
    }

    fun goToNextDay() {
        changeDay(1)
    }

    private fun changeDay(deltaDays: Int) {
        try {
            val cal = Calendar.getInstance()
            val parsed = dateFormat.parse(_selectedDate.value) ?: Date()
            cal.time = parsed
            cal.add(Calendar.DAY_OF_YEAR, deltaDays)
            val nextDate = dateFormat.format(cal.time)
            setDate(nextDate)
        } catch (_: Exception) {}
    }

    fun getDisplayDate(): String {
        return try {
            val parsed = dateFormat.parse(_selectedDate.value) ?: Date()
            displayDateFormat.format(parsed)
        } catch (_: Exception) {
            _selectedDate.value
        }
    }

    fun isToday(): Boolean {
        return _selectedDate.value == dateFormat.format(Date())
    }

    // --- Phase Operations ---

    fun addPhase(
        title: String,
        category: PhaseCategory,
        startMinutes: Int,
        endMinutes: Int,
        isLocked: Boolean = false,
        notes: String = "",
        subject: String = "",
        topic: String = "",
        goal: String = "",
        startAlarm: Boolean = true,
        endAlarm: Boolean = false,
        preReminder: Int = 10
    ) {
        viewModelScope.launch {
            val phase = Phase(
                dayDate = _selectedDate.value,
                title = title,
                category = category.name,
                startMinutes = startMinutes,
                endMinutes = endMinutes,
                isLocked = isLocked,
                notes = notes,
                subject = subject,
                topic = topic,
                goal = goal,
                startAlarmEnabled = startAlarm,
                endAlarmEnabled = endAlarm,
                preReminderMinutes = preReminder,
                sortOrder = phases.value.size
            )
            repository.insertPhase(phase)
        }
    }

    fun updatePhase(phase: Phase, shiftSubsequent: Boolean = true) {
        viewModelScope.launch {
            val oldPhase = phases.value.firstOrNull { it.id == phase.id }
            val delta = if (oldPhase != null) {
                phase.startMinutes - oldPhase.startMinutes
            } else 0

            repository.updatePhaseWithIntelligentShift(
                modifiedPhase = phase,
                deltaMinutes = delta,
                shiftSubsequent = shiftSubsequent && delta != 0
            )
        }
    }

    fun toggleLock(phase: Phase) {
        viewModelScope.launch {
            repository.updatePhase(phase.copy(isLocked = !phase.isLocked))
        }
    }

    fun toggleStartAlarm(phase: Phase) {
        viewModelScope.launch {
            repository.updatePhase(phase.copy(startAlarmEnabled = !phase.startAlarmEnabled))
        }
    }

    fun toggleEndAlarm(phase: Phase) {
        viewModelScope.launch {
            repository.updatePhase(phase.copy(endAlarmEnabled = !phase.endAlarmEnabled))
        }
    }

    fun resizePhase(phase: Phase, deltaMinutes: Int) {
        viewModelScope.launch {
            val newEnd = (phase.endMinutes + deltaMinutes + 1440) % 1440
            repository.extendPhase(phase.copy(endMinutes = newEnd), deltaMinutes)
        }
    }

    fun splitPhase(phase: Phase) {
        viewModelScope.launch {
            repository.splitPhase(phase)
        }
    }

    fun mergeWithNext(phase: Phase) {
        val currentList = phases.value.sortedBy { it.startMinutes }
        val index = currentList.indexOfFirst { it.id == phase.id }
        if (index != -1 && index < currentList.size - 1) {
            val nextPhase = currentList[index + 1]
            viewModelScope.launch {
                repository.mergeWithNext(phase, nextPhase)
            }
        }
    }

    fun duplicatePhase(phase: Phase) {
        viewModelScope.launch {
            repository.duplicatePhase(phase)
        }
    }

    fun deletePhase(phase: Phase) {
        viewModelScope.launch {
            repository.deletePhase(phase)
        }
    }

    fun extendCurrentPhase15m(phaseId: Long) {
        val p = phases.value.firstOrNull { it.id == phaseId } ?: return
        viewModelScope.launch {
            repository.extendPhase(p, 15)
            dismissAlarmPrompt()
        }
    }

    fun requestSkipPhase(phase: Phase) {
        _pendingSkipPhase.value = phase
    }

    fun confirmSkipPhase(shiftRemaining: Boolean) {
        val phaseToSkip = _pendingSkipPhase.value ?: return
        _pendingSkipPhase.value = null
        viewModelScope.launch {
            if (shiftRemaining) {
                // Shift subsequent phases backward by skipped duration
                val dur = phaseToSkip.durationMinutes()
                repository.updatePhaseWithIntelligentShift(
                    modifiedPhase = phaseToSkip.copy(isCompleted = true),
                    deltaMinutes = -dur,
                    shiftSubsequent = true
                )
            }
            repository.updatePhase(phaseToSkip.copy(isCompleted = true))
            dismissAlarmPrompt()
        }
    }

    fun cancelSkipPhase() {
        _pendingSkipPhase.value = null
    }

    fun setStudyGoalCompleted(phase: Phase, status: String) {
        viewModelScope.launch {
            repository.updatePhase(phase.copy(goalCompletedStatus = status, isCompleted = true))
        }
    }

    // --- Task List inside Phase ---
    fun addTaskToPhase(phase: Phase, taskText: String) {
        if (taskText.isBlank()) return
        val currentTasks = parseTasks(phase.tasksJson).toMutableList()
        currentTasks.add(TaskItem(id = UUID.randomUUID().toString(), text = taskText, completed = false))
        val updatedJson = serializeTasks(currentTasks)
        viewModelScope.launch {
            repository.updatePhase(phase.copy(tasksJson = updatedJson))
        }
    }

    fun toggleTask(phase: Phase, taskId: String) {
        val currentTasks = parseTasks(phase.tasksJson).map {
            if (it.id == taskId) it.copy(completed = !it.completed) else it
        }
        val updatedJson = serializeTasks(currentTasks)
        viewModelScope.launch {
            repository.updatePhase(phase.copy(tasksJson = updatedJson))
        }
    }

    fun deleteTask(phase: Phase, taskId: String) {
        val currentTasks = parseTasks(phase.tasksJson).filter { it.id != taskId }
        val updatedJson = serializeTasks(currentTasks)
        viewModelScope.launch {
            repository.updatePhase(phase.copy(tasksJson = updatedJson))
        }
    }

    // --- 24h Math Validation & Gaps ---

    fun calculateTotalPlannedMinutes(phaseList: List<Phase>): Int {
        return phaseList.sumOf { it.durationMinutes() }
    }

    fun calculateUnallocatedMinutes(phaseList: List<Phase>): Int {
        val total = calculateTotalPlannedMinutes(phaseList)
        return (1440 - total).coerceAtLeast(0)
    }

    fun fillUnallocatedGap(gapMinutes: Int) {
        val current = phases.value.sortedBy { it.startMinutes }
        if (current.isEmpty()) {
            addPhase("Personal Time", PhaseCategory.PERSONAL, 0, 1440)
            return
        }
        // Expand the last unlocked phase or add a rest block
        val lastUnlocked = current.lastOrNull { !it.isLocked } ?: current.last()
        resizePhase(lastUnlocked, gapMinutes)
    }

    fun findConflicts(phaseList: List<Phase>): List<ScheduleConflict> {
        val conflicts = mutableListOf<ScheduleConflict>()
        val sorted = phaseList.sortedBy { it.startMinutes }
        for (i in 0 until (sorted.size - 1)) {
            val p1 = sorted[i]
            val p2 = sorted[i + 1]

            // If p1 does not span midnight
            if (p1.endMinutes > p1.startMinutes) {
                if (p1.endMinutes > p2.startMinutes) {
                    val overlap = p1.endMinutes - p2.startMinutes
                    conflicts.add(ScheduleConflict(p1, p2, overlap))
                }
            }
        }
        return conflicts
    }

    fun fixConflictsAutomatically() {
        viewModelScope.launch {
            repository.autoFixConflicts(_selectedDate.value)
        }
    }

    // --- Current Active Phase ---

    fun getCurrentPhase(phaseList: List<Phase>): Phase? {
        val cal = Calendar.getInstance()
        val currentMins = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)

        return phaseList.firstOrNull { p ->
            if (p.endMinutes >= p.startMinutes) {
                currentMins in p.startMinutes until p.endMinutes
            } else {
                // Spans midnight (e.g. 20:00 to 04:00)
                currentMins >= p.startMinutes || currentMins < p.endMinutes
            }
        }
    }

    fun getMinutesRemainingInCurrentPhase(phase: Phase): Int {
        val cal = Calendar.getInstance()
        val currentMins = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)

        return if (phase.endMinutes >= phase.startMinutes) {
            (phase.endMinutes - currentMins).coerceAtLeast(0)
        } else {
            if (currentMins >= phase.startMinutes) {
                (1440 - currentMins + phase.endMinutes)
            } else {
                (phase.endMinutes - currentMins).coerceAtLeast(0)
            }
        }
    }

    fun calculateTodayProgress(phaseList: List<Phase>): Pair<Int, Int> {
        val totalPlanned = calculateTotalPlannedMinutes(phaseList)
        val cal = Calendar.getInstance()
        val currentMins = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)

        // Sum durations of phases that ended or are ongoing
        var completed = 0
        phaseList.forEach { p ->
            if (p.isCompleted) {
                completed += p.durationMinutes()
            } else if (p.endMinutes >= p.startMinutes) {
                if (currentMins >= p.endMinutes) {
                    completed += p.durationMinutes()
                } else if (currentMins in p.startMinutes until p.endMinutes) {
                    completed += (currentMins - p.startMinutes)
                }
            } else {
                // Spans midnight
                if (currentMins < p.endMinutes) {
                    completed += (1440 - p.startMinutes + currentMins)
                } else if (currentMins >= p.startMinutes) {
                    completed += (currentMins - p.startMinutes)
                }
            }
        }
        return Pair(totalPlanned, completed.coerceIn(0, totalPlanned))
    }

    // --- Auto Arrange Algorithm ---

    fun autoArrangeSchedule(
        sleepHours: Int,
        studyHours: Int,
        workHours: Int,
        exerciseHours: Int,
        socialHours: Int,
        foodHours: Int,
        personalHours: Int
    ) {
        viewModelScope.launch {
            val date = _selectedDate.value
            val newPhases = mutableListOf<Phase>()

            var cur = 0 // start at 00:00 (midnight)

            // 1. Sleep: 00:00 to sleepHours*60 or default 8h
            val sleepMins = sleepHours * 60
            newPhases.add(
                Phase(dayDate = date, title = "Sleep", category = PhaseCategory.SLEEP.name, startMinutes = 0, endMinutes = sleepMins, isLocked = true, sortOrder = 0)
            )
            cur = sleepMins

            // Morning prep
            val mornMins = 60
            newPhases.add(
                Phase(dayDate = date, title = "Morning Routine & Breakfast", category = PhaseCategory.FOOD.name, startMinutes = cur, endMinutes = cur + mornMins, sortOrder = 1)
            )
            cur += mornMins

            // Study session
            val studyMins = (studyHours * 60).coerceAtLeast(60)
            val studyBlock1 = studyMins / 2
            newPhases.add(
                Phase(dayDate = date, title = "Focused Study Session", category = PhaseCategory.STUDY.name, startMinutes = cur, endMinutes = cur + studyBlock1, sortOrder = 2)
            )
            cur += studyBlock1

            // Work session
            val workMins = (workHours * 60).coerceAtLeast(60)
            val workBlock1 = workMins / 2
            newPhases.add(
                Phase(dayDate = date, title = "Work Sprint", category = PhaseCategory.WORK.name, startMinutes = cur, endMinutes = cur + workBlock1, sortOrder = 3)
            )
            cur += workBlock1

            // Lunch
            newPhases.add(
                Phase(dayDate = date, title = "Lunch & Refresh", category = PhaseCategory.FOOD.name, startMinutes = cur, endMinutes = cur + 60, sortOrder = 4)
            )
            cur += 60

            // Study session 2
            val studyBlock2 = studyMins - studyBlock1
            newPhases.add(
                Phase(dayDate = date, title = "Study & Review", category = PhaseCategory.STUDY.name, startMinutes = cur, endMinutes = cur + studyBlock2, sortOrder = 5)
            )
            cur += studyBlock2

            // Work session 2
            val workBlock2 = workMins - workBlock1
            newPhases.add(
                Phase(dayDate = date, title = "Project Work", category = PhaseCategory.WORK.name, startMinutes = cur, endMinutes = cur + workBlock2, sortOrder = 6)
            )
            cur += workBlock2

            // Exercise
            val exMins = exerciseHours * 60
            newPhases.add(
                Phase(dayDate = date, title = "Exercise & Fitness", category = PhaseCategory.EXERCISE.name, startMinutes = cur, endMinutes = cur + exMins, sortOrder = 7)
            )
            cur += exMins

            // Social & Dinner
            val socialMins = socialHours * 60
            newPhases.add(
                Phase(dayDate = date, title = "Dinner & Social Wind Down", category = PhaseCategory.SOCIAL.name, startMinutes = cur, endMinutes = (cur + socialMins).coerceAtMost(1440), sortOrder = 8)
            )
            cur += socialMins

            // Fill remainder to 1440 with Personal/Relax
            if (cur < 1440) {
                newPhases.add(
                    Phase(dayDate = date, title = "Personal Wind Down", category = PhaseCategory.PERSONAL.name, startMinutes = cur, endMinutes = 1440, sortOrder = 9)
                )
            }

            repository.clearAndSetDay(date, newPhases)
        }
    }

    // --- Templates ---

    fun saveCurrentDayAsTemplate(name: String) {
        viewModelScope.launch {
            repository.saveDayAsTemplate(_selectedDate.value, name)
        }
    }

    fun applyTemplate(template: DailyTemplate) {
        viewModelScope.launch {
            repository.applyTemplateToDay(_selectedDate.value, template)
        }
    }

    fun copyYesterday() {
        val cal = Calendar.getInstance()
        val parsed = dateFormat.parse(_selectedDate.value) ?: Date()
        cal.time = parsed
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = dateFormat.format(cal.time)
        viewModelScope.launch {
            repository.copyPreviousDay(_selectedDate.value, yesterday)
        }
    }

    fun useBlankDay() {
        viewModelScope.launch {
            repository.clearAndSetDay(_selectedDate.value, emptyList())
        }
    }

    // --- Helper JSON Serializers ---
    fun parseTasks(tasksJson: String): List<TaskItem> {
        return try {
            val list = mutableListOf<TaskItem>()
            val arr = JSONArray(tasksJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    TaskItem(
                        id = obj.getString("id"),
                        text = obj.getString("text"),
                        completed = obj.optBoolean("completed", false)
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun serializeTasks(tasks: List<TaskItem>): String {
        val arr = JSONArray()
        tasks.forEach {
            val obj = JSONObject().apply {
                put("id", it.id)
                put("text", it.text)
                put("completed", it.completed)
            }
            arr.put(obj)
        }
        return arr.toString()
    }
}
