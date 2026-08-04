package com.batteryguardian.data.repository

import com.batteryguardian.data.local.dao.AlertStateDao
import com.batteryguardian.data.local.dao.AlertThresholdDao
import com.batteryguardian.data.local.entity.AlertStateEntity
import com.batteryguardian.data.local.entity.AlertThresholdEntity
import com.batteryguardian.domain.model.AlertEvent
import com.batteryguardian.domain.model.AlertState
import com.batteryguardian.domain.repository.AlertRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

/**
 * Implementation of AlertRepository.
 */
class AlertRepositoryImpl @Inject constructor(
    private val alertStateDao: AlertStateDao,
    private val alertThresholdDao: AlertThresholdDao
) : AlertRepository {

    override fun getAlertState(deviceId: String): Flow<AlertState> {
        return alertStateDao.getByDevice(deviceId)
            .map { entity ->
                entity?.let { mapToDomain(it) } ?: AlertState.Normal
            }
    }

    override fun getAllAlertStates(): Flow<Map<String, AlertState>> {
        return alertStateDao.getAll()
            .map { entities ->
                entities.associateBy({ it.deviceId }, { mapToDomain(it) })
            }
    }

    override fun getDevicesInAlert(): Flow<List<String>> {
        return alertStateDao.getDevicesInAlert()
    }

    override suspend fun updateAlertState(
        deviceId: String,
        state: AlertState
    ) {
        val entity = mapToEntity(deviceId, state)
        alertStateDao.insert(entity)
    }

    override suspend fun clearAlertState(deviceId: String) {
        alertStateDao.delete(deviceId)
    }

    override suspend fun clearAllAlertStates() {
        alertStateDao.deleteAll()
    }

    override suspend fun isInAlert(deviceId: String): Boolean {
        return alertStateDao.getDevicesInAlert().value.contains(deviceId)
    }

    override fun getLastAlertEvent(deviceId: String): Flow<AlertEvent?> {
        // In a real implementation, this would query an alert_events table
        return alertStateDao.getByDevice(deviceId)
            .map { entity ->
                entity?.let {
                    // Map to AlertEvent based on state
                    when (val state = mapToDomain(it)) {
                        is AlertState.Low -> AlertEvent.LowBattery(
                            deviceId = deviceId,
                            currentLevel = state.threshold,
                            threshold = state.threshold
                        )
                        is AlertState.High -> AlertEvent.Prediction(
                            deviceId = deviceId,
                            milestone = state.threshold,
                            estimatedTime = Instant.now()
                        )
                        else -> null
                    }
                }
            }
    }

    override fun getAllAlertEvents(): Flow<List<AlertEvent>> {
        // In a real implementation, this would query an alert_events table
        return alertStateDao.getAll()
            .map { entities ->
                entities.mapNotNull { entity ->
                    when (val state = mapToDomain(entity)) {
                        is AlertState.Low -> AlertEvent.LowBattery(
                            deviceId = entity.deviceId,
                            currentLevel = state.threshold,
                            threshold = state.threshold
                        )
                        is AlertState.High -> AlertEvent.Prediction(
                            deviceId = entity.deviceId,
                            milestone = state.threshold,
                            estimatedTime = Instant.now()
                        )
                        else -> null
                    }
                }
            }
    }

    override suspend fun addAlertEvent(event: AlertEvent) {
        // In a real implementation, this would insert into an alert_events table
        // For now, we just update the alert state
        val state = when (event) {
            is AlertEvent.LowBattery -> AlertState.Low(event.threshold, Instant.now())
            is AlertEvent.Prediction -> AlertState.Low(event.milestone, Instant.now())
            is AlertEvent.HealthWarning -> AlertState.Normal
        }
        updateAlertState(event.deviceId, state)
    }

    override fun getAlertHistory(deviceId: String): Flow<List<AlertEvent>> {
        // In a real implementation, this would query an alert_events table
        return alertStateDao.getByDevice(deviceId)
            .map { entity ->
                entity?.let {
                    listOf(
                        AlertEvent.LowBattery(
                            deviceId = deviceId,
                            currentLevel = it.lastAlertThreshold ?: 20,
                            threshold = it.lastAlertThreshold ?: 20
                        )
                    )
                } ?: emptyList()
            }
    }

    override suspend fun cleanupOldAlertEvents(days: Int) {
        // In a real implementation, this would clean up old alert events
    }

    /**
     * Map AlertStateEntity to AlertState domain model.
     */
    private fun mapToDomain(entity: AlertStateEntity): AlertState {
        return when {
            entity.currentState.startsWith("LOW:") -> {
                val threshold = entity.currentState.substringAfter(":").toInt()
                AlertState.Low(threshold, entity.lastAlertTimestamp ?: Instant.now())
            }
            entity.currentState.startsWith("HIGH:") -> {
                val threshold = entity.currentState.substringAfter(":").toInt()
                AlertState.High(threshold, entity.lastAlertTimestamp ?: Instant.now())
            }
            else -> AlertState.Normal
        }
    }

    /**
     * Map AlertState domain model to AlertStateEntity.
     */
    private fun mapToEntity(deviceId: String, state: AlertState): AlertStateEntity {
        return when (state) {
            is AlertState.Normal -> AlertStateEntity(
                deviceId = deviceId,
                currentState = "NORMAL",
                lastAlertThreshold = null,
                lastAlertTimestamp = null,
                hysteresisBand = 2
            )
            is AlertState.Low -> AlertStateEntity(
                deviceId = deviceId,
                currentState = "LOW:${state.threshold}",
                lastAlertThreshold = state.threshold,
                lastAlertTimestamp = state.triggeredAt,
                hysteresisBand = 2
            )
            is AlertState.High -> AlertStateEntity(
                deviceId = deviceId,
                currentState = "HIGH:${state.threshold}",
                lastAlertThreshold = state.threshold,
                lastAlertTimestamp = state.triggeredAt,
                hysteresisBand = 2
            )
        }
    }
}
