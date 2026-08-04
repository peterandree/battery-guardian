package com.batteryguardian.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.BatteryFull
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.DeviceUnknown
import androidx.compose.material.icons.outlined.Factory
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.batteryguardian.R
import com.batteryguardian.domain.model.AlertState
import com.batteryguardian.domain.model.BatteryHealth
import com.batteryguardian.domain.model.BatteryPrediction
import com.batteryguardian.domain.model.Device
import com.batteryguardian.domain.model.DeviceType
import com.batteryguardian.ui.theme.BatteryGuardianTheme
import java.time.Instant

/**
 * Device detail screen.
 * 
 * Shows detailed information about a specific device including:
 * - Current battery level
 * - Battery history
 * - Battery health metrics
 * - Predictions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailScreen(
    deviceId: String,
    onBack: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: DeviceDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Load device data when screen is composed
    LaunchedEffect(deviceId) {
        viewModel.loadDevice(deviceId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(uiState.device?.alias ?: uiState.device?.name ?: stringResource(R.string.device_unknown))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.settings)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        DeviceDetailContent(
            uiState = uiState,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

/**
 * Content for the device detail screen.
 */
@Composable
fun DeviceDetailContent(
    uiState: DeviceDetailUiState,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is DeviceDetailUiState.Loading -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier.fillMaxSize()
            ) {
                CircularProgressIndicator()
            }
        }
        is DeviceDetailUiState.Error -> {
            ErrorScreen(
                message = uiState.message,
                onRetry = { /* TODO */ },
                modifier = modifier
            )
        }
        is DeviceDetailUiState.Success -> {
            DeviceDetailScreenContent(
                device = uiState.device,
                batteryHealth = uiState.batteryHealth,
                predictions = uiState.predictions,
                modifier = modifier
            )
        }
    }
}

/**
 * Main content for device detail screen.
 */
@Composable
fun DeviceDetailScreenContent(
    device: Device,
    batteryHealth: BatteryHealth?,
    predictions: List<BatteryPrediction>,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Battery level card
        BatteryLevelCard(
            level = device.currentBatteryLevel,
            isCharging = device.isCharging,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Device info card
        DeviceInfoCard(
            device = device,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Battery health card
        if (batteryHealth != null) {
            BatteryHealthCard(
                health = batteryHealth,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Predictions card
        if (predictions.isNotEmpty()) {
            PredictionsCard(
                predictions = predictions,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Card showing current battery level.
 */
@Composable
fun BatteryLevelCard(
    level: Int?,
    isCharging: Boolean?,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = stringResource(R.string.battery_level),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (level != null) {
                Text(
                    text = "$level%",
                    style = MaterialTheme.typography.displayLarge
                )
            } else {
                Text(
                    text = stringResource(R.string.battery_unknown),
                    style = MaterialTheme.typography.displayLarge
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Battery icon
            Icon(
                imageVector = when (level) {
                    null -> Icons.Outlined.DeviceUnknown
                    in 0..20 -> Icons.Outlined.Battery0Bar
                    in 21..40 -> Icons.Outlined.Battery2Bar
                    in 41..60 -> Icons.Outlined.Battery4Bar
                    in 61..80 -> Icons.Outlined.Battery6Bar
                    else -> Icons.Outlined.BatteryFull
                },
                contentDescription = null,
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (isCharging == true) {
                Text(
                    text = stringResource(R.string.battery_charging),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (level == null) {
                Text(
                    text = stringResource(R.string.battery_not_available),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Card showing device information.
 */
@Composable
fun DeviceInfoCard(
    device: Device,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.device_name),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = device.alias ?: device.name,
                style = MaterialTheme.typography.bodyLarge
            )
            
            Row {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.device_type),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = device.type.name,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.device_manufacturer),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = device.manufacturer ?: stringResource(R.string.battery_unknown),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            Row {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.device_address),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = device.id,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.device_last_seen),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = device.lastSeen?.toString() ?: stringResource(R.string.battery_unknown),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

/**
 * Card showing battery health metrics.
 */
@Composable
fun BatteryHealthCard(
    health: BatteryHealth,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.device_battery_health),
                style = MaterialTheme.typography.titleMedium
            )
            
            Row {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.device_average_drain_rate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "%.1f%%/h".format(health.averageDrainRate),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.device_capacity_degradation),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = health.capacityDegradation?.let { "%.1f%%".format(it) } 
                            ?: stringResource(R.string.battery_unknown),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            if (health.lastFullCharge != null) {
                Text(
                    text = stringResource(R.string.device_time_between_charges),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = health.lastFullCharge.toString(),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

/**
 * Card showing battery predictions.
 */
@Composable
fun PredictionsCard(
    predictions: List<BatteryPrediction>,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Predictions",
                style = MaterialTheme.typography.titleMedium
            )
            
            predictions.forEach { prediction ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${prediction.milestone}%",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "in ~${prediction.estimatedTime}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = "Confidence: ${prediction.confidence * 100}%",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * UI state for the device detail screen.
 */
sealed class DeviceDetailUiState {
    object Loading : DeviceDetailUiState()
    data class Error(val message: String) : DeviceDetailUiState()
    data class Success(
        val device: Device,
        val batteryHealth: BatteryHealth?,
        val predictions: List<BatteryPrediction>
    ) : DeviceDetailUiState()
}

/**
 * Preview for DeviceDetailScreen.
 */
@Preview(showBackground = true)
@Composable
fun DeviceDetailScreenPreview() {
    BatteryGuardianTheme {
        DeviceDetailScreen(
            deviceId = "test",
            onBack = {},
            onSettingsClick = {}
        )
    }
}
