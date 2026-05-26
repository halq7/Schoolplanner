package com.future.schoolplanner.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.future.schoolplanner.data.Lesson
import java.util.*
import androidx.compose.ui.res.stringResource
import com.future.schoolplanner.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLessonScreen(
    onBack: () -> Unit,
    onLessonAdded: (Lesson) -> Unit,
    viewModel: GradeViewModel,
    lessonToEdit: Lesson? = null,
    initialDay: Int? = null,
    initialHour: Int? = null,
    fixedDay: Boolean = false,
    fixedHour: Boolean = false
) {
    val subjects = viewModel.subjectsForCurrentYear.collectAsState()

    var selectedSubjectId by remember { mutableStateOf(lessonToEdit?.subjectId ?: "") }
    var selectedDay by remember { mutableIntStateOf(lessonToEdit?.dayOfWeek ?: initialDay ?: 1) }
    var startTime by remember { mutableStateOf(lessonToEdit?.startTime ?: "08:00") }
    var endTime by remember { mutableStateOf(lessonToEdit?.endTime ?: "08:45") }
    var teacher by remember { mutableStateOf(lessonToEdit?.teacher ?: "") }
    var room by remember { mutableStateOf(lessonToEdit?.room ?: "") }
    var isVisible by remember { mutableStateOf(lessonToEdit?.isVisible ?: true) }

    val daysOfWeek = listOf(stringResource(R.string.monday), stringResource(R.string.tuesday), stringResource(R.string.wednesday), stringResource(R.string.thursday), stringResource(R.string.friday))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (lessonToEdit == null) stringResource(R.string.add_lesson) else stringResource(R.string.edit_lesson)) },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Subject selection
            Text(stringResource(R.string.subject), style = MaterialTheme.typography.titleMedium)
            var expandedSubject by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedSubject,
                onExpandedChange = { expandedSubject = it }
            ) {
                OutlinedTextField(
                    value = subjects.value.find { it.id == selectedSubjectId }?.name ?: stringResource(R.string.choose_subject),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSubject)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedSubject,
                    onDismissRequest = { expandedSubject = false }
                ) {
                    subjects.value.forEach { subject ->
                        DropdownMenuItem(
                            text = { Text(subject.name) },
                            onClick = {
                                selectedSubjectId = subject.id
                                expandedSubject = false
                            }
                        )
                    }
                }
            }

            // Day selection
            Text("Tag", style = MaterialTheme.typography.titleMedium)
            if (fixedDay) {
                Text(daysOfWeek[selectedDay - 1], style = MaterialTheme.typography.bodyLarge)
            } else {
                var expandedDay by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedDay,
                    onExpandedChange = { expandedDay = it }
                ) {
                    OutlinedTextField(
                        value = daysOfWeek[selectedDay - 1],
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDay)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDay,
                        onDismissRequest = { expandedDay = false }
                    ) {
                        daysOfWeek.forEachIndexed { index, day ->
                            DropdownMenuItem(
                                text = { Text(day) },
                                onClick = {
                                    selectedDay = index + 1
                                    expandedDay = false
                                }
                            )
                        }
                    }
                }
            }

            // Time selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = { Text(stringResource(R.string.start)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = endTime,
                    onValueChange = { endTime = it },
                    label = { Text(stringResource(R.string.end)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Teacher
            OutlinedTextField(
                value = teacher,
                onValueChange = { teacher = it },
                label = { Text(stringResource(R.string.teacher)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Room
            OutlinedTextField(
                value = room,
                onValueChange = { room = it },
                label = { Text(stringResource(R.string.room)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (selectedSubjectId.isNotEmpty()) {
                        val currentSchoolYearId = viewModel.currentSchoolYearId.value
                        if (currentSchoolYearId != null) {
                            val lesson = lessonToEdit?.copy(
                                subjectId = selectedSubjectId,
                                dayOfWeek = selectedDay,
                                startTime = startTime,
                                endTime = endTime,
                                teacher = teacher,
                                room = room,
                                isVisible = isVisible
                            ) ?: Lesson(
                                id = UUID.randomUUID().toString(),
                                subjectId = selectedSubjectId,
                                dayOfWeek = selectedDay,
                                startTime = startTime,
                                endTime = endTime,
                                teacher = teacher,
                                room = room,
                                isVisible = isVisible,
                                schoolYearId = currentSchoolYearId
                            )
                            onLessonAdded(lesson)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedSubjectId.isNotEmpty()
            ) {
                Text(if (lessonToEdit == null) stringResource(R.string.add_lesson) else stringResource(R.string.save_lesson))
            }
        }
    }
}
