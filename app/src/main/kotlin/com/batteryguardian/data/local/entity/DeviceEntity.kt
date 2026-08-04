package com.batteryguardian.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.batteryguardian.domain.model.DeviceType
import java.time.Instant

/**
 * Room entity for storing device information.
 */
@Entity(
    tableName = "devices",
    indices = [
        Index(value = ["name"], unique = false),
        Index(value = ["type"], unique = false),
        Index(value = ["manufacturer"], unique = false),
        Index(value = ["isMonitored"], unique = false),
        Index(value = ["isIgnored"], unique = false)
    ]
)
data class DeviceEntity(
    /** Bluetooth MAC address (primary key) */
    @PrimaryKey val id: String,
    
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
    val isIgnored: Boolean
)
