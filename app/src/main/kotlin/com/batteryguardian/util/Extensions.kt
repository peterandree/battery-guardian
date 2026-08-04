package com.batteryguardian.util

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.batteryguardian.domain.model.AlertState
import com.batteryguardian.domain.model.BatteryReadMethod
import com.batteryguardian.domain.model.DeviceType
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Extension functions for common operations.
 */

// ==================== Instant Extensions ====================

/**
 * Format this Instant as a time string (HH:mm).
 */
fun Instant.formatTime(): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    return atZone(ZoneId.systemDefault()).format(formatter)
}

/**
 * Format this Instant as a date string (MMM dd).
 */
fun Instant.formatDate(): String {
    val formatter = DateTimeFormatter.ofPattern("MMM dd")
    return atZone(ZoneId.systemDefault()).format(formatter)
}

/**
 * Format this Instant as a date and time string (MMM dd, HH:mm).
 */
fun Instant.formatDateTime(): String {
    val formatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm")
    return atZone(ZoneId.systemDefault()).format(formatter)
}

/**
 * Format this Instant as a relative time string (e.g., "2 hours ago").
 */
fun Instant.formatRelative(): String {
    val now = Instant.now()
    val duration = Duration.between(this, now)
    
    return when {
        duration.isNegative -> "In ${(-duration).formatDuration()}"
        duration.toHours() >= 24 -> "${duration.toHours() / 24} days ago"
        duration.toHours() >= 1 -> "${duration.toHours()} hours ago"
        duration.toMinutes() >= 1 -> "${duration.toMinutes()} minutes ago"
        else -> "Just now"
    }
}

/**
 * Format a Duration as a human-readable string.
 */
fun Duration.formatDuration(): String {
    return when {
        toHours() >= 24 -> "${toHours() / 24}d ${toHours() % 24}h"
        toHours() >= 1 -> "${toHours()}h ${toMinutes() % 60}m"
        toMinutes() >= 1 -> "${toMinutes()}m"
        toSeconds() >= 1 -> "${toSeconds()}s"
        else -> "0s"
    }
}

/**
 * Format a Duration as a short string (e.g., "2h", "30m", "45s").
 */
fun Duration.formatShort(): String {
    return when {
        toHours() >= 1 -> "${toHours()}h"
        toMinutes() >= 1 -> "${toMinutes()}m"
        else -> "${toSeconds()}s"
    }
}

// ==================== BluetoothDevice Extensions ====================

/**
 * Get a display name for this Bluetooth device.
 */
fun BluetoothDevice.getDisplayName(): String {
    return name ?: "Unknown Device (${address})"
}

/**
 * Get a short display name for this Bluetooth device.
 */
fun BluetoothDevice.getShortDisplayName(): String {
    return name ?: address.take(8)
}

/**
 * Check if this device is connected.
 */
fun BluetoothDevice.isConnected(): Boolean {
    // Note: This is a simplified check
    // In a real implementation, we would track connection state
    return bondState == BluetoothDevice.BOND_BONDED
}

// ==================== DeviceType Extensions ====================

/**
 * Get a display string for this device type.
 */
fun DeviceType.getDisplayString(): String {
    return when (this) {
        DeviceType.HEADPHONES -> "Headphones"
        DeviceType.SPEAKER -> "Speaker"
        DeviceType.SMARTWATCH -> "Smartwatch"
        DeviceType.KEYBOARD -> "Keyboard"
        DeviceType.MOUSE -> "Mouse"
        DeviceType.GAME_CONTROLLER -> "Game Controller"
        DeviceType.HEARING_AID -> "Hearing Aid"
        DeviceType.MEDICAL_DEVICE -> "Medical Device"
        DeviceType.OTHER -> "Other"
    }
}

/**
 * Get an icon resource ID for this device type.
 */
fun DeviceType.getIconResId(): Int {
    // These would be actual drawable resources in a real implementation
    return when (this) {
        DeviceType.HEADPHONS -> android.R.drawable.ic_media_play
        DeviceType.SPEAKER -> android.R.drawable.ic_media_play
        DeviceType.SMARTWATCH -> android.R.drawable.ic_lock_idle_lock
        DeviceType.KEYBOARD -> android.R.drawable.ic_input_add
        DeviceType.MOUSE -> android.R.drawable.ic_input_mouse
        DeviceType.GAME_CONTROLLER -> android.R.drawable.ic_media_play
        DeviceType.HEARING_AID -> android.R.drawable.ic_secure
        DeviceType.MEDICAL_DEVICE -> android.R.drawable.ic_plusone_dark
        DeviceType.OTHER -> android.R.drawable.ic_dialog_info
    }
}

// ==================== AlertState Extensions ====================

/**
 * Check if this alert state is active (not Normal).
 */
fun AlertState.isActive(): Boolean {
    return this !is AlertState.Normal
}

/**
 * Get the threshold for this alert state.
 */
fun AlertState.getThreshold(): Int? {
    return when (this) {
        is AlertState.Normal -> null
        is AlertState.Low -> threshold
        is AlertState.High -> threshold
    }
}

// ==================== BatteryReadMethod Extensions ====================

/**
 * Get a display string for this battery read method.
 */
fun BatteryReadMethod.getDisplayString(): String {
    return when (this) {
        BatteryReadMethod.GATT -> "BLE (GATT)"
        BatteryReadMethod.CLASSIC -> "Classic Bluetooth"
        BatteryReadMethod.MANUAL -> "Manual"
        BatteryReadMethod.NONE -> "None"
    }
}

// ==================== Context Extensions ====================

/**
 * Open battery optimization settings.
 */
fun Context.openBatteryOptimizationSettings() {
    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
    if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
    }
}

/**
 * Open app notification settings.
 */
fun Context.openAppNotificationSettings() {
    val intent = Intent().apply {
        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
        putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
    }
    if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
    }
}

/**
 * Open Bluetooth settings.
 */
fun Context.openBluetoothSettings() {
    val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
    if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
    }
}

/**
 * Open Location settings.
 */
fun Context.openLocationSettings() {
    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
    }
}

/**
 * Check if a permission is granted.
 */
fun Context.isPermissionGranted(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == 
            PackageManager.PERMISSION_GRANTED
}

/**
 * Check if Bluetooth is enabled.
 */
fun Context.isBluetoothEnabled(): Boolean {
    val bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
    return bluetoothAdapter?.isEnabled == true
}

/**
 * Check if Location is enabled.
 */
@RequiresApi(Build.VERSION_CODES.P)
fun Context.isLocationEnabled(): Boolean {
    val locationManager = getSystemService(Context.LOCATION_SERVICE) 
        as android.location.LocationManager
    return locationManager.isLocationEnabled
}

// ==================== Number Extensions ====================

/**
 * Clamp this Int to the specified range.
 */
fun Int.clamp(min: Int, max: Int): Int {
    return when {
        this < min -> min
        this > max -> max
        else -> this
    }
}

/**
 * Clamp this Float to the specified range.
 */
fun Float.clamp(min: Float, max: Float): Float {
    return when {
        this < min -> min
        this > max -> max
        else -> this
    }
}

/**
 * Convert percentage to a float between 0 and 1.
 */
fun Int.toProgress(): Float {
    return this.clamp(0, 100) / 100f
}

/**
 * Convert a float between 0 and 1 to a percentage.
 */
fun Float.toPercentage(): Int {
    return (this * 100).toInt().clamp(0, 100)
}

// ==================== Collection Extensions ====================

/**
 * Get the average of this List of Int.
 */
fun List<Int>.averageInt(): Int {
    return if (isEmpty()) 0 else sum() / size
}

/**
 * Get the average of this List of Float.
 */
fun List<Float>.averageFloat(): Float {
    return if (isEmpty()) 0f else sum() / size
}

/**
 * Get the most recent elements from this List (sorted by timestamp).
 */
fun <T> List<T>.mostRecent(count: Int): List<T> {
    return if (size <= count) this else subList(size - count, size)
}

/**
 * Get the oldest elements from this List (sorted by timestamp).
 */
fun <T> List<T>.oldest(count: Int): List<T> {
    return if (size <= count) this else subList(0, count)
}

// ==================== String Extensions ====================

/**
 * Capitalize the first letter of this string.
 */
fun String.capitalizeFirst(): String {
    return if (isEmpty()) this else "${this[0].uppercase()}${substring(1)}"
}

/**
 * Truncate this string to the specified length.
 */
fun String.truncate(length: Int): String {
    return if (this.length <= length) this else "${substring(0, length)}..."
}

/**
 * Check if this string contains any of the specified substrings (case-insensitive).
 */
fun String.containsAny(vararg substrings: String): Boolean {
    val lowerThis = lowercase()
    return substrings.any { lowerThis.contains(it.lowercase()) }
}

// ==================== Boolean Extensions ====================

/**
 * Toggle this Boolean.
 */
fun Boolean.toggle(): Boolean {
    return !this
}

/**
 * Convert this Boolean to an Int (1 for true, 0 for false).
 */
fun Boolean.toInt(): Int {
    return if (this) 1 else 0
}

/**
 * Convert this Int to a Boolean (non-zero for true, 0 for false).
 */
fun Int.toBoolean(): Boolean {
    return this != 0
}
