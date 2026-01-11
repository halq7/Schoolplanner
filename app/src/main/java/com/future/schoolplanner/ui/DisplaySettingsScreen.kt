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
import androidx.compose.ui.res.stringResource
import com.future.schoolplanner.R

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
                title = { Text(stringResource(R.string.schedule_display)) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {


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
                        text = stringResource(R.string.show_teachers),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = stringResource(R.string.show_teachers_description),
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
                        text = stringResource(R.string.show_rooms),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = stringResource(R.string.show_rooms_description),
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
                text = stringResource(R.string.week_configuration),
                style = MaterialTheme.typography.titleMedium
            )


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = weekTypeEvenWeeks == WeekType.A,
                    onClick = { viewModel.setWeekTypeEvenWeeks(WeekType.A) },
                    label = { Text(stringResource(R.string.a_weeks_even)) }
                )
                FilterChip(
                    selected = weekTypeEvenWeeks == WeekType.B,
                    onClick = { viewModel.setWeekTypeEvenWeeks(WeekType.B) },
                    label = { Text(stringResource(R.string.b_weeks_even)) }
                )
            }
            

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.copy_week),
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = stringResource(R.string.copy_week_description),
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
                        Text(stringResource(R.string.copy_a_to_b))
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
                        Text(stringResource(R.string.copy_b_to_a))
                    }
                }
            }

            Text(
                text = stringResource(R.string.copy_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
