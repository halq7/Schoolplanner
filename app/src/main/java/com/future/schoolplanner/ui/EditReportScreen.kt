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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.future.schoolplanner.data.Report
import com.future.schoolplanner.data.ReportSubject
import com.future.schoolplanner.data.SchoolYear
import java.util.UUID
import androidx.compose.ui.res.stringResource
import com.future.schoolplanner.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditReportScreen(
    report: Report,
    onBack: () -> Unit,
    onReportUpdated: (Report) -> Unit,
    viewModel: GradeViewModel
) {
    val schoolYears = viewModel.schoolYears.collectAsState()

    var reportName by remember { mutableStateOf(report.name) }
    var reportDate by remember { mutableStateOf(report.date) }
    var reportSubjects by remember { mutableStateOf(report.reportSubjects) }

    val schoolYear = schoolYears.value.find { it.id == report.schoolYearId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_report)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val updatedReport = report.copy(
                                name = reportName,
                                reportSubjects = reportSubjects,
                                date = reportDate
                            )
                            onReportUpdated(updatedReport)
                        }
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
            // Report Info
            Text(
                text = stringResource(R.string.report_information),
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = reportName,
                onValueChange = { reportName = it },
                label = { Text(stringResource(R.string.report_name)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = reportDate,
                onValueChange = { reportDate = it },
                label = { Text(stringResource(R.string.issue_date_optional)) },
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "${stringResource(R.string.school_year)} ${schoolYear?.name ?: stringResource(R.string.unknown)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.subjects),
                style = MaterialTheme.typography.titleMedium
            )

            reportSubjects.forEachIndexed { index, subject ->
                EditableReportSubjectItem(
                    subject = subject,
                    onSubjectChange = { updatedSubject ->
                        reportSubjects = reportSubjects.toMutableList().apply {
                            this[index] = updatedSubject
                        }
                    },
                    onRemove = {
                        reportSubjects = reportSubjects.toMutableList().apply {
                            removeAt(index)
                        }
                    },
                    viewModel = viewModel
                )
            }

            Button(
                onClick = {
                    reportSubjects = reportSubjects + ReportSubject(
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

@Composable
fun EditableReportSubjectItem(
    subject: ReportSubject,
    onSubjectChange: (ReportSubject) -> Unit,
    onRemove: () -> Unit,
    viewModel: GradeViewModel
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (subject.isExtraSubject) {
            OutlinedTextField(
                value = subject.name,
                onValueChange = { name ->
                    onSubjectChange(subject.copy(name = name))
                },
                label = { Text(stringResource(R.string.subject_name)) },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = subject.abbreviation,
                onValueChange = { abbr ->
                    onSubjectChange(subject.copy(abbreviation = abbr))
                },
                label = { Text(stringResource(R.string.abbreviation)) },
                modifier = Modifier.width(80.dp)
            )
            OutlinedTextField(
                value = subject.finalGrade ?: "",
                onValueChange = { gradeStr ->
                    onSubjectChange(subject.copy(finalGrade = gradeStr.ifEmpty { null }))
                },
                label = { Text(stringResource(R.string.grade)) },
                modifier = Modifier.width(100.dp)
            )
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
            }
        } else {
            // Regular subjects - editable grade field
            Text(
                text = subject.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )
            OutlinedTextField(
                value = subject.finalGrade ?: "",
                onValueChange = { gradeStr ->
                    onSubjectChange(subject.copy(finalGrade = gradeStr.ifEmpty { null }))
                },
                label = { Text(stringResource(R.string.grade)) },
                modifier = Modifier.width(100.dp)
            )
        }
    }
}
