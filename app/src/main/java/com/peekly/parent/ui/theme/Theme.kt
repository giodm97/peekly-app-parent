package com.peekly.parent.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary              = WarmPrimary,
    onPrimary            = Color.White,
    primaryContainer     = WarmPrimarySoft,
    onPrimaryContainer   = WarmInk,
    secondary            = WarmGreen,
    onSecondary          = Color.White,
    secondaryContainer   = WarmGreenBg,
    onSecondaryContainer = WarmInk,
    background           = WarmBg,
    onBackground         = WarmInk,
    surface              = WarmSurface,
    onSurface            = WarmInk,
    surfaceVariant       = WarmSurfaceAlt,
    onSurfaceVariant     = WarmMuted,
    outline              = WarmHairline,
    error                = WarmWarn,
    onError              = Color.White,
)

@Composable
fun PeeklyParentTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography  = Typography,
        content     = content
    )
}
