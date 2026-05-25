package com.future.schoolplanner.data.persistence

import android.content.Context
import android.util.Log
import com.future.schoolplanner.data.*
import com.future.schoolplanner.data.serialization.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

class DataPersistenceManager(private val context: Context) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val dataFileName = "school_planner_data.json"

    fun saveAppData(
        subjects: List<Subject>,
        schoolYears: List<SchoolYear>,
        currentSchoolYearId: String?,
        lessons: List<Lesson>,
        reports: List<Report>,
        tasks: List<Task>,
        gradeInputMethod: GradeInputMethod,
        showTeachers: Boolean,
        showRooms: Boolean,
        isDarkTheme: Boolean,
        useDynamicColors: Boolean,
        useAmoledTheme: Boolean,
        customAccentColor: Int,
        tasksTabEnabled: Boolean,
        defaultSubjectAlpha: Float = 1.0f
    ) {
        try {
            val appData = AppData(
                subjects = subjects.map { it.toSerializable() },
                schoolYears = schoolYears.map { it.toSerializable() },
                currentSchoolYearId = currentSchoolYearId,
                lessons = lessons.map { it.toSerializable() },
                reports = reports.map { it.toSerializable() },
                tasks = tasks.map { it.toSerializable() },
                settings = AppSettings(
                    gradeInputMethod = gradeInputMethod.name,
                    showTeachers = showTeachers,
                    showRooms = showRooms,
                    isDarkTheme = isDarkTheme,
                    useDynamicColors = useDynamicColors,
                    useAmoledTheme = useAmoledTheme,
                    customAccentColor = customAccentColor,
                    tasksTabEnabled = tasksTabEnabled,
                    defaultSubjectAlpha = defaultSubjectAlpha
                )
            )

            saveAppData(appData)
            Log.d("DataPersistence", "Data saved successfully")
        } catch (e: IOException) {
            Log.e("DataPersistence", "Error saving data", e)
        } catch (e: Exception) {
            Log.e("DataPersistence", "Error serializing data", e)
        }
    }

    fun saveAppData(appData: AppData) {
        val jsonString = encodeAppData(appData)
        context.openFileOutput(dataFileName, Context.MODE_PRIVATE).use {
            it.write(jsonString.toByteArray(Charsets.UTF_8))
        }
    }

    fun encodeAppData(appData: AppData): String {
        return json.encodeToString(appData)
    }

    fun decodeAppData(jsonString: String): AppData {
        return json.decodeFromString(jsonString)
    }

    fun loadAppData(): AppData? {
        try {
            val file = File(context.filesDir, dataFileName)
            if (!file.exists()) {
                Log.d("DataPersistence", "No saved data found")
                return null
            }

            val jsonString = file.readText()
            return json.decodeFromString<AppData>(jsonString)
        } catch (e: IOException) {
            Log.e("DataPersistence", "Error reading data file", e)
            return null
        } catch (e: Exception) {
            Log.e("DataPersistence", "Error deserializing data", e)
            return null
        }
    }

    fun deleteAppData() {
        try {
            context.deleteFile(dataFileName)
            Log.d("DataPersistence", "Data deleted successfully")
        } catch (e: Exception) {
            Log.e("DataPersistence", "Error deleting data", e)
        }
    }
}
