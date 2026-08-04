package com.batteryguardian.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Default shapes for Battery Guardian.
 */
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

/**
 * Battery indicator shape.
 */
val BatteryIndicatorShape = RoundedCornerShape(4.dp)

/**
 * Card shape.
 */
val CardShape = RoundedCornerShape(12.dp)

/**
 * Dialog shape.
 */
val DialogShape = RoundedCornerShape(16.dp)

/**
 * Button shape.
 */
val ButtonShape = RoundedCornerShape(8.dp)

/**
 * Full circle shape.
 */
val CircleShape = RoundedCornerShape(50.dp)
