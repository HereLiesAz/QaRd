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

class QrDataStore(private val context: Context) {

    companion object {
        private fun qrConfigKey(appWidgetId: Int) = stringPreferencesKey("qr_config_$appWidgetId")
        private val SAVED_CONFIGS_KEY = stringPreferencesKey("saved_qr_configs_list")
    }

    fun getConfig(appWidgetId: Int): Flow<QrConfig> {
        val key = qrConfigKey(appWidgetId)
        return context.dataStore.data
            .map { preferences ->
                val jsonString = preferences[key]
                Log.d("QrDataStore", "Loading config for widget $appWidgetId: $jsonString")
                if (jsonString != null) {
                    try {
                        Json.decodeFromString<QrConfig>(jsonString)
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
            val jsonString = Json.encodeToString(config)
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
                    Json.decodeFromString<List<QrConfig>>(jsonString)
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
            preferences[SAVED_CONFIGS_KEY] = Json.encodeToString(configs)
        }
    }
}