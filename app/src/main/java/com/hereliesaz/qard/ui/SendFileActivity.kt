package com.hereliesaz.qard.ui

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hereliesaz.qard.data.QrConfig
import com.hereliesaz.qard.data.QrData
import com.hereliesaz.qard.transfer.LocalFileServer
import com.hereliesaz.qard.transfer.NetworkUtils
import com.hereliesaz.qard.ui.theme.QaRdTheme
import com.hereliesaz.qard.widget.QrGenerator
import java.util.Locale

/**
 * Sender-only "pass it on" screen for files. Phase 1: same-Wi-Fi hand-off — pick a file,
 * serve it from a local HTTP server, and show a QR of the URL. The receiver scans it with
 * their stock camera; no app, no scanner, no account needed on the other end.
 */
class SendFileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QaRdTheme {
                SendFileScreen()
            }
        }
    }
}

private sealed interface SendState {
    data object Idle : SendState
    data object NoNetwork : SendState
    data class Error(val message: String) : SendState
    data class Serving(
        val url: String,
        val fileName: String,
        val fileSize: Long,
    ) : SendState
}

@Composable
fun SendFileScreen() {
    val context = LocalContext.current
    var state by remember { mutableStateOf<SendState>(SendState.Idle) }
    var server by remember { mutableStateOf<LocalFileServer?>(null) }

    fun stopServer() {
        server?.let { runCatching { it.stop() } }
        server = null
    }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        stopServer()

        val ip = NetworkUtils.localIpAddress()
        if (ip == null) {
            state = SendState.NoNetwork
            return@rememberLauncherForActivityResult
        }

        val meta = queryFile(context, uri)
        val srv = LocalFileServer(
            appContext = context.applicationContext,
            fileUri = uri,
            fileName = meta.name,
            mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream",
            fileSize = meta.size,
        )
        try {
            srv.start(NanoHttpdTimeoutMs, false)
        } catch (e: Exception) {
            Log.e("SendFileActivity", "Failed to start local server", e)
            state = SendState.Error("Couldn't start the local server. Try again.")
            return@rememberLauncherForActivityResult
        }
        server = srv
        state = SendState.Serving(srv.url(ip), meta.name, meta.size)
    }

    // Tear the server down when the screen leaves composition.
    DisposableEffect(Unit) {
        onDispose { stopServer() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when (val s = state) {
            is SendState.Idle -> {
                Text(
                    "Send a file to a nearby phone on the same Wi-Fi. Pick a file and a QR " +
                        "appears — the other person scans it with their normal camera and the " +
                        "file downloads in their browser. No app needed on their end.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }

            is SendState.NoNetwork -> {
                Text(
                    "You're not on a Wi-Fi network. Connect both phones to the same Wi-Fi, " +
                        "then try again.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }

            is SendState.Error -> {
                Text(
                    s.message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }

            is SendState.Serving -> {
                val bitmap = remember(s.url) {
                    QrGenerator.generate(
                        QrConfig(data = listOf(QrData.Links(links = listOf(s.url))))
                    )
                }
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "QR code for the file download link",
                        modifier = Modifier.size(280.dp),
                    )
                }
                Text(
                    s.fileName,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
                if (s.fileSize >= 0) {
                    Text(
                        formatSize(s.fileSize),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Text(
                    "Have them scan this with their camera while you stay on this screen. " +
                        "The link stops working when you leave.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
                Text(
                    s.url,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Button(
            onClick = { picker.launch(arrayOf("*/*")) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (state is SendState.Serving) "Pick another file" else "Choose a file")
        }
    }
}

private data class FileMeta(val name: String, val size: Long)

/** Reads the display name and size of a SAF document Uri. Size is -1 if unknown. */
private fun queryFile(context: Context, uri: Uri): FileMeta {
    var name = "file"
    var size = -1L
    runCatching {
        context.contentResolver.query(uri, null, null, null, null)?.use { c ->
            val nameIdx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIdx = c.getColumnIndex(OpenableColumns.SIZE)
            if (c.moveToFirst()) {
                if (nameIdx >= 0) c.getString(nameIdx)?.let { name = it }
                if (sizeIdx >= 0 && !c.isNull(sizeIdx)) size = c.getLong(sizeIdx)
            }
        }
    }
    return FileMeta(name, size)
}

private fun formatSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val units = listOf("KB", "MB", "GB", "TB")
    var value = bytes.toDouble() / 1024
    var unitIndex = 0
    while (value >= 1024 && unitIndex < units.lastIndex) {
        value /= 1024
        unitIndex++
    }
    return String.format(Locale.US, "%.1f %s", value, units[unitIndex])
}

// NanoHTTPD socket read timeout (ms); generous so a slow download isn't dropped.
private const val NanoHttpdTimeoutMs = 60_000
