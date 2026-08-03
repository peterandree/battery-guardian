package com.batteryguardian.domain

import java.time.Instant

@JvmInline value class BatterySourceId(val value: String)
enum class ChargingState { CHARGING, DISCHARGING, FULL, NOT_CHARGING, UNKNOWN }
data class BatterySnapshot(val sourceId: BatterySourceId, val observedAt: Instant, val chargePercent: Int?, val chargingState: ChargingState, val displayName: String)
