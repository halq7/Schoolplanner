package com.future.schoolplanner.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Custom light color scheme optimized for readability
val LightColorScheme = lightColorScheme(
    primary = Color(0xFF000000), // Blue 700 - good contrast
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFFFFF), // Light blue
    onPrimaryContainer = Color(0xFF000000), // Dark blue
    secondary = Color(0xFF388E3C), // Green 700
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3E3E3), // Light green
    onSecondaryContainer = Color(0xFF000000), // Dark green
    tertiary = Color(0xFFF57C00), // Orange 700
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE0B2), // Light orange
    onTertiaryContainer = Color(0xFFE65100), // Dark orange
    error = Color(0xFFD32F2F), // Red 700
    onError = Color.White,
    errorContainer = Color(0xFFFFCDD2), // Light red
    onErrorContainer = Color(0xFFB71C1C), // Dark red
    background = Color(0xFFFAFAFA), // Very light gray
    onBackground = Color(0xFF212121), // Dark gray for text
    surface = Color.White,
    onSurface = Color(0xFF212121), // Dark gray for text
    surfaceVariant = Color(0xFFF5F5F5), // Light gray
    onSurfaceVariant = Color(0xFF616161), // Medium gray
    outline = Color(0xFFBDBDBD) // Light gray for outlines
)

@Composable
fun LightTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}