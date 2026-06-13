package com.hereliesaz.qard.ui

import android.Manifest
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor
import com.hereliesaz.aznavrail.*
import com.hereliesaz.qard.data.BackgroundType
import com.hereliesaz.qard.data.ForegroundType
import com.hereliesaz.qard.data.QrConfig
import com.hereliesaz.qard.data.QrData
import com.hereliesaz.qard.data.QrDataStore
import com.hereliesaz.qard.data.QrDataType
import com.hereliesaz.qard.data.QrShape
import com.hereliesaz.qard.data.SocialLink
import com.hereliesaz.qard.ui.theme.LogoPink
import com.hereliesaz.qard.ui.theme.QaRdTheme
import com.hereliesaz.qard.widget.QrGenerator
import com.hereliesaz.qard.widget.QrImageExporter
import com.hereliesaz.qard.widget.QrWidget
import com.hereliesaz.qard.widget.QrWidgetReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlin.random.Random

class ConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    companion object {
        const val EXTRA_MODE = "extra_mode"
        const val MODE_WIDGET_CONFIG = "widget_config"
        const val MODE_STANDALONE = "standalone"
        private const val EXTRA_CONFIG_JSON = "extra_config_json"

        /**
         * Launch the construction screen in standalone mode (no widget). Used by
         * double-tap-to-edit from a placed widget. Pass an existing [config] to
         * edit it, or null to start from a blank code.
         */
        fun standaloneIntent(context: Context, config: QrConfig? = null): Intent =
            Intent(context, ConfigActivity::class.java).apply {
                putExtra(EXTRA_MODE, MODE_STANDALONE)
                if (config != null) putExtra(EXTRA_CONFIG_JSON, Json.encodeToString(config))
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)

        val mode = intent?.getStringExtra(EXTRA_MODE) ?: MODE_WIDGET_CONFIG

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        Log.d("ConfigActivityLog", "onCreate: mode = $mode, appWidgetId = $appWidgetId")

        if (mode == MODE_WIDGET_CONFIG && appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val initialConfig = intent?.getStringExtra(EXTRA_CONFIG_JSON)?.let { json ->
            try {
                Json.decodeFromString<QrConfig>(json)
            } catch (e: Exception) {
                null
            }
        }

        setContent {
            QaRdTheme {
                ConfigScreen(
                    mode = mode,
                    appWidgetId = appWidgetId,
                    initialConfig = initialConfig
                ) {
                    if (mode == MODE_WIDGET_CONFIG) {
                        val resultValue =
                            Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        setResult(Activity.RESULT_OK, resultValue)
                    }
                    finish()
                }
            }
        }
    }
}

private fun generateRandomPresets(): List<QrConfig> {
    return (1..20).map {
        val random = Random.Default
        val shape = QrShape.entries.random()
        val fgType = ForegroundType.entries.random()
        val bgType = BackgroundType.entries.random()

        fun randomColor() = Color(random.nextFloat(), random.nextFloat(), random.nextFloat(), 1f).toArgb()
        fun randomColorList() = listOf(randomColor(), randomColor())

        QrConfig(
            data = listOf(QrData.Links(links = listOf("https://github.com/hereliesaz/qard"))),
            shape = shape,
            foregroundType = fgType,
            foregroundColor = randomColor(),
            foregroundGradientColors = randomColorList(),
            backgroundType = bgType,
            backgroundColor = randomColor(),
            backgroundGradientColors = randomColorList(),
            backgroundGradientAngle = random.nextFloat() * 360
        )
    }
}

private fun shapeIcon(shape: QrShape): ImageVector = when (shape) {
    QrShape.Square -> Icons.Default.CropSquare
    QrShape.Circle -> Icons.Default.Circle
    QrShape.RoundSquare -> Icons.Default.CheckBoxOutlineBlank
    QrShape.Diamond -> Icons.Default.FavoriteBorder // Placeholder
}

private fun QrData.dataType(): QrDataType = when (this) {
    is QrData.Links -> QrDataType.Links
    is QrData.Contact -> QrDataType.Contact
    is QrData.SocialMedia -> QrDataType.SocialMedia
}

private fun dataTypeLabel(type: QrDataType): String = when (type) {
    QrDataType.Links -> "Links"
    QrDataType.Contact -> "Contact"
    QrDataType.SocialMedia -> "Social Media"
}

private fun dataTypeIcon(type: QrDataType): ImageVector = when (type) {
    QrDataType.Links -> Icons.Default.Link
    QrDataType.Contact -> Icons.Default.Person
    QrDataType.SocialMedia -> Icons.Default.Share
}

private fun defaultDataFor(type: QrDataType): QrData = when (type) {
    QrDataType.Links -> QrData.Links(links = listOf(""))
    QrDataType.Contact -> QrData.Contact()
    QrDataType.SocialMedia -> QrData.SocialMedia(links = listOf(SocialLink("", "")))
}

private fun replaceData(
    config: QrConfig,
    old: QrData,
    new: QrData,
    updateConfig: (QrConfig) -> Unit
) {
    val list = config.data.toMutableList()
    val index = list.indexOf(old)
    if (index != -1) {
        list[index] = new
        updateConfig(config.copy(data = list))
    }
}

private fun QrConfig.hasContent(): Boolean = data.any {
    when (it) {
        is QrData.Links -> it.links.any { link -> link.isNotBlank() }
        is QrData.Contact -> it.name.isNotBlank()
        is QrData.SocialMedia -> it.links.any { social -> social.url.isNotBlank() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(
    mode: String,
    appWidgetId: Int,
    initialConfig: QrConfig?,
    onConfigComplete: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataStore = remember { QrDataStore(context) }
    val isStandalone = mode == ConfigActivity.MODE_STANDALONE

    var config by remember { mutableStateOf<QrConfig?>(null) }

    val updateConfig = { newConfig: QrConfig ->
        Log.d("ConfigActivityLog", "updateConfig: newConfig = $newConfig")
        config = newConfig
    }
    var showForegroundColorPicker by remember { mutableStateOf(false) }
    var showBackgroundColorPicker by remember { mutableStateOf(false) }
    var showGradientColorPicker1 by remember { mutableStateOf(false) }
    var showGradientColorPicker2 by remember { mutableStateOf(false) }
    var showFgGradientColorPicker1 by remember { mutableStateOf(false) }
    var showFgGradientColorPicker2 by remember { mutableStateOf(false) }

    var presets by remember { mutableStateOf<List<QrConfig>>(emptyList()) }

    // The saved-list entry the current edits belong to, so repeated saves update
    // it in place instead of piling up duplicates. Seeded with the config we
    // opened (when editing) and re-pointed whenever the user loads a saved code.
    var persistedSnapshot by remember { mutableStateOf(initialConfig) }

    // Persist the current config into the saved-configs list so it shows up in
    // the Load screen and reloads later. Updates the tracked entry in place; in
    // widget mode it also writes through to the widget's own config.
    suspend fun persistCurrentConfig() {
        val cfg = config ?: return
        if (!cfg.hasContent()) return
        val saved = dataStore.getSavedConfigs().first().toMutableList()
        val previous = persistedSnapshot
        val idx = if (previous != null) saved.indexOf(previous) else -1
        when {
            idx >= 0 -> saved[idx] = cfg
            !saved.contains(cfg) -> saved.add(cfg)
        }
        dataStore.saveConfigs(saved)
        persistedSnapshot = cfg
        if (!isStandalone) {
            dataStore.saveConfig(appWidgetId, cfg)
        }
    }

    // Loading a saved code makes it the entry future edits update in place.
    val loadSavedConfig = { cfg: QrConfig ->
        persistedSnapshot = cfg
        config = cfg
    }

    fun saveCurrentAsImage() {
        val cfg = config ?: return
        scope.launch {
            persistCurrentConfig()
            val uri = withContext(Dispatchers.IO) {
                QrImageExporter.saveToGallery(context, cfg)
            }
            Toast.makeText(
                context,
                if (uri != null) "Saved to gallery (Pictures/QaRd)" else "Couldn't save image",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val writePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            saveCurrentAsImage()
        } else {
            Toast.makeText(
                context,
                "Storage permission is needed to save the image",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val onSaveImageClick = {
        val needsPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        if (needsPermission) {
            writePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            saveCurrentAsImage()
        }
    }

    // Finalize the widget that was being configured via the launcher's picker.
    fun finalizeWidget() {
        val cfg = config ?: return
        scope.launch {
            dataStore.saveConfig(appWidgetId, cfg)
            val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
            QrWidget().update(context, glanceId)
            persistCurrentConfig()
            onConfigComplete()
        }
    }

    // Pin a brand-new widget to the home screen — the same flow as the launcher's
    // widget picker "Add" button. The launcher drops the widget in an open spot
    // with its resize bounding box; the new widget's id arrives in the pin-success
    // broadcast handled by QrWidgetReceiver.
    fun pinCurrentAsWidget() {
        val cfg = config ?: return
        val manager = AppWidgetManager.getInstance(context)
        if (!manager.isRequestPinAppWidgetSupported) {
            Toast.makeText(
                context,
                "Your launcher doesn't support adding widgets this way.",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        scope.launch {
            persistCurrentConfig()
            dataStore.savePendingPinConfig(cfg)
            val provider = ComponentName(context, QrWidgetReceiver::class.java)
            manager.requestPinAppWidget(provider, null, QrWidgetReceiver.pinSuccessCallback(context))
        }
    }

    LaunchedEffect(key1 = appWidgetId) {
        val loadedConfig = if (isStandalone) {
            initialConfig ?: QrConfig()
        } else {
            dataStore.getConfig(appWidgetId).first()
        }
        Log.d("ConfigActivityLog", "LaunchedEffect: loadedConfig = $loadedConfig")
        config = loadedConfig
        presets = generateRandomPresets()
    }

    val currentConfig = config
    if (currentConfig == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Auto-save: whatever the user enters is persisted shortly after they stop
    // typing, so there's no Save button to remember to press.
    LaunchedEffect(currentConfig) {
        if (!currentConfig.hasContent()) return@LaunchedEffect
        delay(400)
        persistCurrentConfig()
    }

    val hasData = currentConfig.hasContent()

    val navController = rememberNavController()

    AzHostActivityLayout(navController = navController) {
        // Selected rail item highlight — logo pink so it stands out on the dark rail.
        azTheme(activeColor = LogoPink)
        azRailItem(id = "load", text = "Load", route = "load", content = Icons.Default.FolderOpen)
        azRailItem(id = "data", text = "Data", route = "data", content = Icons.Default.Edit)
        azRailItem(id = "presets", text = "Presets", route = "presets", content = Icons.Default.AutoAwesome)
        azRailItem(id = "design", text = "Design", route = "design", content = Icons.Default.Palette)
        azRailItem(id = "preview", text = "Preview", route = "preview", content = Icons.Default.Visibility)
        azRailItem(id = "save", text = "Save", route = "save", content = Icons.Default.Save)

        onscreen {
            AzNavHost(startDestination = "data") {
                composable("load") {
                    LoadScreen(dataStore = dataStore, updateConfig = loadSavedConfig)
                }
                composable("data") {
                    DataScreen(currentConfig = currentConfig, updateConfig = updateConfig)
                }
                composable("presets") {
                    PresetsScreen(
                        presets = presets,
                        currentConfig = currentConfig,
                        updateConfig = updateConfig
                    )
                }
                composable("design") {
                    DesignScreen(
                        currentConfig = currentConfig,
                        updateConfig = updateConfig,
                        showForegroundColorPicker = { showForegroundColorPicker = true },
                        showBackgroundColorPicker = { showBackgroundColorPicker = true },
                        showGradientColorPicker1 = { showGradientColorPicker1 = true },
                        showGradientColorPicker2 = { showGradientColorPicker2 = true },
                        showFgGradientColorPicker1 = { showFgGradientColorPicker1 = true },
                        showFgGradientColorPicker2 = { showFgGradientColorPicker2 = true }
                    )
                }
                composable("preview") {
                    PreviewScreen(config = currentConfig)
                }
                composable("save") {
                    SaveScreen(
                        config = currentConfig,
                        hasData = hasData,
                        isStandalone = isStandalone,
                        onSaveImage = onSaveImageClick,
                        onCreateWidget = { if (isStandalone) pinCurrentAsWidget() else finalizeWidget() }
                    )
                }
            }
        }
    }

    if (showForegroundColorPicker) {
        ColorPickerDialog(
            initialColor = currentConfig.foregroundColor,
            onColorSelected = {
                updateConfig(currentConfig.copy(foregroundColor = it))
                showForegroundColorPicker = false
            },
            onDismiss = { showForegroundColorPicker = false }
        )
    }

    if (showBackgroundColorPicker) {
        ColorPickerDialog(
            initialColor = currentConfig.backgroundColor,
            showAlphaBar = true,
            onColorSelected = {
                updateConfig(currentConfig.copy(backgroundColor = it))
                showBackgroundColorPicker = false
            },
            onDismiss = { showBackgroundColorPicker = false }
        )
    }

    if (showGradientColorPicker1) {
        ColorPickerDialog(
            initialColor = currentConfig.backgroundGradientColors.getOrElse(0) { 0xFFFFFFFF.toInt() },
            showAlphaBar = true,
            onColorSelected = {
                val newColors = currentConfig.backgroundGradientColors.toMutableList()
                newColors[0] = it
                updateConfig(currentConfig.copy(backgroundGradientColors = newColors))
                showGradientColorPicker1 = false
            },
            onDismiss = { showGradientColorPicker1 = false }
        )
    }

    if (showGradientColorPicker2) {
        ColorPickerDialog(
            initialColor = currentConfig.backgroundGradientColors.getOrElse(1) { 0xFF000000.toInt() },
            showAlphaBar = true,
            onColorSelected = {
                val newColors = currentConfig.backgroundGradientColors.toMutableList()
                newColors[1] = it
                updateConfig(currentConfig.copy(backgroundGradientColors = newColors))
                showGradientColorPicker2 = false
            },
            onDismiss = { showGradientColorPicker2 = false }
        )
    }

    if (showFgGradientColorPicker1) {
        ColorPickerDialog(
            initialColor = currentConfig.foregroundGradientColors.getOrElse(0) { 0xFF000000.toInt() },
            showAlphaBar = true,
            onColorSelected = {
                val newColors = currentConfig.foregroundGradientColors.toMutableList()
                newColors[0] = it
                updateConfig(currentConfig.copy(foregroundGradientColors = newColors))
                showFgGradientColorPicker1 = false
            },
            onDismiss = { showFgGradientColorPicker1 = false }
        )
    }

    if (showFgGradientColorPicker2) {
        ColorPickerDialog(
            initialColor = currentConfig.foregroundGradientColors.getOrElse(1) { 0xFF0000FF.toInt() },
            showAlphaBar = true,
            onColorSelected = {
                val newColors = currentConfig.foregroundGradientColors.toMutableList()
                newColors[1] = it
                updateConfig(currentConfig.copy(foregroundGradientColors = newColors))
                showFgGradientColorPicker2 = false
            },
            onDismiss = { showFgGradientColorPicker2 = false }
        )
    }
}

@Composable
fun DataScreen(
    currentConfig: QrConfig,
    updateConfig: (QrConfig) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Data", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Flip on the kinds of data you want to encode and fill them in.",
            style = MaterialTheme.typography.bodyMedium
        )
        // Every data type is shown up front as its own section — no need to press
        // anything to discover what can be added.
        QrDataType.entries.forEach { type ->
            DataTypeSection(
                type = type,
                currentConfig = currentConfig,
                updateConfig = updateConfig
            )
        }
    }
}

@Composable
private fun DataTypeSection(
    type: QrDataType,
    currentConfig: QrConfig,
    updateConfig: (QrConfig) -> Unit
) {
    val existing = currentConfig.data.firstOrNull { it.dataType() == type }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = dataTypeIcon(type), contentDescription = null)
                    Text(dataTypeLabel(type), style = MaterialTheme.typography.titleMedium)
                }
                Switch(
                    checked = existing != null,
                    onCheckedChange = { isOn ->
                        val data = currentConfig.data.toMutableList()
                        if (isOn) {
                            if (existing == null) data.add(defaultDataFor(type))
                        } else {
                            data.removeAll { it.dataType() == type }
                        }
                        updateConfig(currentConfig.copy(data = data))
                    }
                )
            }
            when (existing) {
                is QrData.Links -> LinksForm(links = existing) {
                    replaceData(currentConfig, existing, it, updateConfig)
                }
                is QrData.Contact -> ContactForm(contact = existing) {
                    replaceData(currentConfig, existing, it, updateConfig)
                }
                is QrData.SocialMedia -> SocialMediaForm(socialMedia = existing) {
                    replaceData(currentConfig, existing, it, updateConfig)
                }
                null -> { /* section is off — nothing to show */ }
            }
        }
    }
}

@Composable
fun DesignScreen(
    currentConfig: QrConfig,
    updateConfig: (QrConfig) -> Unit,
    showForegroundColorPicker: () -> Unit,
    showBackgroundColorPicker: () -> Unit,
    showGradientColorPicker1: () -> Unit,
    showGradientColorPicker2: () -> Unit,
    showFgGradientColorPicker1: () -> Unit,
    showFgGradientColorPicker2: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Design", style = MaterialTheme.typography.headlineMedium)

        // Shape
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Shape", style = MaterialTheme.typography.titleMedium)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(items = QrShape.entries) { shape ->
                        val isSelected = currentConfig.shape == shape
                        Card(
                            onClick = { updateConfig(currentConfig.copy(shape = shape)) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = shapeIcon(shape),
                                    contentDescription = shape.name,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(shape.name, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }

        // Colour
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Colour", style = MaterialTheme.typography.titleMedium)

                Text("Foreground Type", style = MaterialTheme.typography.bodyLarge)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    ForegroundType.entries.forEachIndexed { index, foregroundType ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = ForegroundType.entries.size
                            ),
                            onClick = { updateConfig(currentConfig.copy(foregroundType = foregroundType)) },
                            selected = currentConfig.foregroundType == foregroundType
                        ) {
                            Text(foregroundType.name)
                        }
                    }
                }

                when (currentConfig.foregroundType) {
                    ForegroundType.SOLID -> {
                        ColorPickerField(
                            label = "Foreground Color",
                            color = currentConfig.foregroundColor,
                            onClick = showForegroundColorPicker
                        )
                    }
                    ForegroundType.GRADIENT -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ColorPickerField(
                                label = "Foreground Gradient Color 1",
                                color = currentConfig.foregroundGradientColors.getOrElse(0) { 0xFF000000.toInt() },
                                onClick = showFgGradientColorPicker1
                            )
                            ColorPickerField(
                                label = "Foreground Gradient Color 2",
                                color = currentConfig.foregroundGradientColors.getOrElse(1) { 0xFF0000FF.toInt() },
                                onClick = showFgGradientColorPicker2
                            )
                        }
                    }
                }

                Text("Background Type", style = MaterialTheme.typography.bodyLarge)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    BackgroundType.entries.forEachIndexed { index, backgroundType ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = BackgroundType.entries.size
                            ),
                            onClick = { updateConfig(currentConfig.copy(backgroundType = backgroundType)) },
                            selected = currentConfig.backgroundType == backgroundType
                        ) {
                            Text(backgroundType.name)
                        }
                    }
                }

                when (currentConfig.backgroundType) {
                    BackgroundType.SOLID -> {
                        ColorPickerField(
                            label = "Background Color",
                            color = currentConfig.backgroundColor,
                            onClick = showBackgroundColorPicker
                        )
                    }
                    BackgroundType.GRADIENT -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ColorPickerField(
                                label = "Gradient Color 1",
                                color = currentConfig.backgroundGradientColors.getOrElse(0) { 0xFFFFFFFF.toInt() },
                                onClick = showGradientColorPicker1
                            )
                            ColorPickerField(
                                label = "Gradient Color 2",
                                color = currentConfig.backgroundGradientColors.getOrElse(1) { 0xFF000000.toInt() },
                                onClick = showGradientColorPicker2
                            )
                            Text("Gradient Angle: ${currentConfig.backgroundGradientAngle.toInt()}°", style = MaterialTheme.typography.bodyLarge)
                            Slider(
                                value = currentConfig.backgroundGradientAngle,
                                onValueChange = { updateConfig(currentConfig.copy(backgroundGradientAngle = it)) },
                                valueRange = 0f..360f,
                                steps = 35
                            )
                        }
                    }
                }

                Text("Background Transparency", style = MaterialTheme.typography.bodyLarge)
                Slider(
                    value = currentConfig.backgroundAlpha,
                    onValueChange = { updateConfig(currentConfig.copy(backgroundAlpha = it)) },
                    valueRange = 0f..1f
                )
            }
        }
    }
}

@Composable
fun PresetsScreen(
    presets: List<QrConfig>,
    currentConfig: QrConfig,
    updateConfig: (QrConfig) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Presets", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Tap a preset to apply its colours and shape to your data.",
            style = MaterialTheme.typography.bodyMedium
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 120.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            gridItems(presets) { preset ->
                Card(onClick = { updateConfig(preset.copy(data = currentConfig.data)) }) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QrCodePreview(config = preset, title = null, imageSize = 96.dp)
                        Text(preset.shape.name, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun LoadScreen(
    dataStore: QrDataStore,
    updateConfig: (QrConfig) -> Unit
) {
    var savedConfigs by remember { mutableStateOf<List<QrConfig>>(emptyList()) }
    LaunchedEffect(key1 = Unit) {
        dataStore.getSavedConfigs().collect { savedConfigs = it }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Load", style = MaterialTheme.typography.headlineMedium)
        if (savedConfigs.isEmpty()) {
            Text(
                "Codes you save will appear here.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            Text(
                "Tap a saved code to load it into the editor.",
                style = MaterialTheme.typography.bodyMedium
            )
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                gridItems(savedConfigs) { savedConfig ->
                    Card(onClick = { updateConfig(savedConfig) }) {
                        Box(modifier = Modifier.padding(8.dp)) {
                            QrCodePreview(config = savedConfig, title = null, imageSize = 96.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PreviewScreen(config: QrConfig) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        QrCodePreview(config = config, title = "Preview", imageSize = 280.dp)
    }
}

@Composable
fun SaveScreen(
    config: QrConfig,
    hasData: Boolean,
    isStandalone: Boolean,
    onSaveImage: () -> Unit,
    onCreateWidget: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Save", style = MaterialTheme.typography.headlineMedium)
        QrCodePreview(config = config, title = null, imageSize = 220.dp)
        Text(
            "Your code is saved automatically and shows up under Load. " +
                "Drop it on your home screen as a widget, or export it as an image.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Button(
            onClick = onCreateWidget,
            enabled = hasData,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isStandalone) "Create widget" else "Add to home screen")
        }
        OutlinedButton(
            onClick = onSaveImage,
            enabled = hasData,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save as image")
        }
    }
}

@Composable
fun ContactForm(contact: QrData.Contact, onContactChange: (QrData.Contact) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = contact.name,
            onValueChange = { onContactChange(contact.copy(name = it)) },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = contact.phone,
            onValueChange = { onContactChange(contact.copy(phone = it)) },
            label = { Text("Phone") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = contact.email,
            onValueChange = { onContactChange(contact.copy(email = it)) },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = contact.organization,
            onValueChange = { onContactChange(contact.copy(organization = it)) },
            label = { Text("Organization") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = contact.website,
            onValueChange = { onContactChange(contact.copy(website = it)) },
            label = { Text("Website") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun LinksForm(links: QrData.Links, onLinksChange: (QrData.Links) -> Unit) {
    EditableList(
        items = links.links,
        onAdd = { onLinksChange(links.copy(links = links.links + "")) },
        onRemove = { index -> onLinksChange(links.copy(links = links.links.filterIndexed { i, _ -> i != index })) },
        itemContent = { index, item ->
            OutlinedTextField(
                value = item,
                onValueChange = { newItem ->
                    onLinksChange(links.copy(links = links.links.toMutableList().apply { set(index, newItem) }))
                },
                label = { Text("Link to File") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}

@Composable
fun <T> EditableList(
    items: List<T>,
    onAdd: () -> Unit,
    onRemove: (Int) -> Unit,
    itemContent: @Composable (Int, T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEachIndexed { index, item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(1f)) {
                    itemContent(index, item)
                }
                IconButton(onClick = { onRemove(index) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
        Button(onClick = onAdd) {
            Text("Add")
        }
    }
}

@Composable
fun SocialMediaForm(socialMedia: QrData.SocialMedia, onSocialMediaChange: (QrData.SocialMedia) -> Unit) {
    EditableList(
        items = socialMedia.links,
        onAdd = { onSocialMediaChange(socialMedia.copy(links = socialMedia.links + SocialLink("", ""))) },
        onRemove = { index -> onSocialMediaChange(socialMedia.copy(links = socialMedia.links.filterIndexed { i, _ -> i != index })) },
        itemContent = { index, item ->
            Row {
                OutlinedTextField(
                    value = item.platform,
                    onValueChange = { newPlatform ->
                        val newLinks = socialMedia.links.toMutableList().apply {
                            set(index, item.copy(platform = newPlatform))
                        }
                        onSocialMediaChange(socialMedia.copy(links = newLinks))
                    },
                    label = { Text("Platform") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = item.url,
                    onValueChange = { newUrl ->
                        val newLinks = socialMedia.links.toMutableList().apply {
                            set(index, item.copy(url = newUrl))
                        }
                        onSocialMediaChange(socialMedia.copy(links = newLinks))
                    },
                    label = { Text("URL") },
                    modifier = Modifier.weight(2f)
                )
            }
        }
    )
}

@Composable
fun ColorPickerField(label: String, color: Int, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(label, modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = Color(color),
                        shape = MaterialTheme.shapes.small
                    )
            )
        }
    }
}

@Composable
fun ColorPickerDialog(
    initialColor: Int,
    showAlphaBar: Boolean = false,
    onColorSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var color by remember { mutableStateOf(HsvColor.from(Color(initialColor))) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Color") },
        text = {
            ClassicColorPicker(
                color = color,
                onColorChanged = { hsvColor -> color = hsvColor },
                modifier = Modifier.height(250.dp),
                showAlphaBar = showAlphaBar
            )
        },
        confirmButton = {
            Button(onClick = { onColorSelected(color.toColor().toArgb()) }) {
                Text("OK")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun QrCodePreview(config: QrConfig, title: String? = "Preview", imageSize: Dp = 150.dp) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (title != null) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
        }
        val qrBitmap = remember(config) { QrGenerator.generate(config) }
        if (qrBitmap != null) {
            Image(
                bitmap = qrBitmap.asImageBitmap(),
                contentDescription = "QR Code Preview",
                modifier = Modifier.size(imageSize)
            )
        } else {
            Text("Enter data to see preview")
        }
    }
}
