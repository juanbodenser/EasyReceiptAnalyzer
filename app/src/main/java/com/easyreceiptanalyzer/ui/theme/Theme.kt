package com.easyreceiptanalyzer.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ControlColorScheme = darkColorScheme(
    primary = Accent,
    onPrimary = Ink,
    primaryContainer = AccentDim,
    onPrimaryContainer = Mist,
    secondary = AccentCyan,
    onSecondary = Ink,
    tertiary = Warning,
    onTertiary = Ink,
    background = Ink,
    onBackground = Mist,
    surface = InkSurface,
    onSurface = Mist,
    surfaceVariant = InkSurfaceHigh,
    onSurfaceVariant = MistMuted,
    error = Danger,
    onError = Ink,
    outline = MistMuted
)

@Composable
fun EasyReceiptAnalyzerTheme(
    darkTheme: Boolean = true,      // Ignorado, siempre oscuro
    dynamicColor: Boolean = false,  // Ignorado
    content: @Composable () -> Unit
) {
    val colorScheme = ControlColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Ink.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
