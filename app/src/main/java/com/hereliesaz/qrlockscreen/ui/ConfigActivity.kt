package com.hereliesaz.qrLockscreen.ui

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.hereliesaz.qrLockscreen.data.QrConfig
import com.hereliesaz.qrLockscreen.data.QrDataStore
import com.hereliesaz.qrLockscreen.data.QrShape
import com.hereliesaz.qrLockscreen.ui.theme.QrLockscreenTheme
import com.hereliesaz.qrLockscreen.widget.QrWidget
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
            ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            QrLockscreenTheme {
                ConfigScreen(appWidgetId = appWidgetId) {
                    val resultValue =
                        Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    setResult(Activity.RESULT_OK, resultValue)
                    finish()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(appWidgetId: Int, onConfigComplete: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataStore = remember { QrDataStore(context) }

    var config by remember { mutableStateOf(QrConfig()) }

    LaunchedEffect(key1 = appWidgetId) {
        config = dataStore.getConfig(appWidgetId).first()
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Configure QR Code", style = MaterialTheme.typography.headlineSmall)

            OutlinedTextField(
                value = config.data,
                onValueChange = { config = config.copy(data = it) },
                label = { Text("QR Code Data") },
                modifier = Modifier.fillMaxWidth()
            )

            var shapeExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = shapeExpanded,
                onExpandedChange = { shapeExpanded = !shapeExpanded }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = config.shape.name,
                    onValueChange = {},
                    label = { Text("Shape") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = shapeExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = shapeExpanded,
                    onDismissRequest = { shapeExpanded = false }
                ) {
                    QrShape.values().forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption.name) },
                            onClick = {
                                config = config.copy(shape = selectionOption)
                                shapeExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = config.foregroundColor,
                onValueChange = { config = config.copy(foregroundColor = it) },
                label = { Text("Foreground Color (e.g., #000000)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = config.backgroundColor,
                onValueChange = { config = config.copy(backgroundColor = it) },
                label = { Text("Background Color (e.g., #FFFFFF)") },
                modifier = Modifier.fillMaxWidth()
            )


            Button(
                onClick = {
                    scope.launch {
                        dataStore.saveConfig(appWidgetId, config)
                        QrWidget().update(
                            context,
                            AppWidgetManager.getInstance(context),
                            appWidgetId
                        )
                        onConfigComplete()
                    }
                },
                // Basic validation
                enabled = config.data.isNotBlank()
            ) {
                Text("Create Widget")
            }
        }
    }
}