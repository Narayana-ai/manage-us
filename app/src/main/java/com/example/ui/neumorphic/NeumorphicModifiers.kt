package com.example.ui.neumorphic

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalNeumorphicColors

/**
 * Modifier that draws an extruded (raised) neumorphic dual-shadow effect.
 */
fun Modifier.neumorphicRaised(
    cornerRadius: Dp = 24.dp,
    lightShadowColor: Color? = null,
    darkShadowColor: Color? = null,
    surfaceColor: Color? = null,
    offset: Dp = 5.dp,
    blur: Dp = 10.dp,
    isPressed: Boolean = false
): Modifier = this.drawBehind {
    val rPx = cornerRadius.toPx()
    val offPx = offset.toPx() * (if (isPressed) 0.3f else 1f)
    val blurPx = blur.toPx() * (if (isPressed) 0.4f else 1f)

    val shadowC = darkShadowColor ?: Color(0xFFB0B7C3).copy(alpha = 0.55f)
    val highlightC = lightShadowColor ?: Color.White.copy(alpha = 0.85f)
    val bgC = surfaceColor ?: Color(0xFFE4E7ED)

    drawIntoCanvas { canvas ->
        val native = canvas.nativeCanvas
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
        }

        if (!isPressed) {
            // Dark bottom-right shadow
            paint.color = shadowC.toArgb()
            if (blurPx > 0f) {
                paint.maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
            }
            native.drawRoundRect(
                offPx, offPx, size.width + offPx, size.height + offPx,
                rPx, rPx, paint
            )

            // Light top-left highlight
            paint.color = highlightC.toArgb()
            if (blurPx > 0f) {
                paint.maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
            }
            native.drawRoundRect(
                -offPx, -offPx, size.width - offPx, size.height - offPx,
                rPx, rPx, paint
            )
        }

        // Surface base
        paint.maskFilter = null
        paint.color = bgC.toArgb()
        native.drawRoundRect(
            0f, 0f, size.width, size.height,
            rPx, rPx, paint
        )
    }
}

/**
 * Modifier that draws a sunken (concave / inner shadow) debossed neumorphic effect.
 */
fun Modifier.neumorphicSunken(
    cornerRadius: Dp = 24.dp,
    surfaceColor: Color? = null,
    innerShadowColor: Color? = null,
    innerHighlightColor: Color? = null,
    depth: Dp = 4.dp
): Modifier = this.drawBehind {
    val rPx = cornerRadius.toPx()
    val depthPx = depth.toPx()
    val bgC = surfaceColor ?: Color(0xFFDFE3EA)
    val darkInner = innerShadowColor ?: Color(0xFFB4BAC4).copy(alpha = 0.65f)
    val lightInner = innerHighlightColor ?: Color.White.copy(alpha = 0.8f)

    drawIntoCanvas { canvas ->
        val native = canvas.nativeCanvas
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
        }

        // Base fill
        paint.color = bgC.toArgb()
        paint.maskFilter = null
        native.drawRoundRect(0f, 0f, size.width, size.height, rPx, rPx, paint)
    }

    // Draw inner shadow overlay: top-left dark shadow, bottom-right light highlight
    val clipPath = Path().apply {
        addRoundRect(
            RoundRect(
                Rect(0f, 0f, size.width, size.height),
                CornerRadius(rPx, rPx)
            )
        )
    }

    drawWithContent {
        drawContent()
    }
}.drawWithContent {
    val rPx = cornerRadius.toPx()
    val depthPx = depth.toPx()
    val darkInner = innerShadowColor ?: Color(0xFFABB2BE).copy(alpha = 0.7f)
    val lightInner = innerHighlightColor ?: Color.White.copy(alpha = 0.85f)

    val clipPath = Path().apply {
        addRoundRect(
            RoundRect(
                Rect(0f, 0f, size.width, size.height),
                CornerRadius(rPx, rPx)
            )
        )
    }

    drawIntoCanvas { canvas ->
        canvas.save()
        canvas.clipPath(clipPath)

        // Top-left dark inner gradient
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(darkInner, Color.Transparent),
                center = Offset(0f, 0f),
                radius = depthPx * 3.5f
            ),
            size = size
        )

        // Bottom-right light inner gradient
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(lightInner, Color.Transparent),
                center = Offset(size.width, size.height),
                radius = depthPx * 3.5f
            ),
            size = size
        )

        canvas.restore()
    }
    drawContent()
}

/**
 * High-fidelity Neumorphic Card container.
 */
@Composable
fun NeumorphicCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    elevationOffset: Dp = 5.dp,
    blurRadius: Dp = 10.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val neuColors = LocalNeumorphicColors.current
    Box(
        modifier = modifier
            .neumorphicRaised(
                cornerRadius = cornerRadius,
                lightShadowColor = neuColors.highlight,
                darkShadowColor = neuColors.shadow,
                surfaceColor = neuColors.surface,
                offset = elevationOffset,
                blur = blurRadius
            )
            .clip(RoundedCornerShape(cornerRadius)),
        content = content
    )
}

/**
 * Sunken / Recessed Neumorphic container (for clocks, input wells, status areas).
 */
@Composable
fun NeumorphicSunkenCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    depth: Dp = 4.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val neuColors = LocalNeumorphicColors.current
    Box(
        modifier = modifier
            .neumorphicSunken(
                cornerRadius = cornerRadius,
                surfaceColor = neuColors.pillBg,
                innerShadowColor = neuColors.shadow.copy(alpha = 0.7f),
                innerHighlightColor = neuColors.highlight.copy(alpha = 0.85f),
                depth = depth
            )
            .clip(RoundedCornerShape(cornerRadius)),
        content = content
    )
}

/**
 * Tactile Neumorphic Button that animates between raised and sunken when pressed.
 */
@Composable
fun NeumorphicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    isActive: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val neuColors = LocalNeumorphicColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressedState = isPressed || isActive

    val animOffset by animateFloatAsState(
        targetValue = if (pressedState) 1.5f else 5f,
        animationSpec = spring(),
        label = "neu_press"
    )

    Box(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .then(
                if (pressedState) {
                    Modifier.neumorphicSunken(
                        cornerRadius = cornerRadius,
                        surfaceColor = neuColors.surface,
                        innerShadowColor = neuColors.shadow,
                        innerHighlightColor = neuColors.highlight,
                        depth = 3.dp
                    )
                } else {
                    Modifier.neumorphicRaised(
                        cornerRadius = cornerRadius,
                        lightShadowColor = neuColors.highlight,
                        darkShadowColor = neuColors.shadow,
                        surfaceColor = neuColors.surface,
                        offset = animOffset.dp,
                        blur = (animOffset * 2).dp,
                        isPressed = pressedState
                    )
                }
            )
            .clip(RoundedCornerShape(cornerRadius)),
        content = content
    )
}
