package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Phase
import com.example.ui.neumorphic.NeumorphicButton
import com.example.ui.neumorphic.NeumorphicCard
import com.example.ui.theme.LocalNeumorphicColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkipShiftConfirmDialog(
    phase: Phase,
    onDismiss: () -> Unit,
    onConfirm: (shiftSchedule: Boolean) -> Unit
) {
    val neuColors = LocalNeumorphicColors.current

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        NeumorphicCard(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .testTag("skip_shift_dialog"),
            cornerRadius = 20.dp,
            elevationOffset = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Skip \"${phase.title}\"?",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = neuColors.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Shift the remaining schedule forward by ${phase.formattedDuration()} or keep existing phase times?",
                    fontSize = 13.sp,
                    color = neuColors.textSecondary
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NeumorphicButton(
                        onClick = { onConfirm(false) },
                        modifier = Modifier.weight(1f),
                        cornerRadius = 10.dp
                    ) {
                        Text(
                            text = "Keep Times",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = neuColors.textSecondary,
                            modifier = Modifier.padding(vertical = 10.dp).align(Alignment.Center)
                        )
                    }

                    NeumorphicButton(
                        onClick = { onConfirm(true) },
                        modifier = Modifier.weight(1.2f),
                        cornerRadius = 10.dp
                    ) {
                        Text(
                            text = "Shift Schedule",
                            fontSize = 12.sp,
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
