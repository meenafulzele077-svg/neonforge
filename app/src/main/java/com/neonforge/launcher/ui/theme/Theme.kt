package com.neonforge.launcher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NfDarkColors = darkColorScheme(
    primary = NfCyan,
    onPrimary = Color.Black,
    primaryContainer = NfSurfaceHigh,
    onPrimaryContainer = NfTextPrimary,
    secondary = NfViolet,
    onSecondary = Color.White,
    tertiary = NfMagenta,
    onTertiary = Color.Black,
    background = NfMidnight,
    onBackground = NfTextPrimary,
    surface = NfSurface,
    onSurface = NfTextPrimary,
    surfaceVariant = NfSurfaceHigh,
    onSurfaceVariant = NfTextSecondary,
    outline = NfOutline,
    error = NfError,
    onError = Color.Black
)

@Composable
fun NeonForgeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NfDarkColors,
        typography = NfTypography,
        shapes = NfShapes,
        content = content
    )
}
