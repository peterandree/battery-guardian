package com.batteryguardian.domain.repository

import com.batteryguardian.domain.model.AlertEvent
import com.batteryguardian.domain.model.AlertState
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Repository interface for alert operations.
 */
interface AlertRepository {

    /**
     * Get the current alert state for a device.
     */
    fun getAlertState(deviceId: String): Flow<AlertState>

    /**
     * Get alert states for all devices.
     */
    fun getAllAlertStates(): Flow<Map<String, AlertState>>

    /**
     * Get devices that are currently in alert state.
     */
    fun getDevicesInAlert(): Flow<List<String>>

    /**
     * Update the alert state for a device.
     */
    suspend fun updateAlertState(
        deviceId: String,
        state: AlertState
    )

    /**
     * Clear the alert state for a device.
     */
    suspend fun clearAlertState(deviceId: String)

    /**
     * Clear all alert states.
     */
    suspend fun clearAllAlertStates()

    /**
     * Check if a device is in alert state.
     */
    suspend fun isInAlert(deviceId: String): Boolean

    /**
     * Get the last alert event for a device.
     */
    fun getLastAlertEvent(deviceId: String): Flow<AlertEvent?>

    /**
     * Get all alert events.
     */
    fun getAllAlertEvents(): Flow<List<AlertEvent>>

    /**
     * Add an alert event.
     */
    suspend fun addAlertEvent(event: AlertEvent)

    /**
     * Get alert history for a device.
     */
    fun getAlertHistory(deviceId: String): Flow<List<AlertEvent>>

    /**
     * Clean up old alert events.
     */
    suspend fun cleanupOldAlertEvents(days: Int)
}
