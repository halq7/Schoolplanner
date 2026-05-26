package com.future.schoolplanner.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.future.schoolplanner.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperOptionsScreen(
    onBack: () -> Unit,
    viewModel: GradeViewModel
) {
    var showClearSubjectsDialog by remember { mutableStateOf(false) }
    var showClearReportsDialog by remember { mutableStateOf(false) }
    var showClearSchoolYearsDialog by remember { mutableStateOf(false) }
    var showClearLessonsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.developer_options)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    titleContentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Warning Section
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(R.string.dangerous_actions),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Stats Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.debug_info),
                    style = MaterialTheme.typography.titleMedium
                )
                
                val subjectsCount = viewModel.subjects.collectAsState().value.size
                val reportsCount = viewModel.reports.collectAsState().value.size
                val schoolYearsCount = viewModel.schoolYears.collectAsState().value.size
                val lessonsCount = viewModel.lessons.collectAsState().value.size

                DebugInfoRow(stringResource(R.string.subjects_count), subjectsCount.toString())
                DebugInfoRow(stringResource(R.string.reports_count), reportsCount.toString())
                DebugInfoRow(stringResource(R.string.school_years_count), schoolYearsCount.toString())
                DebugInfoRow(stringResource(R.string.lessons_count), lessonsCount.toString())
            }

            // Actions Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showClearSubjectsDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.delete_all_subjects))
                }

                Button(
                    onClick = { showClearReportsDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.delete_all_reports))
                }

                Button(
                    onClick = { showClearSchoolYearsDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.delete_all_school_years))
                }

                Button(
                    onClick = { showClearLessonsDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.delete_all_lessons))
                }
            }
        }
    }

    // Confirmation Dialogs
    if (showClearSubjectsDialog) {
        AlertDialog(
            onDismissRequest = { showClearSubjectsDialog = false },
            title = { Text(stringResource(R.string.delete_all_subjects)) },
            text = { Text(stringResource(R.string.delete_subject_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllSubjects()
                        showClearSubjectsDialog = false
                    }
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearSubjectsDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showClearReportsDialog) {
        AlertDialog(
            onDismissRequest = { showClearReportsDialog = false },
            title = { Text(stringResource(R.string.delete_all_reports)) },
            text = { Text(stringResource(R.string.undone)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllReports()
                        showClearReportsDialog = false
                    }
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearReportsDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showClearSchoolYearsDialog) {
        AlertDialog(
            onDismissRequest = { showClearSchoolYearsDialog = false },
            title = { Text(stringResource(R.string.delete_all_school_years)) },
            text = { Text(stringResource(R.string.undone)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllSchoolYears()
                        showClearSchoolYearsDialog = false
                    }
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearSchoolYearsDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showClearLessonsDialog) {
        AlertDialog(
            onDismissRequest = { showClearLessonsDialog = false },
            title = { Text(stringResource(R.string.delete_all_lessons)) },
            text = { Text(stringResource(R.string.delete_lesson_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllLessons()
                        showClearLessonsDialog = false
                    }
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearLessonsDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun DebugInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
    }
}
