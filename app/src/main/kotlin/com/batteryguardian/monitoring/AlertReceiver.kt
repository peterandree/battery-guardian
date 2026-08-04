package com.batteryguardian.monitoring

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Broadcast receiver for handling battery alerts.
 * 
 * This receiver is triggered by AlarmManager for critical alerts
 * that need to wake the device from Doze Mode.
 */
class AlertReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_CRITICAL_ALERT -> {
                val deviceId = intent.getStringExtra(EXTRA_DEVICE_ID)
                if (deviceId != null) {
                    handleCriticalAlert(context, deviceId)
                }
            }
        }
    }

    /**
     * Handle a critical battery alert.
     */
    private fun handleCriticalAlert(context: Context, deviceId: String) {
        // In a real implementation, this would:
        // 1. Show a high-priority notification
        // 2. Play a sound (if enabled)
        // 3. Vibrate (if enabled)
        // 4. Wake up the screen (if enabled)
        
        // For now, just show a notification
        // NotificationService.showCriticalAlert(context, deviceId)
    }

    companion object {
        /** Action for critical alerts */
        const val ACTION_CRITICAL_ALERT = "com.batteryguardian.action.CRITICAL_ALERT"

        /** Extra for device ID */
        const val EXTRA_DEVICE_ID = "device_id"
    }
}
