package com.future.schoolplanner.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.future.schoolplanner.data.Subject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolPlannerApp() {
    val navController = rememberNavController()
    val viewModel: GradeViewModel = viewModel()
    val tasksTabEnabled by viewModel.tasksTabEnabled.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val baseTabs = listOf(
        TabItem("Noten", Icons.Default.List),
        TabItem("Stundenplan", Icons.Default.DateRange)
    )

    val tasksTab = if (tasksTabEnabled) listOf(TabItem("Aufgaben", Icons.Default.CheckCircle)) else emptyList()

    val moreTab = listOf(TabItem("Mehr", Icons.Default.Menu))

    val tabs = baseTabs + tasksTab + moreTab

    val startRoutes = mutableListOf("subjectList", "schedule")
    if (tasksTabEnabled) startRoutes.add("tasks")
    startRoutes.add("moreMenu")

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) },
                        selected = selectedTabIndex == index,
                        onClick = {
                            val targetStart = startRoutes[index]
                            if (selectedTabIndex == index) {
                                // reset to tab start
                                navController.navigate(targetStart) {
                                    popUpTo(startRoutes[selectedTabIndex]) { inclusive = true }
                                    launchSingleTop = true
                                }
                            } else {
                                // switch tab
                                navController.navigate(targetStart) {
                                    popUpTo(startRoutes[selectedTabIndex]) { inclusive = true }
                                    launchSingleTop = true
                                }
                                selectedTabIndex = index
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "subjectList",
            modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            GradesTab(this, navController, viewModel, paddingValues)
            ScheduleTab(this, navController, viewModel, paddingValues)
            if (tasksTabEnabled) {
                TasksTab(this, navController, viewModel, paddingValues, onAddTask = { navController.navigate("addTask") })
            }
            MoreTab(this, navController, viewModel, paddingValues)
        }

        LaunchedEffect(navController.currentBackStackEntryAsState().value) {
            val route = navController.currentDestination?.route
            selectedTabIndex = when {
                route?.startsWith("subjectList") == true || route?.startsWith("gradeDetail") == true || route?.startsWith("addSubject") == true || route?.startsWith("settings") == true || route?.startsWith("subjectSettings") == true -> 0
                route?.startsWith("schedule") == true || route?.startsWith("addLesson") == true || route?.startsWith("editLesson") == true -> 1
                tasksTabEnabled && (route?.startsWith("tasks") == true || route?.startsWith("addTask") == true || route?.startsWith("editTask") == true) -> 2
                else -> if (tasksTabEnabled) 3 else 2
            }
        }
    }
}



data class TabItem(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

fun GradesTab(builder: NavGraphBuilder, navController: NavHostController, viewModel: GradeViewModel, paddingValues: androidx.compose.foundation.layout.PaddingValues) {
    builder.composable("subjectList") {
        SubjectListScreen(
            onSubjectSelected = { subject ->
                viewModel.selectSubject(subject)
                navController.navigate("gradeDetail/${subject.id}")
            },
            onAddSubject = {
                navController.navigate("addSubject")
            },
            onSettings = {
                navController.navigate("settings")
            },
            onSubjectSettings = { subject ->
                navController.navigate("subjectSettings/${subject.id}")
            },
            viewModel = viewModel
        )
    }

    builder.composable("gradeDetail/{subjectId}") { backStackEntry ->
        val subjectId = backStackEntry.arguments?.getString("subjectId") ?: ""
        val subjects = viewModel.subjects.collectAsState()
        val subject = subjects.value.find { it.id == subjectId }

        val selectedSubject = viewModel.selectedSubject.collectAsState()
        val finalSubject = subject ?: selectedSubject.value

        if (finalSubject != null) {
            GradeDetailScreen(
                subject = finalSubject,
                onBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        } else {
            navController.navigate("subjectList") {
                popUpTo("subjectList") { inclusive = true }
            }
        }
    }

    builder.composable("addSubject") {
        AddSubjectScreen(
            onBack = { navController.popBackStack() },
            onSubjectAdded = { subject ->
                viewModel.addSubject(subject)
                navController.popBackStack()
            },
            viewModel = viewModel
        )
    }

    builder.composable("settings") {
        GradeSettingsScreen(
            onBack = { navController.popBackStack() },
            viewModel = viewModel
        )
    }

    builder.composable("subjectSettings/{subjectId}") { backStackEntry ->
        val subjectId = backStackEntry.arguments?.getString("subjectId") ?: ""
        val subjects = viewModel.subjects.collectAsState()
        val subject = subjects.value.find { it.id == subjectId }

        if (subject != null) {
            SubjectSettingsScreen(
                subject = subject,
                onBack = { navController.popBackStack() },
                onSubjectUpdated = { updatedSubject ->
                    viewModel.updateSubject(updatedSubject)
                    navController.popBackStack()
                }
            )
        } else {
            navController.navigate("subjectList") {
                popUpTo("subjectList") { inclusive = true }
            }
        }
    }
}

fun ScheduleTab(builder: NavGraphBuilder, navController: NavHostController, viewModel: GradeViewModel, paddingValues: androidx.compose.foundation.layout.PaddingValues) {
    builder.composable("schedule") {
        ScheduleScreen(
            onAddLesson = { day, hour, weekType ->
                if (day == null || hour == null || weekType == null) {
                    navController.navigate("addLesson")
                } else {
                    val wtStr = if (weekType == com.future.schoolplanner.data.WeekType.B) "B" else "A"
                    navController.navigate("addLesson/$day/$hour/$wtStr")
                }
            },
            onEditLesson = { lessonId ->
                navController.navigate("editLesson/$lessonId")
            },
            viewModel = viewModel
        )
    }

    builder.composable("addLesson") {
        AddLessonScreen(
            onBack = { navController.popBackStack() },
            onLessonAdded = { lesson ->
                if (viewModel.lessons.value.any { it.id == lesson.id }) {
                    viewModel.updateLesson(lesson)
                } else {
                    viewModel.addLesson(lesson)
                }
                navController.popBackStack()
            },
            viewModel = viewModel
        )
    }

    builder.composable("addLesson/{day}/{hour}/{weekType}") { backStackEntry ->
        val day = backStackEntry.arguments?.getString("day")?.toIntOrNull() ?: 1
        val hour = backStackEntry.arguments?.getString("hour")?.toIntOrNull() ?: 1
        val weekTypeStr = backStackEntry.arguments?.getString("weekType") ?: "A"
        val weekType = if (weekTypeStr == "B") com.future.schoolplanner.data.WeekType.B else com.future.schoolplanner.data.WeekType.A

        AddLessonScreen(
            onBack = { navController.popBackStack() },
            onLessonAdded = { lesson ->
                viewModel.addLesson(lesson)
                navController.popBackStack()
            },
            viewModel = viewModel,
            initialDay = day,
            initialHour = hour,
            initialWeekType = weekType,
            fixedDay = true,
            fixedHour = true,
            fixedWeekType = true
        )
    }

    builder.composable("editLesson/{lessonId}") { backStackEntry ->
        val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
        val lesson = viewModel.lessons.value.find { it.id == lessonId }

        if (lesson != null) {
            AddLessonScreen(
                onBack = { navController.popBackStack() },
                onLessonAdded = { updatedLesson ->
                    viewModel.updateLesson(updatedLesson)
                    navController.popBackStack()
                },
                viewModel = viewModel,
                lessonToEdit = lesson
            )
        } else {
            navController.navigate("schedule") {
                popUpTo("schedule") { inclusive = true }
            }
        }
    }
}

fun TasksTab(builder: NavGraphBuilder, navController: NavHostController, viewModel: GradeViewModel, paddingValues: androidx.compose.foundation.layout.PaddingValues, onAddTask: () -> Unit) {
    builder.composable("tasks") {
        TasksScreen(
            onBack = { navController.popBackStack() },
            onAddTask = onAddTask,
            onEditTask = { taskId ->
                navController.navigate("editTask/$taskId")
            },
            viewModel = viewModel
        )
    }

    builder.composable("addTask") {
        AddTaskScreen(
            onBack = { navController.popBackStack() },
            onTaskAdded = { task ->
                viewModel.addTask(task)
                navController.popBackStack()
            },
            viewModel = viewModel
        )
    }

    builder.composable("editTask/{taskId}") { backStackEntry ->
        val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
        val task = viewModel.getTaskById(taskId)

        if (task != null) {
            AddTaskScreen(
                onBack = { navController.popBackStack() },
                onTaskAdded = { updatedTask ->
                    viewModel.updateTask(updatedTask)
                    navController.popBackStack()
                },
                viewModel = viewModel,
                taskToEdit = task
            )
        } else {
            navController.navigate("tasks") {
                popUpTo("tasks") { inclusive = true }
            }
        }
    }
}

fun MoreTab(builder: NavGraphBuilder, navController: NavHostController, viewModel: GradeViewModel, paddingValues: androidx.compose.foundation.layout.PaddingValues) {
    builder.composable("moreMenu") {
        MoreMenuScreen(
            onNavigateToSchoolYears = { navController.navigate("schoolYears") },
            onNavigateToReports = { navController.navigate("reports") },
            onNavigateToSettings = { navController.navigate("mainSettings") },
            onNavigateToExtensions = { navController.navigate("extensions") }
        )
    }

    builder.composable("schoolYears") {
        SchoolYearsScreen(
            onBack = { navController.popBackStack() },
            onAddSchoolYear = { navController.navigate("addSchoolYear") },
            onEditSchoolYear = { schoolYearId ->
                navController.navigate("editSchoolYear/$schoolYearId")
            },
            viewModel = viewModel
        )
    }

    builder.composable("addSchoolYear") {
        AddSchoolYearScreen(
            onBack = { navController.popBackStack() },
            onSchoolYearAdded = { schoolYear ->
                viewModel.addSchoolYear(schoolYear)
                navController.popBackStack()
            }
        )
    }

    builder.composable("editSchoolYear/{schoolYearId}") { backStackEntry ->
        val schoolYearId = backStackEntry.arguments?.getString("schoolYearId") ?: ""
        val schoolYear = viewModel.schoolYears.value.find { it.id == schoolYearId }

        if (schoolYear != null) {
            EditSchoolYearScreen(
                schoolYear = schoolYear,
                onBack = { navController.popBackStack() },
                onSchoolYearUpdated = { updatedSchoolYear ->
                    viewModel.updateSchoolYear(updatedSchoolYear)
                    navController.popBackStack()
                }
            )
        } else {
            navController.navigate("schoolYears") {
                popUpTo("schoolYears") { inclusive = true }
            }
        }
    }

    builder.composable("reports") {
        ReportsScreen(
            onBack = { navController.popBackStack() },
            onAddReport = { navController.navigate("addReport") },
            onViewReport = { reportId ->
                navController.navigate("viewReport/$reportId")
            },
            onEditReport = { reportId ->
                navController.navigate("editReport/$reportId")
            },
            viewModel = viewModel
        )
    }

    builder.composable("viewReport/{reportId}") { backStackEntry ->
        val reportId = backStackEntry.arguments?.getString("reportId") ?: ""
        val report = viewModel.getReportById(reportId)

        if (report != null) {
            ViewReportScreen(
                report = report,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate("editReport/$reportId") },
                viewModel = viewModel
            )
        } else {
            navController.navigate("reports") {
                popUpTo("reports") { inclusive = true }
            }
        }
    }

    builder.composable("addReport") {
        AddReportScreen(
            onBack = { navController.popBackStack() },
            onReportAdded = { report ->
                viewModel.addReport(report)
                navController.popBackStack()
            },
            viewModel = viewModel
        )
    }

    builder.composable("editReport/{reportId}") { backStackEntry ->
        val reportId = backStackEntry.arguments?.getString("reportId") ?: ""
        val report = viewModel.getReportById(reportId)

        if (report != null) {
            EditReportScreen(
                report = report,
                onBack = { navController.popBackStack() },
                onReportUpdated = { updatedReport ->
                    viewModel.updateReport(updatedReport)
                    navController.popBackStack()
                },
                viewModel = viewModel
            )
        } else {
            navController.navigate("reports") {
                popUpTo("reports") { inclusive = true }
            }
        }
    }

    builder.composable("mainSettings") {
        MainSettingsScreen(
            onBack = { navController.popBackStack() },
            onNavigateToGradeSettings = {
                navController.navigate("gradeSettings")
            },
            onNavigateToDisplaySettings = {
                navController.navigate("displaySettings")
            },
            onNavigateToAppSettings = {
                navController.navigate("appSettings")
            },
            viewModel = viewModel
        )
    }

    builder.composable("gradeSettings") {
        GradeSettingsScreen(
            onBack = { navController.popBackStack() },
            viewModel = viewModel
        )
    }

    builder.composable("displaySettings") {
        DisplaySettingsScreen(
            onBack = { navController.popBackStack() },
            viewModel = viewModel
        )
    }

    builder.composable("appSettings") {
        AppSettingsScreen(
            onBack = { navController.popBackStack() },
            onNavigateToThemeSettings = {
                navController.navigate("themeSettings")
            },
            onNavigateToPrivacySettings = {
                navController.navigate("privacySettings")
            },
            onNavigateToAboutScreen = {
                navController.navigate("aboutScreen")
            },
            viewModel = viewModel
        )
    }

    builder.composable("themeSettings") {
        ThemeSettingsScreen(
            onBack = { navController.popBackStack() },
            viewModel = viewModel
        )
    }

    builder.composable("privacySettings") {
        PrivacyScreen(
            onBack = { navController.popBackStack() }
        )
    }

    builder.composable("aboutScreen") {
        AboutScreen(
            onBack = { navController.popBackStack() },
            onNavigateToDeveloperOptions = {
                navController.navigate("developerOptions")
            }
        )
    }

    builder.composable("developerOptions") {
        DeveloperOptionsScreen(
            onBack = { navController.popBackStack() },
            viewModel = viewModel
        )
    }

    builder.composable("extensions") {
        ExtensionsScreen(
            onBack = { navController.popBackStack() },
            viewModel = viewModel
        )
    }
}
