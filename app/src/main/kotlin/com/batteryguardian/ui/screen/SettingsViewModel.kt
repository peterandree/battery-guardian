package com.batteryguardian.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batteryguardian.domain.model.BatteryDisplayFormat
import com.batteryguardian.domain.model.NotificationPriority
import com.batteryguardian.domain.model.UserPreferences
import com.batteryguardian.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the settings screen.
 * 
 * Provides user preferences and handles preference changes.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadPreferences()
    }

    /**
     * Load user preferences.
     */
    private fun loadPreferences() {
        viewModelScope.launch {
            userPreferencesRepository.preferences.collect { preferences ->
                _uiState.value = SettingsUiState.Success(preferences)
            }
        }
    }

    /**
     * Toggle dark theme.
     */
    fun toggleDarkTheme() {
        viewModelScope.launch {
            val current = _uiState.value
            if (current is SettingsUiState.Success) {
                userPreferencesRepository.setDarkTheme(!current.preferences.darkTheme)
            }
        }
    }

    /**
     * Set polling interval.
     */
    fun setPollingInterval(minutes: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setPollingInterval(minutes)
        }
    }

    /**
     * Set low battery threshold.
     */
    fun setLowThreshold(percent: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setLowThreshold(percent)
        }
    }

    /**
     * Set medium battery threshold.
     */
    fun setMediumThreshold(percent: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setMediumThreshold(percent)
        }
    }

    /**
     * Set critical battery threshold.
     */
    fun setCriticalThreshold(percent: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setCriticalThreshold(percent)
        }
    }

    /**
     * Set hysteresis band.
     */
    fun setHysteresisBand(percent: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setHysteresisBand(percent)
        }
    }

    /**
     * Toggle notifications.
     */
    fun toggleNotifications() {
        viewModelScope.launch {
            val current = _uiState.value
            if (current is SettingsUiState.Success) {
                userPreferencesRepository.setNotificationsEnabled(
                    !current.preferences.notificationsEnabled
                )
            }
        }
    }

    /**
     * Set notification priority.
     */
    fun setNotificationPriority(priority: NotificationPriority) {
        viewModelScope.launch {
            userPreferencesRepository.setNotificationPriority(priority)
        }
    }

    /**
     * Set battery display format.
     */
    fun setBatteryDisplayFormat(format: BatteryDisplayFormat) {
        viewModelScope.launch {
            userPreferencesRepository.setBatteryDisplayFormat(format)
        }
    }

    /**
     * Reset all preferences to defaults.
     */
    fun resetToDefaults() {
        viewModelScope.launch {
            userPreferencesRepository.resetToDefaults()
        }
    }
}

/**
 * UI state for the settings screen.
 */
sealed class SettingsUiState {
    object Loading : SettingsUiState()
    data class Success(val preferences: UserPreferences) : SettingsUiState()
}
