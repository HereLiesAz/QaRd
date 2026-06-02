package com.hereliesaz.qard.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hereliesaz.qard.data.QrConfig
import com.hereliesaz.qard.data.QrDataStore
import com.hereliesaz.qard.ui.theme.QaRdTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QaRdTheme {
                HomeScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val dataStore = remember { QrDataStore(context) }
    var savedConfigs by remember { mutableStateOf<List<QrConfig>>(emptyList()) }

    LaunchedEffect(Unit) {
        dataStore.getSavedConfigs().collect {
            savedConfigs = it
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("QaRd") })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Create QR code") },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                onClick = { context.startActivity(ConfigActivity.standaloneIntent(context)) }
            )
        }
    ) { padding ->
        if (savedConfigs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tap “Create QR code” to build a QR code and save it as an image, " +
                        "or add a QaRd widget to your home screen.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Text(
                    text = "Saved QR Codes",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)
                )
                Text(
                    text = "Tap to view its info · double-tap to edit",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                )
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = savedConfigs) { config ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        context.startActivity(
                                            DetailActivity.configIntent(context, config)
                                        )
                                    },
                                    onDoubleClick = {
                                        context.startActivity(
                                            ConfigActivity.standaloneIntent(context, config)
                                        )
                                    }
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                QrCodePreview(config = config, title = null)
                            }
                        }
                    }
                }
            }
        }
    }
}
