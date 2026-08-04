package com.batteryguardian.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.batteryguardian.data.local.entity.DeviceCapabilityEntity
import com.batteryguardian.domain.model.BatteryReadMethod
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for DeviceCapability entities.
 */
@Dao
interface DeviceCapabilityDao {

    // ==================== Query Methods ====================

    @Query("SELECT * FROM device_capabilities WHERE deviceId = :deviceId")
    fun getByDevice(deviceId: String): Flow<DeviceCapabilityEntity?>

    @Query("SELECT * FROM device_capabilities")
    fun getAll(): Flow<List<DeviceCapabilityEntity>>

    @Query("SELECT * FROM device_capabilities WHERE supportsGattBattery = 1")
    fun getGattSupported(): Flow<List<DeviceCapabilityEntity>>

    @Query("SELECT * FROM device_capabilities WHERE supportsClassicBattery = 1")
    fun getClassicSupported(): Flow<List<DeviceCapabilityEntity>>

    @Query("SELECT * FROM device_capabilities WHERE preferredMethod = :method")
    fun getByPreferredMethod(method: BatteryReadMethod): Flow<List<DeviceCapabilityEntity>>

    @Query("SELECT deviceId FROM device_capabilities WHERE preferredMethod = :method")
    fun getDeviceIdsByPreferredMethod(method: BatteryReadMethod): Flow<List<String>>

    // ==================== Insert/Update Methods ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(capability: DeviceCapabilityEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(capabilities: List<DeviceCapabilityEntity>)

    @Update
    suspend fun update(capability: DeviceCapabilityEntity)

    @Query("UPDATE device_capabilities SET preferredMethod = :method WHERE deviceId = :deviceId")
    suspend fun updatePreferredMethod(deviceId: String, method: BatteryReadMethod)

    @Query("UPDATE device_capabilities SET lastDetected = :timestamp WHERE deviceId = :deviceId")
    suspend fun updateLastDetected(deviceId: String, timestamp: java.time.Instant)

    // ==================== Delete Methods ====================

    @Query("DELETE FROM device_capabilities WHERE deviceId = :deviceId")
    suspend fun delete(deviceId: String)

    @Query("DELETE FROM device_capabilities WHERE deviceId IN (:deviceIds)")
    suspend fun deleteByDevices(deviceIds: List<String>)

    @Query("DELETE FROM device_capabilities")
    suspend fun deleteAll()

    // ==================== Utility Methods ====================

    @Query("SELECT EXISTS(SELECT 1 FROM device_capabilities WHERE deviceId = :deviceId)")
    suspend fun exists(deviceId: String): Boolean

    @Query("SELECT preferredMethod FROM device_capabilities WHERE deviceId = :deviceId")
    suspend fun getPreferredMethod(deviceId: String): BatteryReadMethod?

    @Query("SELECT supportsGattBattery FROM device_capabilities WHERE deviceId = :deviceId")
    suspend fun supportsGatt(deviceId: String): Boolean

    @Query("SELECT supportsClassicBattery FROM device_capabilities WHERE deviceId = :deviceId")
    suspend fun supportsClassic(deviceId: String): Boolean
}
