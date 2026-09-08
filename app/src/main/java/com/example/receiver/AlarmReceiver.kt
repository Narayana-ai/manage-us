package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class ActiveAlarmEvent(
    val phaseId: Long,
    val title: String,
    val category: String,
    val alarmType: String, // TYPE_START, TYPE_END, TYPE_PRE_REMINDER
    val startTime: String,
    val endTime: String,
    val duration: String,
    val reminderMinutes: Int = 0
)

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_TRIGGER_PHASE_ALARM = "com.example.ACTION_TRIGGER_PHASE_ALARM"
        const val EXTRA_PHASE_ID = "extra_phase_id"
        const val EXTRA_PHASE_TITLE = "extra_phase_title"
        const val EXTRA_PHASE_CATEGORY = "extra_phase_category"
        const val EXTRA_ALARM_TYPE = "extra_alarm_type"
        const val EXTRA_START_TIME = "extra_start_time"
        const val EXTRA_END_TIME = "extra_end_time"
        const val EXTRA_DURATION = "extra_duration"
        const val EXTRA_REMINDER_MINS = "extra_reminder_mins"

        const val TYPE_START = "TYPE_START"
        const val TYPE_END = "TYPE_END"
        const val TYPE_PRE_REMINDER = "TYPE_PRE_REMINDER"

        const val CHANNEL_ID = "phase24_alarms_channel"

        private val _alarmEvents = MutableSharedFlow<ActiveAlarmEvent>(extraBufferCapacity = 1)
        val alarmEvents = _alarmEvents.asSharedFlow()
    }

    override fun onReceive(context: Context, intent: Intent) {
        val phaseId = intent.getLongExtra(EXTRA_PHASE_ID, -1L)
        val title = intent.getStringExtra(EXTRA_PHASE_TITLE) ?: "Phase Alarm"
        val category = intent.getStringExtra(EXTRA_PHASE_CATEGORY) ?: "OTHER"
        val alarmType = intent.getStringExtra(EXTRA_ALARM_TYPE) ?: TYPE_START
        val startTime = intent.getStringExtra(EXTRA_START_TIME) ?: ""
        val endTime = intent.getStringExtra(EXTRA_END_TIME) ?: ""
        val duration = intent.getStringExtra(EXTRA_DURATION) ?: ""
        val reminderMinutes = intent.getIntExtra(EXTRA_REMINDER_MINS, 0)

        // Emit for in-app full screen dialog
        _alarmEvents.tryEmit(
            ActiveAlarmEvent(
                phaseId = phaseId,
                title = title,
                category = category,
                alarmType = alarmType,
                startTime = startTime,
                endTime = endTime,
                duration = duration,
                reminderMinutes = reminderMinutes
            )
        )

        // Post system status bar notification
        showNotification(
            context = context,
            phaseId = phaseId,
            title = title,
            category = category,
            alarmType = alarmType,
            startTime = startTime,
            endTime = endTime,
            duration = duration,
            reminderMinutes = reminderMinutes
        )
    }

    private fun showNotification(
        context: Context,
        phaseId: Long,
        title: String,
        category: String,
        alarmType: String,
        startTime: String,
        endTime: String,
        duration: String,
        reminderMinutes: Int
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Phase Alarms & Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when phases start, end, or transition"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("trigger_phase_id", phaseId)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            phaseId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val headerText = when (alarmType) {
            TYPE_PRE_REMINDER -> "Upcoming in $reminderMinutes min: $title"
            TYPE_START -> "⏰ TIME FOR: $title"
            TYPE_END -> "🏁 Completed: $title"
            else -> title
        }

        val bodyText = when (alarmType) {
            TYPE_PRE_REMINDER -> "$startTime — $endTime ($duration)"
            TYPE_START -> "Scheduled: $startTime — $endTime ($duration)"
            TYPE_END -> "Phase completed. Ready for next phase?"
            else -> "$startTime — $endTime"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(headerText)
            .setContentText(bodyText)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .build()

        notificationManager.notify(phaseId.toInt().coerceAtLeast(1), notification)
    }
}
