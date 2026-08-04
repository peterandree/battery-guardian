package com.batteryguardian.monitoring

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Broadcast receiver for handling device boot completion.
 * 
 * Restarts the BatteryMonitorService after the device reboots.
 */
@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var batteryMonitorServiceIntent: Intent

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_QUICKBOOT_POWERON ||
            intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            
            startBatteryMonitorService(context)
        }
    }

    /**
     * Start the BatteryMonitorService.
     */
    private fun startBatteryMonitorService(context: Context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(batteryMonitorServiceIntent)
        } else {
            context.startService(batteryMonitorServiceIntent)
        }
    }
}
