package com.future.schoolplanner.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.future.schoolplanner.R
import com.future.schoolplanner.data.Lesson
import com.future.schoolplanner.ui.theme.blendOver
import com.future.schoolplanner.ui.theme.getContrastingTextColor
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

enum class ScheduleViewMode {
    DAY, WEEK, MONTH
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    onAddLesson: (day: Int?, hour: Int?) -> Unit,
    onEditLesson: (String) -> Unit,
    viewModel: GradeViewModel
) {
    val lessons by viewModel.lessonsForCurrentYear.collectAsState()
    var viewMode by remember { mutableStateOf(ScheduleViewMode.WEEK) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    stringResource(R.string.schedule_view),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.view_day)) },
                    selected = viewMode == ScheduleViewMode.DAY,
                    onClick = {
                        viewMode = ScheduleViewMode.DAY
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Today, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.view_week)) },
                    selected = viewMode == ScheduleViewMode.WEEK,
                    onClick = {
                        viewMode = ScheduleViewMode.WEEK
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.ViewWeek, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.view_month)) },
                    selected = viewMode == ScheduleViewMode.MONTH,
                    onClick = {
                        viewMode = ScheduleViewMode.MONTH
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.CalendarMonth, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                when (viewMode) {
                                    ScheduleViewMode.DAY -> stringResource(R.string.view_day)
                                    ScheduleViewMode.WEEK -> stringResource(R.string.view_week)
                                    ScheduleViewMode.MONTH -> stringResource(R.string.view_month)
                                },
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                when (viewMode) {
                                    ScheduleViewMode.DAY -> selectedDate.format(DateTimeFormatter.ofPattern("EEEE, dd. MMMM", Locale.getDefault()))
                                    ScheduleViewMode.WEEK -> stringResource(R.string.current_week_schedule)
                                    ScheduleViewMode.MONTH -> selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))
                                },
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, "Menu")
                        }
                    },
                    actions = {
                        if (selectedDate != LocalDate.now()) {
                            IconButton(onClick = { selectedDate = LocalDate.now() }) {
                                Icon(Icons.Default.History, "Hôm nay")
                            }
                        }
                        IconButton(onClick = {
                            selectedDate = when (viewMode) {
                                ScheduleViewMode.DAY -> selectedDate.minusDays(1)
                                ScheduleViewMode.MONTH -> selectedDate.minusMonths(1)
                                ScheduleViewMode.WEEK -> selectedDate.minusWeeks(1)
                            }
                        }) {
                            Icon(Icons.Default.ChevronLeft, null)
                        }
                        IconButton(onClick = {
                            selectedDate = when (viewMode) {
                                ScheduleViewMode.DAY -> selectedDate.plusDays(1)
                                ScheduleViewMode.MONTH -> selectedDate.plusMonths(1)
                                ScheduleViewMode.WEEK -> selectedDate.plusWeeks(1)
                            }
                        }) {
                            Icon(Icons.Default.ChevronRight, null)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { onAddLesson(selectedDate.dayOfWeek.value, null) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, stringResource(R.string.add_lesson))
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues).fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
                when (viewMode) {
                    ScheduleViewMode.DAY -> DayTimelineView(selectedDate, lessons, viewModel, onEditLesson, onDateSelected = { selectedDate = it })
                    ScheduleViewMode.WEEK -> WeekListView(selectedDate, lessons, viewModel, onEditLesson)
                    ScheduleViewMode.MONTH -> MonthGridView(selectedDate, lessons, viewModel)
                }
            }
        }
    }
}

@Composable
fun DayTimelineView(
    date: LocalDate,
    lessons: List<Lesson>,
    viewModel: GradeViewModel,
    onEditLesson: (String) -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val dayOfWeek = date.dayOfWeek.value
    val dayLessons = lessons.filter { it.dayOfWeek == dayOfWeek }.sortedBy { it.startTime }

    Column(modifier = Modifier.fillMaxSize()) {
        DaySelector(selectedDate = date, onDateSelected = onDateSelected)
        
        if (dayLessons.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.EventBusy, 
                        null, 
                        modifier = Modifier.size(64.dp), 
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(R.string.no_subjects_schedule), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(dayLessons) { lesson ->
                    TimelineLessonItem(lesson, viewModel, onEditLesson)
                }
            }
        }
    }
}

@Composable
fun DaySelector(selectedDate: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    val startOfWeek = selectedDate.minusDays((selectedDate.dayOfWeek.value - 1).toLong())
    
    Surface(tonalElevation = 2.dp, shadowElevation = 1.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            for (i in 0..6) {
                val date = startOfWeek.plusDays(i.toLong())
                val isSelected = date == selectedDate
                val isToday = date == LocalDate.now()
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onDateSelected(date) }
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun WeekListView(
    selectedDate: LocalDate,
    lessons: List<Lesson>,
    viewModel: GradeViewModel,
    onEditLesson: (String) -> Unit
) {
    val startOfWeek = selectedDate.minusDays((selectedDate.dayOfWeek.value - 1).toLong())
    val daysOfWeek = (0..6).map { startOfWeek.plusDays(it.toLong()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items(daysOfWeek) { date ->
            val dayLessons = lessons.filter { it.dayOfWeek == date.dayOfWeek.value }.sortedBy { it.startTime }
            
            if (dayLessons.isNotEmpty() || date.dayOfWeek.value <= 5) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = if (date == LocalDate.now()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    date.dayOfMonth.toString(),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (date == LocalDate.now()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = date.format(DateTimeFormatter.ofPattern("EEEE", Locale.getDefault())),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (date == LocalDate.now()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (dayLessons.isEmpty()) {
                        Text(
                            stringResource(R.string.no_subjects_schedule),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 44.dp)
                        )
                    } else {
                        Column(
                            modifier = Modifier.padding(start = 44.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            dayLessons.forEach { lesson ->
                                CompactLessonItem(lesson, viewModel, onEditLesson)
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun TimelineLessonItem(lesson: Lesson, viewModel: GradeViewModel, onEdit: (String) -> Unit) {
    val subject = viewModel.getSubjectById(lesson.subjectId)
    val defaultAlpha by viewModel.defaultSubjectAlpha.collectAsState()
    
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(modifier = Modifier.width(60.dp).padding(top = 4.dp), horizontalAlignment = Alignment.End) {
            Text(lesson.startTime, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(lesson.endTime, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Card(
            onClick = { onEdit(lesson.id) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = subject?.color?.copy(alpha = 0.15f * defaultAlpha)?.blendOver(MaterialTheme.colorScheme.surface) ?: MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.width(4.dp).height(40.dp).clip(CircleShape).background(subject?.color ?: Color.Gray)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        subject?.name ?: "Môn học không xác định",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (lesson.room.isNotEmpty() || lesson.teacher.isNotEmpty()) {
                        Text(
                            text = listOfNotNull(lesson.room.ifEmpty { null }, lesson.teacher.ifEmpty { null }).joinToString(" • "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun CompactLessonItem(lesson: Lesson, viewModel: GradeViewModel, onEdit: (String) -> Unit) {
    val subject = viewModel.getSubjectById(lesson.subjectId)
    val defaultAlpha by viewModel.defaultSubjectAlpha.collectAsState()

    Surface(
        onClick = { onEdit(lesson.id) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = subject?.color?.copy(alpha = 0.1f * defaultAlpha)?.blendOver(MaterialTheme.colorScheme.surface) ?: MaterialTheme.colorScheme.surfaceVariant,
        border = if (subject != null) null else border(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${lesson.startTime} - ${lesson.endTime}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(90.dp)
            )
            
            VerticalDivider(modifier = Modifier.height(16.dp).padding(horizontal = 8.dp), color = subject?.color ?: Color.Gray)
            
            Text(
                subject?.name ?: "Unknown",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            if (lesson.room.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        lesson.room,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MonthGridView(
    date: LocalDate,
    lessons: List<Lesson>,
    viewModel: GradeViewModel
) {
    val firstDayOfMonth = date.withDayOfMonth(1)
    val daysInMonth = date.lengthOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value
    
    val days = (1 until firstDayOfWeek).map { null } + (1..daysInMonth).map { date.withDayOfMonth(it) }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(days) { day ->
                if (day == null) {
                    Box(modifier = Modifier.aspectRatio(1f))
                } else {
                    val dayLessons = lessons.filter { it.dayOfWeek == day.dayOfWeek.value }
                    val isToday = day == LocalDate.now()
                    
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isToday) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.aspectRatio(0.8f)
                    ) {
                        Column(modifier = Modifier.padding(4.dp)) {
                            Text(
                                text = day.dayOfMonth.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            dayLessons.take(3).forEach { lesson ->
                                val subject = viewModel.getSubjectById(lesson.subjectId)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(3.dp)
                                        .clip(CircleShape)
                                        .background(subject?.color ?: Color.Gray)
                                        .padding(vertical = 1.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                            if (dayLessons.size > 3) {
                                Text("...", fontSize = 8.sp, lineHeight = 8.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun border(width: androidx.compose.ui.unit.Dp, color: Color) = 
    androidx.compose.foundation.BorderStroke(width, color)
