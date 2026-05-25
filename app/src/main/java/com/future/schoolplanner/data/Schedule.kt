package com.future.schoolplanner.data

data class Lesson(
    val id: String,
    val subjectId: String,
    val dayOfWeek: Int, // 1 = Monday, 2 = Tuesday, ..., 7 = Sunday
    val startTime: String = "08:00",
    val endTime: String = "08:45",
    val hour: Int = 1, // Keep for backward compatibility or as a sequence number
    val teacher: String = "",
    val room: String = "",
    val isVisible: Boolean = true,
    val schoolYearId: String
)
