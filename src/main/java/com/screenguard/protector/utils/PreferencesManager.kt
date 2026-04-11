package com.screenguard.protector.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "screenguard_settings")

class PreferencesManager(private val context: Context) {
    
    private companion object {
        val IS_SERVICE_RUNNING = booleanPreferencesKey("is_service_running")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val ALERT_COUNT = intPreferencesKey("alert_count")
        val DEVICE_WHITELISTED = booleanPreferencesKey("device_whitelisted")
    }

    val isServiceRunning: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_SERVICE_RUNNING] ?: false
    }

    suspend fun saveServiceRunning(isRunning: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_SERVICE_RUNNING] = isRunning
        }
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATIONS_ENABLED] ?: true
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    val vibrationEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[VIBRATION_ENABLED] ?: true
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[VIBRATION_ENABLED] = enabled
        }
    }

    val soundEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SOUND_ENABLED] ?: true
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SOUND_ENABLED] = enabled
        }
    }

    val alertCount: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[ALERT_COUNT] ?: 0
    }

    suspend fun incrementAlertCount() {
        context.dataStore.edit { preferences ->
            val current = preferences[ALERT_COUNT] ?: 0
            preferences[ALERT_COUNT] = current + 1
        }
    }

    suspend fun resetAlertCount() {
        context.dataStore.edit { preferences ->
            preferences[ALERT_COUNT] = 0
        }
    }

    suspend fun isCurrentDeviceWhitelisted(): Boolean {
        return context.dataStore.data
            .map { prefs -> prefs[DEVICE_WHITELISTED] ?: false }
            .first()
    }

    suspend fun setDeviceWhitelisted(whitelisted: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DEVICE_WHITELISTED] = whitelisted
        }
    }
}
