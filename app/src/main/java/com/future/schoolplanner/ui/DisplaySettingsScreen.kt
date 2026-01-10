package com.future.schoolplanner.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.future.schoolplanner.data.WeekType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplaySettingsScreen(
    onBack: () -> Unit,
    viewModel: GradeViewModel = viewModel()
) {
    val showTeachers by viewModel.showTeachers.collectAsState()
    val showRooms by viewModel.showRooms.collectAsState()
    val weekTypeEvenWeeks by viewModel.weekTypeEvenWeeks.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Anzeigeeinstellungen") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Stundenplan-Anzeige",
                style = MaterialTheme.typography.titleLarge
            )


            Spacer(modifier = Modifier.height(4.dp))

            // Teacher toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setShowTeachers(!showTeachers) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Lehrer anzeigen",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Zeigt Lehrerinformationen im Stundenplan an",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Switch(
                    checked = showTeachers,
                    onCheckedChange = { viewModel.setShowTeachers(it) }
                )
            }

            // Room toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setShowRooms(!showRooms) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Räume anzeigen",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Zeigt Rauminformationen im Stundenplan an",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Switch(
                    checked = showRooms,
                    onCheckedChange = { viewModel.setShowRooms(it) }
                )
            }

            // Week type configuration
            Text(
                text = "Wochen-Konfiguration",
                style = MaterialTheme.typography.titleMedium
            )


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = weekTypeEvenWeeks == WeekType.A,
                    onClick = { viewModel.setWeekTypeEvenWeeks(WeekType.A) },
                    label = { Text("A-Wochen = gerade") }
                )
                FilterChip(
                    selected = weekTypeEvenWeeks == WeekType.B,
                    onClick = { viewModel.setWeekTypeEvenWeeks(WeekType.B) },
                    label = { Text("B-Wochen = gerade") }
                )
            }
            

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Wochen kopieren",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Kopieren Sie den Stundenplan einer Woche in die andere:",
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { viewModel.copyWeekLessons(WeekType.A, WeekType.B) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text("A → B kopieren")
                    }
                }
                OutlinedButton(
                    onClick = { viewModel.copyWeekLessons(WeekType.B, WeekType.A) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text("B → A kopieren")
                    }
                }
            }

            Text(
                text = "Hinweis: Bestehende Stunden in der Zielwoche werden überschrieben.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
