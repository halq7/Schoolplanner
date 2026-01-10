package com.future.schoolplanner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.future.schoolplanner.data.WeekType
import com.future.schoolplanner.data.Lesson
import com.future.schoolplanner.ui.theme.blendOver
import com.future.schoolplanner.ui.theme.getContrastingTextColor
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    onAddLesson: (day: Int?, hour: Int?, weekType: WeekType?) -> Unit,
    onEditLesson: (String) -> Unit,
    viewModel: GradeViewModel
) {
    val lessons = viewModel.lessonsForCurrentYear.collectAsState()
    val weekTypeEvenWeeks by viewModel.weekTypeEvenWeeks.collectAsState()

    // Calculate current week type based on settings
    val currentWeekType = remember(weekTypeEvenWeeks) {
        val currentDate = LocalDate.now()
        val weekFields = WeekFields.of(Locale.getDefault())
        val weekOfYear = currentDate.get(weekFields.weekOfYear())
        val isEvenWeek = weekOfYear % 2 == 0

        if ((weekTypeEvenWeeks == WeekType.A && isEvenWeek) ||
            (weekTypeEvenWeeks == WeekType.B && !isEvenWeek)) {
            WeekType.A
        } else {
            WeekType.B
        }
    }

    var selectedWeekType by remember(currentWeekType) { mutableStateOf(currentWeekType) }

    val daysOfWeek = listOf("Mo", "Di", "Mi", "Do", "Fr") // Short names for table
    val maxHours = maxOf(lessons.value.maxOfOrNull { it.hour } ?: 8, 8)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stundenplan") },
                actions = {
                    // Week type selector
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Text("Woche:")
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(
                            selected = selectedWeekType == WeekType.A,
                            onClick = { selectedWeekType = WeekType.A },
                            label = { Text("A") }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        FilterChip(
                            selected = selectedWeekType == WeekType.B,
                            onClick = { selectedWeekType = WeekType.B },
                            label = { Text("B") }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAddLesson(null, null, null) }) {
                Icon(Icons.Default.Add, "Stunde hinzufügen")
            }
        }
    ) { paddingValues ->
        if (viewModel.subjectsForCurrentYear.collectAsState().value.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
               Text(
                    text = "Keine Fächer vorhanden",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // Header row
                item {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        // Time column header (empty)
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Zeit", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        // Day headers
                        daysOfWeek.forEach { day ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .border(1.dp, Color.Gray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(day, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                // Hour rows
                items(maxHours) { hourIndex ->
                    val hour = hourIndex + 1
                    Row(modifier = Modifier.fillMaxWidth()) {
                        // Time column
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(60.dp)
                                .border(1.dp, Color.Gray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("$hour.", style = MaterialTheme.typography.bodyMedium)
                        }
                        // Day cells
                        daysOfWeek.forEachIndexed { dayIndex, _ ->
                            val day = dayIndex + 1
                            val lesson = lessons.value.find {
                                it.dayOfWeek == day && it.hour == hour && it.weekType == selectedWeekType && it.isVisible
                            }
                            ScheduleCell(
                                lesson = lesson,
                                viewModel = viewModel,
                                onEdit = { onEditLesson(it) },
                                onAdd = { onAddLesson(day, hour, selectedWeekType) },
                                defaultSubjectAlpha = viewModel.defaultSubjectAlpha.value
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

}

@Composable
fun ScheduleCell(
    lesson: Lesson?,
    viewModel: GradeViewModel,
    onEdit: (String) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
    defaultSubjectAlpha: Float = 1.0f
) {
    val subject = lesson?.let { viewModel.getSubjectById(it.subjectId) }
    val showTeachers by viewModel.showTeachers.collectAsState()
    val showRooms by viewModel.showRooms.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .width(64.dp)
            .height(60.dp)
            .border(1.dp, Color.Gray)
            .background(
                color = if (subject != null) subject.color.copy(alpha = defaultSubjectAlpha) else Color.Transparent
            )
            .combinedClickable(
                onClick = {
                    if (lesson != null) {
                        onEdit(lesson.id)
                    } else {
                        onAdd()
                    }
                },
                onLongClick = {
                    if (lesson != null) {
                        showDeleteDialog = true
                    }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (subject != null) {
            val effectiveBackgroundColor = subject.color.copy(alpha = 0.2f * defaultSubjectAlpha).blendOver(MaterialTheme.colorScheme.surface)
            val textColor = effectiveBackgroundColor.getContrastingTextColor()

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = subject.abbreviation.ifEmpty { subject.name.take(3) },
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = textColor
                )

                // Show teacher and room info if enabled
                if (showTeachers || showRooms) {
                    val teacherInfo = if (showTeachers && lesson?.teacher?.isNotEmpty() == true) lesson.teacher else null
                    val roomInfo = if (showRooms && lesson?.room?.isNotEmpty() == true) lesson.room else null

                    val infoParts = listOfNotNull(teacherInfo, roomInfo)
                    if (infoParts.isNotEmpty()) {
                        Text(
                            text = infoParts.joinToString(", "),
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.8,
                            textAlign = TextAlign.Center,
                            color = textColor
                        )
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog && lesson != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Stunde löschen") },
            text = {
                Text("Möchten Sie diese Stunde wirklich löschen? Diese Aktion kann nicht rückgängig gemacht werden.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteLesson(lesson.id)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Löschen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}
