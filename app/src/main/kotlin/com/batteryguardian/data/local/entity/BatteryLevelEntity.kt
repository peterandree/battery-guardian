package com.batteryguardian.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Room entity for storing battery level history.
 */
@Entity(
    tableName = "battery_levels",
    foreignKeys = [
        ForeignKey(
            entity = DeviceEntity::class,
            parentColumns = ["id"],
            childColumns = ["deviceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["deviceId"], unique = false),
        Index(value = ["timestamp"], unique = false),
        Index(value = ["deviceId", "timestamp"], unique = true)
    ]
)
data class BatteryLevelEntity(
    /** Auto-incrementing primary key */
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    
    /** Device ID (Bluetooth MAC address, foreign key to DeviceEntity) */
    val deviceId: String,
    
    /** Battery level (0-100) */
    val level: Int,
    
    /** Timestamp when the reading was taken */
    val timestamp: Instant,
    
    /** Whether this is a real reading or predicted */
    val isPredicted: Boolean = false
)
