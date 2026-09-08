package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ui.theme.*
import java.util.Locale

enum class PhaseCategory(val displayName: String, val defaultColor: Color) {
    STUDY("Study", CategoryStudy),
    WORK("Work", CategoryWork),
    SLEEP("Sleep", CategorySleep),
    EXERCISE("Exercise", CategoryExercise),
    FOOD("Food", CategoryFood),
    REST("Rest", CategoryRest),
    SOCIAL("Social", CategorySocial),
    CREATIVE("Creative", CategoryCreative),
    PERSONAL("Personal", CategoryPersonal),
    TRAVEL("Travel", CategoryTravel),
    OTHER("Other", CategoryOther);

    fun getIcon(): ImageVector {
        return when (this) {
            STUDY -> Icons.Outlined.MenuBook
            WORK -> Icons.Outlined.WorkOutline
            SLEEP -> Icons.Outlined.Bedtime
            EXERCISE -> Icons.Outlined.FitnessCenter
            FOOD -> Icons.Outlined.Restaurant
            REST -> Icons.Outlined.Spa
            SOCIAL -> Icons.Outlined.People
            CREATIVE -> Icons.Outlined.Palette
            PERSONAL -> Icons.Outlined.Person
            TRAVEL -> Icons.Outlined.DirectionsWalk
            OTHER -> Icons.Outlined.Schedule
        }
    }
}

@Entity(tableName = "phases")
data class Phase(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dayDate: String,
    val title: String,
    val category: String = PhaseCategory.OTHER.name,
    val customCategoryName: String? = null,
    val startMinutes: Int,
    val endMinutes: Int,
    val isLocked: Boolean = false,
    val startAlarmEnabled: Boolean = true,
    val endAlarmEnabled: Boolean = false,
    val preReminderMinutes: Int = 10,
    val notes: String = "",
    val tasksJson: String = "[]",
    val subject: String = "",
    val topic: String = "",
    val goal: String = "",
    val goalCompletedStatus: String = "NONE",
    val actualMinutesSpent: Int = 0,
    val isCompleted: Boolean = false,
    val sortOrder: Int = 0
) {
    fun durationMinutes(): Int {
        return if (endMinutes >= startMinutes) {
            endMinutes - startMinutes
        } else {
            (1440 - startMinutes) + endMinutes
        }
    }

    fun getCategoryEnum(): PhaseCategory {
        return try {
            PhaseCategory.valueOf(category)
        } catch (_: Exception) {
            PhaseCategory.OTHER
        }
    }

    fun getIcon(): ImageVector = getCategoryEnum().getIcon()

    fun getThemeColor(): Color = getCategoryEnum().defaultColor

    fun formattedStartTime(): String = formatMinutesToTimeString(startMinutes)

    fun formattedEndTime(): String = formatMinutesToTimeString(endMinutes)

    fun formattedDuration(): String = formatDurationString(durationMinutes())

    companion object {
        fun formatMinutesToTimeString(totalMinutes: Int): String {
            val normalized = ((totalMinutes % 1440) + 1440) % 1440
            val hour24 = normalized / 60
            val minutes = normalized % 60
            val amPm = if (hour24 < 12) "AM" else "PM"
            val hour12 = when (val h = hour24 % 12) {
                0 -> 12
                else -> h
            }
            return String.format(Locale.getDefault(), "%d:%02d %s", hour12, minutes, amPm)
        }

        fun formatDurationString(durationMinutes: Int): String {
            val hours = durationMinutes / 60
            val minutes = durationMinutes % 60
            return if (hours > 0) {
                String.format(Locale.getDefault(), "%dh %02dm", hours, minutes)
            } else {
                String.format(Locale.getDefault(), "%dm", minutes)
            }
        }
    }
}
