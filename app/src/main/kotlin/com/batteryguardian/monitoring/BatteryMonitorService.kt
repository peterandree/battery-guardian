package com.batteryguardian.monitoring

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.batteryguardian.BatteryGuardianApplication
import com.batteryguardian.MainActivity
import com.batteryguardian.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service for monitoring Bluetooth device batteries.
 * 
 * This service runs continuously in the background to monitor battery levels
 * and trigger alerts when thresholds are crossed.
 */
@AndroidEntryPoint
class BatteryMonitorService : Service() {

    @Inject
    lateinit var batteryMonitor: BatteryMonitor

    @Inject
    lateinit var notificationManager: NotificationManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val binder = LocalBinder()

    /**
     * Local binder for clients to bind to this service.
     */
    inner class LocalBinder : IBinder {
        fun getService(): BatteryMonitorService = this@BatteryMonitorService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        startMonitoring()
        return START_STICKY
    }

    override fun onDestroy() {
        stopMonitoring()
        serviceScope.cancel()
        super.onDestroy()
    }

    /**
     * Start battery monitoring.
     */
    private fun startMonitoring() {
        serviceScope.launch {
            batteryMonitor.start()
        }
    }

    /**
     * Stop battery monitoring.
     */
    private fun stopMonitoring() {
        serviceScope.launch {
            batteryMonitor.stop()
        }
    }

    /**
     * Create the notification channel for foreground service.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_monitoring),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background monitoring service"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_SECRET
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Create the foreground notification.
     */
    private fun createNotification(): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            REQUEST_CONTENT,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.app_description))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentIntent)
            .build()
    }

    companion object {
        /** Channel ID for foreground service notification */
        private const val CHANNEL_ID = BatteryGuardianApplication.MONITORING_CHANNEL_ID

        /** Notification ID for foreground service */
        private const val NOTIFICATION_ID = BatteryGuardianApplication.MONITORING_NOTIFICATION_ID

        /** Request code for content intent */
        private const val REQUEST_CONTENT = 1001
    }
}
