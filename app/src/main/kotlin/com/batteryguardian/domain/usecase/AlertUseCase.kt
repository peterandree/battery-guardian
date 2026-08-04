package com.batteryguardian.domain.usecase

import com.batteryguardian.domain.model.AlertEvent
import com.batteryguardian.domain.model.AlertState
import com.batteryguardian.domain.model.Device
import com.batteryguardian.domain.repository.AlertRepository
import com.batteryguardian.domain.repository.DeviceRepository
import com.batteryguardian.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import java.time.Instant
import javax.inject.Inject

/**
 * Use case for managing battery alerts.
 */
class AlertUseCase @Inject constructor(
    private val alertRepository: AlertRepository,
    private val deviceRepository: DeviceRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) {

    /**
     * Get all devices that currently have alerts.
     */
    fun getDevicesWithAlerts(): Flow<List<Device>> {
        return alertRepository.getDevicesInAlert()
            .combine(deviceRepository.getAllDevices()) { alertedDevices, allDevices ->
                allDevices.filter { device ->
                    alertedDevices.contains(device.id)
                }
            }
            .flowOn(kotlinx.coroutines.Dispatchers.Default)
    }

    /**
     * Get the current alert state for a device.
     */
    fun getAlertState(deviceId: String): Flow<AlertState> {
        return alertRepository.getAlertState(deviceId)
    }

    /**
     * Check if a device is in alert state.
     */
    suspend fun isInAlert(deviceId: String): Boolean {
        return alertRepository.isInAlert(deviceId)
    }

    /**
     * Trigger an alert for a device.
     */
    suspend fun triggerAlert(
        deviceId: String,
        currentLevel: Int,
        threshold: Int
    ) {
        val event = AlertEvent.LowBattery(
            deviceId = deviceId,
            currentLevel = currentLevel,
            threshold = threshold
        )
        alertRepository.addAlertEvent(event)
        alertRepository.updateAlertState(
            deviceId,
            AlertState.Low(threshold, Instant.now())
        )
    }

    /**
     * Clear alert for a device.
     */
    suspend fun clearAlert(deviceId: String) {
        alertRepository.clearAlertState(deviceId)
    }

    /**
     * Clear all alerts.
     */
    suspend fun clearAllAlerts() {
        alertRepository.clearAllAlertStates()
    }

    /**
     * Check if any devices need alerts based on current battery levels.
     */
    fun checkForAlerts(): Flow<List<AlertEvent>> {
        return deviceRepository.getMonitoredDevices()
            .combine(userPreferencesRepository.preferences) { devices, preferences ->
                val alerts = mutableListOf<AlertEvent>()
                val thresholds = listOf(
                    preferences.criticalThreshold,
                    preferences.mediumThreshold,
                    preferences.lowThreshold
                ).sorted()

                devices.forEach { device ->
                    device.currentBatteryLevel?.let { level ->
                        thresholds.forEach { threshold ->
                            if (level <= threshold) {
                                val currentState = alertRepository.getAlertState(device.id).value
                                if (shouldTriggerAlert(currentState, level, threshold)) {
                                    alerts.add(AlertEvent.LowBattery(
                                        deviceId = device.id,
                                        currentLevel = level,
                                        threshold = threshold
                                    ))
                                    alertRepository.updateAlertState(
                                        device.id,
                                        AlertState.Low(threshold, Instant.now())
                                    )
                                }
                            } else {
                                val currentState = alertRepository.getAlertState(device.id).value
                                if (shouldClearAlert(currentState, level, threshold, preferences.hysteresisBand)) {
                                    alertRepository.clearAlertState(device.id)
                                }
                            }
                        }
                    }
                }
                alerts
            }
            .flowOn(kotlinx.coroutines.Dispatchers.Default)
    }

    /**
     * Check if an alert should be triggered.
     */
    private fun shouldTriggerAlert(
        currentState: AlertState,
        batteryLevel: Int,
        threshold: Int
    ): Boolean {
        return when (currentState) {
            is AlertState.Normal -> batteryLevel <= threshold
            is AlertState.Low -> {
                // Only trigger if battery dropped further
                batteryLevel < currentState.threshold
            }
            is AlertState.High -> false
        }
    }

    /**
     * Check if an alert should be cleared.
     */
    private fun shouldClearAlert(
        currentState: AlertState,
        batteryLevel: Int,
        threshold: Int,
        hysteresisBand: Int
    ): Boolean {
        return when (currentState) {
            is AlertState.Low -> {
                currentState.threshold == threshold &&
                batteryLevel > threshold + hysteresisBand
            }
            else -> false
        }
    }

    /**
     * Get alert history for a device.
     */
    fun getAlertHistory(deviceId: String): Flow<List<AlertEvent>> {
        return alertRepository.getAlertHistory(deviceId)
    }

    /**
     * Get all alert events.
     */
    fun getAllAlertEvents(): Flow<List<AlertEvent>> {
        return alertRepository.getAllAlertEvents()
    }
}
