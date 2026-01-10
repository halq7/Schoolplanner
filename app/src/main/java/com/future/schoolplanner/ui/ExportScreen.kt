package com.future.schoolplanner.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.future.schoolplanner.data.serialization.*
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    onBack: () -> Unit,
    viewModel: GradeViewModel
) {
    val context = LocalContext.current
    var isExporting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daten exportieren") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Ihre Daten werden als JSON-Datei exportiert und können geteilt werden.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Button(
                onClick = {
                    isExporting = true
                    exportData(context, viewModel) {
                        isExporting = false
                    }
                },
                enabled = !isExporting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Exportiere...")
                } else {
                    Text("Daten exportieren")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Die exportierte Datei enthält alle Fächer, Noten, Schuljahre, Aufgaben und Einstellungen.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun exportData(
    context: Context,
    viewModel: GradeViewModel,
    onComplete: () -> Unit
) {
    try {
        // Create AppData from current viewModel state
        val appData = AppData(
            subjects = viewModel.subjects.value.map { it.toSerializable() },
            schoolYears = viewModel.schoolYears.value.map { it.toSerializable() },
            currentSchoolYearId = viewModel.currentSchoolYearId.value,
            lessons = viewModel.lessons.value.map { it.toSerializable() },
            reports = viewModel.reports.value.map { it.toSerializable() },
            tasks = viewModel.tasks.value.map { it.toSerializable() },
            settings = com.future.schoolplanner.data.serialization.AppSettings(
                gradeInputMethod = viewModel.gradeInputMethod.value.name,
                showTeachers = viewModel.showTeachers.value,
                showRooms = viewModel.showRooms.value,
                isDarkTheme = viewModel.isDarkTheme.value,
                useDynamicColors = viewModel.useDynamicColors.value,
                useAmoledTheme = viewModel.useAmoledTheme.value,
                customAccentColor = viewModel.customAccentColor.value.toArgb(),
                tasksTabEnabled = viewModel.tasksTabEnabled.value
            )
        )

        val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
        val jsonString = json.encodeToString(AppData.serializer(), appData)

        // Create temporary file
        val fileName = "school_planner_export_${System.currentTimeMillis()}.json"
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { outputStream ->
            outputStream.write(jsonString.toByteArray(Charsets.UTF_8))
        }

        // Share the file
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Schoolplanner Daten Export")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooserIntent = Intent.createChooser(shareIntent, "Daten exportieren")
        context.startActivity(chooserIntent)

        Toast.makeText(context, "Daten erfolgreich exportiert", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Fehler beim Exportieren: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    } finally {
        onComplete()
    }
}
