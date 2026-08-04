package com.batteryguardian.monitoring

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.batteryguardian.domain.model.BatteryReadingResult
import com.batteryguardian.domain.repository.BatteryRepository
import com.batteryguardian.domain.repository.DeviceRepository
import com.batteryguardian.domain.repository.UserPreferencesRepository
import com.batteryguardian.domain.usecase.AlertUseCase
import com.batteryguardian.domain.usecase.MonitorBatteryUseCase
import com.batteryguardian.domain.usecase.PredictBatteryUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

/**
 * Main battery monitoring component.
 * 
 * Coordinates between battery readers, prediction engine, and alert manager
 * to monitor Bluetooth device batteries and trigger alerts.
 */
class BatteryMonitor @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val batteryRepository: BatteryRepository,
    private val gattBatteryReader: GattBatteryReader,
    private val classicBatteryReader: ClassicBatteryReader,
    private val batteryPredictionEngine: BatteryPredictionEngine,
    private val alertManager: AlertManager,
    private val pollingOrchestrator: PollingOrchestrator,
    private val userPreferencesRepository: UserPreferencesRepository
) {

    private val monitorScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isMonitoring = false

    /**
     * Start monitoring battery levels.
     */
    fun start() {
        if (isMonitoring) return
        
        isMonitoring = true
        startBatteryReaders()
        startPolling()
        
        monitorScope.launch {
            // Initial scan
            scanAllDevices()
        }
    }

    /**
     * Stop monitoring battery levels.
     */
    fun stop() {
        if (!isMonitoring) return
        
        isMonitoring = false
        stopBatteryReaders()
        stopPolling()
        monitorScope.cancel()
    }

    /**
     * Start the battery readers.
     */
    private fun startBatteryReaders() {
        classicBatteryReader.start()
    }

    /**
     * Stop the battery readers.
     */
    private fun stopBatteryReaders() {
        classicBatteryReader.stop()
    }

    /**
     * Start periodic polling.
     */
    private fun startPolling() {
        monitorScope.launch {
            userPreferencesRepository.preferences.collect { preferences ->
                pollingOrchestrator.startPolling(preferences.pollingInterval)
            }
        }
    }

    /**
     * Stop periodic polling.
     */
    private fun stopPolling() {
        pollingOrchestrator.stopPolling()
    }

    /**
     * Scan all monitored devices for battery levels.
     */
    suspend fun scanAllDevices() {
        val devices = deviceRepository.getMonitoredDevices().first()
        
        devices.forEach { device ->
            readBatteryLevel(device.id)
        }
    }

    /**
     * Read battery level for a specific device.
     */
    suspend fun readBatteryLevel(deviceId: String) {
        // Try GATT first
        val gattResult = try {
            gattBatteryReader.readBattery(deviceId)
        } catch (e: Exception) {
            BatteryReadingResult(
                deviceId = deviceId,
                batteryLevel = null,
                isCharging = null,
                readMethod = com.batteryguardian.domain.model.BatteryReadMethod.GATT,
                timestamp = Instant.now(),
                success = false,
                error = e.message
            )
        }

        if (gattResult.success) {
            processBatteryReading(gattResult)
            return
        }

        // Fall back to Classic Bluetooth
        val classicResult = try {
            classicBatteryReader.readBattery(deviceId)
        } catch (e: Exception) {
            BatteryReadingResult(
                deviceId = deviceId,
                batteryLevel = null,
                isCharging = null,
                readMethod = com.batteryguardian.domain.model.BatteryReadMethod.CLASSIC,
                timestamp = Instant.now(),
                success = false,
                error = e.message
            )
        }

        if (classicResult.success) {
            processBatteryReading(classicResult)
            return
        }

        // If both failed, try to get last known value
        val lastKnown = batteryRepository.getLatestBatteryLevel(deviceId).first()
        if (lastKnown != null) {
            // Use last known value with updated timestamp
            val result = BatteryReadingResult(
                deviceId = deviceId,
                batteryLevel = lastKnown.level,
                isCharging = null,
                readMethod = com.batteryguardian.domain.model.BatteryReadMethod.NONE,
                timestamp = Instant.now(),
                success = true
            )
            processBatteryReading(result)
        }
    }

    /**
     * Process a battery reading result.
     */
    private suspend fun processBatteryReading(result: BatteryReadingResult) {
        if (!result.success) {
            return
        }

        // Save to repository
        batteryRepository.addBatteryLevel(
            com.batteryguardian.domain.repository.BatteryLevel(
                deviceId = result.deviceId,
                level = result.batteryLevel ?: 0,
                timestamp = result.timestamp,
                isPredicted = false
            )
        )

        // Update device status
        deviceRepository.updateDeviceStatus(
            deviceId = result.deviceId,
            batteryLevel = result.batteryLevel,
            isCharging = result.isCharging,
            isConnected = true,
            lastSeen = result.timestamp
        )

        // Update predictions
        batteryPredictionEngine.updateModel(
            deviceId = result.deviceId,
            level = result.batteryLevel ?: 0,
            timestamp = result.timestamp
        )

        // Check for alerts
        alertManager.checkAndTriggerAlerts(result.deviceId, result.batteryLevel ?: 0)
    }

    /**
     * Handle device connection state change.
     */
    fun onDeviceConnected(device: BluetoothDevice) {
        monitorScope.launch {
            deviceRepository.updateDeviceStatus(
                deviceId = device.address,
                batteryLevel = null,
                isCharging = null,
                isConnected = true,
                lastSeen = Instant.now()
            )
            readBatteryLevel(device.address)
        }
    }

    /**
     * Handle device disconnection.
     */
    fun onDeviceDisconnected(device: BluetoothDevice) {
        monitorScope.launch {
            deviceRepository.updateDeviceStatus(
                deviceId = device.address,
                batteryLevel = null,
                isCharging = null,
                isConnected = false,
                lastSeen = Instant.now()
            )
        }
    }

    /**
     * Handle polling tick (time to check battery levels again).
     */
    fun onPollingTick() {
        monitorScope.launch {
            scanAllDevices()
        }
    }

    /**
     * Refresh battery levels for all devices.
     */
    fun refresh() {
        monitorScope.launch {
            scanAllDevices()
        }
    }
}
