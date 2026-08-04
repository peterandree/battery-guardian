package com.batteryguardian.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.batteryguardian.domain.model.BatteryDisplayFormat
import com.batteryguardian.domain.model.NotificationPriority
import com.batteryguardian.domain.model.UserPreferences
import com.batteryguardian.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of UserPreferencesRepository using DataStore.
 */
class UserPreferencesRepositoryImpl @Inject constructor(
    private val context: Context
) : UserPreferencesRepository {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = "user_preferences"
    )

    override val preferences: Flow<UserPreferences> = context.dataStore.data
        .map { preferences ->
            UserPreferences(
                lowThreshold = preferences[LOW_THRESHOLD] ?: 20,
                mediumThreshold = preferences[MEDIUM_THRESHOLD] ?: 10,
                criticalThreshold = preferences[CRITICAL_THRESHOLD] ?: 5,
                hysteresisBand = preferences[HYSTERESIS_BAND] ?: 2,
                pollingInterval = preferences[POLLING_INTERVAL] ?: 5,
                notificationsEnabled = preferences[NOTIFICATIONS_ENABLED] ?: true,
                notificationPriority = NotificationPriority.valueOf(
                    preferences[NOTIFICATION_PRIORITY] ?: "DEFAULT"
                ),
                darkTheme = preferences[DARK_THEME] ?: false,
                batteryDisplayFormat = BatteryDisplayFormat.valueOf(
                    preferences[BATTERY_DISPLAY_FORMAT] ?: "PERCENTAGE"
                )
            )
        }

    override suspend fun setLowThreshold(percent: Int) {
        context.dataStore.edit { preferences ->
            preferences[LOW_THRESHOLD] = percent
        }
    }

    override suspend fun setMediumThreshold(percent: Int) {
        context.dataStore.edit { preferences ->
            preferences[MEDIUM_THRESHOLD] = percent
        }
    }

    override suspend fun setCriticalThreshold(percent: Int) {
        context.dataStore.edit { preferences ->
            preferences[CRITICAL_THRESHOLD] = percent
        }
    }

    override suspend fun setHysteresisBand(percent: Int) {
        require(percent in 1..10) { "Hysteresis band must be between 1% and 10%" }
        context.dataStore.edit { preferences ->
            preferences[HYSTERESIS_BAND] = percent
        }
    }

    override suspend fun setPollingInterval(minutes: Int) {
        require(minutes in listOf(1, 5, 15)) { "Polling interval must be 1, 5, or 15 minutes" }
        context.dataStore.edit { preferences ->
            preferences[POLLING_INTERVAL] = minutes
        }
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    override suspend fun setNotificationPriority(priority: NotificationPriority) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_PRIORITY] = priority.name
        }
    }

    override suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_THEME] = enabled
        }
    }

    override suspend fun setBatteryDisplayFormat(format: BatteryDisplayFormat) {
        context.dataStore.edit { preferences ->
            preferences[BATTERY_DISPLAY_FORMAT] = format.name
        }
    }

    override suspend fun resetToDefaults() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    companion object {
        // Preference keys
        private val LOW_THRESHOLD = intPreferencesKey("low_threshold")
        private val MEDIUM_THRESHOLD = intPreferencesKey("medium_threshold")
        private val CRITICAL_THRESHOLD = intPreferencesKey("critical_threshold")
        private val HYSTERESIS_BAND = intPreferencesKey("hysteresis_band")
        private val POLLING_INTERVAL = intPreferencesKey("polling_interval")
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val NOTIFICATION_PRIORITY = stringPreferencesKey("notification_priority")
        private val DARK_THEME = booleanPreferencesKey("dark_theme")
        private val BATTERY_DISPLAY_FORMAT = stringPreferencesKey("battery_display_format")
    }
}
