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
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.LayersClear
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.DailyTemplate
import com.example.ui.neumorphic.NeumorphicButton
import com.example.ui.neumorphic.NeumorphicCard
import com.example.ui.neumorphic.NeumorphicSunkenCard
import com.example.ui.neumorphic.NeumorphicTextField
import com.example.ui.theme.LocalNeumorphicColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateDialog(
    templates: List<DailyTemplate>,
    onDismiss: () -> Unit,
    onLoadTemplate: (DailyTemplate) -> Unit,
    onSaveCurrentDayAsTemplate: (String) -> Unit,
    onCopyYesterday: () -> Unit,
    onStartBlankDay: () -> Unit
) {
    val neuColors = LocalNeumorphicColors.current
    var isSavingNew by remember { mutableStateOf(false) }
    var newTemplateName by remember { mutableStateOf("") }

    val presetTemplates = listOf(
        "School Day",
        "Work Day",
        "Exam Day",
        "Weekend",
        "Gym Day"
    )

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        NeumorphicCard(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("template_dialog"),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.FolderOpen,
                            contentDescription = null,
                            tint = neuColors.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Daily Templates",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = neuColors.textPrimary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = neuColors.textTertiary, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Fast Action Buttons: Copy Yesterday & Blank Day
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NeumorphicButton(
                        onClick = {
                            onCopyYesterday()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        cornerRadius = 12.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp), tint = neuColors.textPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Yesterday", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = neuColors.textPrimary)
                        }
                    }

                    NeumorphicButton(
                        onClick = {
                            onStartBlankDay()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        cornerRadius = 12.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Outlined.LayersClear, contentDescription = null, modifier = Modifier.size(14.dp), tint = neuColors.textSecondary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Blank Day", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = neuColors.textSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Presets Section
                Text(
                    text = "PRESET SCHEDULES",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = neuColors.textTertiary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                presetTemplates.forEach { presetName ->
                    NeumorphicSunkenCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        cornerRadius = 12.dp,
                        depth = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.BookmarkBorder, contentDescription = null, tint = neuColors.textSecondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(presetName, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = neuColors.textPrimary)
                            }
                            NeumorphicButton(
                                onClick = {
                                    onSaveCurrentDayAsTemplate(presetName)
                                    onDismiss()
                                },
                                cornerRadius = 8.dp
                            ) {
                                Text("Load", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = neuColors.textPrimary, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // User Saved Templates
                if (templates.isNotEmpty()) {
                    Text(
                        text = "SAVED TEMPLATES",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = neuColors.textTertiary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    templates.forEach { tmpl ->
                        NeumorphicSunkenCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            cornerRadius = 12.dp,
                            depth = 2.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(tmpl.name, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = neuColors.textPrimary)
                                NeumorphicButton(
                                    onClick = {
                                        onLoadTemplate(tmpl)
                                        onDismiss()
                                    },
                                    cornerRadius = 8.dp
                                ) {
                                    Text("Apply", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = neuColors.textPrimary, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Save Current Day as Template
                if (!isSavingNew) {
                    NeumorphicButton(
                        onClick = { isSavingNew = true },
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 12.dp
                    ) {
                        Text(
                            text = "+ Save Current Day as Template",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = neuColors.textPrimary,
                            modifier = Modifier.padding(vertical = 9.dp).align(Alignment.Center)
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NeumorphicTextField(
                            value = newTemplateName,
                            onValueChange = { newTemplateName = it },
                            placeholder = "Template Name",
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        NeumorphicButton(
                            onClick = {
                                if (newTemplateName.isNotBlank()) {
                                    onSaveCurrentDayAsTemplate(newTemplateName)
                                    isSavingNew = false
                                    onDismiss()
                                }
                            },
                            cornerRadius = 8.dp
                        ) {
                            Text("Save", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = neuColors.textPrimary, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                        }
                    }
                }
            }
        }
    }
}
