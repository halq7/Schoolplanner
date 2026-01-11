package com.future.schoolplanner.ui

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.future.schoolplanner.R
import com.future.schoolplanner.data.serialization.AppData
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    onBack: () -> Unit,
    viewModel: GradeViewModel
) {
    val context = LocalContext.current
    var isImporting by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isImporting = true
            importDataFromUri(context, uri, viewModel) {
                isImporting = false
                onBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
            title = { Text(stringResource(R.string.import_data)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, stringResource(R.string.back))
                }
            }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.import_description),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Button(
                onClick = { filePickerLauncher.launch("application/json") },
                enabled = !isImporting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isImporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.importing))
                } else {
                    Text(stringResource(R.string.select_file))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.import_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun importDataFromUri(
    context: Context,
    uri: Uri,
    viewModel: GradeViewModel,
    onComplete: () -> Unit
) {
    try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val jsonString = inputStream.readBytes().toString(Charsets.UTF_8)
            val json = Json {
                prettyPrint = true
                ignoreUnknownKeys = true
                encodeDefaults = true
            }
            val appData = json.decodeFromString<AppData>(jsonString)

            // Load the data into viewModel
            viewModel.loadAppData(appData)

            Toast.makeText(context, context.getString(R.string.import_success), Toast.LENGTH_SHORT).show()
        } ?: run {
            Toast.makeText(context, context.getString(R.string.import_file_error), Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, context.getString(R.string.import_error, e.localizedMessage), Toast.LENGTH_SHORT).show()
    } finally {
        onComplete()
    }
}
