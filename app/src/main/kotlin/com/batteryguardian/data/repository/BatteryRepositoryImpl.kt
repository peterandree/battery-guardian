package com.batteryguardian.data.repository

import com.batteryguardian.data.local.dao.BatteryHealthDao
import com.batteryguardian.data.local.dao.BatteryLevelDao
import com.batteryguardian.data.local.entity.BatteryHealthEntity
import com.batteryguardian.data.local.entity.BatteryLevelEntity
import com.batteryguardian.domain.model.BatteryHealth
import com.batteryguardian.domain.model.BatteryPrediction
import com.batteryguardian.domain.repository.BatteryLevel
import com.batteryguardian.domain.repository.BatteryRepository
import com.batteryguardian.domain.repository.BatteryStatistics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

/**
 * Implementation of BatteryRepository.
 */
class BatteryRepositoryImpl @Inject constructor(
    private val batteryLevelDao: BatteryLevelDao,
    private val batteryHealthDao: BatteryHealthDao
) : BatteryRepository {

    override fun getBatteryHistory(
        deviceId: String,
        startTime: Instant,
        endTime: Instant
    ): Flow<List<BatteryLevel>> {
        return batteryLevelDao.getByDeviceInRange(deviceId, startTime, endTime)
            .map { entities ->
                entities.map { entity ->
                    BatteryLevel(
                        id = entity.id,
                        deviceId = entity.deviceId,
                        level = entity.level,
                        timestamp = entity.timestamp,
                        isPredicted = entity.isPredicted
                    )
                }
            }
    }

    override fun getAllBatteryLevels(deviceId: String): Flow<List<BatteryLevel>> {
        return batteryLevelDao.getByDevice(deviceId)
            .map { entities ->
                entities.map { entity ->
                    BatteryLevel(
                        id = entity.id,
                        deviceId = entity.deviceId,
                        level = entity.level,
                        timestamp = entity.timestamp,
                        isPredicted = entity.isPredicted
                    )
                }
            }
    }

    override fun getLatestBatteryLevel(deviceId: String): Flow<BatteryLevel?> {
        return batteryLevelDao.getLatestByDevice(deviceId)
            .map { entity ->
                entity?.let {
                    BatteryLevel(
                        id = it.id,
                        deviceId = it.deviceId,
                        level = it.level,
                        timestamp = it.timestamp,
                        isPredicted = it.isPredicted
                    )
                }
            }
    }

    override fun getAllBatteryLevels(): Flow<Map<String, BatteryLevel?>> {
        return batteryLevelDao.getAll()
            .map { entities ->
                entities.groupBy { it.deviceId }
                    .mapValues { (_, levels) ->
                        levels.maxByOrNull { it.timestamp }?.let {
                            BatteryLevel(
                                id = it.id,
                                deviceId = it.deviceId,
                                level = it.level,
                                timestamp = it.timestamp,
                                isPredicted = it.isPredicted
                            )
                        }
                    }
            }
    }

    override suspend fun addBatteryLevel(level: BatteryLevel) {
        val entity = BatteryLevelEntity(
            id = level.id,
            deviceId = level.deviceId,
            level = level.level,
            timestamp = level.timestamp,
            isPredicted = level.isPredicted
        )
        batteryLevelDao.insert(entity)
    }

    override suspend fun addBatteryLevels(levels: List<BatteryLevel>) {
        val entities = levels.map { level ->
            BatteryLevelEntity(
                id = level.id,
                deviceId = level.deviceId,
                level = level.level,
                timestamp = level.timestamp,
                isPredicted = level.isPredicted
            )
        }
        batteryLevelDao.insertAll(entities)
    }

    override fun getBatteryHealth(deviceId: String): Flow<BatteryHealth?> {
        return batteryHealthDao.getByDevice(deviceId)
            .map { entity ->
                entity?.let { mapToDomain(it) }
            }
    }

    override fun getPredictions(deviceId: String): Flow<List<BatteryPrediction>> {
        // In a real implementation, this would calculate predictions
        // from battery history
        return batteryLevelDao.getByDevice(deviceId)
            .map { entities ->
                // Calculate predictions from history
                calculatePredictions(entities)
            }
    }

    override suspend fun getAverageDrainRate(deviceId: String): Float? {
        return batteryHealthDao.getByDevice(deviceId).value?.averageDrainRate
    }

    override fun getAllPredictions(): Flow<Map<String, List<BatteryPrediction>>> {
        return batteryLevelDao.getAll()
            .map { entities ->
                entities.groupBy { it.deviceId }
                    .mapValues { (_, levels) ->
                        calculatePredictions(levels)
                    }
            }
    }

    override suspend fun cleanupOldData(days: Int) {
        val cutoff = Instant.now().minus(Duration.ofDays(days.toLong()))
        batteryLevelDao.deleteOlderThan(cutoff)
    }

    override suspend fun getStatistics(): BatteryStatistics {
        val allLevels = batteryLevelDao.getAll().value
        
        return BatteryStatistics(
            totalReadings = allLevels.size.toLong(),
            devicesMonitored = allLevels.map { it.deviceId }.toSet().size,
            averageBatteryLevel = allLevels.map { it.level.toFloat() }.average().takeIf { allLevels.isNotEmpty() },
            lowestBatteryLevel = allLevels.minOfOrNull { it.level },
            highestBatteryLevel = allLevels.maxOfOrNull { it.level }
        )
    }

    /**
     * Map BatteryHealthEntity to BatteryHealth domain model.
     */
    private fun mapToDomain(entity: BatteryHealthEntity): BatteryHealth {
        return BatteryHealth(
            averageDrainRate = entity.averageDrainRate,
            predictedTimeTo20 = entity.lastFullCharge?.plus(
                Duration.ofHours((20f / entity.averageDrainRate * -1).toLong())
            ),
            predictedTimeTo10 = entity.lastFullCharge?.plus(
                Duration.ofHours((10f / entity.averageDrainRate * -1).toLong())
            ),
            predictedTimeTo5 = entity.lastFullCharge?.plus(
                Duration.ofHours((5f / entity.averageDrainRate * -1).toLong())
            ),
            capacityDegradation = entity.capacityDegradation,
            lastFullCharge = entity.lastFullCharge,
            lastUpdated = entity.lastUpdated
        )
    }

    /**
     * Calculate battery predictions from battery level history.
     */
    private fun calculatePredictions(
        levels: List<BatteryLevelEntity>
    ): List<BatteryPrediction> {
        if (levels.size < 3) {
            return emptyList()
        }

        // Sort by timestamp
        val sortedLevels = levels.sortedBy { it.timestamp }

        // Calculate drain rate using linear regression
        val points = sortedLevels.map { level ->
            val hours = level.timestamp.epochSecond.toFloat() / 3600f
            Point(hours, level.level.toFloat())
        }

        val (slope, intercept) = calculateRegression(points)

        // Calculate predictions for standard milestones
        val milestones = listOf(20, 10, 5)
        val predictions = mutableListOf<BatteryPrediction>()

        val currentEpochHours = Instant.now().epochSecond.toFloat() / 3600f

        milestones.forEach { milestone ->
            if (slope < 0) { // Only predict if discharging
                val hoursToMilestone = (milestone - intercept) / slope
                val estimatedEpochHours = currentEpochHours + hoursToMilestone
                val estimatedTime = Instant.ofEpochSecond(
                    (estimatedEpochHours * 3600).toLong()
                )
                
                // Simple confidence calculation
                val confidence = minOf(
                    points.size.toFloat() / 20,
                    1.0f
                )
                
                predictions.add(
                    BatteryPrediction(
                        milestone = milestone,
                        estimatedTime = estimatedTime,
                        confidence = confidence
                    )
                )
            }
        }

        return predictions
    }

    /**
     * Calculate linear regression parameters.
     */
    private fun calculateRegression(points: List<Point>): Pair<Float, Float> {
        val n = points.size
        val sumX = points.sumOf { it.x }
        val sumY = points.sumOf { it.y }
        val sumXY = points.sumOf { it.x * it.y }
        val sumX2 = points.sumOf { it.x * it.x }

        val slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX)
        val intercept = (sumY - slope * sumX) / n

        return Pair(slope, intercept)
    }

    /**
     * Point for regression calculation.
     */
    private data class Point(val x: Float, val y: Float)
}
