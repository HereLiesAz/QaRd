package com.hereliesaz.qard.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

private val Context.dataStore by preferencesDataStore(name = "qr_settings")

class QrDataStore(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        fun configKey(appWidgetId: Int) = stringPreferencesKey("config_v2_$appWidgetId")
        val SAVED_CONFIGS = stringPreferencesKey("saved_configs_list")
    }

    fun getSavedConfigs(): Flow<List<QrConfig>> {
        return dataStore.data.map { preferences ->
            preferences[SAVED_CONFIGS]?.let { jsonString ->
                try {
                    Json.decodeFromString<List<QrConfig>>(jsonString)
                } catch (e: Exception) {
                    emptyList()
                }
            } ?: emptyList()
        }
    }

    suspend fun saveConfigs(configs: List<QrConfig>) {
        val jsonString = Json.encodeToString(configs)
        dataStore.edit { settings ->
            settings[SAVED_CONFIGS] = jsonString
        }
    }

    fun getConfig(appWidgetId: Int): Flow<QrConfig> {
        return dataStore.data.map { preferences ->
            preferences[configKey(appWidgetId)]?.let { jsonString ->
                Log.d("WidgetFlow", "Reading jsonString for widget ID $appWidgetId: $jsonString")
                try {
                    Json.decodeFromString<QrConfig>(jsonString)
                } catch (e: Exception) {
                    Log.e("WidgetFlow", "Deserialization failed for widget ID $appWidgetId", e)
                    QrConfig() // Return default on deserialization error
                }
            } ?: run {
                Log.d("WidgetFlow", "No config found for widget ID $appWidgetId. Returning default.")
                QrConfig()
            }
        }
    }

    suspend fun saveConfig(appWidgetId: Int, config: QrConfig) {
        val jsonString = Json.encodeToString(config)
        dataStore.edit { settings ->
            settings[configKey(appWidgetId)] = jsonString
        }
    }

    suspend fun deleteConfig(appWidgetId: Int) {
        dataStore.edit { settings ->
            settings.remove(configKey(appWidgetId))
        }
    }
}
