package com.future.schoolplanner.ui

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.future.schoolplanner.data.Grade
import com.future.schoolplanner.data.GradeInputMethod
import com.future.schoolplanner.data.Lesson
import com.future.schoolplanner.data.Report
import com.future.schoolplanner.data.ReportSubject
import com.future.schoolplanner.data.SchoolYear
import com.future.schoolplanner.data.Subject
import com.future.schoolplanner.data.Task
import com.future.schoolplanner.data.WeekType
import com.future.schoolplanner.data.persistence.DataPersistenceManager
import com.future.schoolplanner.data.serialization.toDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class SimulatedGrade(
    val value: Double,
    val weight: Double = 1.0
)

class GradeViewModel(context: Context? = null) : ViewModel() {
    private val persistenceManager = context?.let { DataPersistenceManager(it) }
    private var isInitialized = false

    private val _subjects = MutableStateFlow<List<Subject>>(emptyList())
    val subjects: StateFlow<List<Subject>> = _subjects.asStateFlow()

    private val _selectedSubject = MutableStateFlow<Subject?>(null)
    val selectedSubject: StateFlow<Subject?> = _selectedSubject.asStateFlow()

    private val _gradeInputMethod = MutableStateFlow(GradeInputMethod.DECIMAL)
    val gradeInputMethod: StateFlow<GradeInputMethod> = _gradeInputMethod.asStateFlow()

    private val _showTeachers = MutableStateFlow(true)
    val showTeachers: StateFlow<Boolean> = _showTeachers.asStateFlow()

    private val _showRooms = MutableStateFlow(true)
    val showRooms: StateFlow<Boolean> = _showRooms.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _useDynamicColors = MutableStateFlow(true)
    val useDynamicColors: StateFlow<Boolean> = _useDynamicColors.asStateFlow()

    private val _useAmoledTheme = MutableStateFlow(false)
    val useAmoledTheme: StateFlow<Boolean> = _useAmoledTheme.asStateFlow()

    private val _customAccentColor = MutableStateFlow(Color(0xFF4CAF50)) // Default green
    val customAccentColor: StateFlow<Color> = _customAccentColor.asStateFlow()

    private val _tasksTabEnabled = MutableStateFlow(false)
    val tasksTabEnabled: StateFlow<Boolean> = _tasksTabEnabled.asStateFlow()

    private val _defaultSubjectAlpha = MutableStateFlow(1.0f)
    val defaultSubjectAlpha: StateFlow<Float> = _defaultSubjectAlpha.asStateFlow()

    private val _weekTypeEvenWeeks = MutableStateFlow(WeekType.A)
    val weekTypeEvenWeeks: StateFlow<WeekType> = _weekTypeEvenWeeks.asStateFlow()

    private val _lessons = MutableStateFlow<List<Lesson>>(emptyList())
    val lessons: StateFlow<List<Lesson>> = _lessons.asStateFlow()

    private val _simulatedGrades = MutableStateFlow<Map<String, SimulatedGrade>>(emptyMap())
    val simulatedGrades: StateFlow<Map<String, SimulatedGrade>> = _simulatedGrades.asStateFlow()

    private val _schoolYears = MutableStateFlow<List<SchoolYear>>(emptyList())
    val schoolYears: StateFlow<List<SchoolYear>> = _schoolYears.asStateFlow()

    private val _currentSchoolYearId = MutableStateFlow<String?>(null)
    val currentSchoolYearId: StateFlow<String?> = _currentSchoolYearId.asStateFlow()

    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports: StateFlow<List<Report>> = _reports.asStateFlow()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    val subjectsForCurrentYear: StateFlow<List<Subject>> = combine<List<Subject>, String?, List<Subject>>(
        _subjects, _currentSchoolYearId
    ) { subjects, currentYearId ->
        if (currentYearId != null) {
            subjects.filter { it.schoolYearId == currentYearId }
        } else {
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lessonsForCurrentYear: StateFlow<List<Lesson>> = combine<List<Lesson>, String?, List<Lesson>>(
        _lessons, _currentSchoolYearId
    ) { lessons, currentYearId ->
        if (currentYearId != null) {
            lessons.filter { it.schoolYearId == currentYearId }
        } else {
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                val savedData = persistenceManager?.loadAppData()
                if (savedData != null) {
                    // Load saved data
                    _subjects.value = savedData.subjects.map { it.toDomain() }
                    _schoolYears.value = savedData.schoolYears.map { it.toDomain() }
                    _currentSchoolYearId.value = savedData.currentSchoolYearId
                    _lessons.value = savedData.lessons.map { it.toDomain() }
                    _reports.value = savedData.reports.map { it.toDomain() }
                    _tasks.value = savedData.tasks.map { it.toDomain() }

                    // Load settings
                    _gradeInputMethod.value = GradeInputMethod.valueOf(savedData.settings.gradeInputMethod)
                    _showTeachers.value = savedData.settings.showTeachers
                    _showRooms.value = savedData.settings.showRooms
                    _isDarkTheme.value = savedData.settings.isDarkTheme
                    _useDynamicColors.value = savedData.settings.useDynamicColors
                    _useAmoledTheme.value = savedData.settings.useAmoledTheme
                    _customAccentColor.value = Color(savedData.settings.customAccentColor)
                    _tasksTabEnabled.value = savedData.settings.tasksTabEnabled
                    _weekTypeEvenWeeks.value = WeekType.valueOf(savedData.settings.weekTypeEvenWeeks)
                    _defaultSubjectAlpha.value = savedData.settings.defaultSubjectAlpha

                    Log.d("GradeViewModel", "Data loaded successfully from persistence")
                } else {
                    // Initialize with default data if no saved data exists
                    initializeDefaultData()
                }
                isInitialized = true
            } catch (e: Exception) {
                Log.e("GradeViewModel", "Error loading data, initializing with defaults", e)
                initializeDefaultData()
            }
        }
    }

    private fun initializeDefaultData() {
        viewModelScope.launch {
            val defaultSchoolYearId = UUID.randomUUID().toString()
            val defaultSchoolYear = SchoolYear(
                id = defaultSchoolYearId,
                name = "2024/2025",
                description = "Aktuelles Schuljahr",
                startDate = "2024-09-01",
                endDate = "2025-06-30"
            )
            _schoolYears.value = listOf(defaultSchoolYear)
            _currentSchoolYearId.value = defaultSchoolYearId

            val defaultSubjects = listOf(
                Subject(
                    id = UUID.randomUUID().toString(),
                    name = "Mathematik",
                    abbreviation = "MA",
                    teacher = "Herr Müller",
                    room = "101",
                    description = "Mathematik Grundkurs",
                    color = Color(0xFF4CAF50),
                    grades = listOf(
                        Grade(
                            id = UUID.randomUUID().toString(),
                            value = 2.0,
                            description = "1. Schulaufgabe",
                            date = "2024-10-15"
                        )
                    ),
                    schoolYearId = defaultSchoolYearId
                ),
                Subject(
                    id = UUID.randomUUID().toString(),
                    name = "Deutsch",
                    abbreviation = "DE",
                    teacher = "Frau Schmidt",
                    room = "102",
                    description = "Deutsch Grundkurs",
                    color = Color(0xFF2196F3),
                    grades = listOf(
                        Grade(
                            id = UUID.randomUUID().toString(),
                            value = 1.5,
                            description = "Aufsatz",
                            date = "2024-09-28"
                        )
                    ),
                    schoolYearId = defaultSchoolYearId
                ),
                Subject(
                    id = UUID.randomUUID().toString(),
                    name = "Englisch",
                    abbreviation = "EN",
                    teacher = "Mr. Johnson",
                    room = "103",
                    description = "English Basic Course",
                    color = Color(0xFFFF9800),
                    grades = listOf(
                        Grade(
                            id = UUID.randomUUID().toString(),
                            value = 3.0,
                            description = "Vocabulary Test",
                            date = "2024-10-05"
                        )
                    ),
                    schoolYearId = defaultSchoolYearId
                )
            )
            _subjects.value = defaultSubjects
        }
    }

    private fun saveData() {
        if (!isInitialized || persistenceManager == null) return

        viewModelScope.launch {
            try {
                persistenceManager.saveAppData(
                    subjects = _subjects.value,
                    schoolYears = _schoolYears.value,
                    currentSchoolYearId = _currentSchoolYearId.value,
                    lessons = _lessons.value,
                    reports = _reports.value,
                    tasks = _tasks.value,
                    gradeInputMethod = _gradeInputMethod.value,
                    showTeachers = _showTeachers.value,
                    showRooms = _showRooms.value,
                    isDarkTheme = _isDarkTheme.value,
                    useDynamicColors = _useDynamicColors.value,
                    useAmoledTheme = _useAmoledTheme.value,
                    customAccentColor = _customAccentColor.value.toArgb(),
                    tasksTabEnabled = _tasksTabEnabled.value,
                    weekTypeEvenWeeks = _weekTypeEvenWeeks.value.name,
                    defaultSubjectAlpha = _defaultSubjectAlpha.value
                )
                Log.d("GradeViewModel", "Data saved successfully")
            } catch (e: Exception) {
                Log.e("GradeViewModel", "Error saving data", e)
            }
        }
    }

    fun selectSubject(subject: Subject) {
        _selectedSubject.value = subject
    }

    fun addGrade(subjectId: String, grade: Grade) {
        viewModelScope.launch {
            val updatedSubjects = _subjects.value.map { subject ->
                if (subject.id == subjectId) {
                    val updatedGrades = subject.grades + grade
                    subject.copy(grades = updatedGrades)
                } else {
                    subject
                }
            }
            _subjects.value = updatedSubjects

            // Update selected subject if it's the one we just modified
            if (_selectedSubject.value?.id == subjectId) {
                _selectedSubject.value = updatedSubjects.find { it.id == subjectId }
            }
            saveData()
        }
    }

    fun addSubject(subject: Subject) {
        viewModelScope.launch {
            _subjects.value = _subjects.value + subject
            saveData()
        }
    }

    fun calculateAverage(subject: Subject, simulatedGrade: SimulatedGrade? = null): Double {
        val allGrades = subject.grades.toMutableList()
        simulatedGrade?.let {
            allGrades.add(Grade(id = "simulated", value = it.value, weight = it.weight))
        }

        if (allGrades.isEmpty()) return 0.0

        val weightedSum = allGrades.sumOf { it.value * it.weight }
        val totalWeight = allGrades.sumOf { it.weight }
        return weightedSum / totalWeight
    }

    fun updateSubject(subjectId: String, name: String, color: Color) {
        viewModelScope.launch {
            val updatedSubjects = _subjects.value.map { subject ->
                if (subject.id == subjectId) {
                    subject.copy(name = name, color = color)
                } else {
                    subject
                }
            }
            _subjects.value = updatedSubjects

            // Update selected subject if it's the one we just modified
            if (_selectedSubject.value?.id == subjectId) {
                _selectedSubject.value = updatedSubjects.find { it.id == subjectId }
            }
            saveData()
        }
    }

    fun updateSubject(subject: Subject) {
        viewModelScope.launch {
            val updatedSubjects = _subjects.value.map {
                if (it.id == subject.id) subject else it
            }
            _subjects.value = updatedSubjects

            // Update selected subject if it's the one we just modified
            if (_selectedSubject.value?.id == subject.id) {
                _selectedSubject.value = subject
            }
            saveData()
        }
    }

    fun deleteSubject(subjectId: String) {
        viewModelScope.launch {
            _subjects.value = _subjects.value.filter { it.id != subjectId }

            // Clear selected subject if it's the one we just deleted
            if (_selectedSubject.value?.id == subjectId) {
                _selectedSubject.value = null
            }
            saveData()
        }
    }

    fun updateGrade(subjectId: String, gradeId: String, value: Double, weight: Double, description: String, date: String) {
        viewModelScope.launch {
            val updatedSubjects = _subjects.value.map { subject ->
                if (subject.id == subjectId) {
                    val updatedGrades = subject.grades.map { grade ->
                        if (grade.id == gradeId) {
                            grade.copy(value = value, weight = weight, description = description, date = date)
                        } else {
                            grade
                        }
                    }
                    subject.copy(grades = updatedGrades)
                } else {
                    subject
                }
            }
            _subjects.value = updatedSubjects

            // Update selected subject if it's the one we just modified
            if (_selectedSubject.value?.id == subjectId) {
                _selectedSubject.value = updatedSubjects.find { it.id == subjectId }
            }
            saveData()
        }
    }

    fun deleteGrade(subjectId: String, gradeId: String) {
        viewModelScope.launch {
            val updatedSubjects = _subjects.value.map { subject ->
                if (subject.id == subjectId) {
                    val updatedGrades = subject.grades.filter { it.id != gradeId }
                    subject.copy(grades = updatedGrades)
                } else {
                    subject
                }
            }
            _subjects.value = updatedSubjects

            // Update selected subject if it's the one we just modified
            if (_selectedSubject.value?.id == subjectId) {
                _selectedSubject.value = updatedSubjects.find { it.id == subjectId }
            }
            saveData()
        }
    }

    fun setGradeInputMethod(method: GradeInputMethod) {
        _gradeInputMethod.value = method
        saveData()
    }

    fun setShowTeachers(show: Boolean) {
        _showTeachers.value = show
        saveData()
    }

    fun setShowRooms(show: Boolean) {
        _showRooms.value = show
        saveData()
    }

    fun setDarkTheme(enabled: Boolean) {
        _isDarkTheme.value = enabled
        saveData()
    }

    fun setUseDynamicColors(enabled: Boolean) {
        _useDynamicColors.value = enabled
        saveData()
    }

    fun setUseAmoledTheme(enabled: Boolean) {
        _useAmoledTheme.value = enabled
        saveData()
    }

    fun setCustomAccentColor(color: Color) {
        _customAccentColor.value = color
        saveData()
    }

    fun setTasksTabEnabled(enabled: Boolean) {
        _tasksTabEnabled.value = enabled
        saveData()
    }

    fun setWeekTypeEvenWeeks(weekType: WeekType) {
        _weekTypeEvenWeeks.value = weekType
        saveData()
    }

    fun setDefaultSubjectAlpha(alpha: Float) {
        _defaultSubjectAlpha.value = alpha
        saveData()
    }

    fun calculateOverallAverage(): Double {
        val currentSubjects = getSubjectsForCurrentYear()
        val subjectsWithGrades = currentSubjects.filter { it.grades.isNotEmpty() || _simulatedGrades.value.containsKey(it.id) }
        if (subjectsWithGrades.isEmpty()) return 0.0

        val subjectAverages = subjectsWithGrades.map { subject ->
            val simulated = _simulatedGrades.value[subject.id]
            calculateAverage(subject, simulated)
        }
        return subjectAverages.average()
    }

    fun parseGradeInput(input: String, method: GradeInputMethod): Double? {
        return when (method) {
            GradeInputMethod.WHOLE -> {
                val intValue = input.toIntOrNull()
                if (intValue != null && intValue in 1..6) intValue.toDouble() else null
            }
            GradeInputMethod.DECIMAL -> {
                val doubleValue = input.toDoubleOrNull()
                if (doubleValue != null && doubleValue in 1.0..6.0) doubleValue else null
            }
            GradeInputMethod.TENDENCY -> {
                // Expect format like "2-" or "3+" or "4"
                val regex = Regex("^([1-6])([+-]?)$")
                val match = regex.matchEntire(input.trim())
                if (match != null) {
                    val base = match.groupValues[1].toInt()
                    val tendency = match.groupValues[2]
                    val adjustment = when (tendency) {
                        "+" -> -0.3
                        "-" -> 0.3
                        else -> 0.0
                    }
                    (base + adjustment).coerceIn(1.0, 6.0)
                } else null
            }
            GradeInputMethod.FIFTEEN_POINT -> {
                val points = input.toIntOrNull()
                if (points != null && points in 0..15) {
                    // Convert 15 points = 1.0, 0 points = 6.0
                    6.0 - (points / 15.0) * 5.0
                } else null
            }
        }
    }

    fun formatGradeForDisplay(value: Double, method: GradeInputMethod): String {
        return when (method) {
            GradeInputMethod.WHOLE -> value.toInt().toString()
            GradeInputMethod.DECIMAL -> String.format("%.1f", value)
            GradeInputMethod.TENDENCY -> {
                // Approximate reverse conversion
                val base = value.toInt()
                val remainder = value - base
                val tendency = when {
                    remainder <= -0.15 -> "+"
                    remainder >= 0.15 -> "-"
                    else -> ""
                }
                "$base$tendency"
            }
            GradeInputMethod.FIFTEEN_POINT -> {
                // Reverse conversion
                val points = ((6.0 - value) / 5.0 * 15.0).toInt().coerceIn(0, 15)
                points.toString()
            }
        }
    }

    // Format grade for reports - always round to nearest tendency
    fun formatGradeForReport(value: Double): String {
        val rounded = kotlin.math.round(value * 2) / 2.0 // Round to nearest 0.5
        val base = rounded.toInt()
        val remainder = rounded - base

        val tendency = when {
            remainder >= 0.25 -> "-"  // 2.25+ becomes 2-
            remainder <= -0.25 -> "+" // 1.75- becomes 2+
            else -> ""               // Exactly 2.0 stays 2
        }

        val finalBase = when (tendency) {
            "-" -> base
            "+" -> base - 1
            else -> base
        }

        return "$finalBase$tendency"
    }

    // Schedule functions
    fun addLesson(lesson: Lesson) {
        viewModelScope.launch {
            _lessons.value = _lessons.value + lesson
            saveData()
        }
    }

    fun updateLesson(lesson: Lesson) {
        viewModelScope.launch {
            val updatedLessons = _lessons.value.map {
                if (it.id == lesson.id) lesson else it
            }
            _lessons.value = updatedLessons
            saveData()
        }
    }

    fun deleteLesson(lessonId: String) {
        viewModelScope.launch {
            _lessons.value = _lessons.value.filter { it.id != lessonId }
            saveData()
        }
    }

    fun copyWeekLessons(fromWeekType: WeekType, toWeekType: WeekType) {
        viewModelScope.launch {
            val currentYearId = _currentSchoolYearId.value ?: return@launch

            // Get all lessons
            val allLessons = _lessons.value.toMutableList()

            // First, remove ALL lessons from the target week for current year
            allLessons.removeIf { lesson ->
                lesson.schoolYearId == currentYearId && lesson.weekType == toWeekType
            }

            // Get lessons from the source week for current year
            val sourceLessons = _lessons.value.filter {
                it.schoolYearId == currentYearId && it.weekType == fromWeekType
            }

            // Create copies of all source lessons for the target week
            val targetLessons = sourceLessons.map { sourceLesson ->
                sourceLesson.copy(
                    id = java.util.UUID.randomUUID().toString(),
                    weekType = toWeekType
                )
            }

            // Add all target lessons
            allLessons.addAll(targetLessons)

            _lessons.value = allLessons
            saveData()
        }
    }

    fun setCurrentSchoolYear(schoolYearId: String) {
        _currentSchoolYearId.value = schoolYearId
        saveData()
    }

    fun getSubjectById(subjectId: String): Subject? {
        return _subjects.value.find { it.id == subjectId }
    }

    fun setSimulatedGrade(subjectId: String, simulatedGrade: SimulatedGrade?) {
        viewModelScope.launch {
            if (simulatedGrade != null) {
                _simulatedGrades.value = _simulatedGrades.value + (subjectId to simulatedGrade)
            } else {
                _simulatedGrades.value = _simulatedGrades.value - subjectId
            }
        }
    }

    fun clearSimulatedGrade(subjectId: String) {
        viewModelScope.launch {
            _simulatedGrades.value = _simulatedGrades.value - subjectId
        }
    }

    // School Year functions
    fun addSchoolYear(schoolYear: SchoolYear) {
        viewModelScope.launch {
            _schoolYears.value = _schoolYears.value + schoolYear
            saveData()
        }
    }

    fun updateSchoolYear(schoolYear: SchoolYear) {
        viewModelScope.launch {
            val updatedSchoolYears = _schoolYears.value.map {
                if (it.id == schoolYear.id) schoolYear else it
            }
            _schoolYears.value = updatedSchoolYears
            saveData()
        }
    }

    fun deleteSchoolYear(schoolYearId: String) {
        viewModelScope.launch {
            _schoolYears.value = _schoolYears.value.filter { it.id != schoolYearId }
            // If we deleted the current school year, set to first available
            if (_currentSchoolYearId.value == schoolYearId) {
                _currentSchoolYearId.value = _schoolYears.value.firstOrNull()?.id
            }
            saveData()
        }
    }

    fun getCurrentSchoolYear(): SchoolYear? {
        return _schoolYears.value.find { it.id == _currentSchoolYearId.value }
    }

    // Filtered data functions
    fun getSubjectsForCurrentYear(): List<Subject> {
        val currentYearId = _currentSchoolYearId.value
        return if (currentYearId != null) {
            _subjects.value.filter { it.schoolYearId == currentYearId }
        } else {
            emptyList()
        }
    }

    fun getLessonsForCurrentYear(): List<Lesson> {
        val currentYearId = _currentSchoolYearId.value
        return if (currentYearId != null) {
            _lessons.value.filter { it.schoolYearId == currentYearId }
        } else {
            emptyList()
        }
    }

    // Report functions
    fun addReport(report: Report) {
        viewModelScope.launch {
            _reports.value = _reports.value + report
            saveData()
        }
    }

    fun updateReport(report: Report) {
        viewModelScope.launch {
            val updatedReports = _reports.value.map {
                if (it.id == report.id) report else it
            }
            _reports.value = updatedReports
            saveData()
        }
    }

    fun deleteReport(reportId: String) {
        viewModelScope.launch {
            _reports.value = _reports.value.filter { it.id != reportId }
            saveData()
        }
    }

    fun getReportById(reportId: String): Report? {
        return _reports.value.find { it.id == reportId }
    }

    fun getReportsForSchoolYear(schoolYearId: String): List<Report> {
        return _reports.value.filter { it.schoolYearId == schoolYearId }
    }

    fun createReportFromSubjects(schoolYearId: String, reportName: String): Report {
        val subjects = getSubjectsForCurrentYear()
        val reportSubjects = subjects.map { subject ->
            val average = calculateAverage(subject)
            ReportSubject(
                id = UUID.randomUUID().toString(),
                name = subject.name,
                abbreviation = subject.abbreviation,
                finalGrade = if (average > 0) formatGradeForReport(average) else null,
                isExtraSubject = false,
                description = subject.description
            )
        }
        return Report(
            id = UUID.randomUUID().toString(),
            schoolYearId = schoolYearId,
            name = reportName,
            reportSubjects = reportSubjects
        )
    }

    fun addReportSubject(reportId: String, reportSubject: ReportSubject) {
        viewModelScope.launch {
            val updatedReports = _reports.value.map { report ->
                if (report.id == reportId) {
                    val updatedSubjects = report.reportSubjects + reportSubject
                    report.copy(reportSubjects = updatedSubjects)
                } else {
                    report
                }
            }
            _reports.value = updatedReports
            saveData()
        }
    }

    fun updateReportSubject(reportId: String, reportSubjectId: String, finalGrade: String?) {
        viewModelScope.launch {
            val updatedReports = _reports.value.map { report ->
                if (report.id == reportId) {
                    val updatedSubjects = report.reportSubjects.map { subject ->
                        if (subject.id == reportSubjectId) {
                            subject.copy(finalGrade = finalGrade)
                        } else {
                            subject
                        }
                    }
                    report.copy(reportSubjects = updatedSubjects)
                } else {
                    report
                }
            }
            _reports.value = updatedReports
            saveData()
        }
    }

    fun deleteReportSubject(reportId: String, reportSubjectId: String) {
        viewModelScope.launch {
            val updatedReports = _reports.value.map { report ->
                if (report.id == reportId) {
                    val updatedSubjects = report.reportSubjects.filter { it.id != reportSubjectId }
                    report.copy(reportSubjects = updatedSubjects)
                } else {
                    report
                }
            }
            _reports.value = updatedReports
            saveData()
        }
    }

    // Task functions
    fun addTask(task: Task) {
        viewModelScope.launch {
            _tasks.value = _tasks.value + task
            saveData()
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            val updatedTasks = _tasks.value.map {
                if (it.id == task.id) task else it
            }
            _tasks.value = updatedTasks
            saveData()
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            _tasks.value = _tasks.value.filter { it.id != taskId }
            saveData()
        }
    }

    fun getTaskById(taskId: String): Task? {
        return _tasks.value.find { it.id == taskId }
    }

    fun getTasksForCurrentYear(): StateFlow<List<Task>> {
        return combine<List<Task>, String?, List<Task>>(
            _tasks, _currentSchoolYearId
        ) { tasks, currentYearId ->
            if (currentYearId != null) {
                tasks.filter { it.schoolYearId == currentYearId }
            } else {
                emptyList()
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun toggleTaskCompletion(taskId: String) {
        viewModelScope.launch {
            val updatedTasks = _tasks.value.map { task ->
                if (task.id == taskId) {
                    task.copy(isCompleted = !task.isCompleted)
                } else {
                    task
                }
            }
            _tasks.value = updatedTasks
            saveData()
        }
    }

    fun loadAppData(appData: com.future.schoolplanner.data.serialization.AppData) {
        viewModelScope.launch {
            // Load data from AppData
            _subjects.value = appData.subjects.map { it.toDomain() }
            _schoolYears.value = appData.schoolYears.map { it.toDomain() }
            _currentSchoolYearId.value = appData.currentSchoolYearId
            _lessons.value = appData.lessons.map { it.toDomain() }
            _reports.value = appData.reports.map { it.toDomain() }
            _tasks.value = appData.tasks.map { it.toDomain() }

            // Load settings
            _gradeInputMethod.value = GradeInputMethod.valueOf(appData.settings.gradeInputMethod)
            _showTeachers.value = appData.settings.showTeachers
            _showRooms.value = appData.settings.showRooms
            _isDarkTheme.value = appData.settings.isDarkTheme
            _useDynamicColors.value = appData.settings.useDynamicColors
            _useAmoledTheme.value = appData.settings.useAmoledTheme
            _customAccentColor.value = Color(appData.settings.customAccentColor)
            _tasksTabEnabled.value = appData.settings.tasksTabEnabled
            _weekTypeEvenWeeks.value = WeekType.valueOf(appData.settings.weekTypeEvenWeeks)
            _defaultSubjectAlpha.value = appData.settings.defaultSubjectAlpha

            // Clear simulated grades
            _simulatedGrades.value = emptyMap()

            isInitialized = true
            saveData()
        }
    }
}
