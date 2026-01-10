package com.future.schoolplanner.ui

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.future.schoolplanner.data.Grade
import com.future.schoolplanner.data.GradeInputMethod
import com.future.schoolplanner.data.Subject
import com.future.schoolplanner.ui.theme.getGradeColor
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeDetailScreen(
    subject: Subject,
    onBack: () -> Unit,
    viewModel: GradeViewModel
) {
    val selectedSubject by viewModel.selectedSubject.collectAsState()
    val currentSubject = selectedSubject ?: subject
    var showAddGradeDialog by remember { mutableStateOf(false) }
    var selectedGrade by remember { mutableStateOf<Grade?>(null) }
    var showGradeActionDialog by remember { mutableStateOf(false) }
    var showEditGradeDialog by remember { mutableStateOf(false) }
    var showDeleteGradeDialog by remember { mutableStateOf(false) }
    var showSimulation by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentSubject.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Zurück")
                    }
                },
                actions = {
                    IconButton(onClick = { showSimulation = !showSimulation }) {
                        Icon(Icons.Default.Build, "Simulation")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = currentSubject.color.copy(alpha = 0.2f),
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddGradeDialog = true }) {
                Icon(Icons.Default.Add, "Note hinzufügen")
            }
        }
    ) { paddingValues ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            val simulatedGrades = viewModel.simulatedGrades.collectAsState()
            val simulatedGrade = simulatedGrades.value[currentSubject.id]

            // Subject info and average
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = currentSubject.color.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = currentSubject.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (currentSubject.grades.isNotEmpty() || simulatedGrade != null) {
                        val average = String.format("%.2f", viewModel.calculateAverage(currentSubject, simulatedGrade))
                        val averageText = if (simulatedGrade != null) {
                            "Simulierter Durchschnitt: $average"
                        } else {
                            "Aktueller Durchschnitt: $average"
                        }
                        Text(
                            text = averageText,
                            style = MaterialTheme.typography.headlineSmall,
                            color = getGradeColor(average.toDouble())
                        )
                        Text(
                            text = "${currentSubject.grades.size} Noten",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (simulatedGrade != null) {
                            Text(
                                text = "Simuliert: ${simulatedGrade.value} (Gewichtung: ${simulatedGrade.weight})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    } else {
                        Text(
                            text = "Keine Noten vorhanden",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Simulation section
            if (showSimulation) {
                SimulationCard(
                    subjectId = currentSubject.id,
                    viewModel = viewModel
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Overall average with simulation
                val overallAverage = viewModel.calculateOverallAverage()
                if (overallAverage > 0.0) {
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
                                text = "Simulierter Gesamtschnitt (alle Fächer)",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = String.format("%.2f", overallAverage),
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = getGradeColor(overallAverage)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Grades list
            if (currentSubject.grades.isNotEmpty()) {
                Text(
                    text = "Deine Noten:",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    currentSubject.grades.forEach { grade ->
                        GradeItem(
                            grade = grade,
                            onLongClick = {
                                selectedGrade = grade
                                showGradeActionDialog = true
                            }
                        )
                    }
                }
            }

            if (showAddGradeDialog) {
                AddGradeDialog(
                    subject = currentSubject,
                    onDismiss = { showAddGradeDialog = false },
                    onAddGrade = { grade ->
                        viewModel.addGrade(currentSubject.id, grade)
                        showAddGradeDialog = false
                    },
                    viewModel = viewModel
                )
            }

            // Grade Action Dialog (Edit/Delete options)
            if (showGradeActionDialog && selectedGrade != null) {
                AlertDialog(
                    onDismissRequest = { showGradeActionDialog = false },
                    title = { Text("Aktion für Note ${selectedGrade!!.value}") },
                    text = {
                        Column {
                            Text("Wählen Sie eine Aktion:")
                        }
                    },
                    confirmButton = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            androidx.compose.material3.TextButton(
                                onClick = {
                                    showGradeActionDialog = false
                                    showEditGradeDialog = true
                                }
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null)
                                Spacer(modifier = Modifier.size(4.dp))
                                Text("Bearbeiten")
                            }
                            androidx.compose.material3.TextButton(
                                onClick = {
                                    showGradeActionDialog = false
                                    showDeleteGradeDialog = true
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                                Spacer(modifier = Modifier.size(4.dp))
                                Text("Löschen")
                            }
                        }
                    },
                    dismissButton = {
                        androidx.compose.material3.TextButton(
                            onClick = { showGradeActionDialog = false }
                        ) {
                            Text("Abbrechen")
                        }
                    }
                )
            }

            // Edit Grade Dialog
            if (showEditGradeDialog && selectedGrade != null) {
                EditGradeDialog(
                    grade = selectedGrade!!,
                    onDismiss = { showEditGradeDialog = false },
                    onGradeUpdated = { value, weight, description, date ->
                        viewModel.updateGrade(currentSubject.id, selectedGrade!!.id, value, weight, description, date)
                        showEditGradeDialog = false
                    },
                    viewModel = viewModel
                )
            }

            // Delete Grade Confirmation Dialog
            if (showDeleteGradeDialog && selectedGrade != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteGradeDialog = false },
                    title = { Text("Note löschen") },
                    text = {
                        Text("Möchten Sie diese Note wirklich löschen? Diese Aktion kann nicht rückgängig gemacht werden.")
                    },
                    confirmButton = {
                            androidx.compose.material3.TextButton(
                                onClick = {
                                    viewModel.deleteGrade(currentSubject.id, selectedGrade!!.id)
                                    showDeleteGradeDialog = false
                                }
                            ) {
                                Text("Löschen", color = MaterialTheme.colorScheme.error)
                            }
                    },
                    dismissButton = {
                        androidx.compose.material3.TextButton(
                            onClick = { showDeleteGradeDialog = false }
                        ) {
                            Text("Abbrechen")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SimulationCard(
    subjectId: String,
    viewModel: GradeViewModel
) {
    val gradeInputMethod by viewModel.gradeInputMethod.collectAsState()
    val simulatedGrades = viewModel.simulatedGrades.collectAsState()
    val simulatedGrade = simulatedGrades.value[subjectId]

    var simGradeValue by remember { mutableStateOf("") }
    var simWeight by remember { mutableStateOf("1.0") }
    var showSimError by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Noten-Simulation",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Simuliere eine Note, um zu sehen, wie sich der Durchschnitt ändert. Die simulierte Note wird nicht gespeichert.",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            val labelText = when (gradeInputMethod) {
                GradeInputMethod.WHOLE -> "Note (1-6)"
                GradeInputMethod.DECIMAL -> "Note (1.0-6.0)"
                GradeInputMethod.TENDENCY -> "Note (z.B. 2-)"
                GradeInputMethod.FIFTEEN_POINT -> "Punkte (0-15)"
                else -> "Note"
            }

            OutlinedTextField(
                value = simGradeValue,
                onValueChange = { simGradeValue = it },
                label = { Text(labelText) },
                modifier = Modifier.fillMaxWidth(),
                isError = showSimError,
                supportingText = {
                    if (showSimError) {
                        Text("Ungültige Note")
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = simWeight,
                onValueChange = { simWeight = it },
                label = { Text("Gewichtung") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        val parsedValue = viewModel.parseGradeInput(simGradeValue, gradeInputMethod)
                        val weightValue = simWeight.toDoubleOrNull() ?: 1.0
                        if (parsedValue != null) {
                            viewModel.setSimulatedGrade(subjectId, SimulatedGrade(parsedValue, weightValue))
                            showSimError = false
                        } else {
                            showSimError = true
                        }
                    },
                    enabled = simGradeValue.isNotBlank()
                ) {
                    Text("Simulieren")
                }

                if (simulatedGrade != null) {
                    IconButton(
                        onClick = {
                            viewModel.clearSimulatedGrade(subjectId)
                            simGradeValue = ""
                            simWeight = "1.0"
                        }
                    ) {
                        Icon(Icons.Default.Clear, "Simulation löschen")
                    }
                }
            }
        }
    }
}

@Composable
fun EditGradeDialog(
    grade: Grade,
    onDismiss: () -> Unit,
    onGradeUpdated: (Double, Double, String, String) -> Unit,
    viewModel: GradeViewModel
) {
    val gradeInputMethod by viewModel.gradeInputMethod.collectAsState()
    var gradeValue by remember { mutableStateOf(viewModel.formatGradeForDisplay(grade.value, gradeInputMethod)) }
    var description by remember { mutableStateOf(grade.description) }
    var weight by remember { mutableStateOf(grade.weight.toString()) }
    var date by remember { mutableStateOf(grade.date) }
    var showError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Note bearbeiten",
                    style = MaterialTheme.typography.headlineSmall
                )

                val labelText = when (gradeInputMethod) {
                    GradeInputMethod.WHOLE -> "Note (1-6)"
                    GradeInputMethod.DECIMAL -> "Note (1.0-6.0)"
                    GradeInputMethod.TENDENCY -> "Note (z.B. 2-)"
                    GradeInputMethod.FIFTEEN_POINT -> "Punkte (0-15)"
                    else -> "Note"
                }

                val errorText = when (gradeInputMethod) {
                    GradeInputMethod.WHOLE -> "Bitte gib eine gültige Note ein (1-6)"
                    GradeInputMethod.DECIMAL -> "Bitte gib eine gültige Note ein (1.0-6.0)"
                    GradeInputMethod.TENDENCY -> "Bitte gib eine gültige Note ein (z.B. 2-)"
                    GradeInputMethod.FIFTEEN_POINT -> "Bitte gib gültige Punkte ein (0-15)"
                    else -> "Bitte gib eine gültige Note ein"
                }

                OutlinedTextField(
                    value = gradeValue,
                    onValueChange = { gradeValue = it },
                    label = { Text(labelText) },
                    isError = showError,
                    supportingText = {
                        if (showError) {
                            Text(errorText)
                        }
                    }
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Beschreibung (optional)") }
                )

                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Gewichtung (optional)") }
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Datum (optional)") }
                )

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(onClick = onDismiss) {
                        Text("Abbrechen")
                    }

                    Spacer(modifier = Modifier.size(8.dp))

                    Button(
                        onClick = {
                            val parsedValue = viewModel.parseGradeInput(gradeValue, gradeInputMethod)
                            if (parsedValue != null) {
                                val weightValue = weight.toDoubleOrNull() ?: 1.0
                                onGradeUpdated(parsedValue, weightValue, description, date)
                            } else {
                                showError = true
                            }
                        }
                    ) {
                        Text("Speichern")
                    }
                }
            }
        }
    }
}

@Composable
fun GradeItem(
    grade: Grade,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = onLongClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = grade.value.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = getGradeColor(grade.value)
                )
                if (grade.date.isNotBlank()) {
                    Text(
                        text = grade.date,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            if (grade.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = grade.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (grade.weight != 1.0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Gewichtung: ${grade.weight}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun AddGradeDialog(
    subject: Subject,
    onDismiss: () -> Unit,
    onAddGrade: (Grade) -> Unit,
    viewModel: GradeViewModel
) {
    val gradeInputMethod by viewModel.gradeInputMethod.collectAsState()
    var gradeValue by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("1.0") }
    var date by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Neue Note hinzufügen",
                    style = MaterialTheme.typography.headlineSmall
                )

                val labelText = when (gradeInputMethod) {
                    GradeInputMethod.WHOLE -> "Note (1-6)"
                    GradeInputMethod.DECIMAL -> "Note (1.0-6.0)"
                    GradeInputMethod.TENDENCY -> "Note (z.B. 2-)"
                    GradeInputMethod.FIFTEEN_POINT -> "Punkte (0-15)"
                    else -> "Note"
                }

                val errorText = when (gradeInputMethod) {
                    GradeInputMethod.WHOLE -> "Bitte gib eine gültige Note ein (1-6)"
                    GradeInputMethod.DECIMAL -> "Bitte gib eine gültige Note ein (1.0-6.0)"
                    GradeInputMethod.TENDENCY -> "Bitte gib eine gültige Note ein (z.B. 2-)"
                    GradeInputMethod.FIFTEEN_POINT -> "Bitte gib gültige Punkte ein (0-15)"
                    else -> "Bitte gib eine gültige Note ein"
                }

                OutlinedTextField(
                    value = gradeValue,
                    onValueChange = { gradeValue = it },
                    label = { Text(labelText) },
                    isError = showError,
                    supportingText = {
                        if (showError) {
                            Text(errorText)
                        }
                    }
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Beschreibung (optional)") }
                )

                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Gewichtung (optional)") }
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Datum (optional)") }
                )

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(onClick = onDismiss) {
                        Text("Abbrechen")
                    }

                    Spacer(modifier = Modifier.size(8.dp))

                    Button(
                        onClick = {
                            val parsedValue = viewModel.parseGradeInput(gradeValue, gradeInputMethod)
                            if (parsedValue != null) {
                                val grade = Grade(
                                    id = UUID.randomUUID().toString(),
                                    value = parsedValue,
                                    weight = weight.toDoubleOrNull() ?: 1.0,
                                    description = description,
                                    date = date
                                )
                                onAddGrade(grade)
                            } else {
                                showError = true
                            }
                        }
                    ) {
                        Text("Hinzufügen")
                    }
                }
            }
        }
    }
}
