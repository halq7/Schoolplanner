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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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
                title = { Text(if (taskToEdit == null) "Aufgabe hinzufügen" else "Aufgabe bearbeiten") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Zurück")
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
                Icon(Icons.Default.DateRange, if (taskToEdit == null) "Aufgabe speichern" else "Änderungen speichern")
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
                label = { Text("Titel") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Beschreibung") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // Task Type
            Text("Typ", style = MaterialTheme.typography.titleMedium)
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
                    Text("Aufgabe")
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { taskType = TaskType.APPOINTMENT }
                ) {
                    RadioButton(
                        selected = taskType == TaskType.APPOINTMENT,
                        onClick = { taskType = TaskType.APPOINTMENT }
                    )
                    Text("Termin")
                }
            }

            // Due Date (Date X)
            OutlinedTextField(
                value = dueDate,
                onValueChange = { dueDate = it },
                label = { Text("Fälligkeitsdatum (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("z.B. 2024-12-31") }
            )

            // Due Time (optional)
            OutlinedTextField(
                value = dueTime,
                onValueChange = { dueTime = it },
                label = { Text("Fälligkeitszeit (HH:mm, optional)") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("z.B. 14:30, leer lassen für ganztägig") }
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
                    } ?: "Kein Fach zugeordnet",
                    onValueChange = { },
                    label = { Text("Zugeordnetes Fach (optional)") },
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
                        text = { Text("Kein Fach zugeordnet") },
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
