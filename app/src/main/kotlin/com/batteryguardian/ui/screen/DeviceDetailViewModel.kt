package com.batteryguardian.ui.screen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batteryguardian.domain.model.BatteryHealth
import com.batteryguardian.domain.model.BatteryPrediction
import com.batteryguardian.domain.model.Device
import com.batteryguardian.domain.usecase.GetDeviceHistoryUseCase
import com.batteryguardian.domain.usecase.ManageDevicesUseCase
import com.batteryguardian.domain.usecase.MonitorBatteryUseCase
import com.batteryguardian.domain.usecase.PredictBatteryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the device detail screen.
 * 
 * Provides detailed information about a specific device including:
 * - Current battery level
 * - Battery history
 * - Battery health metrics
 * - Predictions
 */
@HiltViewModel
class DeviceDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val manageDevicesUseCase: ManageDevicesUseCase,
    private val getDeviceHistoryUseCase: GetDeviceHistoryUseCase,
    private val predictBatteryUseCase: PredictBatteryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<DeviceDetailUiState>(DeviceDetailUiState.Loading)
    val uiState: StateFlow<DeviceDetailUiState> = _uiState.asStateFlow()

    /**
     * Load device data.
     */
    fun loadDevice(deviceId: String) {
        viewModelScope.launch {
            _uiState.value = DeviceDetailUiState.Loading
            
            try {
                // Get device
                val deviceFlow = manageDevicesUseCase.getDevice(deviceId)
                
                // Get battery health
                val healthFlow = predictBatteryUseCase.getBatteryHealth(deviceId)
                
                // Get predictions
                val predictionsFlow = predictBatteryUseCase.getPredictions(deviceId)
                
                // Combine all data
                combine(
                    deviceFlow,
                    healthFlow,
                    predictionsFlow
                ) { device, health, predictions ->
                    DeviceDetailUiState.Success(
                        device = device ?: throw IllegalStateException("Device not found"),
                        batteryHealth = health,
                        predictions = predictions
                    )
                }
                    .catch { e ->
                        _uiState.value = DeviceDetailUiState.Error(
                            e.message ?: "Unknown error"
                        )
                    }
                    .collect { state ->
                        _uiState.value = state
                    }
            } catch (e: Exception) {
                _uiState.value = DeviceDetailUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Toggle monitoring for the device.
     */
    fun toggleMonitoring() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is DeviceDetailUiState.Success) {
                manageDevicesUseCase.toggleMonitoring(currentState.device.id)
            }
        }
    }

    /**
     * Toggle ignore status for the device.
     */
    fun toggleIgnore() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is DeviceDetailUiState.Success) {
                manageDevicesUseCase.toggleIgnore(currentState.device.id)
            }
        }
    }

    /**
     * Rename the device.
     */
    fun renameDevice(newName: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is DeviceDetailUiState.Success) {
                manageDevicesUseCase.renameDevice(
                    currentState.device.id,
                    newName
                )
            }
        }
    }

    /**
     * Set device alias.
     */
    fun setDeviceAlias(alias: String?) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is DeviceDetailUiState.Success) {
                manageDevicesUseCase.setDeviceAlias(
                    currentState.device.id,
                    alias
                )
            }
        }
    }

    /**
     * Refresh device data.
     */
    fun refresh() {
        val currentState = _uiState.value
        if (currentState is DeviceDetailUiState.Success) {
            loadDevice(currentState.device.id)
        }
    }
}

/**
 * UI state for the device detail screen.
 */
sealed class DeviceDetailUiState {
    object Loading : DeviceDetailUiState()
    data class Error(val message: String) : DeviceDetailUiState()
    data class Success(
        val device: Device,
        val batteryHealth: BatteryHealth?,
        val predictions: List<BatteryPrediction>
    ) : DeviceDetailUiState()
}
