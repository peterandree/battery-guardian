package com.batteryguardian.di

import android.content.Context
import androidx.room.Room
import com.batteryguardian.data.local.BatteryDatabase
import com.batteryguardian.data.local.dao.AlertStateDao
import com.batteryguardian.data.local.dao.AlertThresholdDao
import com.batteryguardian.data.local.dao.BatteryHealthDao
import com.batteryguardian.data.local.dao.BatteryLevelDao
import com.batteryguardian.data.local.dao.DeviceCapabilityDao
import com.batteryguardian.data.local.dao.DeviceDao
import com.batteryguardian.data.repository.AlertRepositoryImpl
import com.batteryguardian.data.repository.BatteryRepositoryImpl
import com.batteryguardian.data.repository.DeviceCapabilityRepositoryImpl
import com.batteryguardian.data.repository.DeviceRepositoryImpl
import com.batteryguardian.data.repository.UserPreferencesRepositoryImpl
import com.batteryguardian.domain.repository.AlertRepository
import com.batteryguardian.domain.repository.BatteryRepository
import com.batteryguardian.domain.repository.DeviceCapabilityRepository
import com.batteryguardian.domain.repository.DeviceRepository
import com.batteryguardian.domain.repository.UserPreferencesRepository
import com.batteryguardian.domain.usecase.AlertUseCase
import com.batteryguardian.domain.usecase.GetDeviceHistoryUseCase
import com.batteryguardian.domain.usecase.ManageDevicesUseCase
import com.batteryguardian.domain.usecase.MonitorBatteryUseCase
import com.batteryguardian.domain.usecase.PredictBatteryUseCase
import com.batteryguardian.monitoring.AlertManager
import com.batteryguardian.monitoring.BatteryMonitor
import com.batteryguardian.monitoring.BatteryPredictionEngine
import com.batteryguardian.monitoring.ClassicBatteryReader
import com.batteryguardian.monitoring.DeviceCapabilityDetector
import com.batteryguardian.monitoring.GattBatteryReader
import com.batteryguardian.monitoring.PollingOrchestrator
import dagger.Module
import dagger.Provides
import dagger.Singleton
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing dependencies.
 * 
 * All Hilt bindings should be defined here. Do not create new module files.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ==================== Database ====================

    @Provides
    @Singleton
    fun provideBatteryDatabase(
        @ApplicationContext context: Context
    ): BatteryDatabase {
        return Room.databaseBuilder(
            context,
            BatteryDatabase::class.java,
            "battery_guardian_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideDeviceDao(database: BatteryDatabase): DeviceDao {
        return database.deviceDao()
    }

    @Provides
    @Singleton
    fun provideBatteryLevelDao(database: BatteryDatabase): BatteryLevelDao {
        return database.batteryLevelDao()
    }

    @Provides
    @Singleton
    fun provideBatteryHealthDao(database: BatteryDatabase): BatteryHealthDao {
        return database.batteryHealthDao()
    }

    @Provides
    @Singleton
    fun provideAlertStateDao(database: BatteryDatabase): AlertStateDao {
        return database.alertStateDao()
    }

    @Provides
    @Singleton
    fun provideAlertThresholdDao(database: BatteryDatabase): AlertThresholdDao {
        return database.alertThresholdDao()
    }

    @Provides
    @Singleton
    fun provideDeviceCapabilityDao(database: BatteryDatabase): DeviceCapabilityDao {
        return database.deviceCapabilityDao()
    }

    // ==================== Repositories ====================

    @Provides
    @Singleton
    fun provideDeviceRepository(
        deviceDao: DeviceDao,
        batteryLevelDao: BatteryLevelDao,
        batteryHealthDao: BatteryHealthDao,
        alertStateDao: AlertStateDao,
        alertThresholdDao: AlertThresholdDao
    ): DeviceRepository {
        return DeviceRepositoryImpl(
            deviceDao,
            batteryLevelDao,
            batteryHealthDao,
            alertStateDao,
            alertThresholdDao
        )
    }

    @Provides
    @Singleton
    fun provideBatteryRepository(
        batteryLevelDao: BatteryLevelDao,
        batteryHealthDao: BatteryHealthDao
    ): BatteryRepository {
        return BatteryRepositoryImpl(batteryLevelDao, batteryHealthDao)
    }

    @Provides
    @Singleton
    fun provideAlertRepository(
        alertStateDao: AlertStateDao,
        alertThresholdDao: AlertThresholdDao
    ): AlertRepository {
        return AlertRepositoryImpl(alertStateDao, alertThresholdDao)
    }

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(
        @ApplicationContext context: Context
    ): UserPreferencesRepository {
        return UserPreferencesRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideDeviceCapabilityRepository(
        deviceCapabilityDao: DeviceCapabilityDao
    ): DeviceCapabilityRepository {
        return DeviceCapabilityRepositoryImpl(deviceCapabilityDao)
    }

    // ==================== Monitoring Components ====================

    @Provides
    @Singleton
    fun provideGattBatteryReader(
        @ApplicationContext context: Context
    ): GattBatteryReader {
        return GattBatteryReader(context)
    }

    @Provides
    @Singleton
    fun provideClassicBatteryReader(
        @ApplicationContext context: Context
    ): ClassicBatteryReader {
        return ClassicBatteryReader(context)
    }

    @Provides
    @Singleton
    fun provideDeviceCapabilityDetector(
        gattBatteryReader: GattBatteryReader,
        classicBatteryReader: ClassicBatteryReader,
        deviceCapabilityRepository: DeviceCapabilityRepository
    ): DeviceCapabilityDetector {
        return DeviceCapabilityDetector(
            gattBatteryReader,
            classicBatteryReader,
            deviceCapabilityRepository
        )
    }

    @Provides
    @Singleton
    fun provideBatteryPredictionEngine(
        batteryRepository: BatteryRepository
    ): BatteryPredictionEngine {
        return BatteryPredictionEngine(batteryRepository)
    }

    @Provides
    @Singleton
    fun provideAlertManager(
        userPreferencesRepository: UserPreferencesRepository,
        alertRepository: AlertRepository
    ): AlertManager {
        return AlertManager(userPreferencesRepository, alertRepository)
    }

    @Provides
    @Singleton
    fun providePollingOrchestrator(
        @ApplicationContext context: Context,
        userPreferencesRepository: UserPreferencesRepository
    ): PollingOrchestrator {
        return PollingOrchestrator(context, userPreferencesRepository)
    }

    @Provides
    @Singleton
    fun provideBatteryMonitor(
        deviceRepository: DeviceRepository,
        batteryRepository: BatteryRepository,
        gattBatteryReader: GattBatteryReader,
        classicBatteryReader: ClassicBatteryReader,
        batteryPredictionEngine: BatteryPredictionEngine,
        alertManager: AlertManager,
        pollingOrchestrator: PollingOrchestrator,
        userPreferencesRepository: UserPreferencesRepository
    ): BatteryMonitor {
        return BatteryMonitor(
            deviceRepository,
            batteryRepository,
            gattBatteryReader,
            classicBatteryReader,
            batteryPredictionEngine,
            alertManager,
            pollingOrchestrator,
            userPreferencesRepository
        )
    }

    // ==================== Use Cases ====================

    @Provides
    @Singleton
    fun provideMonitorBatteryUseCase(
        deviceRepository: DeviceRepository,
        batteryRepository: BatteryRepository,
        alertRepository: AlertRepository,
        userPreferencesRepository: UserPreferencesRepository
    ): MonitorBatteryUseCase {
        return MonitorBatteryUseCase(
            deviceRepository,
            batteryRepository,
            alertRepository,
            userPreferencesRepository
        )
    }

    @Provides
    @Singleton
    fun providePredictBatteryUseCase(
        batteryRepository: BatteryRepository
    ): PredictBatteryUseCase {
        return PredictBatteryUseCase(batteryRepository)
    }

    @Provides
    @Singleton
    fun provideAlertUseCase(
        alertRepository: AlertRepository,
        deviceRepository: DeviceRepository,
        userPreferencesRepository: UserPreferencesRepository
    ): AlertUseCase {
        return AlertUseCase(
            alertRepository,
            deviceRepository,
            userPreferencesRepository
        )
    }

    @Provides
    @Singleton
    fun provideManageDevicesUseCase(
        deviceRepository: DeviceRepository,
        capabilityRepository: DeviceCapabilityRepository
    ): ManageDevicesUseCase {
        return ManageDevicesUseCase(deviceRepository, capabilityRepository)
    }

    @Provides
    @Singleton
    fun provideGetDeviceHistoryUseCase(
        batteryRepository: BatteryRepository
    ): GetDeviceHistoryUseCase {
        return GetDeviceHistoryUseCase(batteryRepository)
    }
}
