package com.batteryguardian.domain.usecase

import com.batteryguardian.domain.model.AlertEvent
import com.batteryguardian.domain.model.BatteryReadingResult
import com.batteryguardian.domain.model.Device
import com.batteryguardian.domain.repository.AlertRepository
import com.batteryguardian.domain.repository.BatteryRepository
import com.batteryguardian.domain.repository.DeviceRepository
import com.batteryguardian.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

/**
 * Use case for monitoring battery levels of Bluetooth devices.
 */
class MonitorBatteryUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val batteryRepository: BatteryRepository,
    private val alertRepository: AlertRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * Get all devices with their current battery levels.
     */
    fun getDevicesWithBattery(): Flow<List<Device>> {
        return deviceRepository.getAllDevices()
            .combine(userPreferencesRepository.preferences) { devices, preferences ->
                devices.map { device ->
                    val batteryLevel = getLatestBatteryLevel(device.id)
                    val health = batteryRepository.getBatteryHealth(device.id)
                    val predictions = batteryRepository.getPredictions(device.id)
                    val alertState = alertRepository.getAlertState(device.id)
                    
                    device.copy(
                        currentBatteryLevel = batteryLevel,
                        batteryHealth = health,
                        alertState = alertState
                    )
                }
            }
            .flowOn(Dispatchers.Default)
    }

    /**
     * Get monitored devices with their battery levels.
     */
    fun getMonitoredDevicesWithBattery(): Flow<List<Device>> {
        return deviceRepository.getMonitoredDevices()
            .combine(userPreferencesRepository.preferences) { devices, preferences ->
                devices.map { device ->
                    val batteryLevel = getLatestBatteryLevel(device.id)
                    val health = batteryRepository.getBatteryHealth(device.id)
                    val predictions = batteryRepository.getPredictions(device.id)
                    val alertState = alertRepository.getAlertState(device.id)
                    
                    device.copy(
                        currentBatteryLevel = batteryLevel,
                        batteryHealth = health,
                        alertState = alertState
                    )
                }
            }
            .flowOn(Dispatchers.Default)
    }

    /**
     * Process a new battery reading from a device.
     */
    suspend fun processBatteryReading(result: BatteryReadingResult) {
        if (!result.success) {
            return
        }

        val deviceId = result.deviceId
        val batteryLevel = result.batteryLevel
        val isCharging = result.isCharging
        val timestamp = result.timestamp

        // Update device status
        deviceRepository.updateDeviceStatus(
            deviceId = deviceId,
            batteryLevel = batteryLevel,
            isCharging = isCharging,
            isConnected = true,
            lastSeen = timestamp
        )

        // Save battery level
        batteryRepository.addBatteryLevel(
            BatteryLevel(
                deviceId = deviceId,
                level = batteryLevel ?: 0,
                timestamp = timestamp,
                isPredicted = false
            )
        )

        // Check for alerts
        checkForAlerts(deviceId, batteryLevel, timestamp)
    }

    /**
     * Check if a battery reading triggers any alerts.
     */
    private suspend fun checkForAlerts(
        deviceId: String,
        batteryLevel: Int?,
        timestamp: Instant
    ) {
        if (batteryLevel == null) return

        val preferences = userPreferencesRepository.preferences.value
        val thresholds = listOf(
            preferences.criticalThreshold,
            preferences.mediumThreshold,
            preferences.lowThreshold
        ).sorted()

        val currentState = alertRepository.getAlertState(deviceId).value

        thresholds.forEach { threshold ->
            if (batteryLevel <= threshold) {
                // Check if we should trigger an alert
                if (shouldTriggerAlert(currentState, batteryLevel, threshold)) {
                    val event = AlertEvent.LowBattery(
                        deviceId = deviceId,
                        currentLevel = batteryLevel,
                        threshold = threshold
                    )
                    alertRepository.addAlertEvent(event)
                    alertRepository.updateAlertState(
                        deviceId,
                        AlertState.Low(threshold, timestamp)
                    )
                }
            } else {
                // Check if we should clear the alert
                if (shouldClearAlert(currentState, batteryLevel, threshold, preferences.hysteresisBand)) {
                    alertRepository.clearAlertState(deviceId)
                }
            }
        }
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
     * Get the latest battery level for a device.
     */
    private suspend fun getLatestBatteryLevel(deviceId: String): Int? {
        return batteryRepository.getLatestBatteryLevel(deviceId).value?.level
    }

    /**
     * Refresh all battery levels.
     */
    fun refreshAllBatteryLevels() {
        scope.launch {
            val devices = deviceRepository.getMonitoredDevices().value
            // In a real implementation, this would trigger Bluetooth reads
            // For now, we just mark that a refresh was requested
        }
    }

    /**
     * Start monitoring all devices.
     */
    fun startMonitoring() {
        scope.launch {
            deviceRepository.getMonitoredDevices().collect { devices ->
                devices.forEach { device ->
                    // Trigger battery read for each device
                }
            }
        }
    }

    /**
     * Stop monitoring all devices.
     */
    fun stopMonitoring() {
        // Cancel any ongoing monitoring
    }
}

/**
 * Battery level data class for use in the domain layer.
 */
data class BatteryLevel(
    val deviceId: String,
    val level: Int,
    val timestamp: Instant,
    val isPredicted: Boolean = false
)
