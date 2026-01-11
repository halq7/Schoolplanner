package com.future.schoolplanner.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.future.schoolplanner.data.GradeInputMethod
import androidx.compose.ui.res.stringResource
import com.future.schoolplanner.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeSettingsScreen(
    onBack: () -> Unit,
    viewModel: GradeViewModel = viewModel()
) {
    val currentMethod by viewModel.gradeInputMethod.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tab_grades)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, stringResource(R.string.back))
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

            Spacer(modifier = Modifier.height(8.dp))

            GradeInputMethod.entries.forEach { method ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setGradeInputMethod(method) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentMethod == method,
                        onClick = { viewModel.setGradeInputMethod(method) }
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Column {
                        Text(
                            text = when (method) {
                                GradeInputMethod.WHOLE -> stringResource(R.string.absolute_grade)
                                GradeInputMethod.DECIMAL -> stringResource(R.string.decimal_grade)
                                GradeInputMethod.TENDENCY -> stringResource(R.string.tendency_grade)
                                GradeInputMethod.FIFTEEN_POINT -> stringResource(R.string.fifteen_points)
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = when (method) {
                                GradeInputMethod.WHOLE -> "${stringResource(R.string.eg)} 2"
                                GradeInputMethod.DECIMAL -> "${stringResource(R.string.eg)} 1.5"
                                GradeInputMethod.TENDENCY -> "${stringResource(R.string.eg)} 2-"
                                GradeInputMethod.FIFTEEN_POINT -> "${stringResource(R.string.eg)} 13"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}