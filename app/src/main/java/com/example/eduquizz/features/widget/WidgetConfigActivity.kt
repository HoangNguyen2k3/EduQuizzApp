package com.example.eduquizz.features.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.eduquizz.Screens.WordSearch.ui.theme.EduQuizzTheme

class WidgetConfigActivity : ComponentActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set result as CANCELED in case user backs out
        setResult(RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            EduQuizzTheme {
                WidgetConfigScreen(
                    onConfirm = { widgetType, imageSource, updateInterval ->
                        saveWidgetPreferences(widgetType, imageSource, updateInterval)
                        finishWithSuccess()
                    },
                    onCancel = { finish() }
                )
            }
        }
    }

    private fun saveWidgetPreferences(
        widgetType: String,
        imageSource: String,
        updateInterval: Int
    ) {
        val prefs = getSharedPreferences("widget_prefs", MODE_PRIVATE)
        prefs.edit()
            .putString("widget_type_$appWidgetId", widgetType)
            .putString("image_source_$appWidgetId", imageSource)
            .putInt("update_interval_$appWidgetId", updateInterval)
            .apply()
    }

    private fun finishWithSuccess() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        EduQuizzWidgetProvider().onUpdate(
            this,
            appWidgetManager,
            intArrayOf(appWidgetId)
        )

        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(RESULT_OK, resultValue)
        finish()
    }
}

@Composable
fun WidgetConfigScreen(
    onConfirm: (String, String, Int) -> Unit,
    onCancel: () -> Unit
) {
    var selectedWidgetType by remember { mutableStateOf("streak") }
    var selectedImageSource by remember { mutableStateOf("mapping") }
    var selectedUpdateInterval by remember { mutableStateOf(3600000) } // 1 hour

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Configure Widget",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Widget Type Selection
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Widget Type",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Column(Modifier.selectableGroup()) {
                        RadioOption(
                            text = "🔥 Streak Widget",
                            selected = selectedWidgetType == "streak",
                            onClick = { selectedWidgetType = "streak" }
                        )
                        RadioOption(
                            text = "🖼️ Image Quiz Widget",
                            selected = selectedWidgetType == "image_quiz",
                            onClick = { selectedWidgetType = "image_quiz" }
                        )
                        RadioOption(
                            text = "📝 Word of the Day",
                            selected = selectedWidgetType == "word_of_day",
                            onClick = { selectedWidgetType = "word_of_day" }
                        )
                    }
                }
            }

            // Image Source (only show for image_quiz)
            if (selectedWidgetType == "image_quiz") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Image Source",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Column(Modifier.selectableGroup()) {
                            RadioOption(
                                text = "🗺️ Mapping Game",
                                selected = selectedImageSource == "mapping",
                                onClick = { selectedImageSource = "mapping" }
                            )
                            RadioOption(
                                text = "✏️ Bat Chu Game",
                                selected = selectedImageSource == "batchu",
                                onClick = { selectedImageSource = "batchu" }
                            )
                        }
                    }
                }

                // Update Interval
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Update Interval",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Column(Modifier.selectableGroup()) {
                            RadioOption(
                                text = "Every Hour",
                                selected = selectedUpdateInterval == 3600000,
                                onClick = { selectedUpdateInterval = 3600000 }
                            )
                            RadioOption(
                                text = "Every 3 Hours",
                                selected = selectedUpdateInterval == 10800000,
                                onClick = { selectedUpdateInterval = 10800000 }
                            )
                            RadioOption(
                                text = "Every 6 Hours",
                                selected = selectedUpdateInterval == 21600000,
                                onClick = { selectedUpdateInterval = 21600000 }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        onConfirm(
                            selectedWidgetType,
                            selectedImageSource,
                            selectedUpdateInterval
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Add Widget")
                }
            }
        }
    }
}

@Composable
fun RadioOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Text(
            text = text,
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}