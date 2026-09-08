package com.example.service

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.model.Phase
import com.example.receiver.AlarmReceiver
import java.util.Calendar

class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @SuppressLint("ScheduleExactAlarm")
    fun schedulePhaseAlarms(phase: Phase) {
        val today = Calendar.getInstance()

        // 1. Start Alarm & Pre-Reminder
        if (phase.startAlarmEnabled) {
            val startCal = Calendar.getInstance().apply {
                val hour = phase.startMinutes / 60
                val min = phase.startMinutes % 60
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, min)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (before(today)) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            // Pre-reminder if enabled
            if (phase.preReminderMinutes > 0) {
                val preCal = (startCal.clone() as Calendar).apply {
                    add(Calendar.MINUTE, -phase.preReminderMinutes)
                }
                if (preCal.after(today)) {
                    setExactAlarm(
                        triggerTime = preCal.timeInMillis,
                        requestCode = (phase.id * 10 + 1).toInt(),
                        intent = createIntent(phase, AlarmReceiver.TYPE_PRE_REMINDER, phase.preReminderMinutes)
                    )
                }
            }

            // Exact Start Alarm
            setExactAlarm(
                triggerTime = startCal.timeInMillis,
                requestCode = (phase.id * 10 + 2).toInt(),
                intent = createIntent(phase, AlarmReceiver.TYPE_START)
            )
        }

        // 2. End Alarm
        if (phase.endAlarmEnabled) {
            val endCal = Calendar.getInstance().apply {
                val hour = phase.endMinutes / 60
                val min = phase.endMinutes % 60
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, min)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (before(today)) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            setExactAlarm(
                triggerTime = endCal.timeInMillis,
                requestCode = (phase.id * 10 + 3).toInt(),
                intent = createIntent(phase, AlarmReceiver.TYPE_END)
            )
        }
    }

    fun cancelPhaseAlarms(phaseId: Long) {
        for (sub in 1..3) {
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                (phaseId * 10 + sub).toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }

    private fun setExactAlarm(triggerTime: Long, requestCode: Int, intent: Intent) {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } catch (_: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    private fun createIntent(phase: Phase, alarmType: String, reminderOffset: Int = 0): Intent {
        return Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TRIGGER_PHASE_ALARM
            putExtra(AlarmReceiver.EXTRA_PHASE_ID, phase.id)
            putExtra(AlarmReceiver.EXTRA_PHASE_TITLE, phase.title)
            putExtra(AlarmReceiver.EXTRA_PHASE_CATEGORY, phase.category)
            putExtra(AlarmReceiver.EXTRA_ALARM_TYPE, alarmType)
            putExtra(AlarmReceiver.EXTRA_START_TIME, phase.formattedStartTime())
            putExtra(AlarmReceiver.EXTRA_END_TIME, phase.formattedEndTime())
            putExtra(AlarmReceiver.EXTRA_DURATION, phase.formattedDuration())
            putExtra(AlarmReceiver.EXTRA_REMINDER_MINS, reminderOffset)
        }
    }
}
