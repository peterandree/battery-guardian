package com.batteryguardian.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batteryguardian.domain.repository.BatteryLevel
import com.batteryguardian.domain.usecase.GetDeviceHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the battery history screen.
 * 
 * Provides battery level history for a device and handles time range selection.
 */
@HiltViewModel
class BatteryHistoryViewModel @Inject constructor(
    private val getDeviceHistoryUseCase: GetDeviceHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<BatteryHistoryUiState>(BatteryHistoryUiState.Loading)
    val uiState: StateFlow<BatteryHistoryUiState> = _uiState.asStateFlow()

    private var currentDeviceId: String = ""

    /**
     * Load battery history for a device.
     */
    fun loadBatteryHistory(deviceId: String) {
        currentDeviceId = deviceId
        viewModelScope.launch {
            _uiState.value = BatteryHistoryUiState.Loading
            
            getDeviceHistoryUseCase.getBatteryHistory(
                deviceId = deviceId,
                timeRange = TimeRange.LAST_24_HOURS
            )
                .catch { e ->
                    _uiState.value = BatteryHistoryUiState.Error(
                        e.message ?: "Failed to load battery history"
                    )
                }
                .collectLatest { batteryLevels ->
                    val statistics = calculateStatistics(batteryLevels)
                    _uiState.value = BatteryHistoryUiState.Success(
                        batteryLevels = batteryLevels,
                        statistics = statistics,
                        selectedTimeRange = TimeRange.LAST_24_HOURS
                    )
                }
        }
    }

    /**
     * Set the selected time range.
     */
    fun setTimeRange(timeRange: TimeRange) {
        viewModelScope.launch {
            _uiState.value = BatteryHistoryUiState.Loading
            
            getDeviceHistoryUseCase.getBatteryHistory(
                deviceId = currentDeviceId,
                timeRange = timeRange
            )
                .catch { e ->
                    _uiState.value = BatteryHistoryUiState.Error(
                        e.message ?: "Failed to load battery history"
                    )
                }
                .collectLatest { batteryLevels ->
                    val statistics = calculateStatistics(batteryLevels)
                    _uiState.value = BatteryHistoryUiState.Success(
                        batteryLevels = batteryLevels,
                        statistics = statistics,
                        selectedTimeRange = timeRange
                    )
                }
        }
    }

    /**
     * Calculate statistics from battery levels.
     */
    private fun calculateStatistics(levels: List<BatteryLevel>): BatteryStatistics {
        if (levels.isEmpty()) {
            return BatteryStatistics(
                averageLevel = 0f,
                minLevel = 0,
                maxLevel = 0,
                averageDrainRate = 0f,
                totalDischargeCycles = 0,
                averageDischargeTime = java.time.Duration.ZERO
            )
        }
        
        val averageLevel = levels.map { it.level.toFloat() }.average().toFloat()
        val minLevel = levels.minOf { it.level }
        val maxLevel = levels.maxOf { it.level }
        
        // Calculate average drain rate (simplified)
        val sortedLevels = levels.sortedBy { it.timestamp }
        var totalDrain = 0f
        var totalTimeHours = 0f
        
        for (i in 1 until sortedLevels.size) {
            val levelDiff = sortedLevels[i].level - sortedLevels[i-1].level
            val timeDiff = java.time.Duration.between(
                sortedLevels[i-1].timestamp,
                sortedLevels[i].timestamp
            ).toHours().toFloat()
            
            if (timeDiff > 0) {
                totalDrain += levelDiff
                totalTimeHours += timeDiff
            }
        }
        
        val averageDrainRate = if (totalTimeHours > 0) {
            totalDrain / totalTimeHours
        } else {
            0f
        }
        
        // Count discharge cycles (simplified)
        var dischargeCycles = 0
        var lastLevel = sortedLevels.first().level
        
        for (i in 1 until sortedLevels.size) {
            val currentLevel = sortedLevels[i].level
            
            // Detect discharge from high to low
            if (lastLevel > 80 && currentLevel <= 20) {
                dischargeCycles++
            }
            
            lastLevel = currentLevel
        }
        
        // Calculate average discharge time (simplified)
        val averageDischargeTime = if (dischargeCycles > 0) {
            java.time.Duration.ofHours((totalTimeHours / dischargeCycles).toLong())
        } else {
            java.time.Duration.ZERO
        }
        
        return BatteryStatistics(
            averageLevel = averageLevel,
            minLevel = minLevel,
            maxLevel = maxLevel,
            averageDrainRate = averageDrainRate,
            totalDischargeCycles = dischargeCycles,
            averageDischargeTime = averageDischargeTime
        )
    }
}
