package com.future.schoolplanner.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.future.schoolplanner.data.Subject
import com.future.schoolplanner.ui.theme.getContrastingTextColor
import java.util.UUID
import kotlin.math.max
import kotlin.math.min

// HSV Color conversion functions
data class HSV(val hue: Float, val saturation: Float, val value: Float)

fun ComposeColor.toHSV(): HSV {
    val r = red
    val g = green
    val b = blue

    val max = max(r, max(g, b))
    val min = min(r, min(g, b))
    val delta = max - min

    val h = when {
        delta == 0f -> 0f
        max == r -> 60 * (((g - b) / delta) % 6)
        max == g -> 60 * (((b - r) / delta) + 2)
        else -> 60 * (((r - g) / delta) + 4)
    }

    val s = if (max == 0f) 0f else delta / max
    val v = max

    return HSV(h.coerceIn(0f, 360f), s, v)
}

fun HSV.toColor(): ComposeColor {
    val c = value * saturation
    val x = c * (1 - kotlin.math.abs((hue / 60) % 2 - 1))
    val m = value - c

    val (r, g, b) = when {
        hue < 60 -> Triple(c, x, 0f)
        hue < 120 -> Triple(x, c, 0f)
        hue < 180 -> Triple(0f, c, x)
        hue < 240 -> Triple(0f, x, c)
        hue < 300 -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }

    return ComposeColor((r + m), (g + m), (b + m))
}

@Composable
fun ColorPicker(
    initialColor: ComposeColor,
    onColorSelected: (ComposeColor) -> Unit
) {
    var hsv by remember { mutableStateOf(initialColor.toHSV()) }
    var hexText by remember { mutableStateOf(initialColor.toHex()) }

    LaunchedEffect(initialColor) {
        hsv = initialColor.toHSV()
        hexText = initialColor.toHex()
    }

    LaunchedEffect(hsv) {
        onColorSelected(hsv.toColor())
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Color preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(
                    color = hsv.toColor(),
                    shape = MaterialTheme.shapes.medium
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = hexText,
                color = hsv.toColor().getContrastingTextColor(),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        // Hex input
        OutlinedTextField(
            value = hexText,
            onValueChange = { input ->
                hexText = input.uppercase().take(7)
                if (input.length == 7 && input.startsWith("#")) {
                    try {
                        val color = ComposeColor(("FF" + input.substring(1)).toLong(16))
                        hsv = color.toHSV()
                    } catch (e: Exception) {
                        // Invalid hex, ignore
                    }
                }
            },
            label = { Text("Hex-Farbe") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Hue slider
        Text("Farbton", style = MaterialTheme.typography.bodyMedium)
        HueSlider(
            hue = hsv.hue,
            onHueChange = { newHue ->
                hsv = hsv.copy(hue = newHue)
                hexText = hsv.toColor().toHex()
            },
            modifier = Modifier.fillMaxWidth().height(40.dp)
        )

        // Saturation and Value picker
        Text("Sättigung & Helligkeit", style = MaterialTheme.typography.bodyMedium)
        SaturationValuePicker(
            hsv = hsv,
            onHSVChange = { newHsv ->
                hsv = newHsv
                hexText = hsv.toColor().toHex()
            },
            modifier = Modifier.fillMaxWidth().height(200.dp)
        )
    }
}

@Composable
fun HueSlider(
    hue: Float,
    onHueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var position by remember { mutableStateOf(hue / 360f) }

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val brush = Brush.horizontalGradient(
                colors = (0..360 step 6).map { h ->
                    HSV(h.toFloat(), 1f, 1f).toColor()
                }
            )
            drawRect(brush)
        }

        Canvas(modifier = Modifier.matchParentSize().pointerInput(Unit) {
            detectDragGestures { change, _ ->
                val newPosition = (change.position.x / size.width).coerceIn(0f, 1f)
                position = newPosition
                onHueChange(newPosition * 360f)
            }
        }) {
            val x = position * size.width
            drawCircle(
                color = ComposeColor.White,
                radius = 12f,
                center = Offset(x, size.height / 2)
            )
            drawCircle(
                color = ComposeColor.Black,
                radius = 10f,
                center = Offset(x, size.height / 2)
            )
        }
    }
}

@Composable
fun SaturationValuePicker(
    hsv: HSV,
    onHSVChange: (HSV) -> Unit,
    modifier: Modifier = Modifier
) {
    var saturationPos by remember { mutableStateOf(hsv.saturation) }
    var valuePos by remember { mutableStateOf(1f - hsv.value) }

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.matchParentSize()) {
            // Saturation gradient (left to right)
            val saturationBrush = Brush.horizontalGradient(
                colors = listOf(
                    HSV(hsv.hue, 0f, hsv.value).toColor(),
                    HSV(hsv.hue, 1f, hsv.value).toColor()
                )
            )
            drawRect(saturationBrush)

            // Value gradient (top to bottom)
            val valueBrush = Brush.verticalGradient(
                colors = listOf(
                    ComposeColor.Transparent,
                    ComposeColor.Black
                )
            )
            drawRect(valueBrush)
        }

        Canvas(modifier = Modifier.matchParentSize().pointerInput(Unit) {
            detectDragGestures { change, _ ->
                val newSaturation = (change.position.x / size.width).coerceIn(0f, 1f)
                val newValue = 1f - (change.position.y / size.height).coerceIn(0f, 1f)
                saturationPos = newSaturation
                valuePos = 1f - newValue
                onHSVChange(HSV(hsv.hue, newSaturation, newValue))
            }
        }) {
            val x = saturationPos * size.width
            val y = valuePos * size.height
            drawCircle(
                color = ComposeColor.White,
                radius = 12f,
                center = Offset(x, y)
            )
            drawCircle(
                color = ComposeColor.Black,
                radius = 10f,
                center = Offset(x, y)
            )
        }
    }
}

fun ComposeColor.toHex(): String {
    val r = (red * 255).toInt().toString(16).padStart(2, '0')
    val g = (green * 255).toInt().toString(16).padStart(2, '0')
    val b = (blue * 255).toInt().toString(16).padStart(2, '0')
    return "#$r$g$b".uppercase()
}

// Extension to convert Color to ARGB int for storage if needed
fun ComposeColor.toArgbInt(): Int = ((alpha * 255).toInt() shl 24) or ((red * 255).toInt() shl 16) or ((green * 255).toInt() shl 8) or (blue * 255).toInt()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubjectScreen(
    onBack: () -> Unit,
    onSubjectAdded: (Subject) -> Unit
) {
    var subjectName by remember { mutableStateOf("") }
    var abbreviation by remember { mutableStateOf("") }
    var teacher by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(ComposeColor(0xFF4CAF50)) } // Default green
    var originalColor by remember { mutableStateOf(ComposeColor.Transparent) }
    var showNameError by remember { mutableStateOf(false) }
    var showAbbreviationError by remember { mutableStateOf(false) }
    var showCustomColorDialog by remember { mutableStateOf(false) }

    // Predefined colors for subjects
    val subjectColors = listOf(
        ComposeColor(0xFF4CAF50), // Green
        ComposeColor(0xFF2196F3), // Blue
        ComposeColor(0xFFFF9800), // Orange
        ComposeColor(0xFF9C27B0), // Purple
        ComposeColor(0xFFE91E63), // Pink
        ComposeColor(0xFF3F51B5), // Indigo
        ComposeColor(0xFF00BCD4), // Cyan
        ComposeColor(0xFF8BC34A)  // Light Green
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fach hinzufügen") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Zurück")
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
            Text(
                text = "Neues Fach erstellen",
                style = MaterialTheme.typography.headlineMedium
            )

            OutlinedTextField(
                value = subjectName,
                onValueChange = {
                    subjectName = it
                    showNameError = false
                },
                label = { Text("Fach") },
                isError = showNameError,
                supportingText = {
                    if (showNameError) {
                        Text("Bitte gib einen Fachnamen ein")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = abbreviation,
                onValueChange = {
                    val filtered = it.filter { char -> char.isLetter() }.take(3)
                    abbreviation = filtered
                    showAbbreviationError = false
                },
                label = { Text("Abkürzung (2-3 Buchstaben)") },
                isError = showAbbreviationError,
                supportingText = {
                    if (showAbbreviationError) {
                        Text("Bitte gib eine Abkürzung mit 2-3 Buchstaben ein")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = teacher,
                onValueChange = { teacher = it },
                label = { Text("Lehrer") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = room,
                onValueChange = { room = it },
                label = { Text("Raum") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Beschreibung") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Text(
                text = "Fachfarbe:",
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                subjectColors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = color,
                                shape = MaterialTheme.shapes.small
                            )
                            .clickable { selectedColor = color }
                            .then(
                                if (selectedColor == color) {
                                    Modifier.border(
                                        width = 3.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = MaterialTheme.shapes.small
                                    )
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedColor == color) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = ComposeColor.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Button(
                onClick = { originalColor = selectedColor; showCustomColorDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Eigene Farbe wählen")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    var hasError = false

                    if (subjectName.isBlank()) {
                        showNameError = true
                        hasError = true
                    }

                    if (abbreviation.length < 2 || abbreviation.length > 3) {
                        showAbbreviationError = true
                        hasError = true
                    }

                    if (!hasError) {
                        val newSubject = Subject(
                            id = UUID.randomUUID().toString(),
                            name = subjectName.trim(),
                            abbreviation = abbreviation.trim(),
                            teacher = teacher.trim(),
                            room = room.trim(),
                            description = description.trim(),
                            color = selectedColor,
                            grades = emptyList()
                        )

                        onSubjectAdded(newSubject)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Fach hinzufügen")
            }

            if (showCustomColorDialog) {
                AlertDialog(
                    onDismissRequest = { showCustomColorDialog = false },
                    title = { Text("Eigene Farbe wählen") },
                    text = {
                        ColorPicker(
                            initialColor = selectedColor,
                            onColorSelected = { selectedColor = it }
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { showCustomColorDialog = false }) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { selectedColor = originalColor; showCustomColorDialog = false }) {
                            Text("Abbrechen")
                        }
                    }
                )
            }
        }
    }
}
