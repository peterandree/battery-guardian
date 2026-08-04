package com.batteryguardian.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.batteryguardian.data.local.entity.BatteryLevelEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Data Access Object for BatteryLevel entities.
 */
@Dao
interface BatteryLevelDao {

    // ==================== Query Methods ====================

    @Query("SELECT * FROM battery_levels WHERE deviceId = :deviceId ORDER BY timestamp DESC")
    fun getByDevice(deviceId: String): Flow<List<BatteryLevelEntity>>

    @Query("SELECT * FROM battery_levels WHERE deviceId = :deviceId AND timestamp >= :startTime ORDER BY timestamp")
    fun getByDeviceSince(deviceId: String, startTime: Instant): Flow<List<BatteryLevelEntity>>

    @Query("SELECT * FROM battery_levels WHERE deviceId = :deviceId AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp")
    fun getByDeviceInRange(
        deviceId: String,
        startTime: Instant,
        endTime: Instant
    ): Flow<List<BatteryLevelEntity>>

    @Query("SELECT * FROM battery_levels WHERE timestamp >= :startTime ORDER BY timestamp")
    fun getSince(startTime: Instant): Flow<List<BatteryLevelEntity>>

    @Query("SELECT * FROM battery_levels ORDER BY timestamp DESC LIMIT 1")
    fun getLatest(): Flow<BatteryLevelEntity?>

    @Query("SELECT * FROM battery_levels WHERE deviceId = :deviceId ORDER BY timestamp DESC LIMIT 1")
    fun getLatestByDevice(deviceId: String): Flow<BatteryLevelEntity?>

    @Query("SELECT AVG(level) FROM battery_levels WHERE deviceId = :deviceId AND timestamp >= :startTime")
    suspend fun getAverageLevel(deviceId: String, startTime: Instant): Double?

    @Query("SELECT MIN(level) FROM battery_levels WHERE deviceId = :deviceId AND timestamp >= :startTime")
    suspend fun getMinLevel(deviceId: String, startTime: Instant): Int?

    @Query("SELECT MAX(level) FROM battery_levels WHERE deviceId = :deviceId AND timestamp >= :startTime")
    suspend fun getMaxLevel(deviceId: String, startTime: Instant): Int?

    @Query("SELECT COUNT(*) FROM battery_levels WHERE deviceId = :deviceId")
    suspend fun getCountByDevice(deviceId: String): Long

    @Query("SELECT COUNT(*) FROM battery_levels")
    suspend fun getTotalCount(): Long

    // ==================== Insert Methods ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(level: BatteryLevelEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(levels: List<BatteryLevelEntity>)

    // ==================== Delete Methods ====================

    @Query("DELETE FROM battery_levels WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM battery_levels WHERE deviceId = :deviceId")
    suspend fun deleteByDevice(deviceId: String)

    @Query("DELETE FROM battery_levels WHERE deviceId IN (:deviceIds)")
    suspend fun deleteByDevices(deviceIds: List<String>)

    @Query("DELETE FROM battery_levels WHERE timestamp < :cutoff")
    suspend fun deleteOlderThan(cutoff: Instant)

    @Query("DELETE FROM battery_levels")
    suspend fun deleteAll()

    // ==================== Utility Methods ====================

    @Query("SELECT EXISTS(SELECT 1 FROM battery_levels WHERE deviceId = :deviceId)")
    suspend fun existsForDevice(deviceId: String): Boolean

    @Query("SELECT timestamp FROM battery_levels WHERE deviceId = :deviceId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastTimestamp(deviceId: String): Instant?

    @Query("SELECT timestamp FROM battery_levels WHERE deviceId = :deviceId ORDER BY timestamp ASC LIMIT 1")
    suspend fun getFirstTimestamp(deviceId: String): Instant?
}
