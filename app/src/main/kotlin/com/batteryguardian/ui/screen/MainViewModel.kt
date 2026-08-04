package com.batteryguardian.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batteryguardian.domain.model.Device
import com.batteryguardian.domain.usecase.MonitorBatteryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the main screen.
 * 
 * Provides the list of devices with their battery levels to the UI.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val monitorBatteryUseCase: MonitorBatteryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var isMonitoring = false

    init {
        startMonitoring()
    }

    override fun onCleared() {
        stopMonitoring()
        super.onCleared()
    }

    /**
     * Start monitoring battery levels.
     */
    private fun startMonitoring() {
        if (isMonitoring) return
        
        isMonitoring = true
        viewModelScope.launch {
            monitorBatteryUseCase.getMonitoredDevicesWithBattery()
                .catch { e ->
                    _uiState.value = MainUiState.Error(e.message ?: "Unknown error")
                }
                .collectLatest { devices ->
                    _uiState.value = MainUiState.Success(devices)
                }
        }
    }

    /**
     * Stop monitoring battery levels.
     */
    private fun stopMonitoring() {
        if (!isMonitoring) return
        
        isMonitoring = false
        monitorBatteryUseCase.stopMonitoring()
    }

    /**
     * Refresh battery levels for all devices.
     */
    fun refresh() {
        viewModelScope.launch {
            _uiState.value = MainUiState.Loading
            monitorBatteryUseCase.refreshAllBatteryLevels()
        }
    }

    /**
     * Clear the current error.
     */
    fun clearError() {
        viewModelScope.launch {
            if (_uiState.value is MainUiState.Error) {
                _uiState.value = MainUiState.Loading
                startMonitoring()
            }
        }
    }
}

/**
 * UI state for the main screen.
 */
sealed class MainUiState {
    object Loading : MainUiState()
    data class Error(val message: String) : MainUiState()
    data class Success(val devices: List<Device>) : MainUiState()
}
