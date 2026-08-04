package com.batteryguardian.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Battery0Bar
import androidx.compose.material.icons.outlined.Battery1Bar
import androidx.compose.material.icons.outlined.Battery2Bar
import androidx.compose.material.icons.outlined.Battery3Bar
import androidx.compose.material.icons.outlined.Battery4Bar
import androidx.compose.material.icons.outlined.Battery5Bar
import androidx.compose.material.icons.outlined.Battery6Bar
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.BatteryFull
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.BluetoothDisabled
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.batteryguardian.R
import com.batteryguardian.domain.model.AlertState
import com.batteryguardian.domain.model.Device
import com.batteryguardian.domain.model.DeviceType
import com.batteryguardian.ui.theme.BatteryCritical
import com.batteryguardian.ui.theme.BatteryGuardianTheme
import com.batteryguardian.ui.theme.BatteryHigh
import com.batteryguardian.ui.theme.BatteryLow
import com.batteryguardian.ui.theme.BatteryMedium

/**
 * Enhanced device card with more details.
 */
@Composable
fun DeviceCard(
    device: Device,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            // Device icon
            DeviceIcon(
                device = device,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Device info
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = device.alias ?: device.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (!device.isConnected) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Outlined.BluetoothDisabled,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Device type and manufacturer
                Text(
                    text = buildString {
                        append(device.type.name.lowercase())
                        device.manufacturer?.let { manufacturer ->
                            append(" - ")
                            append(manufacturer)
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Battery level and status
            BatteryStatusDisplay(
                level = device.currentBatteryLevel,
                isCharging = device.isCharging,
                alertState = device.alertState
            )
        }
    }
}

/**
 * Compact device card for lists.
 */
@Composable
fun CompactDeviceCard(
    device: Device,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            // Device icon
            DeviceIcon(
                device = device,
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Device name
            Text(
                text = device.alias ?: device.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Battery level
            BatteryLevelText(
                level = device.currentBatteryLevel,
                alertState = device.alertState
            )
        }
    }
}

/**
 * Device icon based on type and battery level.
 */
@Composable
fun DeviceIcon(
    device: Device,
    modifier: Modifier = Modifier
) {
    val icon = getDeviceIcon(device.type)
    val tint = getBatteryColor(device.currentBatteryLevel)

    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = modifier,
        tint = tint
    )
}

/**
 * Get icon for device type.
 */
@Composable
fun getDeviceIcon(type: DeviceType): ImageVector {
    return when (type) {
        DeviceType.HEADPHONES -> Icons.Outlined.BatteryFull
        DeviceType.SPEAKER -> Icons.Outlined.BatteryFull
        DeviceType.SMARTWATCH -> Icons.Outlined.BatteryFull
        DeviceType.KEYBOARD -> Icons.Outlined.BatteryFull
        DeviceType.MOUSE -> Icons.Outlined.BatteryFull
        DeviceType.GAME_CONTROLLER -> Icons.Outlined.BatteryFull
        DeviceType.HEARING_AID -> Icons.Outlined.BatteryFull
        DeviceType.MEDICAL_DEVICE -> Icons.Outlined.BatteryFull
        DeviceType.OTHER -> Icons.Outlined.Bluetooth
    }
}

/**
 * Battery status display with level and charging indicator.
 */
@Composable
fun BatteryStatusDisplay(
    level: Int?,
    isCharging: Boolean?,
    alertState: AlertState
) {
    Column(
        horizontalAlignment = Alignment.End
    ) {
        if (level != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Battery icon
                val batteryIcon = when {
                    isCharging == true -> Icons.Outlined.BatteryChargingFull
                    level >= 90 -> Icons.Outlined.BatteryFull
                    level >= 75 -> Icons.Outlined.Battery6Bar
                    level >= 50 -> Icons.Outlined.Battery5Bar
                    level >= 35 -> Icons.Outlined.Battery4Bar
                    level >= 20 -> Icons.Outlined.Battery3Bar
                    level >= 10 -> Icons.Outlined.Battery2Bar
                    level >= 5 -> Icons.Outlined.Battery1Bar
                    else -> Icons.Outlined.Battery0Bar
                }

                Icon(
                    imageVector = batteryIcon,
                    contentDescription = null,
                    tint = when {
                        alertState is AlertState.Low -> MaterialTheme.colorScheme.error
                        isCharging == true -> MaterialTheme.colorScheme.primary
                        level <= 20 -> BatteryCritical
                        level <= 50 -> BatteryLow
                        level <= 80 -> BatteryMedium
                        else -> BatteryHigh
                    },
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Battery percentage
                Text(
                    text = "$level%",
                    style = MaterialTheme.typography.bodyLarge,
                    color = when {
                        alertState is AlertState.Low -> MaterialTheme.colorScheme.error
                        isCharging == true -> MaterialTheme.colorScheme.primary
                        level <= 20 -> BatteryCritical
                        level <= 50 -> BatteryLow
                        level <= 80 -> BatteryMedium
                        else -> BatteryHigh
                    }
                )
            }

            if (isCharging == true) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.battery_charging),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            Icon(
                Icons.Outlined.HelpOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Battery level text only.
 */
@Composable
fun BatteryLevelText(
    level: Int?,
    alertState: AlertState
) {
    if (level != null) {
        Text(
            text = "$level%",
            style = MaterialTheme.typography.bodyLarge,
            color = when {
                alertState is AlertState.Low -> MaterialTheme.colorScheme.error
                level <= 20 -> BatteryCritical
                level <= 50 -> BatteryLow
                level <= 80 -> BatteryMedium
                else -> BatteryHigh
            }
        )
    } else {
        Text(
            text = stringResource(R.string.battery_unknown),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Battery level indicator (progress bar).
 */
@Composable
fun BatteryLevelIndicator(
    level: Int?,
    modifier: Modifier = Modifier
) {
    if (level != null) {
        LinearProgressIndicator(
            progress = { level / 100f },
            color = getBatteryColor(level),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(MaterialTheme.shapes.extraSmall)
        )
    } else {
        LinearProgressIndicator(
            progress = { 0f },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(MaterialTheme.shapes.extraSmall)
        )
    }
}

/**
 * Preview for DeviceCard.
 */
@Preview(showBackground = true)
@Composable
fun DeviceCardPreview() {
    BatteryGuardianTheme {
        DeviceCard(
            device = Device(
                id = "test",
                name = "Sennheiser Accentum Wireless",
                alias = null,
                type = DeviceType.HEADPHONES,
                manufacturer = "Sennheiser",
                bluetoothClass = null,
                lastSeen = null,
                currentBatteryLevel = 75,
                isCharging = false,
                isConnected = true,
                isMonitored = true,
                isIgnored = false,
                batteryHealth = null,
                alertState = AlertState.Normal
            ),
            onClick = {}
        )
    }
}

/**
 * Preview for CompactDeviceCard.
 */
@Preview(showBackground = true)
@Composable
fun CompactDeviceCardPreview() {
    BatteryGuardianTheme {
        CompactDeviceCard(
            device = Device(
                id = "test",
                name = "Sennheiser Accentum Wireless",
                alias = "My Headphones",
                type = DeviceType.HEADPHONES,
                manufacturer = "Sennheiser",
                bluetoothClass = null,
                lastSeen = null,
                currentBatteryLevel = 45,
                isCharging = false,
                isConnected = true,
                isMonitored = true,
                isIgnored = false,
                batteryHealth = null,
                alertState = AlertState.Normal
            ),
            onClick = {}
        )
    }
}
