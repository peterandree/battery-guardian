package com.batteryguardian.domain.usecase

import com.batteryguardian.domain.model.BatteryLevel
import com.batteryguardian.domain.repository.BatteryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

/**
 * Use case for retrieving battery history for a device.
 */
class GetDeviceHistoryUseCase @Inject constructor(
    private val batteryRepository: BatteryRepository
) {

    /**
     * Get battery history for a device over a specific time range.
     */
    fun getBatteryHistory(
        deviceId: String,
        timeRange: TimeRange = TimeRange.LAST_24_HOURS
    ): Flow<List<BatteryLevel>> {
        val (startTime, endTime) = when (timeRange) {
            TimeRange.LAST_24_HOURS -> {
                Instant.now().minus(Duration.ofHours(24)) to Instant.now()
            }
            TimeRange.LAST_7_DAYS -> {
                Instant.now().minus(Duration.ofDays(7)) to Instant.now()
            }
            TimeRange.LAST_30_DAYS -> {
                Instant.now().minus(Duration.ofDays(30)) to Instant.now()
            }
            TimeRange.ALL_TIME -> {
                Instant.EPOCH to Instant.now()
            }
        }

        return batteryRepository.getBatteryHistory(deviceId, startTime, endTime)
            .map { levels ->
                levels.sortedBy { it.timestamp }
            }
    }

    /**
     * Get all battery levels for a device.
     */
    fun getAllBatteryLevels(deviceId: String): Flow<List<BatteryLevel>> {
        return batteryRepository.getAllBatteryLevels(deviceId)
    }

    /**
     * Get battery statistics for a device.
     */
    suspend fun getBatteryStatistics(deviceId: String): BatteryStatistics {
        return batteryRepository.getStatistics()
    }

    /**
     * Get the latest battery level for a device.
     */
    fun getLatestBatteryLevel(deviceId: String): Flow<BatteryLevel?> {
        return batteryRepository.getLatestBatteryLevel(deviceId)
    }
}

/**
 * Time range for battery history.
 */
enum class TimeRange {
    LAST_24_HOURS,
    LAST_7_DAYS,
    LAST_30_DAYS,
    ALL_TIME
}

/**
 * Battery statistics for a device.
 */
data class BatteryStatistics(
    val minLevel: Int?,
    val maxLevel: Int?,
    val avgLevel: Float?,
    val totalReadings: Int,
    val firstReading: Instant?,
    val lastReading: Instant?
)
