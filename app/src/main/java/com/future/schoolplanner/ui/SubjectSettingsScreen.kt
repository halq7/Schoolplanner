package com.future.schoolplanner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.unit.dp
import com.future.schoolplanner.data.Subject
import com.future.schoolplanner.ui.theme.getContrastingTextColor
import androidx.compose.ui.res.stringResource
import com.future.schoolplanner.R



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectSettingsScreen(
    subject: Subject,
    onBack: () -> Unit,
    onSubjectUpdated: (Subject) -> Unit
) {
    var subjectName by remember { mutableStateOf(subject.name) }
    var abbreviation by remember { mutableStateOf(subject.abbreviation) }
    var teacher by remember { mutableStateOf(subject.teacher) }
    var room by remember { mutableStateOf(subject.room) }
    var description by remember { mutableStateOf(subject.description) }
    var selectedColor by remember { mutableStateOf(subject.color) }
    var originalColor by remember { mutableStateOf(ComposeColor.Transparent) }
    var showNameError by remember { mutableStateOf(false) }
    var showAbbreviationError by remember { mutableStateOf(false) }
    var showCustomColorDialog by remember { mutableStateOf(false) }

    // Predefined colors for subjects
    val subjectColors = listOf(
        ComposeColor(0xFF4CAF50), // Green
        ComposeColor(0xFF2196F3), // Blue
        ComposeColor(0xFFFF9800), // Orange
        ComposeColor(0xFF9C27B0), // Purple
        ComposeColor(0xFFE91E63), // Pink
        ComposeColor(0xFF3F51B5), // Indigo
        ComposeColor(0xFF00BCD4), // Cyan
        ComposeColor(0xFF8BC34A)  // Light Green
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.subject_settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        var hasError = false

                        if (subjectName.isBlank()) {
                            showNameError = true
                            hasError = true
                        }

                        if (abbreviation.length < 2 || abbreviation.length > 3) {
                            showAbbreviationError = true
                            hasError = true
                        }

                        if (!hasError) {
                            val updatedSubject = subject.copy(
                                name = subjectName.trim(),
                                abbreviation = abbreviation.trim(),
                                teacher = teacher.trim(),
                                room = room.trim(),
                                description = description.trim(),
                                color = selectedColor
                            )
                            onSubjectUpdated(updatedSubject)
                        }
                    }) {
                        Icon(Icons.Filled.Check, stringResource(R.string.save))
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
            OutlinedTextField(
                value = subjectName,
                onValueChange = {
                    subjectName = it
                    showNameError = false
                },
                label = { Text(stringResource(R.string.subject)) },
                isError = showNameError,
                supportingText = {
                    if (showNameError) {
                        Text(stringResource(R.string.enter_subject_name))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = abbreviation,
                onValueChange = {
                    val filtered = it.filter { char -> char.isLetter() }.take(3)
                    abbreviation = filtered
                    showAbbreviationError = false
                },
                label = { Text(stringResource(R.string.abbreviation)) },
                isError = showAbbreviationError,
                supportingText = {
                    if (showAbbreviationError) {
                        Text(stringResource(R.string.enter_abbreviation))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = teacher,
                onValueChange = { teacher = it },
                label = { Text(stringResource(R.string.teacher)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = room,
                onValueChange = { room = it },
                label = { Text(stringResource(R.string.room)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.description)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Text(
                text = stringResource(R.string.subject_color),
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                subjectColors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = color,
                                shape = MaterialTheme.shapes.small
                            )
                            .clickable { selectedColor = color }
                            .then(
                                if (selectedColor == color) {
                                    Modifier.background(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                        shape = MaterialTheme.shapes.small
                                    )
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedColor == color) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = color.getContrastingTextColor(),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Button(
                onClick = { originalColor = selectedColor; showCustomColorDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.choose_custom_color))
            }


            if (showCustomColorDialog) {
                AlertDialog(
                    onDismissRequest = { showCustomColorDialog = false },
                    title = { Text(stringResource(R.string.choose_custom_color)) },
                    text = {
                        ColorPicker(
                            initialColor = selectedColor,
                            onColorSelected = { selectedColor = it }
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { showCustomColorDialog = false }) {
                            Text(stringResource(R.string.ok))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { selectedColor = originalColor; showCustomColorDialog = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }
        }
    }
}
