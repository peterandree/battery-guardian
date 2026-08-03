package com.batteryguardian.domain

data class ThresholdPolicy(val lowEnterPercent: Int, val lowExitPercent: Int) {
    init { require(lowEnterPercent in 0..100 && lowExitPercent in lowEnterPercent..100) }
    fun entersLowZone(snapshot: BatterySnapshot) = snapshot.chargePercent?.let { it <= lowEnterPercent } ?: false
    fun exitsLowZone(snapshot: BatterySnapshot) = snapshot.chargePercent?.let { it >= lowExitPercent } ?: false
}
