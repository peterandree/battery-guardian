package com.batteryguardian.domain.usecase

import com.batteryguardian.domain.model.BatteryHealth
import com.batteryguardian.domain.model.BatteryPrediction
import com.batteryguardian.domain.model.Device
import com.batteryguardian.domain.repository.BatteryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

/**
 * Use case for predicting battery levels using linear regression.
 */
class PredictBatteryUseCase @Inject constructor(
    private val batteryRepository: BatteryRepository
) {

    /**
     * Get battery predictions for a device.
     */
    fun getPredictions(deviceId: String): Flow<List<BatteryPrediction>> {
        return batteryRepository.getPredictions(deviceId)
    }

    /**
     * Get battery health for a device.
     */
    fun getBatteryHealth(deviceId: String): Flow<BatteryHealth?> {
        return batteryRepository.getBatteryHealth(deviceId)
    }

    /**
     * Predict when a device will reach a specific battery milestone.
     */
    suspend fun predictTimeToMilestone(
        deviceId: String,
        milestone: Int
    ): BatteryPrediction? {
        // Get historical battery data
        val history = batteryRepository.getBatteryHistory(
            deviceId = deviceId,
            startTime = Instant.now().minus(Duration.ofDays(7)),
            endTime = Instant.now()
        ).value

        if (history.size < MIN_SAMPLES) {
            // Not enough data, return null
            return null
        }

        // Calculate regression parameters
        val (slope, intercept) = calculateRegression(history)

        // If device is charging (slope >= 0), don't predict discharge milestones
        if (slope >= 0 && milestone < 100) {
            return null
        }

        // Calculate time to milestone
        val currentEpochHours = Instant.now().epochSecond.toFloat() / 3600f
        val hoursToMilestone = (milestone - intercept) / slope
        val estimatedEpochHours = currentEpochHours + hoursToMilestone
        val estimatedTime = Instant.ofEpochSecond((estimatedEpochHours * 3600).toLong())

        // Calculate confidence
        val confidence = calculateConfidence(history, slope)

        return BatteryPrediction(
            milestone = milestone,
            estimatedTime = estimatedTime,
            confidence = confidence
        )
    }

    /**
     * Update predictions for all devices.
     */
    suspend fun updateAllPredictions() {
        // In a real implementation, this would iterate through all devices
        // and update their predictions
    }

    /**
     * Calculate linear regression parameters from battery history.
     * 
     * Uses Ordinary Least Squares method.
     */
    private fun calculateRegression(
        history: List<com.batteryguardian.domain.repository.BatteryLevel>
    ): Pair<Float, Float> {
        val points = history.map { sample ->
            val hours = sample.timestamp.epochSecond.toFloat() / 3600f
            Point(hours, sample.level.toFloat())
        }

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
     * Calculate confidence in the prediction.
     */
    private fun calculateConfidence(
        history: List<com.batteryguardian.domain.repository.BatteryLevel>,
        slope: Float
    ): Float {
        var confidence = 0.0f

        // More data points = higher confidence (capped at 20)
        confidence += minOf(history.size.toFloat() / 20, 0.4f)

        // More recent data = higher confidence
        val mostRecent = history.maxByOrNull { it.timestamp }?.timestamp
        if (mostRecent != null) {
            val hoursSinceLast = Duration.between(mostRecent, Instant.now()).toHours().toFloat()
            confidence += maxOf(0f, 1f - hoursSinceLast / 24) * 0.3f
        }

        // Lower variance in drain rate = higher confidence
        // Calculate variance of recent drain rates
        if (history.size >= 2) {
            val recentDrainRates = mutableListOf<Float>()
            for (i in 1 until history.size) {
                val timeDiff = Duration.between(
                    history[i-1].timestamp,
                    history[i].timestamp
                ).toHours().toFloat()
                if (timeDiff > 0) {
                    val levelDiff = history[i].level - history[i-1].level
                    recentDrainRates.add(levelDiff / timeDiff)
                }
            }
            
            if (recentDrainRates.isNotEmpty()) {
                val meanDrainRate = recentDrainRates.average()
                val variance = recentDrainRates.sumOf { (it - meanDrainRate).pow(2) } / 
                    recentDrainRates.size
                confidence += maxOf(0f, 1f - variance / 100) * 0.3f
            }
        }

        return minOf(confidence, 1.0f)
    }

    /**
     * Point for regression calculation.
     */
    private data class Point(val x: Float, val y: Float)

    companion object {
        /** Minimum number of samples required for prediction */
        private const val MIN_SAMPLES = 3
    }
}
