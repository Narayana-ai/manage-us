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
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.ui.neumorphic.NeumorphicButton
import com.example.ui.neumorphic.NeumorphicCard
import com.example.ui.neumorphic.NeumorphicSunkenCard
import com.example.ui.theme.LocalNeumorphicColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoArrangeDialog(
    onDismiss: () -> Unit,
    onAutoArrange: (
        sleepHours: Int,
        studyHours: Int,
        workHours: Int,
        exerciseHours: Int,
        socialHours: Int,
        foodHours: Int,
        personalHours: Int
    ) -> Unit
) {
    val neuColors = LocalNeumorphicColors.current

    var sleepHours by remember { mutableIntStateOf(8) }
    var studyHours by remember { mutableIntStateOf(4) }
    var workHours by remember { mutableIntStateOf(4) }
    var exerciseHours by remember { mutableIntStateOf(1) }
    var foodHours by remember { mutableIntStateOf(2) }
    var socialHours by remember { mutableIntStateOf(2) }
    var personalHours by remember { mutableIntStateOf(3) }

    val totalHours = sleepHours + studyHours + workHours + exerciseHours + foodHours + socialHours + personalHours

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        NeumorphicCard(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("auto_arrange_dialog"),
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
                    Column {
                        Text(
                            text = "Auto Arrange Schedule",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = neuColors.textPrimary
                        )
                        Text(
                            text = "Distribute your 24 hours by target hours",
                            fontSize = 11.sp,
                            color = neuColors.textTertiary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = neuColors.textTertiary, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Total hours monitor card
                NeumorphicSunkenCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 14.dp,
                    depth = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Target Hours", fontSize = 12.sp, color = neuColors.textSecondary)
                        Text(
                            text = "$totalHours / 24 hrs",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = neuColors.textPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Target Hours Steppers with custom vector icons
                HourTargetStepper(icon = Icons.Outlined.Bedtime, label = "Sleep", hours = sleepHours, onHoursChange = { sleepHours = it })
                Spacer(modifier = Modifier.height(8.dp))
                HourTargetStepper(icon = Icons.Outlined.MenuBook, label = "Study", hours = studyHours, onHoursChange = { studyHours = it })
                Spacer(modifier = Modifier.height(8.dp))
                HourTargetStepper(icon = Icons.Outlined.WorkOutline, label = "Work", hours = workHours, onHoursChange = { workHours = it })
                Spacer(modifier = Modifier.height(8.dp))
                HourTargetStepper(icon = Icons.Outlined.FitnessCenter, label = "Exercise", hours = exerciseHours, onHoursChange = { exerciseHours = it })
                Spacer(modifier = Modifier.height(8.dp))
                HourTargetStepper(icon = Icons.Outlined.Restaurant, label = "Meals", hours = foodHours, onHoursChange = { foodHours = it })
                Spacer(modifier = Modifier.height(8.dp))
                HourTargetStepper(icon = Icons.Outlined.People, label = "Social", hours = socialHours, onHoursChange = { socialHours = it })
                Spacer(modifier = Modifier.height(8.dp))
                HourTargetStepper(icon = Icons.Outlined.Person, label = "Personal", hours = personalHours, onHoursChange = { personalHours = it })

                Spacer(modifier = Modifier.height(20.dp))

                // Action buttons
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
                            onAutoArrange(
                                sleepHours,
                                studyHours,
                                workHours,
                                exerciseHours,
                                socialHours,
                                foodHours,
                                personalHours
                            )
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Generate",
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
private fun HourTargetStepper(
    icon: ImageVector,
    label: String,
    hours: Int,
    onHoursChange: (Int) -> Unit
) {
    val neuColors = LocalNeumorphicColors.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = neuColors.textSecondary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = neuColors.textPrimary)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            NeumorphicButton(
                onClick = { if (hours > 1) onHoursChange(hours - 1) },
                cornerRadius = 8.dp
            ) {
                Text("-1h", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = neuColors.textSecondary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${hours}h",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = neuColors.textPrimary,
                modifier = Modifier.width(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            NeumorphicButton(
                onClick = { if (hours < 16) onHoursChange(hours + 1) },
                cornerRadius = 8.dp
            ) {
                Text("+1h", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = neuColors.textSecondary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
            }
        }
    }
}
