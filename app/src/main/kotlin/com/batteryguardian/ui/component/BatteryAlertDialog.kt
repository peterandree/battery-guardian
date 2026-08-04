package com.batteryguardian.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.batteryguardian.R
import com.batteryguardian.ui.theme.BatteryCritical
import com.batteryguardian.ui.theme.BatteryGuardianTheme
import com.batteryguardian.ui.theme.BatteryHigh
import com.batteryguardian.ui.theme.BatteryLow
import com.batteryguardian.ui.theme.BatteryMedium

/**
 * Dialog for confirming battery alerts.
 */
@Composable
fun BatteryAlertDialog(
    deviceName: String,
    batteryLevel: Int,
    threshold: Int,
    onDismiss: () -> Unit,
    onOpenApp: () -> Unit,
    onIgnore: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = null,
                    tint = when {
                        batteryLevel <= 5 -> BatteryCritical
                        batteryLevel <= 10 -> BatteryCritical
                        batteryLevel <= 20 -> BatteryLow
                        else -> BatteryMedium
                    }
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.notification_title_low_battery),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "$deviceName is at $batteryLevel% battery (≤ $threshold% threshold)",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Would you like to open the app to see more details?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onOpenApp) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onIgnore) {
                Text("Ignore")
            }
        }
    )
}

/**
 * Dialog for battery prediction alerts.
 */
@Composable
fun BatteryPredictionDialog(
    deviceName: String,
    milestone: Int,
    timeUntil: String,
    onDismiss: () -> Unit,
    onOpenApp: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = null,
                    tint = BatteryMedium
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.notification_title_prediction),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "$deviceName will hit $milestone% battery in ~$timeUntil",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Would you like to open the app to see more details?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onOpenApp) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    )
}

/**
 * Dialog for confirming device deletion.
 */
@Composable
fun DeleteDeviceDialog(
    deviceName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete Device",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete $deviceName? This will remove all battery history for this device.",
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Dialog for renaming a device.
 */
@Composable
fun RenameDeviceDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newName by androidx.compose.runtime.mutableStateOf(currentName)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Rename Device",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                Text(
                    text = "Enter a new name for this device:",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // In a real implementation, this would be a TextField
                // For now, just show the current name
                Text(
                    text = newName,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(newName) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Dialog for setting manual battery level.
 */
@Composable
fun SetBatteryLevelDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var batteryLevel by androidx.compose.runtime.mutableStateOf(50)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Set Battery Level",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                Text(
                    text = "Enter the current battery level (0-100%):",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // In a real implementation, this would be a Slider or Number input
                Text(
                    text = "$batteryLevel%",
                    style = MaterialTheme.typography.displayMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0%")
                    Text("100%")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(batteryLevel) }) {
                Text("Set")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Preview for BatteryAlertDialog.
 */
@Preview(showBackground = true)
@Composable
fun BatteryAlertDialogPreview() {
    BatteryGuardianTheme {
        BatteryAlertDialog(
            deviceName = "Sennheiser Accentum",
            batteryLevel = 15,
            threshold = 20,
            onDismiss = {},
            onOpenApp = {},
            onIgnore = {}
        )
    }
}
