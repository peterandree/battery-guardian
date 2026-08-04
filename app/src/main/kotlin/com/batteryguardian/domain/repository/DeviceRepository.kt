package com.batteryguardian.domain.repository

import com.batteryguardian.domain.model.Device
import com.batteryguardian.domain.model.DeviceCapabilities
import com.batteryguardian.domain.model.DeviceType
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Repository interface for device operations.
 */
interface DeviceRepository {

    /**
     * Get all devices.
     */
    fun getAllDevices(): Flow<List<Device>>

    /**
     * Get a specific device by ID.
     */
    fun getDevice(deviceId: String): Flow<Device?>

    /**
     * Get all monitored devices (not ignored).
     */
    fun getMonitoredDevices(): Flow<List<Device>>

    /**
     * Get all ignored devices.
     */
    fun getIgnoredDevices(): Flow<List<Device>>

    /**
     * Get devices by type.
     */
    fun getDevicesByType(type: DeviceType): Flow<List<Device>>

    /**
     * Get devices with low battery (below or at low threshold).
     */
    fun getDevicesWithLowBattery(lowThreshold: Int): Flow<List<Device>>

    /**
     * Get devices that need alerts (below or at any threshold).
     */
    fun getDevicesNeedingAlerts(
        lowThreshold: Int,
        mediumThreshold: Int,
        criticalThreshold: Int
    ): Flow<List<Device>>

    /**
     * Save or update a device.
     */
    suspend fun saveDevice(device: Device)

    /**
     * Update device monitoring status.
     */
    suspend fun setMonitored(deviceId: String, monitored: Boolean)

    /**
     * Update device ignore status.
     */
    suspend fun setIgnored(deviceId: String, ignored: Boolean)

    /**
     * Rename a device.
     */
    suspend fun renameDevice(deviceId: String, newName: String)

    /**
     * Set device alias.
     */
    suspend fun setDeviceAlias(deviceId: String, alias: String?)

    /**
     * Set device type.
     */
    suspend fun setDeviceType(deviceId: String, type: DeviceType)

    /**
     * Update device battery level and connection status.
     */
    suspend fun updateDeviceStatus(
        deviceId: String,
        batteryLevel: Int?,
        isCharging: Boolean?,
        isConnected: Boolean,
        lastSeen: Instant
    )

    /**
     * Update device capabilities.
     */
    suspend fun updateDeviceCapabilities(
        deviceId: String,
        capabilities: DeviceCapabilities
    )

    /**
     * Delete a device.
     */
    suspend fun deleteDevice(deviceId: String)

    /**
     * Check if a device exists.
     */
    suspend fun deviceExists(deviceId: String): Boolean
}
