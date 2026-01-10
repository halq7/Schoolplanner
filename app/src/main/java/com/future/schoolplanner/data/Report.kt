package com.future.schoolplanner.data

data class Report(
    val id: String,
    val schoolYearId: String,
    val name: String,
    val reportSubjects: List<ReportSubject> = emptyList(),
    val date: String = "" // Date when the report was issued
)

data class ReportSubject(
    val id: String,
    val name: String,
    val abbreviation: String = "",
    val finalGrade: String? = null, // Store as nullable string to support tendencies like "2-" or "3+"
    val isExtraSubject: Boolean = false, // Extra subjects like behavior that don't appear in grades tab
    val description: String = ""
)
