package com.future.schoolplanner.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    var showClearGradesDialog by remember { mutableStateOf(false) }
    var showClearReportsDialog by remember { mutableStateOf(false) }
    var showClearSchoolYearsDialog by remember { mutableStateOf(false) }
    var showClearLessonsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.developer_options)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.debug_info),
                            style = MaterialTheme.typography.titleLarge
                        )

                        val totalGrades = viewModel.subjects.value.sumOf { it.grades.size }

                        Text(
                            text = "${stringResource(R.string.subjects_count)} ${viewModel.subjects.value.size}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = "${stringResource(R.string.tab_grades)}: $totalGrades",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = "${stringResource(R.string.reports_count)} ${viewModel.reports.value.size}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = "${stringResource(R.string.school_years_count)} ${viewModel.schoolYears.value.size}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = "${stringResource(R.string.lessons_count)} ${viewModel.lessons.value.size}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.dangerous_actions),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )

                        Button(
                            onClick = { showClearSubjectsDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(stringResource(R.string.delete_all_subjects))
                        }

                        Button(
                            onClick = { showClearGradesDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(stringResource(R.string.delete_all_grades))
                        }

                        Button(
                            onClick = { showClearReportsDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(stringResource(R.string.delete_all_reports))
                        }

                        Button(
                            onClick = { showClearSchoolYearsDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(stringResource(R.string.delete_all_school_years))
                        }

                        Button(
                            onClick = { showClearLessonsDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(stringResource(R.string.delete_all_lessons))
                        }
                    }
                }
            }
        }
    }

    // Confirmation dialogs
    if (showClearSubjectsDialog) {
        AlertDialog(
            onDismissRequest = { showClearSubjectsDialog = false },
            title = { Text(stringResource(R.string.delete_all_subjects)) },
            text = { Text("${stringResource(R.string.delete_subject_confirmation)} ${stringResource(R.string.undone)}") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllSubjects()
                        showClearSubjectsDialog = false
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearSubjectsDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showClearGradesDialog) {
        AlertDialog(
            onDismissRequest = { showClearGradesDialog = false },
            title = { Text(stringResource(R.string.delete_all_grades)) },
            text = { Text("${stringResource(R.string.delete_grade_confirmation)} ${stringResource(R.string.undone)}") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllGrades()
                        showClearGradesDialog = false
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearGradesDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showClearReportsDialog) {
        AlertDialog(
            onDismissRequest = { showClearReportsDialog = false },
            title = { Text(stringResource(R.string.delete_all_reports)) },
            text = { Text("${stringResource(R.string.delete_grade_confirmation)} ${stringResource(R.string.undone)}") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllReports()
                        showClearReportsDialog = false
                    }
                ) {
                    Text(stringResource(R.string.delete))
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
                    Text(stringResource(R.string.delete))
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
            text = { Text("${stringResource(R.string.delete_lesson_confirmation)} ${stringResource(R.string.undone)}") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllLessons()
                        showClearLessonsDialog = false
                    }
                ) {
                    Text(stringResource(R.string.delete))
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
