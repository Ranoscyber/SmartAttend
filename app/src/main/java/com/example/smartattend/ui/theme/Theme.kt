package com.example.smartattend.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1565D8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD8E7FF),
    onPrimaryContainer = Color(0xFF0B3A78),

    secondary = Color(0xFF3B82F6),
    onSecondary = Color.White,

    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),

    surface = Color.White,
    onSurface = Color(0xFF0F172A),

    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF64748B),

    error = Color(0xFFDC2626),
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF60A5FA),
    onPrimary = Color(0xFF082F49),
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),

    secondary = Color(0xFF93C5FD),
    onSecondary = Color(0xFF082F49),

    background = Color(0xFF020617),
    onBackground = Color(0xFFE2E8F0),

    surface = Color(0xFF0F172A),
    onSurface = Color(0xFFE2E8F0),

    surfaceVariant = Color(0xFF111827),
    onSurfaceVariant = Color(0xFFCBD5E1),

    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2)
)

@Composable
fun SmartAttendTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}