package com.future.schoolplanner.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.future.schoolplanner.R
import com.future.schoolplanner.data.Task
import com.future.schoolplanner.data.TaskType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    onBack: () -> Unit,
    onTaskAdded: (Task) -> Unit,
    viewModel: GradeViewModel = viewModel(),
    taskToEdit: Task? = null
) {
    var title by remember { mutableStateOf(taskToEdit?.title ?: "") }
    var description by remember { mutableStateOf(taskToEdit?.description ?: "") }
    var taskType by remember { mutableStateOf(taskToEdit?.type ?: TaskType.TASK) }
    var dueDate by remember { mutableStateOf(taskToEdit?.dueDate ?: "") }
    var dueTime by remember { mutableStateOf(taskToEdit?.dueTime ?: "") }

    var selectedSubjectId by remember { mutableStateOf<String?>(taskToEdit?.subjectId) }

    val subjects = viewModel.getSubjectsForCurrentYear()
    val currentSchoolYear = viewModel.getCurrentSchoolYear()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (taskToEdit == null) stringResource(R.string.add_task) else stringResource(R.string.edit_task)) },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (title.isNotBlank() && currentSchoolYear != null && dueDate.isNotBlank()) {
                        val task = if (taskToEdit != null) {
                            // Update existing task
                            taskToEdit.copy(
                                title = title,
                                description = description,
                                type = taskType,
                                dueDate = dueDate,
                                dueTime = dueTime.takeIf { it.isNotBlank() },
                                subjectId = selectedSubjectId
                            )
                        } else {
                            // Create new task
                            Task(
                                id = java.util.UUID.randomUUID().toString(),
                                title = title,
                                description = description,
                                type = taskType,
                                dueDate = dueDate,
                                dueTime = dueTime.takeIf { it.isNotBlank() },
                                subjectId = selectedSubjectId,
                                schoolYearId = currentSchoolYear.id
                            )
                        }
                        onTaskAdded(task)
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.DateRange, if (taskToEdit == null) stringResource(R.string.save_task) else stringResource(R.string.save_changes))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.description)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // Task Type
            Text(stringResource(R.string.task_type), style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { taskType = TaskType.TASK }
                ) {
                    RadioButton(
                        selected = taskType == TaskType.TASK,
                        onClick = { taskType = TaskType.TASK }
                    )
                    Text(stringResource(R.string.task))
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { taskType = TaskType.APPOINTMENT }
                ) {
                    RadioButton(
                        selected = taskType == TaskType.APPOINTMENT,
                        onClick = { taskType = TaskType.APPOINTMENT }
                    )
                    Text(stringResource(R.string.appointment))
                }
            }

            // Due Date (Date X)
            OutlinedTextField(
                value = dueDate,
                onValueChange = { dueDate = it },
                label = { Text(stringResource(R.string.due_date)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.due_date_placeholder)) }
            )

            // Due Time (optional)
            OutlinedTextField(
                value = dueTime,
                onValueChange = { dueTime = it },
                label = { Text(stringResource(R.string.due_time_optional)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.due_time_placeholder)) }
            )



            // Subject selection (optional)
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedSubjectId?.let { id ->
                        subjects.find { it.id == id }?.name ?: ""
                    } ?: stringResource(R.string.no_subject_associated),
                    onValueChange = { },
                    label = { Text(stringResource(R.string.associated_subject_optional)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.no_subject_associated)) },
                        onClick = {
                            selectedSubjectId = null
                            expanded = false
                        }
                    )
                    subjects.forEach { subject ->
                        DropdownMenuItem(
                            text = { Text(subject.name) },
                            onClick = {
                                selectedSubjectId = subject.id
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
