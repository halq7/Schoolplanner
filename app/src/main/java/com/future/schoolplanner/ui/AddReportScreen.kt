package com.future.schoolplanner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.future.schoolplanner.R
import com.future.schoolplanner.data.Report
import com.future.schoolplanner.data.ReportSubject
import com.future.schoolplanner.data.SchoolYear
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReportScreen(
    onBack: () -> Unit,
    onReportAdded: (Report) -> Unit,
    viewModel: GradeViewModel
) {
    val schoolYears = viewModel.schoolYears.collectAsState()
    val subjects = viewModel.subjects.collectAsState()

    var selectedSchoolYear by remember { mutableStateOf<SchoolYear?>(null) }
    var reportName by remember { mutableStateOf("") }
    var reportDate by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var extraSubjects by remember { mutableStateOf<List<ReportSubject>>(emptyList()) }
    var subjectGrades by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    val subjectsForYear = remember(selectedSchoolYear, subjects.value) {
        selectedSchoolYear?.let { year ->
            subjects.value.filter { it.schoolYearId == year.id }
        } ?: emptyList()
    }

    val reportsTitle = stringResource(R.string.reports_title)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.create_report)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            selectedSchoolYear?.let { schoolYear ->
                val reportSubjects = subjectsForYear.map { subject ->
                    val average = viewModel.calculateAverage(subject)
                    val finalGrade = subjectGrades[subject.id] ?: (if (average > 0) viewModel.formatGradeForReport(average) else "")
                    ReportSubject(
                        id = UUID.randomUUID().toString(),
                        name = subject.name,
                        abbreviation = subject.abbreviation,
                        finalGrade = finalGrade,
                        isExtraSubject = false,
                        description = subject.description
                    )
                } + extraSubjects

                                val report = Report(
                                    id = UUID.randomUUID().toString(),
                                    schoolYearId = schoolYear.id,
                                    name = reportName.ifEmpty { "$reportsTitle ${schoolYear.name}" },
                                    reportSubjects = reportSubjects,
                                    date = reportDate
                                )
                                onReportAdded(report)
                            }
                        },
                        enabled = selectedSchoolYear != null
                    ) {
                        Icon(Icons.Default.Check, contentDescription = stringResource(R.string.save))
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
            // School Year Selection
            Text(
                text = stringResource(R.string.select_school_year),
                style = MaterialTheme.typography.titleMedium
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedSchoolYear?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.school_year)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    schoolYears.value.forEach { schoolYear ->
                        DropdownMenuItem(
                            text = { Text(schoolYear.name) },
                            onClick = {
                                selectedSchoolYear = schoolYear
                                expanded = false
                                if (reportName.isEmpty()) {
                                    reportName = "$reportsTitle ${schoolYear.name}"
                                }
                            }
                        )
                    }
                }
            }

            // Report Name
            OutlinedTextField(
                value = reportName,
                onValueChange = { reportName = it },
                label = { Text(stringResource(R.string.report_name)) },
                modifier = Modifier.fillMaxWidth()
            )

            // Report Date
            OutlinedTextField(
                value = reportDate,
                onValueChange = { reportDate = it },
                label = { Text(stringResource(R.string.issue_date_optional)) },
                modifier = Modifier.fillMaxWidth()
            )

            selectedSchoolYear?.let { schoolYear ->
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.subjects_for_year, schoolYear.name),
                    style = MaterialTheme.typography.titleMedium
                )

                if (subjectsForYear.isEmpty()) {
               Text(
                    text = stringResource(R.string.no_subjects_for_year, schoolYear.name),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                } else {
                    subjectsForYear.forEachIndexed { index, subject ->
                        val average = viewModel.calculateAverage(subject)
                        val currentGradeStr = subjectGrades[subject.id] ?: (if (average > 0) viewModel.formatGradeForReport(average) else "")
                        ReportSubjectItem(
                            name = subject.name,
                            abbreviation = subject.abbreviation,
                            gradeStr = currentGradeStr,
                            isExtra = false,
                            onGradeChange = { gradeStr ->
                                subjectGrades = subjectGrades + (subject.id to gradeStr)
                            },
                            onNameChange = { name ->
                                // Regular subjects keep their original name
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.additional_subjects),
                    style = MaterialTheme.typography.titleMedium
                )

                extraSubjects.forEachIndexed { index, extraSubject ->
                    ReportSubjectItem(
                        name = extraSubject.name,
                        abbreviation = extraSubject.abbreviation ?: "",
                        gradeStr = extraSubject.finalGrade ?: "",
                        isExtra = true,
                        onGradeChange = { gradeStr ->
                            extraSubjects = extraSubjects.toMutableList().apply {
                                this[index] = extraSubject.copy(finalGrade = gradeStr.ifEmpty { null })
                            }
                        },
                        onNameChange = { name ->
                            extraSubjects = extraSubjects.toMutableList().apply {
                                this[index] = extraSubject.copy(name = name)
                            }
                        },
                        onRemove = {
                            extraSubjects = extraSubjects.toMutableList().apply {
                                removeAt(index)
                            }
                        }
                    )
                }

                Button(
                    onClick = {
                        extraSubjects = extraSubjects + ReportSubject(
                            id = UUID.randomUUID().toString(),
                            name = "",
                            abbreviation = "",
                            finalGrade = null,
                            isExtraSubject = true,
                            description = ""
                        )
                    },
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.add_additional_subject))
                }
            }
        }
    }
}

// Version for regular subjects with string grades (tendencies)
@Composable
fun ReportSubjectItem(
    name: String,
    abbreviation: String,
    gradeStr: String,
    isExtra: Boolean,
    onGradeChange: (String) -> Unit = {},
    onNameChange: (String) -> Unit = {},
    onRemove: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isExtra) {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.subject_name_label)) },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = abbreviation,
                onValueChange = { /* abbreviation */ },
                label = { Text(stringResource(R.string.abbreviation)) },
                modifier = Modifier.width(80.dp)
            )
            OutlinedTextField(
                value = gradeStr,
                onValueChange = onGradeChange,
                label = { Text(stringResource(R.string.grade_example)) },
                modifier = Modifier.width(100.dp)
            )
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
            }
        } else {
            // Regular subjects with tendency input
            Text(
                text = name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )
            OutlinedTextField(
                value = gradeStr,
                onValueChange = onGradeChange,
                label = { Text(stringResource(R.string.grade_example)) },
                modifier = Modifier.width(100.dp)
            )
        }
    }
}

// Version for extra subjects with double grades (legacy support)
@Composable
fun ReportSubjectItem(
    name: String,
    abbreviation: String,
    grade: Double,
    isExtra: Boolean,
    onGradeChange: (Double) -> Unit = {},
    onNameChange: (String) -> Unit = {},
    onRemove: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isExtra) {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Fachname") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = abbreviation,
                onValueChange = { /* abbreviation */ },
                label = { Text("Abk.") },
                modifier = Modifier.width(80.dp)
            )
            OutlinedTextField(
                value = if (grade > 0) "%.1f".format(grade) else "",
                onValueChange = { value ->
                    value.toDoubleOrNull()?.let { onGradeChange(it) }
                },
                label = { Text(stringResource(R.string.grade)) },
                modifier = Modifier.width(80.dp)
            )
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
            }
        } else {
            // Regular subjects - this should not be called for regular subjects anymore
            Text(
                text = name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = if (grade > 0) "%.1f".format(grade) else "-",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.width(80.dp)
            )
        }
    }
}
