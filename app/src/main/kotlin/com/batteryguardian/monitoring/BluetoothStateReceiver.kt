package com.batteryguardian.monitoring

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Broadcast receiver for handling Bluetooth state changes.
 * 
 * Notifies the BatteryMonitor when Bluetooth is enabled or disabled.
 */
@AndroidEntryPoint
class BluetoothStateReceiver : BroadcastReceiver() {

    @Inject
    lateinit var batteryMonitor: BatteryMonitor

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
            val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
            
            when (state) {
                BluetoothAdapter.STATE_ON -> {
                    // Bluetooth enabled, resume monitoring
                    batteryMonitor.refresh()
                }
                BluetoothAdapter.STATE_OFF -> {
                    // Bluetooth disabled, pause monitoring
                    // In a real implementation, we might show a notification
                }
                BluetoothAdapter.STATE_TURNING_ON -> {
                    // Bluetooth is turning on
                }
                BluetoothAdapter.STATE_TURNING_OFF -> {
                    // Bluetooth is turning off
                }
            }
        }
    }
}
