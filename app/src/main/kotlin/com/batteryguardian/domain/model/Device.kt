package com.batteryguardian.domain.model

import java.time.Instant

/**
 * Represents a Bluetooth device with its current state.
 */
data class Device(
    /** Unique identifier (Bluetooth MAC address) */
    val id: String,
    
    /** User-friendly name (from Bluetooth or user-defined) */
    val name: String,
    
    /** Custom alias set by the user (nullable) */
    val alias: String?,
    
    /** Device type (headphones, speaker, etc.) */
    val type: DeviceType,
    
    /** Device manufacturer (if available) */
    val manufacturer: String?,
    
    /** Bluetooth device class */
    val bluetoothClass: Int?,
    
    /** Timestamp of last time the device was seen */
    val lastSeen: Instant?,
    
    /** Current battery level (0-100, null if unknown) */
    val currentBatteryLevel: Int?,
    
    /** Whether the device is currently charging */
    val isCharging: Boolean?,
    
    /** Whether the device is currently connected */
    val isConnected: Boolean,
    
    /** Whether the device is being monitored */
    val isMonitored: Boolean,
    
    /** Whether the device is on the ignore list */
    val isIgnored: Boolean,
    
    /** Battery health metrics */
    val batteryHealth: BatteryHealth?,
    
    /** Current alert state */
    val alertState: AlertState,
    
    /** Device capabilities */
    val capabilities: DeviceCapabilities?
)

/**
 * Device type classification.
 */
enum class DeviceType {
    HEADPHONES,
    SPEAKER,
    SMARTWATCH,
    KEYBOARD,
    MOUSE,
    GAME_CONTROLLER,
    HEARING_AID,
    MEDICAL_DEVICE,
    OTHER
}

/**
 * Battery health metrics for a device.
 */
data class BatteryHealth(
    /** Average drain rate in percentage per hour (negative when discharging) */
    val averageDrainRate: Float,
    
    /** Predicted time until battery reaches 20% */
    val predictedTimeTo20: Instant?,
    
    /** Predicted time until battery reaches 10% */
    val predictedTimeTo10: Instant?,
    
    /** Predicted time until battery reaches 5% */
    val predictedTimeTo5: Instant?,
    
    /** Battery capacity degradation as percentage (0-100) */
    val capacityDegradation: Float?,
    
    /** Timestamp of last full charge */
    val lastFullCharge: Instant?,
    
    /** Timestamp when health metrics were last updated */
    val lastUpdated: Instant
)

/**
 * Alert state for a device.
 */
sealed class AlertState {
    /** Battery level is above all thresholds */
    object Normal : AlertState()
    
    /** Battery level is at or below a threshold */
    data class Low(
        val threshold: Int,
        val triggeredAt: Instant
    ) : AlertState()
    
    /** Battery level is at or above high threshold (if configured) */
    data class High(
        val threshold: Int,
        val triggeredAt: Instant
    ) : AlertState()
}

/**
 * Device capabilities for battery reading.
 */
data class DeviceCapabilities(
    /** Whether the device supports GATT Battery Service */
    val supportsGattBattery: Boolean,
    
    /** Whether the device supports Classic Bluetooth battery reporting */
    val supportsClassicBattery: Boolean,
    
    /** The preferred method for reading battery from this device */
    val preferredMethod: BatteryReadMethod,
    
    /** Timestamp of last capability detection */
    val lastDetected: Instant?
)

/**
 * Battery reading methods.
 */
enum class BatteryReadMethod {
    GATT,
    CLASSIC,
    MANUAL,
    NONE
}

/**
 * Battery prediction for a specific milestone.
 */
data class BatteryPrediction(
    /** The battery level milestone (e.g., 20, 10, 5) */
    val milestone: Int,
    
    /** Estimated time when the milestone will be reached */
    val estimatedTime: Instant,
    
    /** Confidence in the prediction (0.0 to 1.0) */
    val confidence: Float
)

/**
 * Battery level reading result.
 */
data class BatteryReadingResult(
    /** Device ID (Bluetooth MAC address) */
    val deviceId: String,
    
    /** Battery level (0-100, null if unknown) */
    val batteryLevel: Int?,
    
    /** Whether the device is charging (null if unknown) */
    val isCharging: Boolean?,
    
    /** The method used to read the battery level */
    val readMethod: BatteryReadMethod,
    
    /** Timestamp when the reading was taken */
    val timestamp: Instant,
    
    /** Whether the reading was successful */
    val success: Boolean,
    
    /** Error message if reading failed */
    val error: String? = null
)

/**
 * User preferences for the app.
 */
data class UserPreferences(
    /** Threshold for low battery alerts (percentage) */
    val lowThreshold: Int = 20,
    
    /** Threshold for medium battery alerts (percentage) */
    val mediumThreshold: Int = 10,
    
    /** Threshold for critical battery alerts (percentage) */
    val criticalThreshold: Int = 5,
    
    /** Hysteresis band for alerts (percentage) */
    val hysteresisBand: Int = 2,
    
    /** Polling interval in minutes */
    val pollingInterval: Int = 5,
    
    /** Whether notifications are enabled */
    val notificationsEnabled: Boolean = true,
    
    /** Notification priority for alerts */
    val notificationPriority: NotificationPriority = NotificationPriority.DEFAULT,
    
    /** Whether dark theme is enabled */
    val darkTheme: Boolean = false,
    
    /** Battery display format */
    val batteryDisplayFormat: BatteryDisplayFormat = BatteryDisplayFormat.PERCENTAGE
)

/**
 * Notification priority levels.
 */
enum class NotificationPriority {
    LOW,
    DEFAULT,
    HIGH,
    URGENT
}

/**
 * Battery display format options.
 */
enum class BatteryDisplayFormat {
    PERCENTAGE,
    ICON,
    BOTH
}

/**
 * Battery trend (direction of change).
 */
enum class BatteryTrend {
    RISING,
    FALLING,
    STABLE,
    UNKNOWN
}

/**
 * Battery alert event.
 */
sealed class AlertEvent {
    /** Low battery alert */
    data class LowBattery(
        val deviceId: String,
        val currentLevel: Int,
        val threshold: Int
    ) : AlertEvent()
    
    /** Prediction alert */
    data class Prediction(
        val deviceId: String,
        val milestone: Int,
        val estimatedTime: Instant
    ) : AlertEvent()
    
    /** Health warning alert */
    data class HealthWarning(
        val deviceId: String,
        val issue: HealthIssue
    ) : AlertEvent()
}

/**
 * Battery health issues.
 */
enum class HealthIssue {
    HIGH_DEGRADATION,
    RAPID_DRAIN,
    NO_CHARGE_DETECTED,
    UNRELIABLE_REPORTING
}

/**
 * Application state.
 */
data class AppState(
    /** Whether monitoring is currently active */
    val isMonitoring: Boolean,
    
    /** Whether the app is initialized */
    val isInitialized: Boolean,
    
    /** List of all monitored devices with their battery state */
    val devices: List<Device>,
    
    /** Whether Bluetooth permission is granted */
    val hasBluetoothPermission: Boolean,
    
    /** Whether notification permission is granted */
    val hasNotificationPermission: Boolean,
    
    /** Whether battery optimization exemption is granted */
    val hasBatteryOptimizationExemption: Boolean,
    
    /** Last error encountered (null if no error) */
    val lastError: ErrorState?
)

/**
 * Error state for the application.
 */
data class ErrorState(
    /** Type of error */
    val type: ErrorType,
    
    /** Error message */
    val message: String,
    
    /** Timestamp when the error occurred */
    val timestamp: Instant,
    
    /** Device ID associated with the error (if applicable) */
    val deviceId: String?
)

/**
 * Error types.
 */
enum class ErrorType {
    BLUETOOTH_PERMISSION_DENIED,
    NOTIFICATION_PERMISSION_DENIED,
    BLUETOOTH_DISABLED,
    LOCATION_DISABLED,
    LOCATION_PERMISSION_DENIED,
    DEVICE_CONNECTION_FAILED,
    BATTERY_READ_FAILED,
    DEVICE_DISCONNECTED,
    STORAGE_ERROR,
    UNKNOWN
}
