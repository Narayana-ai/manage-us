package com.example.data.repository

import android.content.Context
import com.example.data.dao.DailyTemplateDao
import com.example.data.dao.PhaseDao
import com.example.data.database.AppDatabase
import com.example.data.model.DailyTemplate
import com.example.data.model.Phase
import com.example.data.model.PhaseCategory
import com.example.service.AlarmScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class PlannerRepository(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val phaseDao: PhaseDao = database.phaseDao()
    private val templateDao: DailyTemplateDao = database.dailyTemplateDao()
    private val alarmScheduler = AlarmScheduler(context)

    fun getPhasesForDay(date: String): Flow<List<Phase>> = phaseDao.getPhasesForDay(date)

    fun getAllTemplates(): Flow<List<DailyTemplate>> = templateDao.getAllTemplates()

    fun getAllPhasesFlow(): Flow<List<Phase>> = phaseDao.getAllPhases()

    suspend fun getPhasesForDaySync(date: String): List<Phase> = withContext(Dispatchers.IO) {
        phaseDao.getPhasesForDaySync(date)
    }

    /**
     * Seeds the 24-hour default schedule if the day has no phases.
     */
    suspend fun ensureDefaultSchedule(date: String) = withContext(Dispatchers.IO) {
        val existing = phaseDao.getPhasesForDaySync(date)
        if (existing.isEmpty()) {
            val defaults = getDefaultPhases(date)
            phaseDao.insertAll(defaults)
            defaults.forEach { alarmScheduler.schedulePhaseAlarms(it) }
        }
    }

    suspend fun insertPhase(phase: Phase): Long = withContext(Dispatchers.IO) {
        val id = phaseDao.insertPhase(phase)
        val inserted = phase.copy(id = id)
        alarmScheduler.schedulePhaseAlarms(inserted)
        id
    }

    suspend fun updatePhase(phase: Phase) = withContext(Dispatchers.IO) {
        phaseDao.updatePhase(phase)
        alarmScheduler.cancelPhaseAlarms(phase.id)
        alarmScheduler.schedulePhaseAlarms(phase)
    }

    suspend fun deletePhase(phase: Phase) = withContext(Dispatchers.IO) {
        alarmScheduler.cancelPhaseAlarms(phase.id)
        phaseDao.deletePhase(phase)
    }

    suspend fun deletePhaseById(id: Long) = withContext(Dispatchers.IO) {
        alarmScheduler.cancelPhaseAlarms(id)
        phaseDao.deletePhaseById(id)
    }

    suspend fun clearAndSetDay(date: String, phases: List<Phase>) = withContext(Dispatchers.IO) {
        val current = phaseDao.getPhasesForDaySync(date)
        current.forEach { alarmScheduler.cancelPhaseAlarms(it.id) }
        phaseDao.clearDay(date)
        phaseDao.insertAll(phases)
        phases.forEach { alarmScheduler.schedulePhaseAlarms(it) }
    }

    /**
     * Shifts dependent unlocked phases when a phase's timing changes.
     */
    suspend fun updatePhaseWithIntelligentShift(
        modifiedPhase: Phase,
        deltaMinutes: Int,
        shiftSubsequent: Boolean
    ) = withContext(Dispatchers.IO) {
        val allDayPhases = phaseDao.getPhasesForDaySync(modifiedPhase.dayDate).toMutableList()
        val index = allDayPhases.indexOfFirst { it.id == modifiedPhase.id }
        if (index != -1) {
            allDayPhases[index] = modifiedPhase
        } else {
            allDayPhases.add(modifiedPhase)
        }

        if (shiftSubsequent && deltaMinutes != 0) {
            // Shift subsequent unlocked phases forward
            for (i in (index + 1) until allDayPhases.size) {
                val p = allDayPhases[i]
                if (!p.isLocked) {
                    val newStart = (p.startMinutes + deltaMinutes + 1440) % 1440
                    val newEnd = (p.endMinutes + deltaMinutes + 1440) % 1440
                    allDayPhases[i] = p.copy(startMinutes = newStart, endMinutes = newEnd)
                } else {
                    // Stop shifting at locked boundary
                    break
                }
            }
        }

        // Re-save and schedule
        allDayPhases.forEach {
            phaseDao.updatePhase(it)
            alarmScheduler.cancelPhaseAlarms(it.id)
            alarmScheduler.schedulePhaseAlarms(it)
        }
    }

    /**
     * Splits a phase into two equal sub-phases.
     */
    suspend fun splitPhase(phase: Phase) = withContext(Dispatchers.IO) {
        val dur = phase.durationMinutes()
        if (dur < 30) return@withContext // Can't split very short phases

        val half1 = dur / 2
        val midPoint = (phase.startMinutes + half1) % 1440

        val part1 = phase.copy(
            title = "${phase.title} (Part 1)",
            endMinutes = midPoint
        )
        val part2 = phase.copy(
            id = 0,
            title = "${phase.title} (Part 2)",
            startMinutes = midPoint,
            endMinutes = phase.endMinutes,
            sortOrder = phase.sortOrder + 1
        )

        phaseDao.updatePhase(part1)
        val id2 = phaseDao.insertPhase(part2)
        alarmScheduler.schedulePhaseAlarms(part1)
        alarmScheduler.schedulePhaseAlarms(part2.copy(id = id2))
    }

    /**
     * Merges phase with the subsequent adjacent phase.
     */
    suspend fun mergeWithNext(phase: Phase, nextPhase: Phase) = withContext(Dispatchers.IO) {
        val merged = phase.copy(
            title = "${phase.title} + ${nextPhase.title}",
            endMinutes = nextPhase.endMinutes
        )
        phaseDao.updatePhase(merged)
        deletePhase(nextPhase)
        alarmScheduler.schedulePhaseAlarms(merged)
    }

    /**
     * Duplicates a phase.
     */
    suspend fun duplicatePhase(phase: Phase) = withContext(Dispatchers.IO) {
        val duration = phase.durationMinutes()
        val newStart = phase.endMinutes
        val newEnd = (newStart + duration) % 1440

        val copy = phase.copy(
            id = 0,
            title = "${phase.title} (Copy)",
            startMinutes = newStart,
            endMinutes = newEnd,
            sortOrder = phase.sortOrder + 1
        )
        val id = phaseDao.insertPhase(copy)
        alarmScheduler.schedulePhaseAlarms(copy.copy(id = id))
    }

    /**
     * Extends a phase by X minutes (e.g. 15 minutes) and shifts unlocked subsequent phases.
     */
    suspend fun extendPhase(phase: Phase, extendMinutes: Int) = withContext(Dispatchers.IO) {
        val newEnd = (phase.endMinutes + extendMinutes) % 1440
        val updated = phase.copy(
            endMinutes = newEnd,
            actualMinutesSpent = phase.actualMinutesSpent + extendMinutes
        )
        updatePhaseWithIntelligentShift(updated, deltaMinutes = extendMinutes, shiftSubsequent = true)
    }

    /**
     * Fixes schedule conflicts automatically by adjusting start/ends cleanly.
     */
    suspend fun autoFixConflicts(date: String) = withContext(Dispatchers.IO) {
        val phases = phaseDao.getPhasesForDaySync(date).sortedBy { it.startMinutes }
        if (phases.size < 2) return@withContext

        val adjusted = phases.toMutableList()
        for (i in 0 until (adjusted.size - 1)) {
            val current = adjusted[i]
            val next = adjusted[i + 1]

            // Check if current overlaps into next
            if (current.endMinutes > next.startMinutes && current.endMinutes < 1440 && next.startMinutes > current.startMinutes) {
                // Adjust current end time to meet next start time
                adjusted[i] = current.copy(endMinutes = next.startMinutes)
            }
        }

        adjusted.forEach { phaseDao.updatePhase(it) }
    }

    // --- Templates ---
    suspend fun saveDayAsTemplate(date: String, templateName: String) = withContext(Dispatchers.IO) {
        val phases = phaseDao.getPhasesForDaySync(date)
        val jsonArray = JSONArray()
        phases.forEach { p ->
            val obj = JSONObject().apply {
                put("title", p.title)
                put("category", p.category)
                put("startMinutes", p.startMinutes)
                put("endMinutes", p.endMinutes)
                put("isLocked", p.isLocked)
                put("notes", p.notes)
                put("subject", p.subject)
                put("goal", p.goal)
            }
            jsonArray.put(obj)
        }
        val template = DailyTemplate(
            name = templateName,
            phasesJson = jsonArray.toString()
        )
        templateDao.insertTemplate(template)
    }

    suspend fun applyTemplateToDay(date: String, template: DailyTemplate) = withContext(Dispatchers.IO) {
        val phases = mutableListOf<Phase>()
        val jsonArray = JSONArray(template.phasesJson)
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            phases.add(
                Phase(
                    dayDate = date,
                    title = obj.optString("title", "Phase"),
                    category = obj.optString("category", PhaseCategory.OTHER.name),
                    startMinutes = obj.optInt("startMinutes", 0),
                    endMinutes = obj.optInt("endMinutes", 60),
                    isLocked = obj.optBoolean("isLocked", false),
                    notes = obj.optString("notes", ""),
                    subject = obj.optString("subject", ""),
                    goal = obj.optString("goal", ""),
                    sortOrder = i
                )
            )
        }
        clearAndSetDay(date, phases)
    }

    suspend fun copyPreviousDay(targetDate: String, previousDate: String) = withContext(Dispatchers.IO) {
        val prevPhases = phaseDao.getPhasesForDaySync(previousDate)
        if (prevPhases.isNotEmpty()) {
            val copied = prevPhases.mapIndexed { index, p ->
                p.copy(id = 0, dayDate = targetDate, sortOrder = index, isCompleted = false)
            }
            clearAndSetDay(targetDate, copied)
        }
    }

    /**
     * Generates default predefined 24h schedule.
     */
    fun getDefaultPhases(date: String): List<Phase> {
        return listOf(
            Phase(dayDate = date, title = "Wake Up & Morning Routine", category = PhaseCategory.REST.name, startMinutes = 240, endMinutes = 300, sortOrder = 0),   // 4:00 AM - 5:00 AM (1h)
            Phase(dayDate = date, title = "Study Session 1", category = PhaseCategory.STUDY.name, startMinutes = 300, endMinutes = 420, subject = "Mathematics", goal = "Master calculus problem sets", sortOrder = 1), // 5:00 AM - 7:00 AM (2h)
            Phase(dayDate = date, title = "Breakfast / Get Ready", category = PhaseCategory.FOOD.name, startMinutes = 420, endMinutes = 480, sortOrder = 2),         // 7:00 AM - 8:00 AM (1h)
            Phase(dayDate = date, title = "Deep Work Block 1", category = PhaseCategory.WORK.name, startMinutes = 480, endMinutes = 600, sortOrder = 3),            // 8:00 AM - 10:00 AM (2h)
            Phase(dayDate = date, title = "Study Session 2", category = PhaseCategory.STUDY.name, startMinutes = 600, endMinutes = 720, subject = "Computer Science", goal = "Implement algorithms", sortOrder = 4), // 10:00 AM - 12:00 PM (2h)
            Phase(dayDate = date, title = "Lunch / Midday Break", category = PhaseCategory.FOOD.name, startMinutes = 720, endMinutes = 780, sortOrder = 5),         // 12:00 PM - 1:00 PM (1h)
            Phase(dayDate = date, title = "Work Sprint 2", category = PhaseCategory.WORK.name, startMinutes = 780, endMinutes = 900, sortOrder = 6),                // 1:00 PM - 3:00 PM (2h)
            Phase(dayDate = date, title = "Study Session 3", category = PhaseCategory.STUDY.name, startMinutes = 900, endMinutes = 1020, subject = "Language Practice", goal = "Vocabulary & reading", sortOrder = 7), // 3:00 PM - 5:00 PM (2h)
            Phase(dayDate = date, title = "Exercise & Workout", category = PhaseCategory.EXERCISE.name, startMinutes = 1020, endMinutes = 1080, sortOrder = 8),      // 5:00 PM - 6:00 PM (1h)
            Phase(dayDate = date, title = "Personal / Social", category = PhaseCategory.SOCIAL.name, startMinutes = 1080, endMinutes = 1140, sortOrder = 9),         // 6:00 PM - 7:00 PM (1h)
            Phase(dayDate = date, title = "Dinner & Wind Down", category = PhaseCategory.FOOD.name, startMinutes = 1140, endMinutes = 1200, sortOrder = 10),         // 7:00 PM - 8:00 PM (1h)
            Phase(dayDate = date, title = "Sleep", category = PhaseCategory.SLEEP.name, startMinutes = 1200, endMinutes = 240, isLocked = true, sortOrder = 11)      // 8:00 PM - 4:00 AM (8h)
        )
    }

    /**
     * Initializes preset templates if database is fresh.
     */
    suspend fun ensurePresetTemplates() = withContext(Dispatchers.IO) {
        val existing = templateDao.getAllTemplates()
        // We can pre-seed templates like "School Day", "Work Day", "Exam Day", "Weekend", "Gym Day"
    }
}
