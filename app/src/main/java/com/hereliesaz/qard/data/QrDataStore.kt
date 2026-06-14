package com.hereliesaz.qard.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "qr_configs_data_store")

// Tolerate configs written by older app versions whose data model has since
// changed (e.g. the Contact fields) instead of throwing on unknown keys.
private val json = Json { ignoreUnknownKeys = true }

class QrDataStore(private val context: Context) {

    companion object {
        private fun qrConfigKey(appWidgetId: Int) = stringPreferencesKey("qr_config_$appWidgetId")
        private val SAVED_CONFIGS_KEY = stringPreferencesKey("saved_qr_configs_list")
        private val PENDING_PIN_CONFIG_KEY = stringPreferencesKey("pending_pin_config")
    }

    fun getConfig(appWidgetId: Int): Flow<QrConfig> {
        val key = qrConfigKey(appWidgetId)
        return context.dataStore.data
            .map { preferences ->
                val jsonString = preferences[key]
                Log.d("QrDataStore", "Loading config for widget $appWidgetId: $jsonString")
                if (jsonString != null) {
                    try {
                        json.decodeFromString<QrConfig>(jsonString)
                    } catch (e: Exception) {
                        Log.e(
                            "QrDataStore",
                            "Error deserializing config for widget $appWidgetId: ${e.message}"
                        )
                        QrConfig() // Return default if deserialization fails
                    }
                } else {
                    QrConfig() // Return default empty config if not found
                }
            }
    }

    suspend fun saveConfig(appWidgetId: Int, config: QrConfig) {
        val key = qrConfigKey(appWidgetId)
        context.dataStore.edit { preferences ->
            val jsonString = json.encodeToString(config)
            Log.d("QrDataStore", "Saving config for widget $appWidgetId: $jsonString")
            preferences[key] = jsonString
        }
    }

    suspend fun deleteConfig(appWidgetId: Int) {
        val key = qrConfigKey(appWidgetId)
        context.dataStore.edit { preferences ->
            preferences.remove(key)
        }
    }

    fun getSavedConfigs(): Flow<List<QrConfig>> {
        return context.dataStore.data.map { preferences ->
            val jsonString = preferences[SAVED_CONFIGS_KEY]
            if (jsonString != null) {
                try {
                    json.decodeFromString<List<QrConfig>>(jsonString)
                } catch (e: Exception) {
                    emptyList()
                }
            } else {
                emptyList()
            }
        }
    }

    suspend fun saveConfigs(configs: List<QrConfig>) {
        context.dataStore.edit { preferences ->
            preferences[SAVED_CONFIGS_KEY] = json.encodeToString(configs)
        }
    }

    /**
     * Stash the config the user wants to pin as a widget. The widget's appWidgetId
     * isn't known until the launcher actually places it, so the pin-success receiver
     * reads this back and assigns it to the new widget.
     */
    suspend fun savePendingPinConfig(config: QrConfig) {
        context.dataStore.edit { preferences ->
            preferences[PENDING_PIN_CONFIG_KEY] = json.encodeToString(config)
        }
    }

    /** Reads and clears the stashed pin config, or null if none is pending. */
    suspend fun takePendingPinConfig(): QrConfig? {
        var result: QrConfig? = null
        context.dataStore.edit { preferences ->
            val jsonString = preferences[PENDING_PIN_CONFIG_KEY]
            if (jsonString != null) {
                result = try {
                    json.decodeFromString<QrConfig>(jsonString)
                } catch (e: Exception) {
                    null
                }
                preferences.remove(PENDING_PIN_CONFIG_KEY)
            }
        }
        return result
    }
}