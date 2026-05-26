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
    val subjectCode: String = "",
    val isExtraSubject: Boolean = false, // Extra subjects like behavior
    val description: String = ""
)
