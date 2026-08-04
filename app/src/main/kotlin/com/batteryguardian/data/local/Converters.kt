package com.batteryguardian.data.local

import androidx.room.TypeConverter
import com.batteryguardian.domain.model.AlertState
import com.batteryguardian.domain.model.BatteryReadMethod
import com.batteryguardian.domain.model.DeviceType
import java.time.Instant

/**
 * Type converters for Room database.
 * 
 * Converts between domain types and database types.
 */
class Converters {

    // ==================== Instant ====================

    @TypeConverter
    fun instantToEpochMilli(instant: Instant?): Long? {
        return instant?.toEpochMilli()
    }

    @TypeConverter
    fun epochMilliToInstant(epochMilli: Long?): Instant? {
        return epochMilli?.let { Instant.ofEpochMilli(it) }
    }

    // ==================== DeviceType ====================

    @TypeConverter
    fun deviceTypeToString(type: DeviceType?): String? {
        return type?.name
    }

    @TypeConverter
    fun stringToDeviceType(type: String?): DeviceType? {
        return type?.let { DeviceType.valueOf(it) }
    }

    // ==================== BatteryReadMethod ====================

    @TypeConverter
    fun batteryReadMethodToString(method: BatteryReadMethod?): String? {
        return method?.name
    }

    @TypeConverter
    fun stringToBatteryReadMethod(method: String?): BatteryReadMethod? {
        return method?.let { BatteryReadMethod.valueOf(it) }
    }

    // ==================== AlertState ====================

    @TypeConverter
    fun alertStateToString(state: AlertState): String {
        return when (state) {
            is AlertState.Normal -> "NORMAL"
            is AlertState.Low -> "LOW:${state.threshold}"
            is AlertState.High -> "HIGH:${state.threshold}"
        }
    }

    @TypeConverter
    fun stringToAlertState(state: String): AlertState {
        return when {
            state.startsWith("LOW:") -> {
                val threshold = state.substringAfter(":").toInt()
                AlertState.Low(threshold, Instant.now())
            }
            state.startsWith("HIGH:") -> {
                val threshold = state.substringAfter(":").toInt()
                AlertState.High(threshold, Instant.now())
            }
            else -> AlertState.Normal
        }
    }

    // ==================== Int List ====================

    @TypeConverter
    fun intListToString(list: List<Int>?): String? {
        return list?.joinToString(",")
    }

    @TypeConverter
    fun stringToIntList(string: String?): List<Int>? {
        return string?.split(",")?.mapNotNull { it.toIntOrNull() }
    }
}
