package com.batteryguardian.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.batteryguardian.data.local.entity.AlertThresholdEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for AlertThreshold entities.
 */
@Dao
interface AlertThresholdDao {

    // ==================== Query Methods ====================

    @Query("SELECT * FROM alert_thresholds WHERE deviceId = :deviceId")
    fun getByDevice(deviceId: String): Flow<AlertThresholdEntity?>

    @Query("SELECT * FROM alert_thresholds")
    fun getAll(): Flow<List<AlertThresholdEntity>>

    @Query("SELECT * FROM alert_thresholds WHERE threshold20 = 1")
    fun getWith20Threshold(): Flow<List<AlertThresholdEntity>>

    @Query("SELECT * FROM alert_thresholds WHERE threshold10 = 1")
    fun getWith10Threshold(): Flow<List<AlertThresholdEntity>>

    @Query("SELECT * FROM alert_thresholds WHERE threshold5 = 1")
    fun getWith5Threshold(): Flow<List<AlertThresholdEntity>>

    // ==================== Insert/Update Methods ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(threshold: AlertThresholdEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(thresholds: List<AlertThresholdEntity>)

    @Update
    suspend fun update(threshold: AlertThresholdEntity)

    @Query("""
        UPDATE alert_thresholds 
        SET threshold20 = :threshold20, 
            threshold10 = :threshold10, 
            threshold5 = :threshold5, 
            customThresholds = :customThresholds
        WHERE deviceId = :deviceId
    """)
    suspend fun updateThresholds(
        deviceId: String,
        threshold20: Boolean,
        threshold10: Boolean,
        threshold5: Boolean,
        customThresholds: List<Int>?
    )

    // ==================== Delete Methods ====================

    @Query("DELETE FROM alert_thresholds WHERE deviceId = :deviceId")
    suspend fun delete(deviceId: String)

    @Query("DELETE FROM alert_thresholds WHERE deviceId IN (:deviceIds)")
    suspend fun deleteByDevices(deviceIds: List<String>)

    @Query("DELETE FROM alert_thresholds")
    suspend fun deleteAll()

    // ==================== Utility Methods ====================

    @Query("SELECT EXISTS(SELECT 1 FROM alert_thresholds WHERE deviceId = :deviceId)")
    suspend fun exists(deviceId: String): Boolean

    @Query("SELECT customThresholds FROM alert_thresholds WHERE deviceId = :deviceId")
    suspend fun getCustomThresholds(deviceId: String): List<Int>?
}
