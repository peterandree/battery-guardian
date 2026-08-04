package com.batteryguardian.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for storing alert thresholds per device.
 */
@Entity(
    tableName = "alert_thresholds",
    foreignKeys = [
        ForeignKey(
            entity = DeviceEntity::class,
            parentColumns = ["id"],
            childColumns = ["deviceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["deviceId"], unique = true)
    ]
)
data class AlertThresholdEntity(
    /** Device ID (Bluetooth MAC address, primary key, foreign key to DeviceEntity) */
    @PrimaryKey val deviceId: String,
    
    /** Whether to alert at 20% */
    val threshold20: Boolean = true,
    
    /** Whether to alert at 10% */
    val threshold10: Boolean = true,
    
    /** Whether to alert at 5% */
    val threshold5: Boolean = true,
    
    /** Custom thresholds (nullable) */
    val customThresholds: List<Int>?
)
