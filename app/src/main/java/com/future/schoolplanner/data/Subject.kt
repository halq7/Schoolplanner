package com.future.schoolplanner.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

data class Subject(
    val id: String,
    val name: String,
    val subjectCode: String = "",
    val teacher: String = "",
    val room: String = "",
    val description: String = "",
    val color: Color,
    val schoolYearId: String
) {
    fun toArgb(): Int = color.toArgb()
}
