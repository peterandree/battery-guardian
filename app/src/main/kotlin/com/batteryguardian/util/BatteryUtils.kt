package com.batteryguardian.util

import com.batteryguardian.domain.model.BatteryLevel
import com.batteryguardian.domain.repository.BatteryLevel
import java.time.Duration
import java.time.Instant

/**
 * Utility functions for working with battery data.
 */

object BatteryUtils {

    // ==================== Battery Level Constants ====================

    /** Minimum battery level */
    const val MIN_BATTERY_LEVEL = 0

    /** Maximum battery level */
    const val MAX_BATTERY_LEVEL = 100

    /** Critical battery level (5%) */
    const val CRITICAL_BATTERY_LEVEL = 5

    /** Low battery level (20%) */
    const val LOW_BATTERY_LEVEL = 20

    /** Medium battery level (50%) */
    const val MEDIUM_BATTERY_LEVEL = 50

    // ==================== Battery Level Validation ====================

    /**
     * Validate a battery level.
     * 
     * @param level The battery level to validate
     * @return The validated battery level (clamped to 0-100)
     */
    fun validateBatteryLevel(level: Int): Int {
        return level.coerceIn(MIN_BATTERY_LEVEL, MAX_BATTERY_LEVEL)
    }

    /**
     * Validate a battery level.
     * 
     * @param level The battery level to validate
     * @return The validated battery level (clamped to 0-100)
     */
    fun validateBatteryLevel(level: Float): Float {
        return level.coerceIn(MIN_BATTERY_LEVEL.toFloat(), MAX_BATTERY_LEVEL.toFloat())
    }

    /**
     * Check if a battery level is valid.
     */
    fun isValidBatteryLevel(level: Int): Boolean {
        return level in MIN_BATTERY_LEVEL..MAX_BATTERY_LEVEL
    }

    // ==================== Battery Level Classification ====================

    /**
     * Classify a battery level.
     */
    fun classifyBatteryLevel(level: Int?): BatteryLevelClassification {
        if (level == null) return BatteryLevelClassification.UNKNOWN
        
        return when {
            level >= 80 -> BatteryLevelClassification.HIGH
            level >= 50 -> BatteryLevelClassification.MEDIUM_HIGH
            level >= 30 -> BatteryLevelClassification.MEDIUM
            level >= 15 -> BatteryLevelClassification.MEDIUM_LOW
            level >= 5 -> BatteryLevelClassification.LOW
            else -> BatteryLevelClassification.CRITICAL
        }
    }

    /**
     * Get the color for a battery level.
     */
    @androidx.compose.ui.graphics.Color
    fun getBatteryLevelColor(level: Int?): Int {
        return when (classifyBatteryLevel(level)) {
            BatteryLevelClassification.HIGH -> android.graphics.Color.GREEN
            BatteryLevelClassification.MEDIUM_HIGH -> android.graphics.Color.parseColor("#8BC34A")
            BatteryLevelClassification.MEDIUM -> android.graphics.Color.YELLOW
            BatteryLevelClassification.MEDIUM_LOW -> android.graphics.Color.parseColor("#FFC107")
            BatteryLevelClassification.LOW -> android.graphics.Color.parseColor("#FF9800")
            BatteryLevelClassification.CRITICAL -> android.graphics.Color.RED
            BatteryLevelClassification.UNKNOWN -> android.graphics.Color.GRAY
        }
    }

    // ==================== Battery Drain Calculation ====================

    /**
     * Calculate the battery drain rate from a list of battery levels.
     * 
     * @param levels List of battery levels sorted by timestamp (oldest first)
     * @return Drain rate in percentage per hour (negative when discharging)
     */
    fun calculateDrainRate(levels: List<BatteryLevel>): Float {
        if (levels.size < 2) return 0f
        
        // Use the first and last points for a simple calculation
        val first = levels.first()
        val last = levels.last()
        
        val levelDiff = last.level - first.level
        val timeDiff = Duration.between(first.timestamp, last.timestamp).toHours()
        
        if (timeDiff == 0L) return 0f
        
        return levelDiff.toFloat() / timeDiff
    }

    /**
     * Calculate the average drain rate from a list of battery levels.
     * 
     * Uses linear regression for more accurate results.
     */
    fun calculateAverageDrainRate(levels: List<BatteryLevel>): Float {
        if (levels.size < 2) return 0f
        
        // Convert to points for regression
        val points = levels.map { level ->
            val hours = level.timestamp.epochSecond.toFloat() / 3600f
            Point(hours, level.level.toFloat())
        }
        
        val (slope, _) = calculateRegression(points)
        return slope
    }

    /**
     * Calculate the time remaining until a specific battery level.
     * 
     * @param currentLevel Current battery level
     * @param targetLevel Target battery level
     * @param drainRate Drain rate in percentage per hour (negative when discharging)
     * @return Time remaining as a Duration, or null if not discharging
     */
    fun calculateTimeRemaining(
        currentLevel: Int,
        targetLevel: Int,
        drainRate: Float
    ): Duration? {
        if (drainRate >= 0) return null // Not discharging
        
        val levelDiff = targetLevel - currentLevel
        val hours = levelDiff.toFloat() / drainRate
        
        if (hours.isInfinite() || hours.isNaN()) return null
        
        return Duration.ofHours(hours.toLong())
    }

    /**
     * Calculate the time to full charge.
     * 
     * @param currentLevel Current battery level
     * @param chargeRate Charge rate in percentage per hour (positive when charging)
     * @return Time to full as a Duration, or null if not charging
     */
    fun calculateTimeToFull(
        currentLevel: Int,
        chargeRate: Float
    ): Duration? {
        if (chargeRate <= 0) return null // Not charging
        
        val levelDiff = MAX_BATTERY_LEVEL - currentLevel
        val hours = levelDiff.toFloat() / chargeRate
        
        if (hours.isInfinite() || hours.isNaN()) return null
        
        return Duration.ofHours(hours.toLong())
    }

    // ==================== Battery Health Calculation ====================

    /**
     * Calculate battery capacity degradation.
     * 
     * @param initialCapacity Initial battery capacity (mAh)
     * @param currentCapacity Current battery capacity (mAh)
     * @return Degradation as a percentage (0-100)
     */
    fun calculateCapacityDegradation(
        initialCapacity: Float,
        currentCapacity: Float
    ): Float {
        if (initialCapacity <= 0) return 0f
        
        val degradation = ((initialCapacity - currentCapacity) / initialCapacity) * 100
        return degradation.coerceAtLeast(0f)
    }

    /**
     * Estimate battery capacity from usage patterns.
     * 
     * This is a simplified estimation based on drain rate and usage time.
     */
    fun estimateBatteryCapacity(
        drainRate: Float,
        usageTime: Duration
    ): Float {
        // This is a placeholder for a more sophisticated calculation
        // In a real implementation, this would use machine learning or
        // manufacturer specifications
        return 1000f // mAh (placeholder)
    }

    // ==================== Battery History Analysis ====================

    /**
     * Analyze battery history for patterns.
     */
    fun analyzeBatteryHistory(levels: List<BatteryLevel>): BatteryHistoryAnalysis {
        if (levels.isEmpty()) {
            return BatteryHistoryAnalysis(
                averageLevel = 0f,
                minLevel = 0,
                maxLevel = 0,
                averageDrainRate = 0f,
                totalDischargeCycles = 0,
                totalChargeCycles = 0,
                averageDischargeTime = Duration.ZERO,
                averageChargeTime = Duration.ZERO
            )
        }
        
        val sortedLevels = levels.sortedBy { it.timestamp }
        
        // Calculate basic statistics
        val averageLevel = sortedLevels.map { it.level.toFloat() }.average().toFloat()
        val minLevel = sortedLevels.minOf { it.level }
        val maxLevel = sortedLevels.maxOf { it.level }
        
        // Calculate drain rate
        val averageDrainRate = calculateAverageDrainRate(sortedLevels)
        
        // Detect charge/discharge cycles
        var dischargeCycles = 0
        var chargeCycles = 0
        var inDischarge = false
        var inCharge = false
        var lastLevel = sortedLevels.first().level
        var dischargeStart: Instant? = null
        var chargeStart: Instant? = null
        var totalDischargeTime = Duration.ZERO
        var totalChargeTime = Duration.ZERO
        
        for (i in 1 until sortedLevels.size) {
            val currentLevel = sortedLevels[i].level
            val timeDiff = Duration.between(
                sortedLevels[i-1].timestamp,
                sortedLevels[i].timestamp
            )
            
            if (currentLevel < lastLevel - 1) {
                // Discharging
                if (!inDischarge) {
                    inDischarge = true
                    inCharge = false
                    dischargeStart = sortedLevels[i-1].timestamp
                }
                totalDischargeTime = totalDischargeTime.plus(timeDiff)
            } else if (currentLevel > lastLevel + 1) {
                // Charging
                if (!inCharge) {
                    inCharge = true
                    inDischarge = false
                    chargeStart = sortedLevels[i-1].timestamp
                }
                totalChargeTime = totalChargeTime.plus(timeDiff)
            }
            
            // Detect cycle completion
            if (inDischarge && currentLevel <= LOW_BATTERY_LEVEL && lastLevel > LOW_BATTERY_LEVEL) {
                dischargeCycles++
            }
            if (inCharge && currentLevel >= MAX_BATTERY_LEVEL - 5 && lastLevel < MAX_BATTERY_LEVEL - 5) {
                chargeCycles++
            }
            
            lastLevel = currentLevel
        }
        
        // Calculate average times
        val averageDischargeTime = if (dischargeCycles > 0) {
            totalDischargeTime.dividedBy(dischargeCycles.toLong())
        } else {
            Duration.ZERO
        }
        
        val averageChargeTime = if (chargeCycles > 0) {
            totalChargeTime.dividedBy(chargeCycles.toLong())
        } else {
            Duration.ZERO
        }
        
        return BatteryHistoryAnalysis(
            averageLevel = averageLevel,
            minLevel = minLevel,
            maxLevel = maxLevel,
            averageDrainRate = averageDrainRate,
            totalDischargeCycles = dischargeCycles,
            totalChargeCycles = chargeCycles,
            averageDischargeTime = averageDischargeTime,
            averageChargeTime = averageChargeTime
        )
    }

    // ==================== Helper Classes ====================

    /**
     * Classification of battery levels.
     */
    enum class BatteryLevelClassification {
        UNKNOWN,
        CRITICAL,
        LOW,
        MEDIUM_LOW,
        MEDIUM,
        MEDIUM_HIGH,
        HIGH
    }

    /**
     * Analysis of battery history.
     */
    data class BatteryHistoryAnalysis(
        val averageLevel: Float,
        val minLevel: Int,
        val maxLevel: Int,
        val averageDrainRate: Float,
        val totalDischargeCycles: Int,
        val totalChargeCycles: Int,
        val averageDischargeTime: Duration,
        val averageChargeTime: Duration
    )

    /**
     * Point for regression calculation.
     */
    private data class Point(val x: Float, val y: Float)

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
}
