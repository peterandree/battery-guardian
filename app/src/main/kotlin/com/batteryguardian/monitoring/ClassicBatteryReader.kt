package com.batteryguardian.monitoring

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.batteryguardian.domain.model.BatteryReadMethod
import com.batteryguardian.domain.model.BatteryReadingResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject

/**
 * Reads battery levels from Classic Bluetooth devices via broadcasts.
 * 
 * This reader listens for ACTION_BATTERY_LEVEL_CHANGED broadcasts
 * and extracts the battery level from the intent extras.
 */
class ClassicBatteryReader @Inject constructor(
    private val context: Context
) {

    private val batteryLevels = mutableMapOf<String, Int>()
    private var isRegistered = false

    /**
     * Start listening for battery level broadcasts.
     */
    fun start() {
        if (isRegistered) return

        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_BATTERY_LEVEL_CHANGED)
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                batteryReceiver,
                filter,
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            context.registerReceiver(batteryReceiver, filter)
        }

        isRegistered = true
    }

    /**
     * Stop listening for battery level broadcasts.
     */
    fun stop() {
        if (!isRegistered) return
        context.unregisterReceiver(batteryReceiver)
        isRegistered = false
    }

    /**
     * Read battery level from a Classic Bluetooth device.
     * 
     * @param device The Bluetooth device to read from
     * @return BatteryReadingResult with the battery level or null if not available
     */
    suspend fun readBattery(device: BluetoothDevice): BatteryReadingResult {
        return withContext(Dispatchers.IO) {
            val level = batteryLevels[device.address]

            BatteryReadingResult(
                deviceId = device.address,
                batteryLevel = level,
                isCharging = null, // Classic Bluetooth doesn't provide charging state
                readMethod = BatteryReadMethod.CLASSIC,
                timestamp = Instant.now(),
                success = level != null
            )
        }
    }

    /**
     * Get the battery level for a device by address.
     */
    fun getBatteryLevel(deviceAddress: String): Int? {
        return batteryLevels[deviceAddress]
    }

    /**
     * Broadcast receiver for battery level changes.
     */
    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == BluetoothDevice.ACTION_BATTERY_LEVEL_CHANGED) {
                val device = intent.getParcelableExtra<BluetoothDevice>(
                    BluetoothDevice.EXTRA_DEVICE
                )
                val level = intent.getIntExtra(
                    BluetoothDevice.EXTRA_BATTERY_LEVEL, -1
                )

                device?.let { d ->
                    if (level in 0..100) {
                        batteryLevels[d.address] = level
                    }
                }
            }
        }
    }
}
