package com.future.schoolplanner.data

enum class WeekType {
    A, B
}

data class Lesson(
    val id: String,
    val subjectId: String,
    val dayOfWeek: Int, // 1 = Monday, 2 = Tuesday, ..., 7 = Sunday
    val hour: Int, // 1-10 or whatever
    val weekType: WeekType,
    val teacher: String = "",
    val room: String = "",
    val isVisible: Boolean = true
)