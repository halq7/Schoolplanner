package com.future.schoolplanner.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.future.schoolplanner.R
import com.future.schoolplanner.data.Subject
import com.future.schoolplanner.ui.GradeViewModel
import com.future.schoolplanner.ui.theme.getContrastingTextColor
import com.future.schoolplanner.ui.theme.getGradeColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectListScreen(
    onSubjectSelected: (Subject) -> Unit,
    onAddSubject: () -> Unit,
    onSettings: () -> Unit,
    onSubjectSettings: (Subject) -> Unit = {},
    viewModel: GradeViewModel
) {
    val subjects = viewModel.subjectsForCurrentYear.collectAsState()
    val simulatedGrades = viewModel.simulatedGrades.collectAsState()

    var selectedSubject by remember { mutableStateOf<Subject?>(null) }
    var showSubjectActionDialog by remember { mutableStateOf(false) }
    var showDeleteSubjectDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.grades_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddSubject) {
                Icon(Icons.Default.Add, stringResource(R.string.add_subject))
            }
        }
    ) { paddingValues ->
            if (subjects.value.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                   Text(
                        text = stringResource(R.string.no_subjects),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val overallAverage = viewModel.calculateOverallAverage()
                val hasSimulatedGrades = simulatedGrades.value.isNotEmpty()
                if (overallAverage > 0.0) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(if (hasSimulatedGrades) R.string.simulated_overall_average else R.string.overall_average),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = String.format("%.2f", overallAverage),
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = getGradeColor(overallAverage)
                                )
                                if (hasSimulatedGrades) {
                                    Text(
                                        text = stringResource(R.string.contains_simulated_grades),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }

                items(subjects.value) { subject ->
                    SubjectCard(
                        subject = subject,
                        onClick = { onSubjectSelected(subject) },
                        onLongClick = {
                            selectedSubject = subject
                            showSubjectActionDialog = true
                        },
                        viewModel = viewModel,
                        defaultSubjectAlpha = viewModel.defaultSubjectAlpha.value
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Subject Action Dialog (Edit/Delete options)
    if (showSubjectActionDialog && selectedSubject != null) {
        AlertDialog(
            onDismissRequest = { showSubjectActionDialog = false },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            showSubjectActionDialog = false
                            onSubjectSettings(selectedSubject!!)
                        }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(stringResource(R.string.edit))
                    }
                    TextButton(
                        onClick = {
                            showSubjectActionDialog = false
                            showDeleteSubjectDialog = true
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(stringResource(R.string.delete))
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSubjectActionDialog = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            title = { Text(stringResource(R.string.action_for_subject, selectedSubject!!.name)) },
            text = {
                Column {
                    Text(stringResource(R.string.choose_action))
                }
            }
        )
    }

    // Delete Subject Confirmation Dialog
    if (showDeleteSubjectDialog && selectedSubject != null) {
        AlertDialog(
            onDismissRequest = { showDeleteSubjectDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSubject(selectedSubject!!.id)
                        showDeleteSubjectDialog = false
                    }
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteSubjectDialog = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            title = { Text(stringResource(R.string.delete_subject)) },
            text = {
                Text(stringResource(R.string.delete_subject_confirmation))
            }
        )
    }
}

@Composable
fun SubjectCard(
    subject: Subject,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    viewModel: GradeViewModel,
    defaultSubjectAlpha: Float
) {
    val simulatedGrades = viewModel.simulatedGrades.collectAsState()
    val simulatedGrade = simulatedGrades.value[subject.id]

    val interactionSource = remember { MutableInteractionSource() }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = subject.color.copy(alpha = defaultSubjectAlpha)
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = subject.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = subject.color.getContrastingTextColor()
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = subject.color,
                            shape = MaterialTheme.shapes.small
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = subject.color.getContrastingTextColor(),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (subject.grades.isNotEmpty() || simulatedGrade != null) {
                val average = String.format("%.2f", viewModel.calculateAverage(subject, simulatedGrade))
                val averageText = if (simulatedGrade != null) {
                    stringResource(R.string.simulated_average, average)
                } else {
                    stringResource(R.string.average, average)
                }
                Text(
                    text = averageText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = subject.color.getContrastingTextColor()
                )
                Text(
                    text = stringResource(R.string.grades_count, subject.grades.size),
                    style = MaterialTheme.typography.bodyMedium,
                    color = subject.color.getContrastingTextColor().copy(alpha = 0.7f)
                )
                if (simulatedGrade != null) {
                    Text(
                        text = stringResource(R.string.simulated, simulatedGrade.value, simulatedGrade.weight),
                        style = MaterialTheme.typography.bodySmall,
                        color = subject.color.getContrastingTextColor().copy(alpha = 0.7f)
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.no_grades_available),
                    style = MaterialTheme.typography.bodyMedium,
                    color = subject.color.getContrastingTextColor().copy(alpha = 0.7f)
                )
            }
        }
    }
}
