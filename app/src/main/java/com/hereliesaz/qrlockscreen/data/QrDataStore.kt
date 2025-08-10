package com.hereliesaz.qrLockscreen.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "qr_settings")

class QrDataStore(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        fun configKey(appWidgetId: Int) = stringPreferencesKey("config_v2_$appWidgetId")
    }

    fun getConfig(appWidgetId: Int): Flow<QrConfig> {
        return dataStore.data.map { preferences ->
            preferences[configKey(appWidgetId)]?.let { jsonString ->
                Json.decodeFromString<QrConfig>(jsonString)
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