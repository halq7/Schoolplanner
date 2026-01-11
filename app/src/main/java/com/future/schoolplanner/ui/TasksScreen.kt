package com.future.schoolplanner.ui

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate
import androidx.compose.ui.res.stringResource
import com.future.schoolplanner.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    onBack: () -> Unit,
    onAddTask: () -> Unit,
    onEditTask: (String) -> Unit,
    viewModel: GradeViewModel = viewModel()
) {
    val tasks by viewModel.getTasksForCurrentYear().collectAsState()
    val pendingTasks = tasks.filter { !it.isCompleted }
    val completedTasks = tasks.filter { it.isCompleted }
    var showCompletedTasks by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<com.future.schoolplanner.data.Task?>(null) }
    var showTaskActionDialog by remember { mutableStateOf(false) }
    var showDeleteTaskDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aufgaben") },
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
                onClick = onAddTask,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Aufgabe hinzufügen")
            }
        }
    ) { paddingValues ->
        if (tasks.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.no_tasks),
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                // Pending tasks section
                if (pendingTasks.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.open_tasks),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(pendingTasks) { task ->
                        TaskItem(
                            task = task,
                            onToggleComplete = { viewModel.toggleTaskCompletion(task.id) },
                            onLongClick = {
                                selectedTask = task
                                showTaskActionDialog = true
                            },
                            viewModel = viewModel
                        )
                    }
                }

                // Completed tasks section (collapsible)
                if (completedTasks.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCompletedTasks = !showCompletedTasks }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${stringResource(R.string.completed_tasks)} (${completedTasks.size})",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Icon(
                                imageVector = if (showCompletedTasks) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (showCompletedTasks) stringResource(R.string.expand) else stringResource(R.string.collapse)
                            )
                        }
                    }

                    // Completed tasks section
                    if (showCompletedTasks) {
                        completedTasks.forEach { task ->
                            item {
                                TaskItem(
                                    task = task,
                                    onToggleComplete = { viewModel.toggleTaskCompletion(task.id) },
                                    onLongClick = {
                                        selectedTask = task
                                        showTaskActionDialog = true
                                    },
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Task Action Dialog (Edit/Delete options)
    if (showTaskActionDialog && selectedTask != null) {
        AlertDialog(
            onDismissRequest = { showTaskActionDialog = false },
            title = { Text("${stringResource(R.string.action_for_task)} \"${selectedTask!!.title}\"") },
            text = {
                Column {
                    Text(stringResource(R.string.choose_action))
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            showTaskActionDialog = false
                            onEditTask(selectedTask!!.id)
                        }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(stringResource(R.string.edit))
                    }
                    TextButton(
                        onClick = {
                            showTaskActionDialog = false
                            showDeleteTaskDialog = true
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
                    onClick = { showTaskActionDialog = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Delete Task Confirmation Dialog
    if (showDeleteTaskDialog && selectedTask != null) {
        AlertDialog(
            onDismissRequest = { showDeleteTaskDialog = false },
            title = { Text(stringResource(R.string.delete_task)) },
            text = {
                Text(stringResource(R.string.delete_task_confirmation))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTask(selectedTask!!.id)
                        showDeleteTaskDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteTaskDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskItem(
    task: com.future.schoolplanner.data.Task,
    onToggleComplete: () -> Unit,
    onLongClick: () -> Unit,
    viewModel: GradeViewModel
) {
    val subject = task.subjectId?.let { viewModel.getSubjectById(it) }
    val isOverdue = !task.isCompleted && try {
        LocalDate.parse(task.dueDate).isBefore(LocalDate.now())
    } catch (e: Exception) {
        false
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isOverdue) androidx.compose.foundation.BorderStroke(2.dp, Color.Red) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {},
                    onLongClick = onLongClick
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggleComplete() }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = if (task.isCompleted)
                        MaterialTheme.typography.titleMedium.copy(textDecoration = TextDecoration.LineThrough)
                    else
                        MaterialTheme.typography.titleMedium
                )
                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${if (task.type == com.future.schoolplanner.data.TaskType.APPOINTMENT) "Termin" else "Aufgabe"} • ${task.dueDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    subject?.let {
                        Text(
                            text = it.abbreviation,
                            style = MaterialTheme.typography.bodySmall,
                            color = it.color
                        )
                    }
                }
            }
        }
    }
}
