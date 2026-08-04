package com.batteryguardian.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Room entity for storing alert states.
 */
@Entity(
    tableName = "alert_states",
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
        Index(value = ["currentState"], unique = false)
    ]
)
data class AlertStateEntity(
    /** Device ID (Bluetooth MAC address, primary key, foreign key to DeviceEntity) */
    @PrimaryKey val deviceId: String,
    
    /** Current alert state (NORMAL, LOW:X, HIGH:X) */
    val currentState: String,
    
    /** Last threshold that triggered an alert */
    val lastAlertThreshold: Int?,
    
    /** Timestamp of last alert */
    val lastAlertTimestamp: Instant?,
    
    /** Hysteresis band in percentage */
    val hysteresisBand: Int
)
