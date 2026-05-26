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
