package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NominalHudColorScheme = darkColorScheme(
    primary = ArcCyan,
    onPrimary = HudPitchBlack,
    primaryContainer = ArcDeepBlue,
    onPrimaryContainer = ArcCyanBright,
    secondary = ArcElectricBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF0A2342),
    onSecondaryContainer = ArcCyanBright,
    tertiary = StarkGold,
    onTertiary = HudPitchBlack,
    error = StarkCrimson,
    onError = Color.White,
    background = HudPitchBlack,
    onBackground = HudTextPrimary,
    surface = HudDeepSpace,
    onSurface = HudTextPrimary,
    surfaceVariant = HudPanelGlass,
    onSurfaceVariant = HudTextSecondary,
    outline = ArcCyan
)

private val RedAlertHudColorScheme = darkColorScheme(
    primary = StarkCrimson,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4A0610),
    onPrimaryContainer = StarkCrimsonGlow,
    secondary = StarkGold,
    onSecondary = HudPitchBlack,
    secondaryContainer = Color(0xFF3B1A00),
    onSecondaryContainer = StarkGold,
    tertiary = StarkOrange,
    onTertiary = HudPitchBlack,
    error = StarkCrimsonGlow,
    onError = HudPitchBlack,
    background = Color(0xFF080204),
    onBackground = Color(0xFFFFECEF),
    surface = Color(0xFF140408),
    onSurface = Color(0xFFFFECEF),
    surfaceVariant = HudPanelRedGlass,
    onSurfaceVariant = HudTextRedSecondary,
    outline = StarkCrimson
)

@Composable
fun MyApplicationTheme(
    isRedAlert: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (isRedAlert) RedAlertHudColorScheme else NominalHudColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
