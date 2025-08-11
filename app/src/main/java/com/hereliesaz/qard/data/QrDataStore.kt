package com.hereliesaz.qard.data

import android.content.Context
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
    }

    fun getConfig(appWidgetId: Int): Flow<QrConfig> {
        return dataStore.data.map { preferences ->
            preferences[configKey(appWidgetId)]?.let { jsonString ->
                try {
                    val jsonObject = Json.parseToJsonElement(jsonString) as JsonObject
                    val data = jsonObject["data"]
                    if (data is JsonPrimitive) {
                        // Old format, data is a string
                        QrConfig(data = QrData.Links(listOf(data.content)))
                    } else {
                        // New format
                        Json.decodeFromJsonElement(QrConfig.serializer(), jsonObject)
                    }
                } catch (e: Exception) {
                    QrConfig() // Return default on deserialization error
                }
            } ?: QrConfig() // Return default config if nothing is stored
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
