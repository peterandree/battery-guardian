package com.batteryguardian.data.repository

import com.batteryguardian.data.local.dao.AlertStateDao
import com.batteryguardian.data.local.dao.AlertThresholdDao
import com.batteryguardian.data.local.dao.BatteryHealthDao
import com.batteryguardian.data.local.dao.BatteryLevelDao
import com.batteryguardian.data.local.dao.DeviceDao
import com.batteryguardian.data.local.entity.DeviceEntity
import com.batteryguardian.domain.model.AlertState
import com.batteryguardian.domain.model.BatteryHealth
import com.batteryguardian.domain.model.Device
import com.batteryguardian.domain.model.DeviceCapabilities
import com.batteryguardian.domain.model.DeviceType
import com.batteryguardian.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

/**
 * Implementation of DeviceRepository.
 */
class DeviceRepositoryImpl @Inject constructor(
    private val deviceDao: DeviceDao,
    private val batteryLevelDao: BatteryLevelDao,
    private val batteryHealthDao: BatteryHealthDao,
    private val alertStateDao: AlertStateDao,
    private val alertThresholdDao: AlertThresholdDao
) : DeviceRepository {

    override fun getAllDevices(): Flow<List<Device>> {
        return deviceDao.getAll()
            .combine(alertStateDao.getAll()) { devices, alertStates ->
                devices.map { entity ->
                    mapToDomain(entity, alertStates.find { it.deviceId == entity.id })
                }
            }
    }

    override fun getDevice(deviceId: String): Flow<Device?> {
        return deviceDao.getById(deviceId)
            .combine(alertStateDao.getByDevice(deviceId)) { device, alertState ->
                device?.let { mapToDomain(it, alertState) }
            }
    }

    override fun getMonitoredDevices(): Flow<List<Device>> {
        return deviceDao.getMonitoredDevices()
            .combine(alertStateDao.getAll()) { devices, alertStates ->
                devices.map { entity ->
                    mapToDomain(entity, alertStates.find { it.deviceId == entity.id })
                }
            }
    }

    override fun getIgnoredDevices(): Flow<List<Device>> {
        return deviceDao.getIgnoredDevices()
            .combine(alertStateDao.getAll()) { devices, alertStates ->
                devices.map { entity ->
                    mapToDomain(entity, alertStates.find { it.deviceId == entity.id })
                }
            }
    }

    override fun getDevicesByType(type: DeviceType): Flow<List<Device>> {
        return deviceDao.getByType(type)
            .combine(alertStateDao.getAll()) { devices, alertStates ->
                devices.map { entity ->
                    mapToDomain(entity, alertStates.find { it.deviceId == entity.id })
                }
            }
    }

    override fun getDevicesWithLowBattery(lowThreshold: Int): Flow<List<Device>> {
        return deviceDao.getMonitoredDevices()
            .combine(alertStateDao.getAll()) { devices, alertStates ->
                devices.filter { device ->
                    device.currentBatteryLevel != null && 
                    device.currentBatteryLevel!! <= lowThreshold
                }.map { entity ->
                    mapToDomain(entity, alertStates.find { it.deviceId == entity.id })
                }
            }
    }

    override fun getDevicesNeedingAlerts(
        lowThreshold: Int,
        mediumThreshold: Int,
        criticalThreshold: Int
    ): Flow<List<Device>> {
        val thresholds = listOf(criticalThreshold, mediumThreshold, lowThreshold).sorted()
        return deviceDao.getMonitoredDevices()
            .combine(alertStateDao.getAll()) { devices, alertStates ->
                devices.filter { device ->
                    device.currentBatteryLevel != null && 
                    thresholds.any { threshold -> device.currentBatteryLevel!! <= threshold }
                }.map { entity ->
                    mapToDomain(entity, alertStates.find { it.deviceId == entity.id })
                }
            }
    }

    override suspend fun saveDevice(device: Device) {
        val entity = mapToEntity(device)
        deviceDao.insert(entity)
    }

    override suspend fun setMonitored(deviceId: String, monitored: Boolean) {
        deviceDao.setMonitored(deviceId, monitored)
    }

    override suspend fun setIgnored(deviceId: String, ignored: Boolean) {
        deviceDao.setIgnored(deviceId, ignored)
    }

    override suspend fun renameDevice(deviceId: String, newName: String) {
        deviceDao.setName(deviceId, newName)
    }

    override suspend fun setDeviceAlias(deviceId: String, alias: String?) {
        deviceDao.setAlias(deviceId, alias)
    }

    override suspend fun setDeviceType(deviceId: String, type: DeviceType) {
        deviceDao.setType(deviceId, type)
    }

    override suspend fun updateDeviceStatus(
        deviceId: String,
        batteryLevel: Int?,
        isCharging: Boolean?,
        isConnected: Boolean,
        lastSeen: Instant
    ) {
        deviceDao.updateStatus(
            id = deviceId,
            batteryLevel = batteryLevel,
            isCharging = isCharging,
            isConnected = isConnected,
            lastSeen = lastSeen
        )
    }

    override suspend fun updateDeviceCapabilities(
        deviceId: String,
        capabilities: DeviceCapabilities
    ) {
        // This would update capabilities in a separate table
        // Implementation depends on how capabilities are stored
    }

    override suspend fun deleteDevice(deviceId: String) {
        deviceDao.delete(deviceId)
    }

    override suspend fun deviceExists(deviceId: String): Boolean {
        return deviceDao.exists(deviceId)
    }

    /**
     * Map DeviceEntity to Device domain model.
     */
    private fun mapToDomain(
        entity: DeviceEntity,
        alertStateEntity: AlertStateEntity?
    ): Device {
        val alertState = alertStateEntity?.let { mapToAlertState(it) } ?: AlertState.Normal
        
        return Device(
            id = entity.id,
            name = entity.name,
            alias = entity.alias,
            type = entity.type,
            manufacturer = entity.manufacturer,
            bluetoothClass = entity.bluetoothClass,
            lastSeen = entity.lastSeen,
            currentBatteryLevel = entity.currentBatteryLevel,
            isCharging = entity.isCharging,
            isConnected = entity.isConnected,
            isMonitored = entity.isMonitored,
            isIgnored = entity.isIgnored,
            batteryHealth = null, // Would be fetched from BatteryHealthDao
            alertState = alertState,
            capabilities = null // Would be fetched from DeviceCapabilityDao
        )
    }

    /**
     * Map Device domain model to DeviceEntity.
     */
    private fun mapToEntity(device: Device): DeviceEntity {
        return DeviceEntity(
            id = device.id,
            name = device.name,
            alias = device.alias,
            type = device.type,
            manufacturer = device.manufacturer,
            bluetoothClass = device.bluetoothClass,
            lastSeen = device.lastSeen,
            currentBatteryLevel = device.currentBatteryLevel,
            isCharging = device.isCharging,
            isConnected = device.isConnected,
            isMonitored = device.isMonitored,
            isIgnored = device.isIgnored
        )
    }

    /**
     * Map AlertStateEntity to AlertState domain model.
     */
    private fun mapToAlertState(entity: AlertStateEntity): AlertState {
        return when {
            entity.currentState.startsWith("LOW:") -> {
                val threshold = entity.currentState.substringAfter(":").toInt()
                AlertState.Low(threshold, entity.lastAlertTimestamp ?: Instant.now())
            }
            entity.currentState.startsWith("HIGH:") -> {
                val threshold = entity.currentState.substringAfter(":").toInt()
                AlertState.High(threshold, entity.lastAlertTimestamp ?: Instant.now())
            }
            else -> AlertState.Normal
        }
    }
}
