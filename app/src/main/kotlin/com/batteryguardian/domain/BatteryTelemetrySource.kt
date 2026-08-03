package com.batteryguardian.domain

interface BatteryTelemetrySource {
    val id: BatterySourceId
    suspend fun readSnapshot(): Result<BatterySnapshot>
}
