package com.batteryguardian.monitoring

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.batteryguardian.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Orchestrates periodic battery level polling.
 * 
 * Uses WorkManager to schedule periodic background work for polling
 * battery levels from Bluetooth devices.
 */
class PollingOrchestrator @Inject constructor(
    private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository
) {

    private val workManager = WorkManager.getInstance(context)
    private val orchestratorScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isPolling = false

    /**
     * Start periodic polling.
     */
    fun startPolling(intervalMinutes: Int) {
        if (isPolling) return
        
        isPolling = true
        schedulePolling(intervalMinutes)
    }

    /**
     * Start polling with the current user preference.
     */
    fun startPolling() {
        orchestratorScope.launch {
            val preferences = userPreferencesRepository.preferences.first()
            startPolling(preferences.pollingInterval)
        }
    }

    /**
     * Stop periodic polling.
     */
    fun stopPolling() {
        if (!isPolling) return
        
        isPolling = false
        workManager.cancelAllWorkByTag(POLLING_TAG)
    }

    /**
     * Update the polling interval.
     */
    fun updatePollingInterval(minutes: Int) {
        stopPolling()
        startPolling(minutes)
    }

    /**
     * Schedule periodic polling work.
     */
    private fun schedulePolling(intervalMinutes: Int) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        // WorkManager has a minimum interval of 15 minutes
        val actualInterval = maxOf(intervalMinutes, 15)

        val pollingRequest = PeriodicWorkRequestBuilder<
            BatteryPollingWorker>(actualInterval.toLong(), TimeUnit.MINUTES)
            .setConstraints(constraints)
            .addTag(POLLING_TAG)
            .build()

        workManager.enqueueUniquePeriodicWork(
            POLLING_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            pollingRequest
        )
    }

    /**
     * Trigger an immediate poll.
     */
    fun triggerImmediatePoll() {
        // In a real implementation, this would trigger a one-time work request
        // For now, we just notify the BatteryMonitor to scan all devices
    }

    companion object {
        /** Tag for polling work */
        private const val POLLING_TAG = "battery_polling"

        /** Name for polling work */
        private const val POLLING_WORK_NAME = "batteryPollingWork"
    }
}

/**
 * Worker for performing battery polling in the background.
 */
class BatteryPollingWorker(
    context: Context,
    workerParams: androidx.work.WorkerParameters
) : androidx.work.CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        // In a real implementation, this would:
        // 1. Get all monitored devices
        // 2. Read battery levels for each device
        // 3. Update repository with new data
        // 4. Check for alerts
        
        // For now, just return success
        return Result.success()
    }
}
