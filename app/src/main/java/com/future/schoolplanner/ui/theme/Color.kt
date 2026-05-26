package com.future.schoolplanner.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.sqrt

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Calculate the relative luminance of a color
fun Color.luminance(): Float {
    val r = red.coerceIn(0f, 1f)
    val g = green.coerceIn(0f, 1f)
    val b = blue.coerceIn(0f, 1f)

    // Use the formula for relative luminance
    return 0.2126f * r + 0.7152f * g + 0.0722f * b
}

// Blend this color (foreground) over a background color
fun Color.blendOver(background: Color): Color {
    val alpha = this.alpha
    return Color(
        red = this.red * alpha + background.red * (1 - alpha),
        green = this.green * alpha + background.green * (1 - alpha),
        blue = this.blue * alpha + background.blue * (1 - alpha),
        alpha = 1f
    )
}

// Get appropriate text color (black or white) for a given background color
fun Color.getContrastingTextColor(): Color {
    return if (luminance() > 0.5f) Color.Black else Color.White
}
