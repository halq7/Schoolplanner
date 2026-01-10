package com.future.schoolplanner.ui

import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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
    val subjects = viewModel.subjects.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Noten") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddSubject) {
                Icon(Icons.Default.Add, "Fach hinzufügen")
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
                Text("Keine Fächer vorhanden. Füge ein neues Fach hinzu.")
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
                                    text = "Gesamtschnitt",
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
                }

                items(subjects.value) { subject ->
                    SubjectCard(
                        subject = subject,
                        onClick = { onSubjectSelected(subject) },
                        onLongClick = {
                            onSubjectSettings(subject)
                        },
                        viewModel = viewModel
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun SubjectCard(
    subject: Subject,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    viewModel: GradeViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = subject.color.copy(alpha = 1f)
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

            if (subject.grades.isNotEmpty()) {
                val average = String.format("%.2f", viewModel.calculateAverage(subject))
                Text(
                    text = "Durchschnitt: $average",
                    style = MaterialTheme.typography.bodyLarge,
                    color = subject.color.getContrastingTextColor()
                )
                Text(
                    text = "${subject.grades.size} Noten",
                    style = MaterialTheme.typography.bodyMedium,
                    color = subject.color.getContrastingTextColor().copy(alpha = 0.7f)
                )
            } else {
                Text(
                    text = "Keine Noten vorhanden",
                    style = MaterialTheme.typography.bodyMedium,
                    color = subject.color.getContrastingTextColor().copy(alpha = 0.7f)
                )
            }
        }
    }
}
