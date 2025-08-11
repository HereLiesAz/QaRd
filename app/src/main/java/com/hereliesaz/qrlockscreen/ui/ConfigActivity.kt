package com.hereliesaz.qrlockscreen.ui

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor
import com.hereliesaz.qrlockscreen.data.*
import com.hereliesaz.qrlockscreen.ui.theme.QrLockscreenTheme
import com.hereliesaz.qrlockscreen.widget.QrGenerator
import com.hereliesaz.qrlockscreen.widget.QrWidget
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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
            QrLockscreenTheme {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(appWidgetId: Int, qrWidget: QrWidget, onConfigComplete: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataStore = remember { QrDataStore(context) }

    var config by remember { mutableStateOf<QrConfig?>(null) }
    var showForegroundColorPicker by remember { mutableStateOf(false) }
    var showBackgroundColorPicker by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = appWidgetId) {
        config = dataStore.getConfig(appWidgetId).first()
    }

    if (config == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Configure QR Code", style = MaterialTheme.typography.headlineSmall)

            var selectedDataType by remember {
                mutableStateOf(
                    when (config!!.data) {
                        is QrData.Links -> QrDataType.Links
                        is QrData.Contact -> QrDataType.Contact
                        is QrData.SocialMedia -> QrDataType.SocialMedia
                    }
                )
            }

            DataTypeSelector(
                selectedType = selectedDataType,
                onTypeSelected = { newType ->
                    selectedDataType = newType
                    val newData = when (newType) {
                        QrDataType.Links -> QrData.Links()
                        QrDataType.Contact -> QrData.Contact()
                        QrDataType.SocialMedia -> QrData.SocialMedia()
                    }
                    config = config!!.copy(data = newData)
                }
            )

            when (val data = config!!.data) {
                is QrData.Links -> {
                    LinksForm(
                        links = data,
                        onLinksChange = { newLinks ->
                            config = config!!.copy(data = newLinks)
                        }
                    )
                }
                is QrData.Contact -> {
                    ContactForm(
                        contact = data,
                        onContactChange = { newContact ->
                            config = config!!.copy(data = newContact)
                        }
                    )
                }
                is QrData.SocialMedia -> {
                    SocialMediaForm(
                        socialMedia = data,
                        onSocialMediaChange = { newSocialMedia ->
                            config = config!!.copy(data = newSocialMedia)
                        }
                    )
                }
            }

            var shapeExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = shapeExpanded,
                onExpandedChange = { shapeExpanded = !shapeExpanded }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = config!!.shape.name,
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
                                config = config!!.copy(shape = selectionOption)
                                shapeExpanded = false
                            }
                        )
                    }
                }
            }

            ColorPickerField(
                label = "Foreground Color",
                color = config!!.foregroundColor,
                onClick = { showForegroundColorPicker = true }
            )

            ColorPickerField(
                label = "Background Color",
                color = config!!.backgroundColor,
                onClick = { showBackgroundColorPicker = true }
            )

            Button(
                onClick = {
                    scope.launch {
                        dataStore.saveConfig(appWidgetId, config!!)
                        val glanceId =
                            GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
                        qrWidget.update(context, glanceId)
                        onConfigComplete()
                    }
                },
                enabled = when (val data = config!!.data) {
                    is QrData.Links -> data.links.any { it.isNotBlank() }
                    is QrData.Contact -> data.name.isNotBlank()
                    is QrData.SocialMedia -> data.links.any { it.url.isNotBlank() }
                }
            ) {
                Text("Create Widget")
            }

            Spacer(modifier = Modifier.height(16.dp))

            QrCodePreview(config = config!!)
        }
    }

    if (showForegroundColorPicker) {
        ColorPickerDialog(
            initialColor = config!!.foregroundColor,
            onColorSelected = {
                config = config!!.copy(foregroundColor = it)
                showForegroundColorPicker = false
            },
            onDismiss = { showForegroundColorPicker = false }
        )
    }

    if (showBackgroundColorPicker) {
        ColorPickerDialog(
            initialColor = config!!.backgroundColor,
            showAlphaBar = true,
            onColorSelected = {
                config = config!!.copy(backgroundColor = it)
                showBackgroundColorPicker = false
            },
            onDismiss = { showBackgroundColorPicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataTypeSelector(
    selectedType: QrDataType,
    onTypeSelected: (QrDataType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val items = QrDataType.values()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            readOnly = true,
            value = selectedType.name,
            onValueChange = {},
            label = { Text("Data Type") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption.name) },
                    onClick = {
                        onTypeSelected(selectionOption)
                        expanded = false
                    }
                )
            }
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

        Spacer(modifier = Modifier.height(8.dp))
        Text("Social Media Links", style = MaterialTheme.typography.titleMedium)

        EditableList(
            items = contact.socialLinks,
            onAdd = { onContactChange(contact.copy(socialLinks = contact.socialLinks + SocialLink("", ""))) },
            onRemove = { index -> onContactChange(contact.copy(socialLinks = contact.socialLinks.filterIndexed { i, _ -> i != index })) },
            itemContent = { index, item ->
                Row {
                    OutlinedTextField(
                        value = item.platform,
                        onValueChange = { newItem ->
                            onContactChange(contact.copy(socialLinks = contact.socialLinks.toMutableList().apply { set(index, item.copy(platform = newItem)) }))
                        },
                        label = { Text("Platform") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = item.url,
                        onValueChange = { newItem ->
                            onContactChange(contact.copy(socialLinks = contact.socialLinks.toMutableList().apply { set(index, item.copy(url = newItem)) }))
                        },
                        label = { Text("URL") },
                        modifier = Modifier.weight(2f)
                    )
                }
            }
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
                    onValueChange = { newItem ->
                        onSocialMediaChange(socialMedia.copy(links = socialMedia.links.toMutableList().apply { set(index, item.copy(platform = newItem)) }))
                    },
                    label = { Text("Platform") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = item.url,
                    onValueChange = { newItem ->
                        onSocialMediaChange(socialMedia.copy(links = socialMedia.links.toMutableList().apply { set(index, item.copy(url = newItem)) }))
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = Color(color),
                    shape = MaterialTheme.shapes.small
                )
        )
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