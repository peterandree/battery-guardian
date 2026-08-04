package com.batteryguardian.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.batteryguardian.data.local.entity.AlertStateEntity
import com.batteryguardian.domain.model.AlertState
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for AlertState entities.
 */
@Dao
interface AlertStateDao {

    // ==================== Query Methods ====================

    @Query("SELECT * FROM alert_states WHERE deviceId = :deviceId")
    fun getByDevice(deviceId: String): Flow<AlertStateEntity?>

    @Query("SELECT * FROM alert_states")
    fun getAll(): Flow<List<AlertStateEntity>>

    @Query("SELECT deviceId FROM alert_states WHERE currentState != 'NORMAL'")
    fun getDevicesInAlert(): Flow<List<String>>

    @Query("SELECT * FROM alert_states WHERE currentState = 'NORMAL'")
    fun getNormalStates(): Flow<List<AlertStateEntity>>

    @Query("SELECT * FROM alert_states WHERE currentState LIKE 'LOW:%'")
    fun getLowStates(): Flow<List<AlertStateEntity>>

    @Query("SELECT * FROM alert_states WHERE currentState LIKE 'HIGH:%'")
    fun getHighStates(): Flow<List<AlertStateEntity>>

    // ==================== Insert/Update Methods ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(state: AlertStateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(states: List<AlertStateEntity>)

    @Update
    suspend fun update(state: AlertStateEntity)

    @Query("UPDATE alert_states SET currentState = :state WHERE deviceId = :deviceId")
    suspend fun updateState(deviceId: String, state: String)

    @Query("""
        UPDATE alert_states 
        SET currentState = :state, 
            lastAlertThreshold = :threshold, 
            lastAlertTimestamp = :timestamp
        WHERE deviceId = :deviceId
    """)
    suspend fun updateFullState(
        deviceId: String,
        state: String,
        threshold: Int?,
        timestamp: java.time.Instant?
    )

    // ==================== Delete Methods ====================

    @Query("DELETE FROM alert_states WHERE deviceId = :deviceId")
    suspend fun delete(deviceId: String)

    @Query("DELETE FROM alert_states WHERE deviceId IN (:deviceIds)")
    suspend fun deleteByDevices(deviceIds: List<String>)

    @Query("DELETE FROM alert_states")
    suspend fun deleteAll()

    // ==================== Utility Methods ====================

    @Query("SELECT EXISTS(SELECT 1 FROM alert_states WHERE deviceId = :deviceId)")
    suspend fun exists(deviceId: String): Boolean

    @Query("SELECT currentState FROM alert_states WHERE deviceId = :deviceId")
    suspend fun getState(deviceId: String): String?

    @Query("SELECT currentState FROM alert_states WHERE deviceId = :deviceId")
    fun getStateFlow(deviceId: String): Flow<String?>
}
