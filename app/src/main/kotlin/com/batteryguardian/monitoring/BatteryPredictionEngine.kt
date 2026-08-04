package com.batteryguardian.monitoring

import com.batteryguardian.domain.model.BatteryPrediction
import com.batteryguardian.domain.repository.BatteryLevel
import com.batteryguardian.domain.repository.BatteryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

/**
 * Engine for predicting battery levels using linear regression.
 * 
 * Calculates when a device will reach specific battery milestones
 * based on historical battery data.
 */
class BatteryPredictionEngine @Inject constructor(
    private val batteryRepository: BatteryRepository
) {

    private val models = mutableMapOf<String, RegressionModel>()

    /**
     * Update the regression model for a device with new data.
     */
    suspend fun updateModel(deviceId: String, level: Int, timestamp: Instant) {
        val history = batteryRepository.getBatteryHistory(
            deviceId = deviceId,
            startTime = timestamp.minus(Duration.ofDays(7)),
            endTime = timestamp
        ).first()

        if (history.size < MIN_SAMPLES) {
            // Not enough data, use default model
            models[deviceId] = createDefaultModel(deviceId)
            return
        }

        // Convert to points for regression
        val points = history.map { sample ->
            val hours = sample.timestamp.epochSecond.toFloat() / 3600f
            Point(hours, sample.level.toFloat())
        }

        val model = calculateRegression(points)
        models[deviceId] = model.copy(
            lastUpdated = timestamp,
            sampleCount = history.size
        )
    }

    /**
     * Predict when a device will reach a specific battery milestone.
     */
    suspend fun predictTimeToMilestone(
        deviceId: String,
        milestone: Int
    ): BatteryPrediction? {
        val model = models[deviceId] ?: return null

        // If device is charging (slope >= 0), don't predict discharge milestones
        if (model.slope >= 0 && milestone < 100) {
            return null
        }

        // Calculate time to milestone
        val currentEpochHours = Instant.now().epochSecond.toFloat() / 3600f
        val hoursToMilestone = (milestone - model.intercept) / model.slope
        val estimatedEpochHours = currentEpochHours + hoursToMilestone
        val estimatedTime = Instant.ofEpochSecond(
            (estimatedEpochHours * 3600).toLong()
        )

        // Calculate confidence
        val confidence = calculateConfidence(models[deviceId]!!)

        return BatteryPrediction(
            milestone = milestone,
            estimatedTime = estimatedTime,
            confidence = confidence
        )
    }

    /**
     * Get predictions for all standard milestones for a device.
     */
    suspend fun getPredictions(deviceId: String): List<BatteryPrediction> {
        val model = models[deviceId] ?: return emptyList()

        // If device is charging, only predict time to full
        if (model.slope >= 0) {
            return listOfNotNull(predictTimeToMilestone(deviceId, 100))
        }

        // Predict for standard milestones
        return listOf(20, 10, 5).mapNotNull { milestone ->
            predictTimeToMilestone(deviceId, milestone)
        }
    }

    /**
     * Calculate linear regression parameters.
     */
    private fun calculateRegression(points: List<Point>): RegressionModel {
        val n = points.size
        val sumX = points.sumOf { it.x }
        val sumY = points.sumOf { it.y }
        val sumXY = points.sumOf { it.x * it.y }
        val sumX2 = points.sumOf { it.x * it.x }

        val slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX)
        val intercept = (sumY - slope * sumX) / n

        // Calculate variance
        val meanY = sumY / n
        val variance = points.sumOf { (it.y - meanY).pow(2) } / n

        return RegressionModel(
            slope = slope,
            intercept = intercept,
            lastUpdated = Instant.now(),
            sampleCount = n,
            variance = variance
        )
    }

    /**
     * Calculate confidence in the prediction.
     */
    private fun calculateConfidence(model: RegressionModel): Float {
        var confidence = 0.0f

        // More data points = higher confidence (capped at 20)
        confidence += minOf(model.sampleCount.toFloat() / 20, 0.4f)

        // More recent data = higher confidence
        val hoursSinceLastUpdate = Duration.between(
            model.lastUpdated,
            Instant.now()
        ).toHours().toFloat()
        confidence += maxOf(0f, 1f - hoursSinceLastUpdate / 24) * 0.3f

        // Lower variance = higher confidence
        confidence += maxOf(0f, 1f - model.variance / 100) * 0.3f

        return minOf(confidence, 1.0f)
    }

    /**
     * Create a default regression model for a device.
     */
    private fun createDefaultModel(deviceId: String): RegressionModel {
        val now = Instant.now()
        val epochHours = now.epochSecond.toFloat() / 3600f
        return RegressionModel(
            slope = -10f, // Default: 10% drain per hour
            intercept = epochHours + 10f, // Reaches 0% in 1 hour
            lastUpdated = now,
            sampleCount = 0,
            variance = 0f
        )
    }

    /**
     * Clear the model for a device.
     */
    fun clearModel(deviceId: String) {
        models.remove(deviceId)
    }

    /**
     * Clear all models.
     */
    fun clearAllModels() {
        models.clear()
    }

    /**
     * Regression model for a device.
     */
    private data class RegressionModel(
        val slope: Float,
        val intercept: Float,
        val lastUpdated: Instant,
        val sampleCount: Int,
        val variance: Float
    )

    /**
     * Point for regression calculation.
     */
    private data class Point(val x: Float, val y: Float)

    companion object {
        /** Minimum number of samples required for prediction */
        private const val MIN_SAMPLES = 3
    }
}
