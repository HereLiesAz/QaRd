// G:/My Drive/QaRd/app/src/main/java/com/hereliesaz/qard/data/QrDataStore.kt (Hypothetical content)
package com.hereliesaz.qard.data

import android.content.Context
import android.util.Log // Add this import
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// This should be at the top level of your file
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "qr_configs_data_store")

class QrDataStore(private val context: Context) {

    // Companion object to hold the key generation logic
    companion object {
        private fun qrConfigKey(appWidgetId: Int) = stringPreferencesKey("qr_config_$appWidgetId")
    }

    fun getConfig(appWidgetId: Int): Flow<QrConfig> {
        val key = qrConfigKey(appWidgetId)
        return context.dataStore.data
            .map { preferences ->
                val jsonString = preferences[key]
                if (jsonString != null) {
                    try {
                        val config = Json.decodeFromString<QrConfig>(jsonString)
                        Log.d(
                            "QrDataStore",
                            "Retrieved config for widget ID $appWidgetId. Data size: ${config.data.size}"
                        )
                        config
                    } catch (e: Exception) {
                        Log.e(
                            "QrDataStore",
                            "Error deserializing config for widget $appWidgetId: ${e.message}"
                        )
                        QrConfig() // Return default if deserialization fails
                    }
                } else {
                    Log.d(
                        "QrDataStore",
                        "No config found for widget ID $appWidgetId. Returning default."
                    )
                    QrConfig() // Return default empty config if not found
                }
            }
    }

    suspend fun saveConfig(appWidgetId: Int, config: QrConfig) {
        val key = qrConfigKey(appWidgetId)
        context.dataStore.edit { preferences ->
            preferences[key] = Json.encodeToString(config)
            Log.d(
                "QrDataStore",
                "Saved config for widget ID $appWidgetId. Data size: ${config.data.size}"
            )
        }
    }

    suspend fun deleteConfig(appWidgetId: Int) {
        val key = qrConfigKey(appWidgetId)
        context.dataStore.edit { preferences ->
            preferences.remove(key)
            Log.d("QrDataStore", "Deleted config for widget ID $appWidgetId.")
        }
    }

    // Your existing getSavedConfigs() would likely need to iterate through all keys
    // that start with "qr_config_" prefix to fetch all saved presets.
    fun getSavedConfigs(): Flow<List<QrConfig>> {
        return context.dataStore.data.map { preferences ->
            preferences.asMap().filterKeys { it.name.startsWith("qr_config_") }
                .mapNotNull { entry ->
                    try {
                        Json.decodeFromString<QrConfig>(entry.value as String)
                    } catch (e: Exception) {
                        null
                    }
                }
        }
    }
}
    