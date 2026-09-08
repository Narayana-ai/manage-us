package com.example.ui.dialogs

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
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.MoreTime
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.Snooze
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.receiver.ActiveAlarmEvent
import com.example.receiver.AlarmReceiver
import com.example.ui.neumorphic.NeumorphicButton
import com.example.ui.neumorphic.NeumorphicCard
import com.example.ui.neumorphic.NeumorphicSunkenCard
import com.example.ui.theme.LocalNeumorphicColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmTriggerDialog(
    alarmEvent: ActiveAlarmEvent,
    onDismiss: () -> Unit,
    onStartPhase: () -> Unit,
    onSnooze: () -> Unit,
    onSkipPhase: () -> Unit,
    onCompletePhase: () -> Unit,
    onExtend15m: () -> Unit,
    onStartNextPhase: () -> Unit
) {
    val neuColors = LocalNeumorphicColors.current
    val isPhaseEnd = alarmEvent.alarmType == AlarmReceiver.TYPE_END

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        NeumorphicCard(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("alarm_trigger_dialog"),
            cornerRadius = 24.dp,
            elevationOffset = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Monochrome Icon in Inset Circle
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(neuColors.pillBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPhaseEnd) Icons.Outlined.CheckCircle else Icons.Outlined.Alarm,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp),
                        tint = neuColors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (isPhaseEnd) "PHASE COMPLETE" else "PHASE STARTING",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = neuColors.textTertiary,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = alarmEvent.title,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = neuColors.textPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Time info box
                NeumorphicSunkenCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 14.dp,
                    depth = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${alarmEvent.startTime} – ${alarmEvent.endTime}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = neuColors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Duration: ${alarmEvent.duration}",
                            fontSize = 12.sp,
                            color = neuColors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (!isPhaseEnd) {
                    // Start Alarm Options: Start, Snooze, Skip
                    NeumorphicButton(
                        onClick = onStartPhase,
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 12.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Outlined.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp), tint = neuColors.textPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Start Phase Now", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = neuColors.textPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NeumorphicButton(
                            onClick = onSnooze,
                            modifier = Modifier.weight(1f),
                            cornerRadius = 12.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Outlined.Snooze, contentDescription = null, modifier = Modifier.size(14.dp), tint = neuColors.textSecondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Snooze 5m", fontSize = 12.sp, color = neuColors.textSecondary)
                            }
                        }

                        NeumorphicButton(
                            onClick = onSkipPhase,
                            modifier = Modifier.weight(1f),
                            cornerRadius = 12.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Outlined.SkipNext, contentDescription = null, modifier = Modifier.size(14.dp), tint = neuColors.textSecondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Skip", fontSize = 12.sp, color = neuColors.textSecondary)
                            }
                        }
                    }
                } else {
                    // End Alarm Options: Complete, Extend 15m, Start Next
                    NeumorphicButton(
                        onClick = onCompletePhase,
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 12.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Outlined.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = neuColors.textPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Complete Session", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = neuColors.textPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NeumorphicButton(
                            onClick = onExtend15m,
                            modifier = Modifier.weight(1f),
                            cornerRadius = 12.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Outlined.MoreTime, contentDescription = null, modifier = Modifier.size(14.dp), tint = neuColors.textSecondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Extend +15m", fontSize = 12.sp, color = neuColors.textSecondary)
                            }
                        }

                        NeumorphicButton(
                            onClick = onStartNextPhase,
                            modifier = Modifier.weight(1f),
                            cornerRadius = 12.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Outlined.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp), tint = neuColors.textSecondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Next Phase", fontSize = 12.sp, color = neuColors.textSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}
