package com.batteryguardian.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.batteryguardian.data.local.entity.DeviceEntity
import com.batteryguardian.domain.model.DeviceType
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Data Access Object for Device entities.
 */
@Dao
interface DeviceDao {

    // ==================== Query Methods ====================

    @Query("SELECT * FROM devices")
    fun getAll(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE id = :id")
    fun getById(id: String): Flow<DeviceEntity?>

    @Query("SELECT * FROM devices WHERE isMonitored = 1")
    fun getMonitoredDevices(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE isIgnored = 1")
    fun getIgnoredDevices(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE type = :type")
    fun getByType(type: DeviceType): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE id IN (:ids)")
    fun getByIds(ids: List<String>): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE name LIKE :query OR alias LIKE :query")
    fun searchByName(query: String): Flow<List<DeviceEntity>>

    @Query("SELECT COUNT(*) FROM devices")
    fun getCount(): Flow<Int>

    // ==================== Insert/Update Methods ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(device: DeviceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(devices: List<DeviceEntity>)

    @Update
    suspend fun update(device: DeviceEntity)

    @Query("UPDATE devices SET isMonitored = :monitored WHERE id = :id")
    suspend fun setMonitored(id: String, monitored: Boolean)

    @Query("UPDATE devices SET isIgnored = :ignored WHERE id = :id")
    suspend fun setIgnored(id: String, ignored: Boolean)

    @Query("UPDATE devices SET name = :name WHERE id = :id")
    suspend fun setName(id: String, name: String)

    @Query("UPDATE devices SET alias = :alias WHERE id = :id")
    suspend fun setAlias(id: String, alias: String?)

    @Query("UPDATE devices SET type = :type WHERE id = :id")
    suspend fun setType(id: String, type: DeviceType)

    @Query("""
        UPDATE devices 
        SET currentBatteryLevel = :batteryLevel, 
            isCharging = :isCharging, 
            isConnected = :isConnected, 
            lastSeen = :lastSeen
        WHERE id = :id
    """)
    suspend fun updateStatus(
        id: String,
        batteryLevel: Int?,
        isCharging: Boolean?,
        isConnected: Boolean,
        lastSeen: Instant
    )

    // ==================== Delete Methods ====================

    @Query("DELETE FROM devices WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM devices WHERE id IN (:ids)")
    suspend fun deleteAll(ids: List<String>)

    @Query("DELETE FROM devices")
    suspend fun deleteAll()

    // ==================== Utility Methods ====================

    @Query("SELECT EXISTS(SELECT 1 FROM devices WHERE id = :id)")
    suspend fun exists(id: String): Boolean

    @Query("SELECT lastSeen FROM devices WHERE id = :id")
    suspend fun getLastSeen(id: String): Instant?
}
