package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class NeumorphicColors(
    val background: Color,
    val surface: Color,
    val border: Color,
    val sunken: Color,
    val highlight: Color,
    val shadow: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val pillBg: Color,
    val blackDock: Color,
    val isDark: Boolean
)

val LocalNeumorphicColors = staticCompositionLocalOf {
    NeumorphicColors(
        background = NeuLightBg,
        surface = NeuLightSurface,
        border = NeuLightBorder,
        sunken = NeuLightSunken,
        highlight = NeuLightHighlight,
        shadow = NeuLightShadow,
        textPrimary = NeuLightTextPrimary,
        textSecondary = NeuLightTextSecondary,
        textTertiary = NeuLightTextTertiary,
        pillBg = NeuLightPillBg,
        blackDock = NeuBlackDock,
        isDark = false
    )
}

private val LightNeuColors = NeumorphicColors(
    background = NeuLightBg,
    surface = NeuLightSurface,
    border = NeuLightBorder,
    sunken = NeuLightSunken,
    highlight = NeuLightHighlight,
    shadow = NeuLightShadow,
    textPrimary = NeuLightTextPrimary,
    textSecondary = NeuLightTextSecondary,
    textTertiary = NeuLightTextTertiary,
    pillBg = NeuLightPillBg,
    blackDock = NeuBlackDock,
    isDark = false
)

private val DarkNeuColors = NeumorphicColors(
    background = NeuDarkBg,
    surface = NeuDarkSurface,
    border = NeuDarkBorder,
    sunken = NeuDarkSunken,
    highlight = NeuDarkHighlight,
    shadow = NeuDarkShadow,
    textPrimary = NeuDarkTextPrimary,
    textSecondary = NeuDarkTextSecondary,
    textTertiary = NeuDarkTextTertiary,
    pillBg = NeuDarkPillBg,
    blackDock = NeuDarkSurface,
    isDark = true
)

private val DarkColorScheme = darkColorScheme(
    primary = CategoryStudy,
    secondary = CategoryWork,
    background = NeuDarkBg,
    surface = NeuDarkSurface,
    onPrimary = Color.White,
    onBackground = NeuDarkTextPrimary,
    onSurface = NeuDarkTextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = CategoryStudy,
    secondary = CategoryWork,
    background = NeuLightBg,
    surface = NeuLightSurface,
    onPrimary = Color.White,
    onBackground = NeuLightTextPrimary,
    onSurface = NeuLightTextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val neuColors = if (darkTheme) DarkNeuColors else LightNeuColors
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalNeumorphicColors provides neuColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

