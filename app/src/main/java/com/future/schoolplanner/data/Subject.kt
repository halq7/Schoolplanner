package com.future.schoolplanner.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

enum class GradeInputMethod {
    WHOLE,      // Ganze Noten (z.B. 2)
    DECIMAL,    // Mit Komma (z.B. 1.5)
    TENDENCY,   // Mit Tendenz (z.B. 2-)
    FIFTEEN_POINT // 15-Punktesystem
}

data class Subject(
    val id: String,
    val name: String,
    val abbreviation: String = "",
    val teacher: String = "",
    val room: String = "",
    val description: String = "",
    val color: Color,
    val grades: List<Grade> = emptyList()
) {
    fun toArgb(): Int = color.toArgb()
}

data class Grade(
    val id: String,
    val value: Double,
    val weight: Double = 1.0,
    val description: String = "",
    val date: String = ""
)
