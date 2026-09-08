package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.ViewTimeline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GroteskFontFamily
import com.example.ui.theme.HelveticaFontFamily
import com.example.ui.theme.LocalNeumorphicColors

enum class AppViewMode {
    TIMELINE,
    RADIAL_DIAL,
    VALIDATION
}

/**
 * Floating Matte Black Capsule Dock directly modeled from Images 2 & 3:
 * - Matte black rounded pill container floating at bottom
 * - Left interconnected circle badge
 * - Middle segmented tabs with active white sliding pill indicator
 * - Right quick-action button
 */
@Composable
fun FloatingCapsuleDock(
    currentMode: AppViewMode,
    onModeSelect: (AppViewMode) -> Unit,
    onQuickAdd: () -> Unit,
    onOpenIntelligence: () -> Unit,
    modifier: Modifier = Modifier
) {
    val neuColors = LocalNeumorphicColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag("floating_capsule_dock"),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Connected Left Circular Node (Image 3 style)
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(Color(0xFF141416))
                .clickable { onOpenIntelligence() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = "Routine Intelligence",
                tint = Color(0xFFF2F1EB),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Main Capsule Container (Image 2 & 3 style)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .shadow(12.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF141416))
                .padding(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab 1: Timeline
                DockTab(
                    title = "Timeline",
                    isSelected = currentMode == AppViewMode.TIMELINE,
                    onClick = { onModeSelect(AppViewMode.TIMELINE) },
                    modifier = Modifier.weight(1f)
                )

                // Thin divider line (Image 3)
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(18.dp)
                        .background(Color(0xFF2A2A2E))
                )

                // Tab 2: Radial Dial (Image 1 feature)
                DockTab(
                    title = "Petal Dial",
                    isSelected = currentMode == AppViewMode.RADIAL_DIAL,
                    onClick = { onModeSelect(AppViewMode.RADIAL_DIAL) },
                    modifier = Modifier.weight(1f)
                )

                // Thin divider line
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(18.dp)
                        .background(Color(0xFF2A2A2E))
                )

                // Tab 3: Validate 24h
                DockTab(
                    title = "24h Check",
                    isSelected = currentMode == AppViewMode.VALIDATION,
                    onClick = { onModeSelect(AppViewMode.VALIDATION) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Right Quick Action Pill (Image 3 right node)
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(Color(0xFFFAF9F6))
                .clickable { onQuickAdd() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "New Phase",
                tint = Color(0xFF141416),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun DockTab(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgAnim by animateColorAsState(
        targetValue = if (isSelected) Color(0xFFFAF9F6) else Color.Transparent,
        label = "dock_tab_bg"
    )
    val textAnim by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF141416) else Color(0xFFA09E96),
        label = "dock_tab_text"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgAnim)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontFamily = if (isSelected) GroteskFontFamily else HelveticaFontFamily,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textAnim,
            letterSpacing = if (isSelected) 0.sp else 0.4.sp,
            maxLines = 1
        )
    }
}
