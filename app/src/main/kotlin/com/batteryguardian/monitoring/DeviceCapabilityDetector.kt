package com.batteryguardian.monitoring

import android.bluetooth.BluetoothDevice
import com.batteryguardian.domain.model.BatteryReadMethod
import com.batteryguardian.domain.model.DeviceCapabilities
import com.batteryguardian.domain.repository.DeviceCapabilityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject

/**
 * Detects device capabilities for battery reading.
 * 
 * Determines which battery reading methods (GATT, Classic, Manual)
 * work for each Bluetooth device.
 */
class DeviceCapabilityDetector @Inject constructor(
    private val gattBatteryReader: GattBatteryReader,
    private val classicBatteryReader: ClassicBatteryReader,
    private val deviceCapabilityRepository: DeviceCapabilityRepository
) {

    /**
     * Detect capabilities for a device.
     */
    suspend fun detectCapabilities(device: BluetoothDevice): DeviceCapabilities {
        return withContext(Dispatchers.IO) {
            // Try GATT first
            val gattSupported = try {
                val result = gattBatteryReader.readBattery(device)
                result.success && result.readMethod == BatteryReadMethod.GATT
            } catch (e: Exception) {
                false
            }

            // Classic Bluetooth is harder to test directly
            // We'll assume it's supported and let the reader handle failures
            val classicSupported = true

            val preferredMethod = when {
                gattSupported -> BatteryReadMethod.GATT
                classicSupported -> BatteryReadMethod.CLASSIC
                else -> BatteryReadMethod.NONE
            }

            val capabilities = DeviceCapabilities(
                deviceId = device.address,
                supportsGattBattery = gattSupported,
                supportsClassicBattery = classicSupported,
                preferredMethod = preferredMethod,
                lastDetected = Instant.now()
            )

            deviceCapabilityRepository.saveCapabilities(capabilities)
            
            capabilities
        }
    }

    /**
     * Get the preferred reading method for a device.
     */
    suspend fun getPreferredMethod(deviceId: String): BatteryReadMethod {
        return deviceCapabilityRepository.getPreferredMethod(deviceId)
    }

    /**
     * Check if a device supports GATT battery reading.
     */
    suspend fun supportsGatt(deviceId: String): Boolean {
        return deviceCapabilityRepository.supportsGatt(deviceId)
    }

    /**
     * Check if a device supports Classic Bluetooth battery reading.
     */
    suspend fun supportsClassic(deviceId: String): Boolean {
        return deviceCapabilityRepository.supportsClassic(deviceId)
    }
}
