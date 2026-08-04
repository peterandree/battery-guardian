package com.batteryguardian.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.batteryguardian.domain.model.BatteryReadMethod
import java.time.Instant

/**
 * Room entity for storing device capabilities.
 */
@Entity(
    tableName = "device_capabilities",
    foreignKeys = [
        ForeignKey(
            entity = DeviceEntity::class,
            parentColumns = ["id"],
            childColumns = ["deviceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["deviceId"], unique = true),
        Index(value = ["supportsGattBattery"], unique = false),
        Index(value = ["supportsClassicBattery"], unique = false),
        Index(value = ["preferredMethod"], unique = false)
    ]
)
data class DeviceCapabilityEntity(
    /** Device ID (Bluetooth MAC address, primary key, foreign key to DeviceEntity) */
    @PrimaryKey val deviceId: String,
    
    /** Whether the device supports GATT Battery Service */
    val supportsGattBattery: Boolean,
    
    /** Whether the device supports Classic Bluetooth battery reporting */
    val supportsClassicBattery: Boolean,
    
    /** The preferred method for reading battery from this device */
    val preferredMethod: BatteryReadMethod,
    
    /** Timestamp of last capability detection */
    val lastDetected: Instant?
)
