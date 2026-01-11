package com.future.schoolplanner.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.future.schoolplanner.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreen(
    onBack: () -> Unit,
    onNavigateToThemeSettings: () -> Unit,
    onNavigateToPrivacySettings: () -> Unit,
    onNavigateToAboutScreen: () -> Unit,
    onNavigateToLanguageSettings: () -> Unit,
    viewModel: GradeViewModel = viewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_settings)) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {


            item {
                SettingsCategory(
                    title = stringResource(R.string.language),
                    description = stringResource(R.string.language),
                    onClick = onNavigateToLanguageSettings
                )
            }

            item {
                SettingsCategory(
                    title = stringResource(R.string.appearance),
                    description = stringResource(R.string.appearance_description),
                    onClick = onNavigateToThemeSettings
                )
            }

            item {
                SettingsCategory(
                    title = stringResource(R.string.privacy),
                    description = stringResource(R.string.privacy_description),
                    onClick = onNavigateToPrivacySettings
                )
            }

            item {
                SettingsCategory(
                    title = stringResource(R.string.about_app),
                    description = stringResource(R.string.about_app_description),
                    onClick = onNavigateToAboutScreen
                )
            }
        }
    }
}
