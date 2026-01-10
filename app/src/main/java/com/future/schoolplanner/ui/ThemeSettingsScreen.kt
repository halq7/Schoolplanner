package com.future.schoolplanner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    onBack: () -> Unit,
    viewModel: GradeViewModel = viewModel()
) {
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val useDynamicColors by viewModel.useDynamicColors.collectAsState()
    val useAmoledTheme by viewModel.useAmoledTheme.collectAsState()
    val customAccentColor by viewModel.customAccentColor.collectAsState()
    var showCustomColorDialog by remember { mutableStateOf(false) }
    var originalColor by remember { mutableStateOf(Color.Transparent) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Theme-Einstellungen") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Zurück")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
            Text(
                text = "Theme-Auswahl",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Wählen Sie zwischen Hell- und Dunkelmodus:",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Theme toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setDarkTheme(!isDarkTheme) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Dunkelmodus",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Aktiviert den Dunkelmodus für die gesamte App",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { viewModel.setDarkTheme(it) }
                )
            }

            // Dynamic colors toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setUseDynamicColors(!useDynamicColors) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Dynamische Farben",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Verwendet die Farben des Systems",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Switch(
                    checked = useDynamicColors,
                    onCheckedChange = { viewModel.setUseDynamicColors(it) }
                )
            }

            // AMOLED theme toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setUseAmoledTheme(!useAmoledTheme) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "AMOLED Modus",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Rein schwarzer Hintergrund für AMOLED-Displays",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Switch(
                    checked = useAmoledTheme,
                    onCheckedChange = { viewModel.setUseAmoledTheme(it) },
                    enabled = isDarkTheme
                )
            }

            // Custom accent color picker - only show when dynamic colors are disabled
            if (!useDynamicColors) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Akzentfarbe",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Wählen Sie eine benutzerdefinierte Akzentfarbe:",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Color picker with predefined colors
                val colorOptions = listOf(
                    Color(0xFF4CAF50), // Green
                    Color(0xFF2196F3), // Blue
                    Color(0xFFFF9800), // Orange
                    Color(0xFFE91E63), // Pink
                    Color(0xFF9C27B0), // Purple
                    Color(0xFF00BCD4), // Cyan
                    Color(0xFF8BC34A), // Light Green
                    Color(0xFFFF5722), // Deep Orange
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    colorOptions.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable { viewModel.setCustomAccentColor(color) }
                                .then(
                                    if (customAccentColor == color) {
                                        Modifier.border(
                                            width = 3.dp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            shape = CircleShape
                                        )
                                    } else {
                                        Modifier
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { originalColor = customAccentColor; showCustomColorDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Eigene Farbe wählen")
                    }

                    Button(
                        onClick = { viewModel.setCustomAccentColor(Color(0xFFD0BCFF)) }, // Reset to original purple
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Standard")
                    }
                }
            }

            if (showCustomColorDialog) {
                AlertDialog(
                    onDismissRequest = { showCustomColorDialog = false },
                    title = { Text("Eigene Akzentfarbe wählen") },
                    text = {
                        ColorPicker(
                            initialColor = customAccentColor,
                            onColorSelected = { viewModel.setCustomAccentColor(it) }
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { showCustomColorDialog = false }) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.setCustomAccentColor(originalColor); showCustomColorDialog = false }) {
                            Text("Abbrechen")
                        }
                    }
                )
            }
        }
    }
}
