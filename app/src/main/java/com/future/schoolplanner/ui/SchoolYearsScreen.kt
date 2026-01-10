package com.future.schoolplanner.ui

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.future.schoolplanner.data.SchoolYear
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolYearsScreen(
    onBack: () -> Unit,
    onAddSchoolYear: () -> Unit,
    onEditSchoolYear: (String) -> Unit,
    viewModel: GradeViewModel
) {
    val schoolYears = viewModel.schoolYears.collectAsState()
    val currentSchoolYearId = viewModel.currentSchoolYearId.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Schuljahre") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddSchoolYear) {
                Icon(Icons.Default.Add, "Schuljahr hinzufügen")
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
                    text = "Keine Schuljahre vorhanden",
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
                    SchoolYearCard(
                        schoolYear = schoolYear,
                        isCurrent = schoolYear.id == currentSchoolYearId.value,
                        onSetCurrent = { viewModel.setCurrentSchoolYear(schoolYear.id) },
                        onEdit = { onEditSchoolYear(schoolYear.id) },
                        onDelete = { viewModel.deleteSchoolYear(schoolYear.id) }
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
    onSetCurrent: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Schuljahr löschen") },
            text = { Text("Möchten Sie das Schuljahr \"${schoolYear.name}\" wirklich löschen? Diese Aktion kann nicht rückgängig gemacht werden.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Löschen", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onSetCurrent,
                onLongClick = { showDeleteDialog = true }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = schoolYear.name,
                    style = MaterialTheme.typography.headlineSmall
                )
                if (schoolYear.description.isNotEmpty()) {
                    Text(
                        text = schoolYear.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Text(
                    text = "Von ${schoolYear.startDate} bis ${schoolYear.endDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Bearbeiten",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                if (isCurrent) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Aktuelles Schuljahr",
                        tint = MaterialTheme.colorScheme.primary
                    )
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
                title = { Text("Neues Schuljahr") },
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
                placeholder = { Text("z.B. 2024/2025") }
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Beschreibung") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Optionale Beschreibung") }
            )

            OutlinedTextField(
                value = startDate,
                onValueChange = { startDate = it },
                label = { Text("Beginn") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("YYYY-MM-DD") }
            )

            OutlinedTextField(
                value = endDate,
                onValueChange = { endDate = it },
                label = { Text("Ende") },
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
                Text("Schuljahr hinzufügen")
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
                title = { Text("Schuljahr bearbeiten") },
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
                placeholder = { Text("z.B. 2024/2025") }
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Beschreibung") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Optionale Beschreibung") }
            )

            OutlinedTextField(
                value = startDate,
                onValueChange = { startDate = it },
                label = { Text("Beginn") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("YYYY-MM-DD") }
            )

            OutlinedTextField(
                value = endDate,
                onValueChange = { endDate = it },
                label = { Text("Ende") },
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
                Text("Schuljahr speichern")
            }
        }
    }
}