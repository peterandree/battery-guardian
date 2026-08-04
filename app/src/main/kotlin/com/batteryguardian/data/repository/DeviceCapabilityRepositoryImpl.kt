package com.batteryguardian.data.repository

import com.batteryguardian.data.local.dao.DeviceCapabilityDao
import com.batteryguardian.data.local.entity.DeviceCapabilityEntity
import com.batteryguardian.domain.model.BatteryReadMethod
import com.batteryguardian.domain.model.DeviceCapabilities
import com.batteryguardian.domain.repository.DeviceCapabilityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

/**
 * Implementation of DeviceCapabilityRepository.
 */
class DeviceCapabilityRepositoryImpl @Inject constructor(
    private val deviceCapabilityDao: DeviceCapabilityDao
) : DeviceCapabilityRepository {

    override fun getCapabilities(deviceId: String): Flow<DeviceCapabilities?> {
        return deviceCapabilityDao.getByDevice(deviceId)
            .map { entity ->
                entity?.let { mapToDomain(it) }
            }
    }

    override fun getAllCapabilities(): Flow<Map<String, DeviceCapabilities>> {
        return deviceCapabilityDao.getAll()
            .map { entities ->
                entities.associateBy({ it.deviceId }, { mapToDomain(it) })
            }
    }

    override fun getDevicesByMethod(method: BatteryReadMethod): Flow<List<String>> {
        return deviceCapabilityDao.getDeviceIdsByPreferredMethod(method)
    }

    override suspend fun saveCapabilities(capabilities: DeviceCapabilities) {
        val entity = mapToEntity(capabilities)
        deviceCapabilityDao.insert(entity)
    }

    override suspend fun updatePreferredMethod(
        deviceId: String,
        method: BatteryReadMethod
    ) {
        deviceCapabilityDao.updatePreferredMethod(deviceId, method)
    }

    override suspend fun supportsGatt(deviceId: String): Boolean {
        return deviceCapabilityDao.supportsGatt(deviceId) ?: false
    }

    override suspend fun supportsClassic(deviceId: String): Boolean {
        return deviceCapabilityDao.supportsClassic(deviceId) ?: false
    }

    override suspend fun getPreferredMethod(deviceId: String): BatteryReadMethod {
        return deviceCapabilityDao.getPreferredMethod(deviceId) ?: BatteryReadMethod.NONE
    }

    override suspend fun deleteCapabilities(deviceId: String) {
        deviceCapabilityDao.delete(deviceId)
    }

    override suspend fun clearAllCapabilities() {
        deviceCapabilityDao.deleteAll()
    }

    override suspend fun updateLastDetected(deviceId: String, timestamp: Instant) {
        deviceCapabilityDao.updateLastDetected(deviceId, timestamp)
    }

    /**
     * Map DeviceCapabilityEntity to DeviceCapabilities domain model.
     */
    private fun mapToDomain(entity: DeviceCapabilityEntity): DeviceCapabilities {
        return DeviceCapabilities(
            deviceId = entity.deviceId,
            supportsGattBattery = entity.supportsGattBattery,
            supportsClassicBattery = entity.supportsClassicBattery,
            preferredMethod = entity.preferredMethod,
            lastDetected = entity.lastDetected
        )
    }

    /**
     * Map DeviceCapabilities domain model to DeviceCapabilityEntity.
     */
    private fun mapToEntity(capabilities: DeviceCapabilities): DeviceCapabilityEntity {
        return DeviceCapabilityEntity(
            deviceId = capabilities.deviceId,
            supportsGattBattery = capabilities.supportsGattBattery,
            supportsClassicBattery = capabilities.supportsClassicBattery,
            preferredMethod = capabilities.preferredMethod,
            lastDetected = capabilities.lastDetected
        )
    }
}
