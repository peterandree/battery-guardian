package com.batteryguardian.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Room entity for storing battery health metrics.
 */
@Entity(
    tableName = "battery_health",
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
data class BatteryHealthEntity(
    /** Device ID (Bluetooth MAC address, primary key, foreign key to DeviceEntity) */
    @PrimaryKey val deviceId: String,
    
    /** Average drain rate in percentage per hour (negative when discharging) */
    val averageDrainRate: Float,
    
    /** Timestamp of last full charge */
    val lastFullCharge: Instant?,
    
    /** Battery capacity degradation as percentage (0-100) */
    val capacityDegradation: Float?,
    
    /** Timestamp when health metrics were last updated */
    val lastUpdated: Instant
)
