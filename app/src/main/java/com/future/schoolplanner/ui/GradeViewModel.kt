package com.future.schoolplanner.ui

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.future.schoolplanner.data.Lesson
import com.future.schoolplanner.data.Report
import com.future.schoolplanner.data.ReportSubject
import com.future.schoolplanner.data.SchoolYear
import com.future.schoolplanner.data.Subject
import com.future.schoolplanner.data.Task
import com.future.schoolplanner.data.TaskType
import com.future.schoolplanner.data.persistence.DataPersistenceManager
import com.future.schoolplanner.data.serialization.AppData
import com.future.schoolplanner.data.serialization.AppSettings
import com.future.schoolplanner.data.serialization.toDomain
import com.future.schoolplanner.data.serialization.toSerializable
import com.future.schoolplanner.data.sync.NextcloudCredentials
import com.future.schoolplanner.data.sync.NextcloudSyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class NextcloudSyncState(
    val enabled: Boolean = false,
    val serverUrl: String = "",
    val username: String = "",
    val password: String = "",
    val isSyncing: Boolean = false,
    val statusMessage: String? = null,
    val lastSyncAt: Long = 0L
)

class GradeViewModel(context: Context? = null) : ViewModel() {
    private val appContext = context?.applicationContext
    private val persistenceManager = appContext?.let { DataPersistenceManager(it) }
    private val nextcloudSyncManager = appContext?.let { NextcloudSyncManager() }
    private val syncPrefs = appContext?.getSharedPreferences("nextcloud_sync", Context.MODE_PRIVATE)
    private var isInitialized = false
    private var suppressNextcloudUpload = false

    private val _subjects = MutableStateFlow<List<Subject>>(emptyList())
    val subjects: StateFlow<List<Subject>> = _subjects.asStateFlow()

    private val _selectedSubject = MutableStateFlow<Subject?>(null)
    val selectedSubject: StateFlow<Subject?> = _selectedSubject.asStateFlow()

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

    private val _lessons = MutableStateFlow<List<Lesson>>(emptyList())
    val lessons: StateFlow<List<Lesson>> = _lessons.asStateFlow()

    private val _schoolYears = MutableStateFlow<List<SchoolYear>>(emptyList())
    val schoolYears: StateFlow<List<SchoolYear>> = _schoolYears.asStateFlow()

    private val _currentSchoolYearId = MutableStateFlow<String?>(null)
    val currentSchoolYearId: StateFlow<String?> = _currentSchoolYearId.asStateFlow()

    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports: StateFlow<List<Report>> = _reports.asStateFlow()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _nextcloudSyncState = MutableStateFlow(loadNextcloudSyncState())
    val nextcloudSyncState: StateFlow<NextcloudSyncState> = _nextcloudSyncState.asStateFlow()

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

    private fun loadNextcloudSyncState(): NextcloudSyncState {
        val prefs = syncPrefs ?: return NextcloudSyncState()
        return NextcloudSyncState(
            enabled = prefs.getBoolean(KEY_NEXTCLOUD_ENABLED, false),
            serverUrl = prefs.getString(KEY_NEXTCLOUD_SERVER_URL, "") ?: "",
            username = prefs.getString(KEY_NEXTCLOUD_USERNAME, "") ?: "",
            password = prefs.getString(KEY_NEXTCLOUD_PASSWORD, "") ?: "",
            lastSyncAt = prefs.getLong(KEY_NEXTCLOUD_LAST_SYNC_AT, 0L)
        )
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                val savedData = persistenceManager?.loadAppData()
                if (savedData != null) {
                    applyAppData(savedData)
                    Log.d("GradeViewModel", "Data loaded successfully from persistence")
                } else {
                    initializeDefaultData()
                }
                isInitialized = true
                if (_nextcloudSyncState.value.enabled) {
                    syncNextcloud()
                }
            } catch (e: Exception) {
                Log.e("GradeViewModel", "Error loading data, initializing with defaults", e)
                initializeDefaultData()
                isInitialized = true
            }
        }
    }

    private fun initializeDefaultData() {
        val defaultSchoolYearId = UUID.randomUUID().toString()
        val defaultSchoolYear = SchoolYear(
            id = defaultSchoolYearId,
            name = "2024/2025",
            description = "Năm học hiện tại",
            startDate = "2024-09-01",
            endDate = "2025-06-30"
        )
        _schoolYears.value = listOf(defaultSchoolYear)
        _currentSchoolYearId.value = defaultSchoolYearId

        val defaultSubjects = listOf(
            Subject(
                id = UUID.randomUUID().toString(),
                name = "Toán học",
                subjectCode = "TOÁN",
                teacher = "Thầy Hùng",
                room = "101",
                description = "Môn Toán cơ bản",
                color = Color(0xFF4CAF50),
                schoolYearId = defaultSchoolYearId
            ),
            Subject(
                id = UUID.randomUUID().toString(),
                name = "Ngữ văn",
                subjectCode = "VĂN",
                teacher = "Cô Lan",
                room = "102",
                description = "Môn Văn học",
                color = Color(0xFF2196F3),
                schoolYearId = defaultSchoolYearId
            ),
            Subject(
                id = UUID.randomUUID().toString(),
                name = "Tiếng Anh",
                subjectCode = "ANH",
                teacher = "Mr. Johnson",
                room = "103",
                description = "English Course",
                color = Color(0xFFFF9800),
                schoolYearId = defaultSchoolYearId
            )
        )
        _subjects.value = defaultSubjects
    }

    private fun createAppData(): AppData {
        return AppData(
            subjects = _subjects.value.map { it.toSerializable() },
            schoolYears = _schoolYears.value.map { it.toSerializable() },
            currentSchoolYearId = _currentSchoolYearId.value,
            lessons = _lessons.value.map { it.toSerializable() },
            reports = _reports.value.map { it.toSerializable() },
            tasks = _tasks.value.map { it.toSerializable() },
            settings = AppSettings(
                showTeachers = _showTeachers.value,
                showRooms = _showRooms.value,
                isDarkTheme = _isDarkTheme.value,
                useDynamicColors = _useDynamicColors.value,
                useAmoledTheme = _useAmoledTheme.value,
                customAccentColor = _customAccentColor.value.toArgb(),
                tasksTabEnabled = _tasksTabEnabled.value,
                defaultSubjectAlpha = _defaultSubjectAlpha.value
            )
        )
    }

    private fun applyAppData(appData: AppData) {
        _subjects.value = appData.subjects.map { it.toDomain() }
        _schoolYears.value = appData.schoolYears.map { it.toDomain() }
        _currentSchoolYearId.value = appData.currentSchoolYearId
        _lessons.value = appData.lessons.map { it.toDomain() }
        _reports.value = appData.reports.map { it.toDomain() }
        _tasks.value = appData.tasks.map { it.toDomain() }

        _showTeachers.value = appData.settings.showTeachers
        _showRooms.value = appData.settings.showRooms
        _isDarkTheme.value = appData.settings.isDarkTheme
        _useDynamicColors.value = appData.settings.useDynamicColors
        _useAmoledTheme.value = appData.settings.useAmoledTheme
        _customAccentColor.value = Color(appData.settings.customAccentColor)
        _tasksTabEnabled.value = appData.settings.tasksTabEnabled
        _defaultSubjectAlpha.value = appData.settings.defaultSubjectAlpha
    }

    private fun saveData() {
        if (!isInitialized || persistenceManager == null) return

        viewModelScope.launch {
            try {
                val appData = createAppData()
                persistenceManager.saveAppData(appData)
                markLocalDataChanged()
                if (_nextcloudSyncState.value.enabled && !suppressNextcloudUpload) {
                    syncNextcloud()
                }
                Log.d("GradeViewModel", "Data saved successfully")
            } catch (e: Exception) {
                Log.e("GradeViewModel", "Error saving data", e)
            }
        }
    }

    private fun markLocalDataChanged() {
        syncPrefs?.edit()
            ?.putLong(KEY_NEXTCLOUD_LOCAL_UPDATED_AT, System.currentTimeMillis())
            ?.putBoolean(KEY_NEXTCLOUD_LOCAL_DIRTY, true)
            ?.apply()
    }

    private fun saveNextcloudPrefs(state: NextcloudSyncState) {
        syncPrefs?.edit()
            ?.putBoolean(KEY_NEXTCLOUD_ENABLED, state.enabled)
            ?.putString(KEY_NEXTCLOUD_SERVER_URL, state.serverUrl)
            ?.putString(KEY_NEXTCLOUD_USERNAME, state.username)
            ?.putString(KEY_NEXTCLOUD_PASSWORD, state.password)
            ?.putLong(KEY_NEXTCLOUD_LAST_SYNC_AT, state.lastSyncAt)
            ?.apply()
    }

    private fun updateNextcloudState(transform: (NextcloudSyncState) -> NextcloudSyncState) {
        val newState = transform(_nextcloudSyncState.value)
        _nextcloudSyncState.value = newState
        saveNextcloudPrefs(newState)
    }

    fun updateNextcloudSettings(serverUrl: String, username: String, password: String) {
        updateNextcloudState {
            it.copy(
                serverUrl = serverUrl,
                username = username,
                password = password,
                statusMessage = null
            )
        }
    }

    fun connectNextcloud(serverUrl: String, username: String, password: String) {
        updateNextcloudSettings(serverUrl, username, password)
        if (serverUrl.isBlank() || username.isBlank() || password.isBlank()) {
            updateNextcloudState { it.copy(enabled = false, statusMessage = "Vui lòng nhập đầy đủ Server, Tên đăng nhập và Mật khẩu ứng dụng.") }
            return
        }

        updateNextcloudState { it.copy(enabled = true, statusMessage = "Đang kết nối với Nextcloud...") }
        syncPrefs?.edit()
            ?.putBoolean(KEY_NEXTCLOUD_LOCAL_DIRTY, true)
            ?.putLong(KEY_NEXTCLOUD_LOCAL_UPDATED_AT, System.currentTimeMillis())
            ?.apply()
        syncNextcloud()
    }

    fun disconnectNextcloud() {
        syncPrefs?.edit()?.clear()?.apply()
        _nextcloudSyncState.value = NextcloudSyncState(statusMessage = "Đã ngắt kết nối Nextcloud.")
    }

    fun syncNextcloud() {
        val manager = nextcloudSyncManager ?: return
        val persistence = persistenceManager ?: return
        val state = _nextcloudSyncState.value
        if (!state.enabled || state.isSyncing) return
        if (state.serverUrl.isBlank() || state.username.isBlank() || state.password.isBlank()) return

        viewModelScope.launch {
            updateNextcloudState { it.copy(isSyncing = true, statusMessage = "Đang đồng bộ...") }
            try {
                val localJson = persistence.encodeAppData(createAppData())
                val result = manager.sync(
                    credentials = NextcloudCredentials(
                        serverUrl = state.serverUrl,
                        username = state.username,
                        password = state.password
                    ),
                    localJson = localJson,
                    localUpdatedAt = syncPrefs?.getLong(KEY_NEXTCLOUD_LOCAL_UPDATED_AT, 0L) ?: 0L,
                    lastRemoteModified = syncPrefs?.getLong(KEY_NEXTCLOUD_LAST_REMOTE_MODIFIED, 0L) ?: 0L,
                    localDirty = syncPrefs?.getBoolean(KEY_NEXTCLOUD_LOCAL_DIRTY, true) ?: true
                )

                if (result.downloaded) {
                    val remoteData = persistence.decodeAppData(result.appDataJson)
                    suppressNextcloudUpload = true
                    try {
                        applyAppData(remoteData)
                        persistence.saveAppData(remoteData)
                    } finally {
                        suppressNextcloudUpload = false
                    }
                }

                val now = System.currentTimeMillis()
                syncPrefs?.edit()
                    ?.putLong(KEY_NEXTCLOUD_LAST_REMOTE_MODIFIED, result.remoteLastModified)
                    ?.putLong(KEY_NEXTCLOUD_LAST_SYNC_AT, now)
                    ?.putBoolean(KEY_NEXTCLOUD_LOCAL_DIRTY, false)
                    ?.apply()

                val message = when {
                    result.downloaded -> "Remote-Daten übernommen."
                    result.uploaded -> "Lokale Daten hochgeladen."
                    else -> "Alles aktuell."
                }
                updateNextcloudState {
                    it.copy(
                        isSyncing = false,
                        statusMessage = message,
                        lastSyncAt = now
                    )
                }
            } catch (e: Exception) {
                Log.e("GradeViewModel", "Nextcloud sync failed", e)
                updateNextcloudState {
                    it.copy(
                        isSyncing = false,
                        statusMessage = "Đồng bộ thất bại: ${e.localizedMessage ?: e.javaClass.simpleName}"
                    )
                }
            }
        }
    }

    fun selectSubject(subject: Subject) {
        _selectedSubject.value = subject
    }

    fun addSubject(subject: Subject) {
        viewModelScope.launch {
            _subjects.value = _subjects.value + subject
            saveData()
        }
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

    fun setDefaultSubjectAlpha(alpha: Float) {
        _defaultSubjectAlpha.value = alpha
        saveData()
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

    fun setCurrentSchoolYear(schoolYearId: String) {
        _currentSchoolYearId.value = schoolYearId
        saveData()
    }

    fun getSubjectById(subjectId: String): Subject? {
        return _subjects.value.find { it.id == subjectId }
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

    fun updateReportSubject(reportId: String, reportSubjectId: String, name: String) {
        viewModelScope.launch {
            val updatedReports = _reports.value.map { report ->
                if (report.id == reportId) {
                    val updatedSubjects = report.reportSubjects.map { subject ->
                        if (subject.id == reportSubjectId) {
                            subject.copy(name = name)
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

    fun loadAppData(appData: AppData) {
        viewModelScope.launch {
            applyAppData(appData)
            isInitialized = true
            saveData()
        }
    }

    // Developer options functions
    fun clearAllSubjects() {
        viewModelScope.launch {
            _subjects.value = emptyList()
            _selectedSubject.value = null
            saveData()
        }
    }

    fun clearAllReports() {
        viewModelScope.launch {
            _reports.value = emptyList()
            saveData()
        }
    }

    fun clearAllSchoolYears() {
        viewModelScope.launch {
            _schoolYears.value = emptyList()
            _currentSchoolYearId.value = null
            saveData()
        }
    }

    fun clearAllLessons() {
        viewModelScope.launch {
            _lessons.value = emptyList()
            saveData()
        }
    }

    companion object {
        private const val KEY_NEXTCLOUD_ENABLED = "enabled"
        private const val KEY_NEXTCLOUD_SERVER_URL = "server_url"
        private const val KEY_NEXTCLOUD_USERNAME = "username"
        private const val KEY_NEXTCLOUD_PASSWORD = "password"
        private const val KEY_NEXTCLOUD_LAST_SYNC_AT = "last_sync_at"
        private const val KEY_NEXTCLOUD_LAST_REMOTE_MODIFIED = "last_remote_modified"
        private const val KEY_NEXTCLOUD_LOCAL_UPDATED_AT = "local_updated_at"
        private const val KEY_NEXTCLOUD_LOCAL_DIRTY = "local_dirty"
    }
}
