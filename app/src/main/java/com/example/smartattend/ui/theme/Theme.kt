package com.example.smartattend.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SmartAttendLightColorScheme = lightColorScheme(
    primary = Color(0xFF1565D8),
    onPrimary = Color.White,

    primaryContainer = Color(0xFFDCEBFF),
    onPrimaryContainer = Color(0xFF0B3D91),

    secondary = Color(0xFF4F6F9F),
    onSecondary = Color.White,

    secondaryContainer = Color(0xFFE6EEFF),
    onSecondaryContainer = Color(0xFF1B2E59),

    background = Color(0xFFF5F7FB),
    onBackground = Color(0xFF111827),

    surface = Color.White,
    onSurface = Color(0xFF111827),

    surfaceVariant = Color(0xFFF5F7FB),
    onSurfaceVariant = Color(0xFF6B7280),

    error = Color(0xFFDC2626),
    onError = Color.White,

    errorContainer = Color(0xFFFFE4E6),
    onErrorContainer = Color(0xFF7F1D1D)
)

@Composable
fun SmartAttendTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme: ColorScheme = SmartAttendLightColorScheme

    val view = LocalView.current

    if (!view.isInEditMode) {
        val window = (view.context as Activity).window
        window.statusBarColor = Color(0xFFF5F7FB).hashCode()
        window.navigationBarColor = Color(0xFFF5F7FB).hashCode()

        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}