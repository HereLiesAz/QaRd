package com.hereliesaz.qard.ui

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.hereliesaz.qard.data.QrConfig
import com.hereliesaz.qard.data.QrData
import com.hereliesaz.qard.data.QrDataStore
import com.hereliesaz.qard.data.displayName
import com.hereliesaz.qard.data.hasInfo
import com.hereliesaz.qard.ui.theme.QaRdTheme
import kotlinx.coroutines.flow.first

/**
 * The small menu shown when a home-screen widget is tapped. Offers acting on the
 * code (like scanning it), viewing its details, editing it, or opening the app.
 * Hosted in a translucent activity so it floats over the launcher.
 */
class WidgetMenuActivity : ComponentActivity() {

    companion object {
        fun intent(context: Context, appWidgetId: Int): Intent =
            Intent(context, WidgetMenuActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appWidgetId = intent?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        setContent {
            QaRdTheme {
                WidgetMenu(appWidgetId = appWidgetId, onClose = { finish() })
            }
        }
    }
}

@Composable
private fun WidgetMenu(appWidgetId: Int, onClose: () -> Unit) {
    val context = LocalContext.current
    var config by remember { mutableStateOf<QrConfig?>(null) }
    LaunchedEffect(appWidgetId) {
        config = QrDataStore(context).getConfig(appWidgetId).first()
    }

    Dialog(onDismissRequest = onClose) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(vertical = 8.dp),
            ) {
                Text(
                    text = "QaRd",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )

                val cfg = config
                if (cfg == null) {
                    // Config still loading — avoid acting on a null (blank) config.
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    primaryAction(cfg)?.let { primary ->
                        MenuRow(Icons.Default.OpenInNew, primary.label) {
                            primary.run(context)
                            onClose()
                        }
                    }
                    MenuRow(Icons.Default.Info, "View details") {
                        context.startActivity(
                            DetailActivity.widgetIntent(context, appWidgetId)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                        onClose()
                    }
                    MenuRow(Icons.Default.Edit, "Edit") {
                        context.startActivity(
                            ConfigActivity.standaloneIntent(context, cfg)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                        onClose()
                    }
                    MenuRow(Icons.Default.Home, "Open QaRd app") {
                        context.startActivity(
                            Intent(context, MainActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                        onClose()
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null)
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

private class PrimaryAction(val label: String, val run: (Context) -> Unit)

/** The "act on the code like a scanner would" action, derived from the config. */
private fun primaryAction(config: QrConfig): PrimaryAction? {
    val contact = config.data.filterIsInstance<QrData.Contact>().firstOrNull()?.takeIf { it.hasInfo() }
    if (contact != null) {
        return PrimaryAction("Add to contacts") { ctx -> openContactInsert(ctx, contact) }
    }
    val url = config.firstUrl()
    if (url != null) {
        return PrimaryAction("Open link") { ctx -> openUrl(ctx, url) }
    }
    return null
}

private fun QrConfig.firstUrl(): String? {
    data.forEach { item ->
        when (item) {
            is QrData.Links -> item.links.firstOrNull { it.isNotBlank() }?.let { return it }
            is QrData.SocialMedia -> item.links.firstOrNull { it.url.isNotBlank() }?.let { return it.url }
            else -> {}
        }
    }
    return null
}

private fun openUrl(context: Context, url: String) {
    val normalized = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(normalized))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (e: Exception) {
        // No handler / invalid URL — ignore.
    }
}

private fun openContactInsert(context: Context, contact: QrData.Contact) {
    val intent = Intent(ContactsContract.Intents.Insert.ACTION).apply {
        type = ContactsContract.RawContacts.CONTENT_TYPE
        putExtra(ContactsContract.Intents.Insert.NAME, contact.displayName())
        contact.phones.firstOrNull { it.value.isNotBlank() }
            ?.let { putExtra(ContactsContract.Intents.Insert.PHONE, it.value) }
        contact.emails.firstOrNull { it.value.isNotBlank() }
            ?.let { putExtra(ContactsContract.Intents.Insert.EMAIL, it.value) }
        contact.addresses.firstOrNull { it.value.isNotBlank() }
            ?.let { putExtra(ContactsContract.Intents.Insert.POSTAL, it.value) }
        if (contact.organization.isNotBlank()) {
            putExtra(ContactsContract.Intents.Insert.COMPANY, contact.organization)
        }
        if (contact.title.isNotBlank()) {
            putExtra(ContactsContract.Intents.Insert.JOB_TITLE, contact.title)
        }
        if (contact.note.isNotBlank()) {
            putExtra(ContactsContract.Intents.Insert.NOTES, contact.note)
        }
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        // No contacts app — ignore.
    }
}
