package com.batteryguardian.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.batteryguardian.data.local.entity.BatteryHealthEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for BatteryHealth entities.
 */
@Dao
interface BatteryHealthDao {

    // ==================== Query Methods ====================

    @Query("SELECT * FROM battery_health WHERE deviceId = :deviceId")
    fun getByDevice(deviceId: String): Flow<BatteryHealthEntity?>

    @Query("SELECT * FROM battery_health")
    fun getAll(): Flow<List<BatteryHealthEntity>>

    @Query("SELECT * FROM battery_health WHERE averageDrainRate < :threshold")
    fun getByDrainRateLessThan(threshold: Float): Flow<List<BatteryHealthEntity>>

    @Query("SELECT * FROM battery_health WHERE capacityDegradation > :threshold")
    fun getByDegradationGreaterThan(threshold: Float): Flow<List<BatteryHealthEntity>>

    // ==================== Insert/Update Methods ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(health: BatteryHealthEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(healthList: List<BatteryHealthEntity>)

    @Update
    suspend fun update(health: BatteryHealthEntity)

    @Query("""
        UPDATE battery_health 
        SET averageDrainRate = :drainRate, 
            lastFullCharge = :lastFullCharge, 
            capacityDegradation = :degradation, 
            lastUpdated = :lastUpdated
        WHERE deviceId = :deviceId
    """)
    suspend fun updateHealth(
        deviceId: String,
        drainRate: Float,
        lastFullCharge: java.time.Instant?,
        degradation: Float?,
        lastUpdated: java.time.Instant
    )

    // ==================== Delete Methods ====================

    @Query("DELETE FROM battery_health WHERE deviceId = :deviceId")
    suspend fun delete(deviceId: String)

    @Query("DELETE FROM battery_health WHERE deviceId IN (:deviceIds)")
    suspend fun deleteByDevices(deviceIds: List<String>)

    @Query("DELETE FROM battery_health")
    suspend fun deleteAll()

    // ==================== Utility Methods ====================

    @Query("SELECT EXISTS(SELECT 1 FROM battery_health WHERE deviceId = :deviceId)")
    suspend fun exists(deviceId: String): Boolean
}
