package com.example.ui.neumorphic

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalNeumorphicColors

/**
 * Tactile Circular Neumorphic Icon Button.
 */
@Composable
fun NeumorphicIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    iconSize: Dp = 20.dp,
    tint: Color? = null,
    isActive: Boolean = false,
    testTag: String = "neu_icon_button"
) {
    val neuColors = LocalNeumorphicColors.current
    val actualTint = tint ?: neuColors.textPrimary
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressedState = isPressed || isActive

    val animElevation by animateFloatAsState(
        targetValue = if (pressedState) 1f else 4f,
        animationSpec = spring(),
        label = "icon_btn_press"
    )

    Box(
        modifier = modifier
            .size(size)
            .testTag(testTag)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .then(
                if (pressedState) {
                    Modifier.neumorphicSunken(
                        cornerRadius = size / 2,
                        surfaceColor = neuColors.surface,
                        innerShadowColor = neuColors.shadow,
                        innerHighlightColor = neuColors.highlight,
                        depth = 3.dp
                    )
                } else {
                    Modifier.neumorphicRaised(
                        cornerRadius = size / 2,
                        lightShadowColor = neuColors.highlight,
                        darkShadowColor = neuColors.shadow,
                        surfaceColor = neuColors.surface,
                        offset = animElevation.dp,
                        blur = (animElevation * 2).dp
                    )
                }
            )
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize),
            tint = actualTint
        )
    }
}

/**
 * Inset Neumorphic Pill badge or interactive chip.
 */
@Composable
fun NeumorphicPill(
    text: String,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    accentColor: Color? = null,
    icon: ImageVector? = null,
    testTag: String = "neu_pill"
) {
    val neuColors = LocalNeumorphicColors.current
    val shape = RoundedCornerShape(20.dp)

    val contentColor = when {
        isSelected -> accentColor ?: neuColors.textPrimary
        else -> neuColors.textSecondary
    }

    val mod = modifier
        .testTag(testTag)
        .then(
            if (onClick != null) {
                Modifier.clickable(onClick = onClick)
            } else {
                Modifier
            }
        )
        .then(
            if (isSelected) {
                Modifier.neumorphicSunken(
                    cornerRadius = 20.dp,
                    surfaceColor = neuColors.surface,
                    innerShadowColor = neuColors.shadow,
                    innerHighlightColor = neuColors.highlight,
                    depth = 2.5.dp
                )
            } else {
                Modifier.neumorphicRaised(
                    cornerRadius = 20.dp,
                    lightShadowColor = neuColors.highlight,
                    darkShadowColor = neuColors.shadow,
                    surfaceColor = neuColors.surface,
                    offset = 2.5.dp,
                    blur = 5.dp
                )
            }
        )
        .clip(shape)
        .padding(horizontal = 14.dp, vertical = 8.dp)

    Box(modifier = mod, contentAlignment = Alignment.Center) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(15.dp)
                        .padding(end = 4.dp),
                    tint = contentColor
                )
            }
            Text(
                text = text,
                color = contentColor,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

/**
 * Sunken Inset Text Input Field.
 */
@Composable
fun NeumorphicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    testTag: String = "neu_text_field"
) {
    val neuColors = LocalNeumorphicColors.current
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .neumorphicSunken(
                cornerRadius = 16.dp,
                surfaceColor = neuColors.surface,
                innerShadowColor = neuColors.shadow.copy(alpha = 0.6f),
                innerHighlightColor = neuColors.highlight.copy(alpha = 0.85f),
                depth = 3.dp
            )
            .clip(shape)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = neuColors.textTertiary,
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .size(20.dp)
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    Text(
                        text = placeholder,
                        color = neuColors.textTertiary,
                        fontSize = 14.sp
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = singleLine,
                    textStyle = TextStyle(
                        color = neuColors.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    cursorBrush = SolidColor(neuColors.textPrimary),
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(testTag)
                )
            }
        }
    }
}

/**
 * Tactile Neumorphic Switch.
 */
@Composable
fun NeumorphicSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = Color(0xFF3B82F6),
    testTag: String = "neu_switch"
) {
    val neuColors = LocalNeumorphicColors.current
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 24.dp else 2.dp,
        animationSpec = spring(),
        label = "switch_thumb"
    )

    Box(
        modifier = modifier
            .size(width = 52.dp, height = 30.dp)
            .testTag(testTag)
            .clickable { onCheckedChange(!checked) }
            .neumorphicSunken(
                cornerRadius = 15.dp,
                surfaceColor = if (checked) activeColor.copy(alpha = 0.2f) else neuColors.surface,
                innerShadowColor = neuColors.shadow,
                innerHighlightColor = neuColors.highlight,
                depth = 2.5.dp
            )
            .clip(RoundedCornerShape(15.dp)),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(26.dp)
                .neumorphicRaised(
                    cornerRadius = 13.dp,
                    lightShadowColor = neuColors.highlight,
                    darkShadowColor = neuColors.shadow,
                    surfaceColor = if (checked) activeColor else neuColors.surface,
                    offset = 2.dp,
                    blur = 3.dp
                )
                .clip(CircleShape)
        )
    }
}

/**
 * Sunken Groove Progress Bar.
 */
@Composable
fun NeumorphicProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    fillBrush: Brush? = null,
    fillColor: Color = Color(0xFF3B82F6),
    height: Dp = 14.dp,
    testTag: String = "neu_progress_bar"
) {
    val neuColors = LocalNeumorphicColors.current
    val clamped = progress.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .testTag(testTag)
            .neumorphicSunken(
                cornerRadius = height / 2,
                surfaceColor = neuColors.pillBg,
                innerShadowColor = neuColors.shadow.copy(alpha = 0.65f),
                innerHighlightColor = neuColors.highlight.copy(alpha = 0.85f),
                depth = 2.5.dp
            )
            .clip(RoundedCornerShape(height / 2))
    ) {
        val brush = fillBrush ?: Brush.horizontalGradient(
            listOf(fillColor.copy(alpha = 0.85f), fillColor)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(clamped)
                .fillMaxHeight()
                .clip(RoundedCornerShape(height / 2))
                .background(brush)
        )
    }
}
