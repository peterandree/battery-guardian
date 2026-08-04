package com.batteryguardian

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

/**
 * Main application class for Battery Guardian.
 * 
 * Initializes Hilt dependency injection and creates notification channels.
 */
@HiltAndroidApp
class BatteryGuardianApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Create notification channels
        createNotificationChannels()
    }

    /**
     * Creates the notification channels required by the app.
     * 
     * This must be called in onCreate() to ensure channels exist before any notifications
     * are shown.
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) 
                as NotificationManager
            
            // Monitoring channel - for the foreground service notification
            val monitoringChannel = NotificationChannel(
                MONITORING_CHANNEL_ID,
                getString(com.batteryguardian.R.string.notification_channel_monitoring),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background monitoring service"
                setShowBadge(false)
                lockscreenVisibility = NotificationManager.IMPORTANCE_LOW
            }
            
            // Alerts channel - for battery level alerts
            val alertsChannel = NotificationChannel(
                ALERTS_CHANNEL_ID,
                getString(com.batteryguardian.R.string.notification_channel_alerts),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Battery level alerts"
                setShowBadge(true)
                enableLights(true)
                enableVibration(true)
            }
            
            notificationManager.createNotificationChannel(monitoringChannel)
            notificationManager.createNotificationChannel(alertsChannel)
        }
    }

    companion object {
        /** Channel ID for foreground service monitoring notification */
        const val MONITORING_CHANNEL_ID = "battery_guardian_monitoring"
        
        /** Channel ID for battery alert notifications */
        const val ALERTS_CHANNEL_ID = "battery_guardian_alerts"
        
        /** Notification ID for foreground service */
        const val MONITORING_NOTIFICATION_ID = 1
    }
}
