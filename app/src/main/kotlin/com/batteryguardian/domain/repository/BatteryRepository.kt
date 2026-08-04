package com.batteryguardian.domain.repository

import com.batteryguardian.domain.model.BatteryHealth
import com.batteryguardian.domain.model.BatteryLevel
import com.batteryguardian.domain.model.BatteryPrediction
import com.batteryguardian.domain.model.BatteryReadingResult
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Repository interface for battery level operations.
 */
interface BatteryRepository {

    /**
     * Get battery history for a device.
     */
    fun getBatteryHistory(
        deviceId: String,
        startTime: Instant,
        endTime: Instant
    ): Flow<List<BatteryLevel>>

    /**
     * Get all battery levels for a device.
     */
    fun getAllBatteryLevels(deviceId: String): Flow<List<BatteryLevel>>

    /**
     * Get the latest battery level for a device.
     */
    fun getLatestBatteryLevel(deviceId: String): Flow<BatteryLevel?>

    /**
     * Get battery levels for all devices.
     */
    fun getAllBatteryLevels(): Flow<Map<String, BatteryLevel?>>

    /**
     * Add a new battery level reading.
     */
    suspend fun addBatteryLevel(level: BatteryLevel)

    /**
     * Add multiple battery level readings.
     */
    suspend fun addBatteryLevels(levels: List<BatteryLevel>)

    /**
     * Get battery health metrics for a device.
     */
    fun getBatteryHealth(deviceId: String): Flow<BatteryHealth?>

    /**
     * Get battery predictions for a device.
     */
    fun getPredictions(deviceId: String): Flow<List<BatteryPrediction>>

    /**
     * Get the average drain rate for a device.
     */
    suspend fun getAverageDrainRate(deviceId: String): Float?

    /**
     * Get battery predictions for all devices.
     */
    fun getAllPredictions(): Flow<Map<String, List<BatteryPrediction>>>

    /**
     * Clean up old battery level data.
     */
    suspend fun cleanupOldData(days: Int)

    /**
     * Get battery reading statistics.
     */
    suspend fun getStatistics(): BatteryStatistics
}

/**
 * Battery level data class.
 */
data class BatteryLevel(
    /** Unique identifier */
    val id: Long = 0,
    
    /** Device ID (Bluetooth MAC address) */
    val deviceId: String,
    
    /** Battery level (0-100) */
    val level: Int,
    
    /** Timestamp when the reading was taken */
    val timestamp: Instant,
    
    /** Whether this is a real reading or predicted */
    val isPredicted: Boolean = false
)

/**
 * Battery statistics.
 */
data class BatteryStatistics(
    /** Total number of battery readings */
    val totalReadings: Long,
    
    /** Number of devices monitored */
    val devicesMonitored: Int,
    
    /** Average battery level across all devices */
    val averageBatteryLevel: Float?,
    
    /** Lowest battery level across all devices */
    val lowestBatteryLevel: Int?,
    
    /** Highest battery level across all devices */
    val highestBatteryLevel: Int?
)
