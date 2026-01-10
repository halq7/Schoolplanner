package com.future.schoolplanner.data

enum class TaskType {
    TASK,      // Normale Aufgabe
    APPOINTMENT // Termin (z.B. Test)
}

data class Task(
    val id: String,
    val title: String,
    val description: String = "",
    val type: TaskType = TaskType.TASK,
    val dueDate: String, // Date X - Haupttermin (YYYY-MM-DD)
    val dueTime: String? = null, // Uhrzeit für Fälligkeitsdatum (HH:mm), null bedeutet ganztägig
    val isCompleted: Boolean = false,
    val subjectId: String? = null, // Optional für Zuordnung zu einem Fach
    val schoolYearId: String
)
