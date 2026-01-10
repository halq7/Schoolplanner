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
import com.future.schoolplanner.data.WeekType
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLessonScreen(
    onBack: () -> Unit,
    onLessonAdded: (Lesson) -> Unit,
    viewModel: GradeViewModel,
    lessonToEdit: Lesson? = null,
    initialDay: Int? = null,
    initialHour: Int? = null,
    initialWeekType: WeekType? = null,
    fixedDay: Boolean = false,
    fixedHour: Boolean = false,
    fixedWeekType: Boolean = false
) {
    val subjects = viewModel.subjectsForCurrentYear.collectAsState()

    var selectedSubjectId by remember { mutableStateOf(lessonToEdit?.subjectId ?: "") }
    var selectedDay by remember { mutableStateOf(lessonToEdit?.dayOfWeek ?: initialDay ?: 1) }
    var selectedHour by remember { mutableStateOf(lessonToEdit?.hour ?: initialHour ?: 1) }
    var selectedWeekType by remember { mutableStateOf(lessonToEdit?.weekType ?: initialWeekType ?: WeekType.A) }
    var teacher by remember { mutableStateOf(lessonToEdit?.teacher ?: "") }
    var room by remember { mutableStateOf(lessonToEdit?.room ?: "") }
    var isVisible by remember { mutableStateOf(lessonToEdit?.isVisible ?: true) }

    val daysOfWeek = listOf("Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (lessonToEdit == null) "Stunde hinzufügen" else "Stunde bearbeiten") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Subject selection
            Text("Fach", style = MaterialTheme.typography.titleMedium)
            var expandedSubject by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedSubject,
                onExpandedChange = { expandedSubject = it }
            ) {
                OutlinedTextField(
                    value = subjects.value.find { it.id == selectedSubjectId }?.name ?: "Fach auswählen",
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

            // Hour selection
            Text("Stunde", style = MaterialTheme.typography.titleMedium)
            if (fixedHour) {
                Text(selectedHour.toString(), style = MaterialTheme.typography.bodyLarge)
            } else {
                OutlinedTextField(
                    value = selectedHour.toString(),
                    onValueChange = { value ->
                        value.toIntOrNull()?.let { hour ->
                            if (hour in 1..12) selectedHour = hour
                        }
                    },
                    label = { Text("Stundennummer (1-12)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Week type selection
            Text("Wochentyp", style = MaterialTheme.typography.titleMedium)
            if (fixedWeekType) {
                Text(if (selectedWeekType == WeekType.A) "A-Woche" else "B-Woche", style = MaterialTheme.typography.bodyLarge)
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedWeekType == WeekType.A,
                        onClick = { selectedWeekType = WeekType.A },
                        label = { Text("A-Woche") }
                    )
                    FilterChip(
                        selected = selectedWeekType == WeekType.B,
                        onClick = { selectedWeekType = WeekType.B },
                        label = { Text("B-Woche") }
                    )
                }
            }

            // Teacher
            OutlinedTextField(
                value = teacher,
                onValueChange = { teacher = it },
                label = { Text("Lehrer") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Room
            OutlinedTextField(
                value = room,
                onValueChange = { room = it },
                label = { Text("Raum") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Visibility
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = isVisible,
                    onCheckedChange = { isVisible = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Im Stundenplan anzeigen")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (selectedSubjectId.isNotEmpty()) {
                        val currentSchoolYearId = viewModel.currentSchoolYearId.value
                        if (currentSchoolYearId != null) {
                            val lesson = lessonToEdit?.copy(
                                subjectId = selectedSubjectId,
                                dayOfWeek = selectedDay,
                                hour = selectedHour,
                                weekType = selectedWeekType,
                                teacher = teacher,
                                room = room,
                                isVisible = isVisible
                            ) ?: Lesson(
                                id = UUID.randomUUID().toString(),
                                subjectId = selectedSubjectId,
                                dayOfWeek = selectedDay,
                                hour = selectedHour,
                                weekType = selectedWeekType,
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
                Text(if (lessonToEdit == null) "Stunde hinzufügen" else "Stunde speichern")
            }
        }
    }
}