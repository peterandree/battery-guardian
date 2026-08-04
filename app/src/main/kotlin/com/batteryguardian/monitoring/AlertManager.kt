package com.batteryguardian.monitoring

import com.batteryguardian.domain.model.AlertState
import com.batteryguardian.domain.repository.AlertRepository
import com.batteryguardian.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

/**
 * Manages battery level alerts with hysteresis.
 * 
 * Prevents notification spam by implementing a hysteresis band
 * (default: 2%) for alert thresholds.
 */
class AlertManager @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val alertRepository: AlertRepository
) {

    private val alertScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val alertStates = mutableMapOf<String, AlertState>()
    private val lastAlertLevels = mutableMapOf<String, Int>()

    /**
     * Check if a battery level triggers an alert and trigger if needed.
     */
    fun checkAndTriggerAlerts(deviceId: String, currentLevel: Int) {
        alertScope.launch {
            val preferences = userPreferencesRepository.preferences.first()
            val thresholds = listOf(
                preferences.criticalThreshold,
                preferences.mediumThreshold,
                preferences.lowThreshold
            ).sorted()

            thresholds.forEach { threshold ->
                if (currentLevel <= threshold) {
                    val shouldTrigger = shouldTriggerLowAlert(
                        deviceId, currentLevel, threshold, preferences.hysteresisBand
                    )
                    if (shouldTrigger) {
                        triggerLowAlert(deviceId, currentLevel, threshold)
                    }
                } else {
                    val shouldClear = shouldClearLowAlert(
                        deviceId, currentLevel, threshold, preferences.hysteresisBand
                    )
                    if (shouldClear) {
                        clearLowAlert(deviceId)
                    }
                }
            }
        }
    }

    /**
     * Check if a low alert should be triggered.
     */
    private suspend fun shouldTriggerLowAlert(
        deviceId: String,
        currentLevel: Int,
        threshold: Int,
        hysteresisBand: Int
    ): Boolean {
        val previousState = alertStates[deviceId] ?: AlertState.Normal

        return when (previousState) {
            is AlertState.Normal -> currentLevel <= threshold
            is AlertState.Low -> {
                // Only trigger if battery dropped further
                val lastLevel = lastAlertLevels[deviceId] ?: Int.MAX_VALUE
                currentLevel < lastLevel
            }
            is AlertState.High -> false
        }
    }

    /**
     * Check if a low alert should be cleared.
     */
    private suspend fun shouldClearLowAlert(
        deviceId: String,
        currentLevel: Int,
        threshold: Int,
        hysteresisBand: Int
    ): Boolean {
        val previousState = alertStates[deviceId] ?: return false

        return when (previousState) {
            is AlertState.Low -> {
                previousState.threshold == threshold &&
                currentLevel > threshold + hysteresisBand
            }
            else -> false
        }
    }

    /**
     * Trigger a low battery alert.
     */
    private suspend fun triggerLowAlert(
        deviceId: String,
        currentLevel: Int,
        threshold: Int
    ) {
        alertStates[deviceId] = AlertState.Low(threshold, Instant.now())
        lastAlertLevels[deviceId] = currentLevel
        
        alertRepository.updateAlertState(
            deviceId,
            AlertState.Low(threshold, Instant.now())
        )
        
        // In a real implementation, this would also show a notification
        // For now, we just update the state
    }

    /**
     * Clear a low battery alert.
     */
    private suspend fun clearLowAlert(deviceId: String) {
        alertStates.remove(deviceId)
        lastAlertLevels.remove(deviceId)
        
        alertRepository.clearAlertState(deviceId)
    }

    /**
     * Get the current alert state for a device.
     */
    fun getAlertState(deviceId: String): AlertState {
        return alertStates[deviceId] ?: AlertState.Normal
    }

    /**
     * Get all devices currently in alert state.
     */
    fun getDevicesInAlert(): List<String> {
        return alertStates.filterValues { it !is AlertState.Normal }
            .keys.toList()
    }

    /**
     * Update the alert state for a device.
     */
    fun updateAlertState(deviceId: String, state: AlertState) {
        alertStates[deviceId] = state
        
        when (state) {
            is AlertState.Low -> lastAlertLevels[deviceId] = state.threshold
            else -> lastAlertLevels.remove(deviceId)
        }
    }

    /**
     * Clear all alert states.
     */
    fun clearAllAlerts() {
        alertStates.clear()
        lastAlertLevels.clear()
    }
}
