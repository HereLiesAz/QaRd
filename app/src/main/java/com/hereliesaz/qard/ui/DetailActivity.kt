package com.hereliesaz.qard.ui

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.hereliesaz.qard.ads.AdBanner
import com.hereliesaz.qard.data.QrConfig
import com.hereliesaz.qard.data.QrData
import com.hereliesaz.qard.data.QrDataStore
import com.hereliesaz.qard.ui.theme.QaRdTheme
import com.hereliesaz.qard.widget.QrGenerator
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

/**
 * Shows the information a saved/widget QR code contains (the single-tap target).
 * Launch with [configIntent] (in-app grid) or [widgetIntent] (home-screen widget).
 */
class DetailActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_CONFIG = "extra_config_json"

        fun configIntent(context: Context, config: QrConfig): Intent =
            Intent(context, DetailActivity::class.java).apply {
                putExtra(EXTRA_CONFIG, Json.encodeToString(config))
            }

        fun widgetIntent(context: Context, appWidgetId: Int): Intent =
            Intent(context, DetailActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val initialConfig = intent?.getStringExtra(EXTRA_CONFIG)?.let { json ->
            try {
                Json.decodeFromString<QrConfig>(json)
            } catch (e: Exception) {
                null
            }
        }
        val appWidgetId = intent?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (initialConfig == null && appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            QaRdTheme {
                DetailRoute(
                    initialConfig = initialConfig,
                    appWidgetId = appWidgetId,
                    onBack = { finish() }
                )
            }
        }
    }
}

@Composable
private fun DetailRoute(initialConfig: QrConfig?, appWidgetId: Int, onBack: () -> Unit) {
    val context = LocalContext.current
    var config by remember { mutableStateOf(initialConfig) }

    // Widget taps don't carry the config; load it from storage off the UI thread.
    LaunchedEffect(appWidgetId) {
        if (config == null && appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            config = QrDataStore(context).getConfig(appWidgetId).first()
        }
    }

    val current = config
    if (current != null) {
        QrCodeDetailScreen(config = current, onBack = onBack)
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrCodeDetailScreen(config: QrConfig, onBack: () -> Unit) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QR Code Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = { AdBanner() }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val qrBitmap = remember(config) { QrGenerator.generate(config) }
            if (qrBitmap != null) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "QR Code",
                    modifier = Modifier.size(240.dp)
                )
            }

            Text(
                text = "Contents",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.fillMaxWidth()
            )

            config.data.forEach { data ->
                when (data) {
                    is QrData.Links -> data.links.filter { it.isNotBlank() }.forEach { link ->
                        InfoRow(label = "Link", value = link) { openUrl(context, link) }
                    }
                    is QrData.Contact -> {
                        if (data.name.isNotBlank()) InfoRow("Name", data.name)
                        if (data.phone.isNotBlank()) InfoRow("Phone", data.phone)
                        if (data.email.isNotBlank()) InfoRow("Email", data.email)
                        if (data.organization.isNotBlank()) InfoRow("Organization", data.organization)
                        if (data.website.isNotBlank()) {
                            InfoRow("Website", data.website) { openUrl(context, data.website) }
                        }
                    }
                    is QrData.SocialMedia -> data.links.filter { it.url.isNotBlank() }.forEach { social ->
                        InfoRow(label = social.platform.ifBlank { "Social" }, value = social.url) {
                            openUrl(context, social.url)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, onClick: (() -> Unit)? = null) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

private fun openUrl(context: Context, url: String) {
    val normalized = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(normalized)))
    } catch (e: Exception) {
        // No browser / invalid URL — silently ignore.
    }
}
