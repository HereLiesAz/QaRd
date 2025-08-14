package com.hereliesaz.qard.ui

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor
import com.hereliesaz.qard.data.*
import com.hereliesaz.qard.data.BackgroundType
import com.hereliesaz.qard.R
import com.hereliesaz.qard.data.ForegroundType
import com.hereliesaz.qard.data.SocialLink
import com.hereliesaz.qard.ui.theme.QaRdTheme
import com.hereliesaz.qard.widget.QrGenerator
import com.hereliesaz.qard.widget.QrWidget
import com.materialkolor.DynamicMaterialTheme
import android.util.Log
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.random.Random

class ConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private val qrWidget = QrWidget()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            QaRdTheme {
                ConfigScreen(appWidgetId = appWidgetId, qrWidget = qrWidget) {
                    // This lambda is called when configuration is complete
                    val resultValue =
                        Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    setResult(Activity.RESULT_OK, resultValue)
                    finish()
                }
            }
        }
    }
}

private fun generateRandomPresets(): List<QrConfig> {
    return (1..20).map {
        val random = Random.Default
        val shape = QrShape.values().random()
        val fgType = ForegroundType.values().random()
        val bgType = BackgroundType.values().random()

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(appWidgetId: Int, qrWidget: QrWidget, onConfigComplete: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataStore = remember { QrDataStore(context) }

    var config by remember { mutableStateOf<QrConfig?>(null) }
    var showForegroundColorPicker by remember { mutableStateOf(false) }
    var showBackgroundColorPicker by remember { mutableStateOf(false) }
    var showGradientColorPicker1 by remember { mutableStateOf(false) }
    var showGradientColorPicker2 by remember { mutableStateOf(false) }
    var showFgGradientColorPicker1 by remember { mutableStateOf(false) }
    var showFgGradientColorPicker2 by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState()
    var isSheetOpen by remember { mutableStateOf(false) }

    var presets by remember { mutableStateOf<List<QrConfig>>(emptyList()) }

    LaunchedEffect(key1 = appWidgetId) {
        config = dataStore.getConfig(appWidgetId).first()
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

    if (isSheetOpen) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { isSheetOpen = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QrCodePreview(config = currentConfig)
            }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Image(
                painter = painterResource(id = com.hereliesaz.qard.R.drawable.ic_launcher),

                contentDescription = "App Icon",
                modifier = Modifier.size(64.dp)
            )

            // Data Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Data", style = MaterialTheme.typography.headlineMedium)

                    val selectedTypes = remember(currentConfig.data) {
                        currentConfig.data.map {
                            when (it) {
                                is QrData.Links -> QrDataType.Links
                                is QrData.Contact -> QrDataType.Contact
                                is QrData.SocialMedia -> QrDataType.SocialMedia
                            }
                        }
                    }

                    MultiChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        QrDataType.values().forEachIndexed { index, type ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = QrDataType.values().size),
                                onCheckedChange = { isChecked ->
                                    val currentData = currentConfig.data.toMutableList()
                                    if (isChecked) {
                                        if (selectedTypes.none { it == type }) {
                                            val newData = when (type) {
                                                QrDataType.Links -> QrData.Links(links = listOf(""))
                                                QrDataType.Contact -> QrData.Contact()
                                                QrDataType.SocialMedia -> QrData.SocialMedia(links = listOf(SocialLink("","")))
                                            }
                                            currentData.add(newData)
                                        }
                                    } else {
                                        currentData.removeAll {
                                            when(it) {
                                                is QrData.Links -> type == QrDataType.Links
                                                is QrData.Contact -> type == QrDataType.Contact
                                                is QrData.SocialMedia -> type == QrDataType.SocialMedia
                                            }
                                        }
                                    }
                                    config = currentConfig.copy(data = currentData)
                                },
                                checked = type in selectedTypes
                            ) {
                                Text(type.name)
                            }
                        }
                    }

                    currentConfig.data.forEach { data ->
                        when (data) {
                            is QrData.Links -> LinksForm(links = data) { newLinks ->
                                val newDataList = currentConfig.data.toMutableList()
                                val index = newDataList.indexOf(data)
                                if (index != -1) {
                                    newDataList[index] = newLinks
                                    config = currentConfig.copy(data = newDataList)
                                }
                            }
                            is QrData.Contact -> ContactForm(contact = data) { newContact ->
                                val newDataList = currentConfig.data.toMutableList()
                                val index = newDataList.indexOf(data)
                                if (index != -1) {
                                    newDataList[index] = newContact
                                    config = currentConfig.copy(data = newDataList)
                                }
                            }
                            is QrData.SocialMedia -> SocialMediaForm(socialMedia = data) { newSocialMedia ->
                                val newDataList = currentConfig.data.toMutableList()
                                val index = newDataList.indexOf(data)
                                if (index != -1) {
                                    newDataList[index] = newSocialMedia
                                    config = currentConfig.copy(data = newDataList)
                                }
                            }
                        }
                    }

                    Text("Background Transparency", style = MaterialTheme.typography.bodyLarge)
                    Slider(
                        value = currentConfig.backgroundAlpha,
                        onValueChange = { config = currentConfig.copy(backgroundAlpha = it) },
                        valueRange = 0f..1f
                    )
                }
            }

            // Appearance Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Appearance", style = MaterialTheme.typography.headlineMedium)

                    Text("Background Type", style = MaterialTheme.typography.bodyLarge)
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        BackgroundType.values().forEachIndexed { index, backgroundType ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = BackgroundType.values().size),
                                onClick = { config = currentConfig.copy(backgroundType = backgroundType) },
                                selected = currentConfig.backgroundType == backgroundType
                            ) {
                                Text(backgroundType.name)
                            }
                        }
                    }

                    Text("Shape", style = MaterialTheme.typography.bodyLarge)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(items = QrShape.values()) { shape ->
                            val isSelected = currentConfig.shape == shape
                            Card(
                                onClick = { config = currentConfig.copy(shape = shape) },
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
                                        imageVector = when (shape) {
                                            QrShape.Square -> Icons.Default.CropSquare
                                            QrShape.Circle -> Icons.Default.Circle
                                            QrShape.RoundSquare -> Icons.Default.CheckBoxOutlineBlank
                                            QrShape.Diamond -> Icons.Default.FavoriteBorder // Placeholder
                                            else -> Icons.Default.CropSquare
                                        },
                                        contentDescription = shape.name,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(shape.name, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

                    Text("Foreground Type", style = MaterialTheme.typography.bodyLarge)
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        ForegroundType.values().forEachIndexed { index, foregroundType ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = ForegroundType.values().size),
                                onClick = { config = currentConfig.copy(foregroundType = foregroundType) },
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
                                onClick = { showForegroundColorPicker = true }
                            )
                        }
                        ForegroundType.GRADIENT -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                ColorPickerField(
                                    label = "Foreground Gradient Color 1",
                                    color = currentConfig.foregroundGradientColors.getOrElse(0) { 0xFF000000.toInt() },
                                    onClick = { showFgGradientColorPicker1 = true }
                                )
                                ColorPickerField(
                                    label = "Foreground Gradient Color 2",
                                    color = currentConfig.foregroundGradientColors.getOrElse(1) { 0xFF0000FF.toInt() },
                                    onClick = { showFgGradientColorPicker2 = true }
                                )
                            }
                        }
                    }

                    when (currentConfig.backgroundType) {
                        BackgroundType.SOLID -> {
                            ColorPickerField(
                                label = "Background Color",
                                color = currentConfig.backgroundColor,
                                onClick = { showBackgroundColorPicker = true }
                            )
                        }
                        BackgroundType.GRADIENT -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                ColorPickerField(
                                    label = "Gradient Color 1",
                                    color = currentConfig.backgroundGradientColors.getOrElse(0) { 0xFFFFFFFF.toInt() },
                                    onClick = { showGradientColorPicker1 = true }
                                )
                                ColorPickerField(
                                    label = "Gradient Color 2",
                                    color = currentConfig.backgroundGradientColors.getOrElse(1) { 0xFF000000.toInt() },
                                    onClick = { showGradientColorPicker2 = true }
                                )
                                Text("Gradient Angle: ${currentConfig.backgroundGradientAngle.toInt()}°", style = MaterialTheme.typography.bodyLarge)
                                Slider(
                                    value = currentConfig.backgroundGradientAngle,
                                    onValueChange = { config = currentConfig.copy(backgroundGradientAngle = it) },
                                    valueRange = 0f..360f,
                                    steps = 35
                                )
                            }
                        }
                    }
                }
            }

            Text("Presets", style = MaterialTheme.typography.headlineMedium)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(items = presets) { preset ->
                    Card(
                        onClick = { config = preset.copy(data = currentConfig.data) },
                    ) {
                        Box(modifier = Modifier.padding(8.dp)) {
                            QrCodePreview(config = preset)
                        }
                    }
                }
            }

            Text("Saved", style = MaterialTheme.typography.headlineMedium)
            var savedConfigs by remember { mutableStateOf<List<QrConfig>>(emptyList()) }
            LaunchedEffect(key1 = Unit) {
                dataStore.getSavedConfigs().collect {
                    savedConfigs = it
                }
            }
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(items = savedConfigs) { savedConfig ->
                    Card(
                        onClick = { config = savedConfig },
                    ) {
                        Box(modifier = Modifier.padding(8.dp)) {
                            QrCodePreview(config = savedConfig)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = { isSheetOpen = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Show Preview")
                }
                Button(
                    onClick = {
                        scope.launch {
                            Log.d("WidgetFlow", "Saving config for widget ID: $appWidgetId")
                            Log.d("WidgetFlow", "Config data: $currentConfig")
                            val currentSaved = dataStore.getSavedConfigs().first()
                            val newSaved = (currentSaved + currentConfig).distinct()
                            dataStore.saveConfigs(newSaved)

                            dataStore.saveConfig(appWidgetId, currentConfig)
                            val glanceId =
                                GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
                            qrWidget.update(context, glanceId)
                            onConfigComplete()
                        }
                    },
                    enabled = currentConfig.data.any {
                        when (it) {
                            is QrData.Links -> it.links.any { link -> link.isNotBlank() }
                            is QrData.Contact -> it.name.isNotBlank()
                            is QrData.SocialMedia -> it.links.any { social -> social.url.isNotBlank() }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Create Widget")
                }
            }
        }
    }

    if (showForegroundColorPicker) {
        ColorPickerDialog(
            initialColor = currentConfig.foregroundColor,
            onColorSelected = {
                config = currentConfig.copy(foregroundColor = it)
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
                config = currentConfig.copy(backgroundColor = it)
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
                config = currentConfig.copy(backgroundGradientColors = newColors)
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
                config = currentConfig.copy(backgroundGradientColors = newColors)
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
                config = currentConfig.copy(foregroundGradientColors = newColors)
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
                config = currentConfig.copy(foregroundGradientColors = newColors)
                showFgGradientColorPicker2 = false
            },
            onDismiss = { showFgGradientColorPicker2 = false }
        )
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
fun QrCodePreview(config: QrConfig) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Preview", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        val qrBitmap = QrGenerator.generate(config)
        if (qrBitmap != null) {
            Image(
                bitmap = qrBitmap.asImageBitmap(),
                contentDescription = "QR Code Preview",
                modifier = Modifier.size(150.dp)
            )
        } else {
            Text("Enter data to see preview")
        }
    }
}