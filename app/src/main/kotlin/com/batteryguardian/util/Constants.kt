package com.batteryguardian.util

/**
 * Constants used throughout the Battery Guardian application.
 */

object Constants {
    // ==================== App Constants ====================
    
    /** Application ID */
    const val APP_ID = "com.batteryguardian"
    
    /** App name */
    const val APP_NAME = "Battery Guardian"
    
    /** Minimum Android SDK version */
    const val MIN_SDK = 31
    
    /** Target Android SDK version */
    const val TARGET_SDK = 35
    
    /** Compile Android SDK version */
    const val COMPILE_SDK = 35
    
    // ==================== Bluetooth Constants ====================
    
    /** Bluetooth Low Energy */
    object Bluetooth {
        /** Maximum number of concurrent GATT connections */
        const val MAX_CONCURRENT_CONNECTIONS = 3
        
        /** GATT connection timeout in seconds */
        const val GATT_CONNECTION_TIMEOUT_SECONDS = 5L
        
        /** GATT read timeout in seconds */
        const val GATT_READ_TIMEOUT_SECONDS = 5L
        
        /** Battery Service UUID */
        const val BATTERY_SERVICE_UUID = "0000180f-0000-1000-8000-00805f9b34fb"
        
        /** Battery Level Characteristic UUID */
        const val BATTERY_LEVEL_CHAR_UUID = "00002a19-0000-1000-8000-00805f9b34fb"
        
        /** Battery Status Characteristic UUID (BT 2.0) */
        const val BATTERY_STATUS_CHAR_UUID = "00002bea-0000-1000-8000-00805f9b34fb"
        
        /** Battery Power State Characteristic UUID */
        const val BATTERY_POWER_STATE_CHAR_UUID = "00002a1b-0000-1000-8000-00805f9b34fb"
    }
    
    // ==================== Battery Constants ====================
    
    /** Battery level constants */
    object Battery {
        /** Minimum battery level */
        const val MIN_LEVEL = 0
        
        /** Maximum battery level */
        const val MAX_LEVEL = 100
        
        /** Default low threshold */
        const val DEFAULT_LOW_THRESHOLD = 20
        
        /** Default medium threshold */
        const val DEFAULT_MEDIUM_THRESHOLD = 10
        
        /** Default critical threshold */
        const val DEFAULT_CRITICAL_THRESHOLD = 5
        
        /** Default hysteresis band */
        const val DEFAULT_HYSTERESIS_BAND = 2
        
        /** Minimum samples for prediction */
        const val MIN_SAMPLES_FOR_PREDICTION = 3
        
        /** Maximum age of samples for prediction (in days) */
        const val MAX_SAMPLE_AGE_FOR_PREDICTION_DAYS = 7L
    }
    
    // ==================== Monitoring Constants ====================
    
    /** Monitoring constants */
    object Monitoring {
        /** Default polling interval in minutes */
        const val DEFAULT_POLLING_INTERVAL_MINUTES = 5L
        
        /** Minimum polling interval in minutes */
        const val MIN_POLLING_INTERVAL_MINUTES = 1L
        
        /** Maximum polling interval in minutes */
        const val MAX_POLLING_INTERVAL_MINUTES = 15L
        
        /** Foreground service notification ID */
        const val FOREGROUND_SERVICE_NOTIFICATION_ID = 1
        
        /** Monitoring channel ID */
        const val MONITORING_CHANNEL_ID = "battery_guardian_monitoring"
        
        /** Alerts channel ID */
        const val ALERTS_CHANNEL_ID = "battery_guardian_alerts"
    }
    
    // ==================== Database Constants ====================
    
    /** Database constants */
    object Database {
        /** Database name */
        const val NAME = "battery_guardian_db"
        
        /** Database version */
        const val VERSION = 1
        
        /** Maximum age of battery history data (in days) */
        const val MAX_HISTORY_AGE_DAYS = 365L
        
        /** Maximum age of alert events (in days) */
        const val MAX_ALERT_EVENTS_AGE_DAYS = 30L
    }
    
    // ==================== Notification Constants ====================
    
    /** Notification constants */
    object Notification {
        /** Notification ID prefix for battery alerts */
        const val BATTERY_ALERT_PREFIX = "battery_alert_"
        
        /** Notification ID prefix for predictions */
        const val PREDICTION_PREFIX = "prediction_"
        
        /** Request code for Bluetooth permission */
        const val REQUEST_BLUETOOTH_PERMISSION = 1001
        
        /** Request code for Location permission */
        const val REQUEST_LOCATION_PERMISSION = 1002
        
        /** Request code for Notification permission */
        const val REQUEST_NOTIFICATION_PERMISSION = 1003
        
        /** Request code for Battery optimization exemption */
        const val REQUEST_BATTERY_OPTIMIZATION = 1004
    }
    
    // ==================== Time Constants ====================
    
    /** Time constants */
    object Time {
        /** Milliseconds in a second */
        const val MILLIS_IN_SECOND = 1000L
        
        /** Seconds in a minute */
        const val SECONDS_IN_MINUTE = 60L
        
        /** Minutes in an hour */
        const val MINUTES_IN_HOUR = 60L
        
        /** Hours in a day */
        const val HOURS_IN_DAY = 24L
        
        /** Seconds in an hour */
        const val SECONDS_IN_HOUR = SECONDS_IN_MINUTE * MINUTES_IN_HOUR
        
        /** Milliseconds in a minute */
        const val MILLIS_IN_MINUTE = MILLIS_IN_SECOND * SECONDS_IN_MINUTE
        
        /** Milliseconds in an hour */
        const val MILLIS_IN_HOUR = MILLIS_IN_MINUTE * MINUTES_IN_HOUR
        
        /** Milliseconds in a day */
        const val MILLIS_IN_DAY = MILLIS_IN_HOUR * HOURS_IN_DAY
    }
    
    // ==================== UI Constants ====================
    
    /** UI constants */
    object UI {
        /** Default animation duration in milliseconds */
        const val DEFAULT_ANIMATION_DURATION = 300L
        
        /** Default corner radius in dp */
        const val DEFAULT_CORNER_RADIUS_DP = 8
        
        /** Default elevation in dp */
        const val DEFAULT_ELEVATION_DP = 4
        
        /** Default spacing in dp */
        const val DEFAULT_SPACING_DP = 16
        
        /** Default small spacing in dp */
        const val DEFAULT_SPACING_SMALL_DP = 8
        
        /** Default large spacing in dp */
        const val DEFAULT_SPACING_LARGE_DP = 24
        
        /** Maximum line length for text */
        const val MAX_LINE_LENGTH = 120
    }
}
