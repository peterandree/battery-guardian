package com.batteryguardian.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.batteryguardian.data.local.dao.AlertStateDao
import com.batteryguardian.data.local.dao.AlertThresholdDao
import com.batteryguardian.data.local.dao.BatteryHealthDao
import com.batteryguardian.data.local.dao.BatteryLevelDao
import com.batteryguardian.data.local.dao.DeviceCapabilityDao
import com.batteryguardian.data.local.dao.DeviceDao
import com.batteryguardian.data.local.entity.AlertStateEntity
import com.batteryguardian.data.local.entity.AlertThresholdEntity
import com.batteryguardian.data.local.entity.BatteryHealthEntity
import com.batteryguardian.data.local.entity.BatteryLevelEntity
import com.batteryguardian.data.local.entity.DeviceCapabilityEntity
import com.batteryguardian.data.local.entity.DeviceEntity

/**
 * Room database for Battery Guardian.
 * 
 * Stores all persistent data for the application.
 */
@Database(
    entities = [
        DeviceEntity::class,
        BatteryLevelEntity::class,
        BatteryHealthEntity::class,
        DeviceCapabilityEntity::class,
        AlertStateEntity::class,
        AlertThresholdEntity::class
    ],
    version = 1,
    exportSchema = true,
    autoMigrations = [
        @AutoMigration(from = 1, to = 2)
    ]
)
@TypeConverters(Converters::class)
abstract class BatteryDatabase : RoomDatabase() {

    abstract fun deviceDao(): DeviceDao
    abstract fun batteryLevelDao(): BatteryLevelDao
    abstract fun batteryHealthDao(): BatteryHealthDao
    abstract fun deviceCapabilityDao(): DeviceCapabilityDao
    abstract fun alertStateDao(): AlertStateDao
    abstract fun alertThresholdDao(): AlertThresholdDao

    companion object {
        /** Database name */
        const val DATABASE_NAME = "battery_guardian_db"
    }
}
