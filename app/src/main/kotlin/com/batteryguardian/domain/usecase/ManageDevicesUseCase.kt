package com.batteryguardian.domain.usecase

import com.batteryguardian.domain.model.Device
import com.batteryguardian.domain.model.DeviceCapabilities
import com.batteryguardian.domain.model.DeviceType
import com.batteryguardian.domain.repository.DeviceCapabilityRepository
import com.batteryguardian.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject

/**
 * Use case for managing devices.
 */
class ManageDevicesUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val capabilityRepository: DeviceCapabilityRepository
) {

    /**
     * Get all devices.
     */
    fun getAllDevices(): Flow<List<Device>> {
        return deviceRepository.getAllDevices()
    }

    /**
     * Get monitored devices.
     */
    fun getMonitoredDevices(): Flow<List<Device>> {
        return deviceRepository.getMonitoredDevices()
    }

    /**
     * Get ignored devices.
     */
    fun getIgnoredDevices(): Flow<List<Device>> {
        return deviceRepository.getIgnoredDevices()
    }

    /**
     * Add a new device.
     */
    suspend fun addDevice(
        id: String,
        name: String,
        type: DeviceType = DeviceType.OTHER,
        manufacturer: String? = null,
        bluetoothClass: Int? = null
    ) {
        val device = Device(
            id = id,
            name = name,
            alias = null,
            type = type,
            manufacturer = manufacturer,
            bluetoothClass = bluetoothClass,
            lastSeen = Instant.now(),
            currentBatteryLevel = null,
            isCharging = null,
            isConnected = false,
            isMonitored = true,
            isIgnored = false,
            batteryHealth = null,
            alertState = com.batteryguardian.domain.model.AlertState.Normal,
            capabilities = null
        )
        deviceRepository.saveDevice(device)
    }

    /**
     * Update a device.
     */
    suspend fun updateDevice(device: Device) {
        deviceRepository.saveDevice(device)
    }

    /**
     * Toggle monitoring for a device.
     */
    suspend fun toggleMonitoring(deviceId: String) {
        val device = deviceRepository.getDevice(deviceId).value ?: return
        deviceRepository.setMonitored(deviceId, !device.isMonitored)
    }

    /**
     * Toggle ignore status for a device.
     */
    suspend fun toggleIgnore(deviceId: String) {
        val device = deviceRepository.getDevice(deviceId).value ?: return
        deviceRepository.setIgnored(deviceId, !device.isIgnored)
    }

    /**
     * Rename a device.
     */
    suspend fun renameDevice(deviceId: String, newName: String) {
        deviceRepository.renameDevice(deviceId, newName)
    }

    /**
     * Set device alias.
     */
    suspend fun setDeviceAlias(deviceId: String, alias: String?) {
        deviceRepository.setDeviceAlias(deviceId, alias)
    }

    /**
     * Set device type.
     */
    suspend fun setDeviceType(deviceId: String, type: DeviceType) {
        deviceRepository.setDeviceType(deviceId, type)
    }

    /**
     * Delete a device.
     */
    suspend fun deleteDevice(deviceId: String) {
        deviceRepository.deleteDevice(deviceId)
        capabilityRepository.deleteCapabilities(deviceId)
    }

    /**
     * Get device capabilities.
     */
    fun getDeviceCapabilities(deviceId: String): Flow<DeviceCapabilities?> {
        return capabilityRepository.getCapabilities(deviceId)
    }

    /**
     * Update device capabilities.
     */
    suspend fun updateDeviceCapabilities(
        deviceId: String,
        capabilities: DeviceCapabilities
    ) {
        capabilityRepository.saveCapabilities(capabilities)
    }

    /**
     * Get the preferred reading method for a device.
     */
    suspend fun getPreferredReadMethod(deviceId: String): com.batteryguardian.domain.model.BatteryReadMethod {
        return capabilityRepository.getPreferredMethod(deviceId)
    }

    /**
     * Check if a device exists.
     */
    suspend fun deviceExists(deviceId: String): Boolean {
        return deviceRepository.deviceExists(deviceId)
    }

    /**
     * Get device by ID.
     */
    fun getDevice(deviceId: String): Flow<Device?> {
        return deviceRepository.getDevice(deviceId)
    }
}
