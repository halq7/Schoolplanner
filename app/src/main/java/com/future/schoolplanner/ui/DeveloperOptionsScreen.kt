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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperOptionsScreen(
    onBack: () -> Unit,
    viewModel: GradeViewModel
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Developer Options") },
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
                Text(
                    text = "Developer Options",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                Text(
                    text = "⚠️ Diese Aktionen löschen Daten unwiderruflich!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }



            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Debug Informationen",
                            style = MaterialTheme.typography.titleLarge
                        )

                        val totalGrades = viewModel.subjects.value.sumOf { it.grades.size }

                        Text(
                            text = "Anzahl Fächer: ${viewModel.subjects.value.size}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = "Anzahl Noten: $totalGrades",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = "Anzahl Zeugnisse: ${viewModel.reports.value.size}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = "Anzahl Schuljahre: ${viewModel.schoolYears.value.size}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = "Anzahl Stunden: ${viewModel.lessons.value.size}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
