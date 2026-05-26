package com.future.schoolplanner.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.future.schoolplanner.R
import androidx.compose.ui.graphics.toArgb
import com.future.schoolplanner.data.serialization.AppData
import com.future.schoolplanner.data.serialization.AppSettings
import com.future.schoolplanner.data.serialization.toSerializable
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.OutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    onBack: () -> Unit,
    viewModel: GradeViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isExporting by remember { mutableStateOf(false) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                isExporting = true
                try {
                    val appData = createExportData(viewModel)
                    val jsonString = Json { prettyPrint = true }.encodeToString(appData)
                    
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(jsonString.toByteArray())
                    }
                    
                    Toast.makeText(context, context.getString(R.string.export_success), Toast.LENGTH_SHORT).show()
                    onBack()
                } catch (e: Exception) {
                    Toast.makeText(context, context.getString(R.string.export_error, e.localizedMessage), Toast.LENGTH_LONG).show()
                } finally {
                    isExporting = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.export_data)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = stringResource(R.string.export_description),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(R.string.export_details),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    val fileName = "school_planner_backup_${System.currentTimeMillis()}.json"
                    createDocumentLauncher.launch(fileName)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isExporting
            ) {
                if (isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(R.string.export_data))
                }
            }
        }
    }
}

private fun createExportData(viewModel: GradeViewModel): AppData {
    return AppData(
        subjects = viewModel.subjects.value.map { it.toSerializable() },
        schoolYears = viewModel.schoolYears.value.map { it.toSerializable() },
        currentSchoolYearId = viewModel.currentSchoolYearId.value,
        lessons = viewModel.lessons.value.map { it.toSerializable() },
        reports = viewModel.reports.value.map { it.toSerializable() },
        tasks = viewModel.tasks.value.map { it.toSerializable() },
        settings = AppSettings(
            showTeachers = viewModel.showTeachers.value,
            showRooms = viewModel.showRooms.value,
            isDarkTheme = viewModel.isDarkTheme.value,
            useDynamicColors = viewModel.useDynamicColors.value,
            useAmoledTheme = viewModel.useAmoledTheme.value,
            customAccentColor = viewModel.customAccentColor.value.toArgb(),
            tasksTabEnabled = viewModel.tasksTabEnabled.value,
            defaultSubjectAlpha = viewModel.defaultSubjectAlpha.value
        )
    )
}
