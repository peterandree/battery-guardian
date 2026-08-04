package com.batteryguardian.monitoring

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.batteryguardian.BatteryGuardianApplication
import com.batteryguardian.MainActivity
import com.batteryguardian.R
import com.batteryguardian.domain.model.NotificationPriority
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for showing battery alert notifications.
 * 
 * Handles creating notification channels and displaying notifications
 * for battery level alerts.
 */
@Singleton
class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    /**
     * Initialize notification channels.
     * 
     * Must be called before showing any notifications.
     */
    fun initialize() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createAlertsChannel()
        }
    }

    /**
     * Create the alerts notification channel.
     */
    private fun createAlertsChannel() {
        val channel = NotificationChannel(
            ALERTS_CHANNEL_ID,
            context.getString(R.string.notification_channel_alerts),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Battery level alerts"
            setShowBadge(true)
            enableLights(true)
            enableVibration(true)
            lightColor = android.graphics.Color.RED
        }
        
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Show a battery alert notification.
     * 
     * @param deviceId The device ID that triggered the alert
     * @param deviceName The user-friendly name of the device
     * @param batteryLevel The current battery level
     * @param threshold The threshold that was crossed
     * @param priority The notification priority
     */
    fun showBatteryAlert(
        deviceId: String,
        deviceName: String,
        batteryLevel: Int,
        threshold: Int,
        priority: NotificationPriority = NotificationPriority.DEFAULT
    ) {
        val notificationId = "$deviceId-$threshold".hashCode()
        
        val contentIntent = PendingIntent.getActivity(
            context,
            notificationId,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, ALERTS_CHANNEL_ID)
            .setContentTitle(context.getString(R.string.notification_title_low_battery))
            .setContentText(
                context.getString(
                    R.string.notification_message,
                    deviceName,
                    batteryLevel
                )
            )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(getNotificationPriority(priority))
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .setOnlyAlertOnce(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    /**
     * Show a battery prediction notification.
     * 
     * @param deviceId The device ID
     * @param deviceName The user-friendly name of the device
     * @param milestone The battery milestone (e.g., 20%)
     * @param timeUntil The time until the milestone is reached
     */
    fun showPredictionAlert(
        deviceId: String,
        deviceName: String,
        milestone: Int,
        timeUntil: String
    ) {
        val notificationId = "prediction-$deviceId-$milestone".hashCode()
        
        val contentIntent = PendingIntent.getActivity(
            context,
            notificationId,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, ALERTS_CHANNEL_ID)
            .setContentTitle(context.getString(R.string.notification_title_prediction))
            .setContentText(
                context.getString(
                    R.string.notification_message_prediction,
                    deviceName,
                    milestone,
                    timeUntil
                )
            )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .setOnlyAlertOnce(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    /**
     * Show a critical battery alert that bypasses Do Not Disturb.
     */
    fun showCriticalAlert(deviceId: String, deviceName: String, batteryLevel: Int) {
        showBatteryAlert(
            deviceId = deviceId,
            deviceName = deviceName,
            batteryLevel = batteryLevel,
            threshold = 5,
            priority = NotificationPriority.URGENT
        )
    }

    /**
     * Cancel a notification.
     */
    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    /**
     * Cancel all notifications for a device.
     */
    fun cancelNotificationsForDevice(deviceId: String) {
        // Cancel all notifications for this device
        listOf(20, 10, 5).forEach { threshold ->
            val notificationId = "$deviceId-$threshold".hashCode()
            notificationManager.cancel(notificationId)
        }
        
        // Also cancel prediction notifications
        listOf(20, 10, 5).forEach { milestone ->
            val notificationId = "prediction-$deviceId-$milestone".hashCode()
            notificationManager.cancel(notificationId)
        }
    }

    /**
     * Cancel all notifications.
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }

    /**
     * Convert domain NotificationPriority to Android NotificationCompat priority.
     */
    private fun getNotificationPriority(
        priority: NotificationPriority
    ): Int {
        return when (priority) {
            NotificationPriority.LOW -> NotificationCompat.PRIORITY_LOW
            NotificationPriority.DEFAULT -> NotificationCompat.PRIORITY_DEFAULT
            NotificationPriority.HIGH -> NotificationCompat.PRIORITY_HIGH
            NotificationPriority.URGENT -> NotificationCompat.PRIORITY_MAX
        }
    }

    companion object {
        /** Channel ID for battery alerts */
        const val ALERTS_CHANNEL_ID = BatteryGuardianApplication.ALERTS_CHANNEL_ID
    }
}
