package com.future.schoolplanner.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.content.Context
import android.content.SharedPreferences
import android.app.Activity
import com.future.schoolplanner.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSettingsScreen(
    onBack: () -> Unit,
    viewModel: GradeViewModel = viewModel()
) {
    val context = LocalContext.current
    var selectedLanguage by remember { mutableStateOf("vi") } // Default to Vietnamese

    // Load current language preference
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        selectedLanguage = prefs.getString("language", "vi") ?: "vi"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.language)) },
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedLanguage == "en",
                        onClick = {
                            selectedLanguage = "en"
                            saveLanguagePreference(context, "en")
                            changeAppLanguage(context, "en")
                        }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(stringResource(R.string.english))
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedLanguage == "vi",
                        onClick = {
                            selectedLanguage = "vi"
                            saveLanguagePreference(context, "vi")
                            changeAppLanguage(context, "vi")
                        }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(stringResource(R.string.vietnamese))
                }
            }
        }
    }
}

private fun saveLanguagePreference(context: Context, language: String) {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    prefs.edit().putString("language", language).apply()
}

private fun changeAppLanguage(context: android.content.Context, language: String) {
    val locale = java.util.Locale(language)
    java.util.Locale.setDefault(locale)
    val config = android.content.res.Configuration(context.resources.configuration)
    config.setLocale(locale)
    context.resources.updateConfiguration(config, context.resources.displayMetrics)

    // Restart the activity to apply changes
    val intent = (context as? android.app.Activity)?.intent
    intent?.let {
        (context as android.app.Activity).finish()
        context.startActivity(it)
    }
}
