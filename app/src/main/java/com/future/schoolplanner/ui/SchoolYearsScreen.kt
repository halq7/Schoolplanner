package com.future.schoolplanner.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.future.schoolplanner.data.SchoolYear
import com.future.schoolplanner.data.Subject
import java.util.UUID
import androidx.compose.ui.res.stringResource
import com.future.schoolplanner.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolYearsScreen(
    onBack: () -> Unit,
    onAddSchoolYear: () -> Unit,
    onEditSchoolYear: (String) -> Unit,
    onAddSubject: (String) -> Unit,
    onSubjectSettings: (String) -> Unit,
    viewModel: GradeViewModel
) {
    val schoolYears = viewModel.schoolYears.collectAsState()
    val currentSchoolYearId = viewModel.currentSchoolYearId.collectAsState()
    val allSubjects = viewModel.subjects.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.school_years)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddSchoolYear) {
                Icon(Icons.Default.Add, stringResource(R.string.add_year))
            }
        }
    ) { paddingValues ->
        if (schoolYears.value.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
               Text(
                    text = stringResource(R.string.no_years),
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
                items(schoolYears.value) { schoolYear ->
                    val yearSubjects = allSubjects.value.filter { it.schoolYearId == schoolYear.id }
                    SchoolYearCard(
                        schoolYear = schoolYear,
                        isCurrent = schoolYear.id == currentSchoolYearId.value,
                        subjects = yearSubjects,
                        onSetCurrent = { viewModel.setCurrentSchoolYear(schoolYear.id) },
                        onEdit = { onEditSchoolYear(schoolYear.id) },
                        onDelete = { viewModel.deleteSchoolYear(schoolYear.id) },
                        onAddSubject = { onAddSubject(schoolYear.id) },
                        onSubjectSettings = onSubjectSettings
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SchoolYearCard(
    schoolYear: SchoolYear,
    isCurrent: Boolean,
    subjects: List<Subject>,
    onSetCurrent: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddSubject: () -> Unit,
    onSubjectSettings: (String) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_year)) },
            text = { Text("${stringResource(R.string.delete)} \"${schoolYear.name}\"? ${stringResource(R.string.undone)}" )},
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { expanded = !expanded },
                onLongClick = { showDeleteDialog = true }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = schoolYear.name,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        if (isCurrent) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(R.string.current_year),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    if (schoolYear.description.isNotEmpty()) {
                        Text(
                            text = schoolYear.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    if (!isCurrent) {
                        IconButton(onClick = onSetCurrent) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Set Current",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                    Text(
                        text = stringResource(R.string.subjects),
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    if (subjects.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_subjects),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        subjects.forEach { subject ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSubjectSettings(subject.id) }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(subject.color, MaterialTheme.shapes.small)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(subject.name)
                                }
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    TextButton(
                        onClick = onAddSubject,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.add_subject))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSchoolYearScreen(
    onBack: () -> Unit,
    onSchoolYearAdded: (SchoolYear) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.new_year)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.year_eg)) }
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.description)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.description_optional),) }
            )

            OutlinedTextField(
                value = startDate,
                onValueChange = { startDate = it },
                label = { Text(stringResource(R.string.start)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("YYYY-MM-DD") }
            )

            OutlinedTextField(
                value = endDate,
                onValueChange = { endDate = it },
                label = { Text(stringResource(R.string.end)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("YYYY-MM-DD") }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (name.isNotEmpty() && startDate.isNotEmpty() && endDate.isNotEmpty()) {
                        val schoolYear = SchoolYear(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            description = description,
                            startDate = startDate,
                            endDate = endDate
                        )
                        onSchoolYearAdded(schoolYear)
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotEmpty() && startDate.isNotEmpty() && endDate.isNotEmpty()
            ) {
                Text(stringResource(R.string.add_year))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSchoolYearScreen(
    schoolYear: SchoolYear,
    onBack: () -> Unit,
    onSchoolYearUpdated: (SchoolYear) -> Unit
) {
    var name by remember { mutableStateOf(schoolYear.name) }
    var description by remember { mutableStateOf(schoolYear.description) }
    var startDate by remember { mutableStateOf(schoolYear.startDate) }
    var endDate by remember { mutableStateOf(schoolYear.endDate) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_year),) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.year_eg)) }
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.description)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.description_optional)) }
            )

            OutlinedTextField(
                value = startDate,
                onValueChange = { startDate = it },
                label = { Text(stringResource(R.string.start)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("YYYY-MM-DD") }
            )

            OutlinedTextField(
                value = endDate,
                onValueChange = { endDate = it },
                label = { Text(stringResource(R.string.end)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("YYYY-MM-DD") }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (name.isNotEmpty() && startDate.isNotEmpty() && endDate.isNotEmpty()) {
                        val updatedSchoolYear = schoolYear.copy(
                            name = name,
                            description = description,
                            startDate = startDate,
                            endDate = endDate
                        )
                        onSchoolYearUpdated(updatedSchoolYear)
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotEmpty() && startDate.isNotEmpty() && endDate.isNotEmpty()
            ) {
                Text(stringResource(R.string.save_year))
            }
        }
    }
}
