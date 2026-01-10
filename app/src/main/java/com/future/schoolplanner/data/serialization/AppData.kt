package com.future.schoolplanner.data.serialization

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import com.future.schoolplanner.data.*

@Serializable
data class AppData(
    val subjects: List<SerializableSubject> = emptyList(),
    val schoolYears: List<SerializableSchoolYear> = emptyList(),
    val currentSchoolYearId: String? = null,
    val lessons: List<SerializableLesson> = emptyList(),
    val reports: List<SerializableReport> = emptyList(),
    val tasks: List<SerializableTask> = emptyList(),
    val settings: AppSettings = AppSettings()
)

@Serializable
data class AppSettings(
    val gradeInputMethod: String = "DECIMAL",
    val showTeachers: Boolean = true,
    val showRooms: Boolean = true,
    val isDarkTheme: Boolean = true,
    val useDynamicColors: Boolean = true,
    val useAmoledTheme: Boolean = false,
    val customAccentColor: Int = 0xFF4CAF50.toInt(), // Default green
    val tasksTabEnabled: Boolean = false,
    val weekTypeEvenWeeks: String = "A" // Which week type corresponds to even weeks
)

// Serializable versions of data classes
@Serializable
data class SerializableSubject(
    val id: String,
    val name: String,
    val abbreviation: String = "",
    val teacher: String = "",
    val room: String = "",
    val description: String = "",
    val color: Int, // Store as ARGB integer
    val grades: List<SerializableGrade> = emptyList(),
    val schoolYearId: String
)

@Serializable
data class SerializableGrade(
    val id: String,
    val value: Double,
    val weight: Double = 1.0,
    val description: String = "",
    val date: String = ""
)

@Serializable
data class SerializableSchoolYear(
    val id: String,
    val name: String,
    val description: String = "",
    val startDate: String,
    val endDate: String
)

@Serializable
data class SerializableLesson(
    val id: String,
    val subjectId: String,
    val dayOfWeek: Int,
    val hour: Int,
    val weekType: String, // Store as string
    val teacher: String = "",
    val room: String = "",
    val isVisible: Boolean = true,
    val schoolYearId: String
)

@Serializable
data class SerializableTask(
    val id: String,
    val title: String,
    val description: String = "",
    val type: String = "TASK", // Store as string
    val dueDate: String,
    val dueTime: String? = null,
    val isCompleted: Boolean = false,
    val subjectId: String? = null,
    val schoolYearId: String
)

@Serializable
data class SerializableReport(
    val id: String,
    val schoolYearId: String,
    val name: String,
    val reportSubjects: List<SerializableReportSubject> = emptyList(),
    val date: String = ""
)

@Serializable
data class SerializableReportSubject(
    val id: String,
    val name: String,
    val abbreviation: String = "",
    val finalGrade: String? = null,
    val isExtraSubject: Boolean = false,
    val description: String = ""
)

// Extension functions to convert between domain and serializable models
fun Subject.toSerializable(): SerializableSubject {
    return SerializableSubject(
        id = id,
        name = name,
        abbreviation = abbreviation,
        teacher = teacher,
        room = room,
        description = description,
        color = color.toArgb(),
        grades = grades.map { it.toSerializable() },
        schoolYearId = schoolYearId
    )
}

fun SerializableSubject.toDomain(): Subject {
    return Subject(
        id = id,
        name = name,
        abbreviation = abbreviation,
        teacher = teacher,
        room = room,
        description = description,
        color = Color(color),
        grades = grades.map { it.toDomain() },
        schoolYearId = schoolYearId
    )
}

fun Grade.toSerializable(): SerializableGrade {
    return SerializableGrade(
        id = id,
        value = value,
        weight = weight,
        description = description,
        date = date
    )
}

fun SerializableGrade.toDomain(): Grade {
    return Grade(
        id = id,
        value = value,
        weight = weight,
        description = description,
        date = date
    )
}

fun SchoolYear.toSerializable(): SerializableSchoolYear {
    return SerializableSchoolYear(
        id = id,
        name = name,
        description = description,
        startDate = startDate,
        endDate = endDate
    )
}

fun SerializableSchoolYear.toDomain(): SchoolYear {
    return SchoolYear(
        id = id,
        name = name,
        description = description,
        startDate = startDate,
        endDate = endDate
    )
}

fun Lesson.toSerializable(): SerializableLesson {
    return SerializableLesson(
        id = id,
        subjectId = subjectId,
        dayOfWeek = dayOfWeek,
        hour = hour,
        weekType = weekType.name,
        teacher = teacher,
        room = room,
        isVisible = isVisible,
        schoolYearId = schoolYearId
    )
}

fun SerializableLesson.toDomain(): Lesson {
    return Lesson(
        id = id,
        subjectId = subjectId,
        dayOfWeek = dayOfWeek,
        hour = hour,
        weekType = WeekType.valueOf(weekType),
        teacher = teacher,
        room = room,
        isVisible = isVisible,
        schoolYearId = schoolYearId
    )
}

fun Task.toSerializable(): SerializableTask {
    return SerializableTask(
        id = id,
        title = title,
        description = description,
        type = type.name,
        dueDate = dueDate,
        dueTime = dueTime,
        isCompleted = isCompleted,
        subjectId = subjectId,
        schoolYearId = schoolYearId
    )
}

fun SerializableTask.toDomain(): Task {
    return Task(
        id = id,
        title = title,
        description = description,
        type = TaskType.valueOf(type),
        dueDate = dueDate,
        dueTime = dueTime,
        isCompleted = isCompleted,
        subjectId = subjectId,
        schoolYearId = schoolYearId
    )
}

fun Report.toSerializable(): SerializableReport {
    return SerializableReport(
        id = id,
        schoolYearId = schoolYearId,
        name = name,
        reportSubjects = reportSubjects.map { it.toSerializable() },
        date = date
    )
}

fun SerializableReport.toDomain(): Report {
    return Report(
        id = id,
        schoolYearId = schoolYearId,
        name = name,
        reportSubjects = reportSubjects.map { it.toDomain() },
        date = date
    )
}

fun ReportSubject.toSerializable(): SerializableReportSubject {
    return SerializableReportSubject(
        id = id,
        name = name,
        abbreviation = abbreviation,
        finalGrade = finalGrade,
        isExtraSubject = isExtraSubject,
        description = description
    )
}

fun SerializableReportSubject.toDomain(): ReportSubject {
    return ReportSubject(
        id = id,
        name = name,
        abbreviation = abbreviation,
        finalGrade = finalGrade,
        isExtraSubject = isExtraSubject,
        description = description
    )
}
