package com.batteryguardian.domain.repository

import com.batteryguardian.domain.model.BatteryDisplayFormat
import com.batteryguardian.domain.model.NotificationPriority
import com.batteryguardian.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user preferences.
 */
interface UserPreferencesRepository {

    /**
     * Get all user preferences.
     */
    val preferences: Flow<UserPreferences>

    /**
     * Set the low battery threshold.
     */
    suspend fun setLowThreshold(percent: Int)

    /**
     * Set the medium battery threshold.
     */
    suspend fun setMediumThreshold(percent: Int)

    /**
     * Set the critical battery threshold.
     */
    suspend fun setCriticalThreshold(percent: Int)

    /**
     * Set the hysteresis band.
     */
    suspend fun setHysteresisBand(percent: Int)

    /**
     * Set the polling interval in minutes.
     */
    suspend fun setPollingInterval(minutes: Int)

    /**
     * Enable or disable notifications.
     */
    suspend fun setNotificationsEnabled(enabled: Boolean)

    /**
     * Set the notification priority.
     */
    suspend fun setNotificationPriority(priority: NotificationPriority)

    /**
     * Enable or disable dark theme.
     */
    suspend fun setDarkTheme(enabled: Boolean)

    /**
     * Set the battery display format.
     */
    suspend fun setBatteryDisplayFormat(format: BatteryDisplayFormat)

    /**
     * Reset all preferences to defaults.
     */
    suspend fun resetToDefaults()
}
