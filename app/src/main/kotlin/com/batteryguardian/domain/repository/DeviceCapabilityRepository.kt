package com.batteryguardian.domain.repository

import com.batteryguardian.domain.model.BatteryReadMethod
import com.batteryguardian.domain.model.DeviceCapabilities
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Repository interface for device capability operations.
 */
interface DeviceCapabilityRepository {

    /**
     * Get capabilities for a specific device.
     */
    fun getCapabilities(deviceId: String): Flow<DeviceCapabilities?>

    /**
     * Get capabilities for all devices.
     */
    fun getAllCapabilities(): Flow<Map<String, DeviceCapabilities>>

    /**
     * Get devices by preferred reading method.
     */
    fun getDevicesByMethod(method: BatteryReadMethod): Flow<List<String>>

    /**
     * Save or update device capabilities.
     */
    suspend fun saveCapabilities(capabilities: DeviceCapabilities)

    /**
     * Update the preferred reading method for a device.
     */
    suspend fun updatePreferredMethod(
        deviceId: String,
        method: BatteryReadMethod
    )

    /**
     * Check if a device supports GATT battery reading.
     */
    suspend fun supportsGatt(deviceId: String): Boolean

    /**
     * Check if a device supports Classic Bluetooth battery reading.
     */
    suspend fun supportsClassic(deviceId: String): Boolean

    /**
     * Get the preferred reading method for a device.
     */
    suspend fun getPreferredMethod(deviceId: String): BatteryReadMethod

    /**
     * Delete capabilities for a device.
     */
    suspend fun deleteCapabilities(deviceId: String)

    /**
     * Clear all capabilities.
     */
    suspend fun clearAllCapabilities()

    /**
     * Update the last detected timestamp for a device.
     */
    suspend fun updateLastDetected(deviceId: String, timestamp: Instant)
}
