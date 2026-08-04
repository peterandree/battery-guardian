package com.batteryguardian.monitoring

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.content.Context
import com.batteryguardian.domain.model.BatteryReadMethod
import com.batteryguardian.domain.model.BatteryReadingResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.time.Instant
import java.util.UUID
import java.util.concurrent.Semaphore
import javax.inject.Inject

/**
 * Reads battery levels from BLE devices using GATT Battery Service.
 * 
 * This reader uses the standard Bluetooth Battery Service (UUID: 0x180F)
 * to read battery levels from BLE devices.
 */
class GattBatteryReader @Inject constructor(
    private val context: Context
) {

    private val connectionCache = mutableMapOf<String, BluetoothGatt>()
    private val semaphore = Semaphore(MAX_CONCURRENT_READS)
    private val timeout = Duration.ofSeconds(5)

    /**
     * Read battery level from a BLE device.
     * 
     * @param device The Bluetooth device to read from
     * @return BatteryReadingResult with the battery level or error
     */
    suspend fun readBattery(device: BluetoothDevice): BatteryReadingResult {
        return withContext(Dispatchers.IO) {
            semaphore.acquire()
            try {
                val gatt = getOrCreateGatt(device)
                val batteryLevel = readBatteryLevel(gatt)
                val isCharging = readChargingState(gatt)

                BatteryReadingResult(
                    deviceId = device.address,
                    batteryLevel = batteryLevel,
                    isCharging = isCharging,
                    readMethod = BatteryReadMethod.GATT,
                    timestamp = Instant.now(),
                    success = batteryLevel != null
                )
            } catch (e: Exception) {
                connectionCache.remove(device.address)
                BatteryReadingResult(
                    deviceId = device.address,
                    batteryLevel = null,
                    isCharging = null,
                    readMethod = BatteryReadMethod.GATT,
                    timestamp = Instant.now(),
                    success = false,
                    error = e.message
                )
            } finally {
                semaphore.release()
            }
        }
    }

    /**
     * Get or create a GATT connection for a device.
     */
    private suspend fun getOrCreateGatt(device: BluetoothDevice): BluetoothGatt {
        return connectionCache[device.address] ?: run {
            val gatt = withTimeoutOrNull(timeout.toMillis()) {
                device.connectGatt(
                    context,
                    false,
                    gattCallback,
                    BluetoothDevice.TRANSPORT_LE
                )
            } ?: throw TimeoutException("Failed to connect to device within timeout")
            
            connectionCache[device.address] = gatt
            // Wait for connection to be established
            withTimeoutOrNull(timeout.toMillis()) {
                while (gatt.connectionState != BluetoothProfile.STATE_CONNECTED) {
                    // Wait for connection
                    kotlinx.coroutines.delay(100)
                }
            } ?: throw TimeoutException("Failed to establish connection within timeout")
            
            gatt
        }
    }

    /**
     * Read battery level from GATT Battery Service.
     */
    private suspend fun readBatteryLevel(gatt: BluetoothGatt): Int? {
        val batteryService = gatt.getService(BATTERY_SERVICE_UUID)
            ?: return null

        val batteryChar = batteryService.getCharacteristic(BATTERY_LEVEL_CHAR_UUID)
            ?: return null

        val value = withTimeoutOrNull(timeout.toMillis()) {
            batteryChar.readValue()
        } ?: return null

        return if (value.isNotEmpty()) value[0].toInt() and 0xFF else null
    }

    /**
     * Read charging state from GATT characteristics.
     */
    private suspend fun readChargingState(gatt: BluetoothGatt): Boolean? {
        // Try Battery Status characteristic first (BT 2.0)
        var charging = tryReadChargingStateFromCharacteristic(
            gatt, BATTERY_STATUS_CHAR_UUID
        )

        // Fall back to Battery Power State characteristic
        if (charging == null) {
            charging = tryReadChargingStateFromCharacteristic(
                gatt, BATTERY_POWER_STATE_CHAR_UUID
            )
        }

        return charging
    }

    /**
     * Try to read charging state from a specific characteristic.
     */
    private suspend fun tryReadChargingStateFromCharacteristic(
        gatt: BluetoothGatt,
        characteristicUuid: UUID
    ): Boolean? {
        val batteryService = gatt.getService(BATTERY_SERVICE_UUID) ?: return null
        val characteristic = batteryService.getCharacteristic(characteristicUuid)
            ?: return null

        val value = withTimeoutOrNull(timeout.toMillis()) {
            characteristic.readValue()
        } ?: return null

        if (value.isEmpty()) return null

        return if (characteristicUuid == BATTERY_STATUS_CHAR_UUID) {
            // Lower nibble: 0x01 = Charging, 0x02 = Discharging
            (value[0].toInt() and 0x0F) == 0x01
        } else if (characteristicUuid == BATTERY_POWER_STATE_CHAR_UUID) {
            // Bits 6-7: 0b11 (0xC0) = Charging, 0b10 (0x80) = Discharging
            (value[0].toInt() and 0xC0) == 0xC0
        } else {
            null
        }
    }

    /**
     * GATT callback for connection state changes.
     */
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                connectionCache.remove(gatt.device.address)
                gatt.close()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Services discovered, can now read characteristics
            }
        }
    }

    /**
     * Close all cached GATT connections.
     */
    fun close() {
        connectionCache.values.forEach { gatt ->
            gatt.close()
        }
        connectionCache.clear()
    }

    companion object {
        /** Maximum number of concurrent GATT reads */
        private const val MAX_CONCURRENT_READS = 3

        /** Battery Service UUID (Standard Bluetooth SIG) */
        val BATTERY_SERVICE_UUID = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb")

        /** Battery Level Characteristic UUID */
        val BATTERY_LEVEL_CHAR_UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb")

        /** Battery Status Characteristic UUID (BT 2.0) */
        val BATTERY_STATUS_CHAR_UUID = UUID.fromString("00002bea-0000-1000-8000-00805f9b34fb")

        /** Battery Power State Characteristic UUID */
        val BATTERY_POWER_STATE_CHAR_UUID = UUID.fromString("00002a1b-0000-1000-8000-00805f9b34fb")
    }
}
