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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.batteryguardian.R
import com.batteryguardian.domain.model.AlertState
import com.batteryguardian.domain.model.Device
import com.batteryguardian.domain.model.DeviceType
import com.batteryguardian.ui.theme.BatteryCritical
import com.batteryguardian.ui.theme.BatteryHigh
import com.batteryguardian.ui.theme.BatteryLow
import com.batteryguardian.ui.theme.BatteryMedium
import com.batteryguardian.ui.theme.BatteryGuardianTheme

/**
 * Main screen for Battery Guardian.
 * 
 * Displays a list of all monitored Bluetooth devices with their battery levels.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onDeviceClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error messages in snackbar
    if (uiState.error != null) {
        LaunchedEffect(uiState.error) {
            snackbarHostState.showSnackbar(uiState.error.message)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh))
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings))
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.refresh() },
                icon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                text = { Text(stringResource(R.string.refresh)) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        MainContent(
            uiState = uiState,
            onDeviceClick = onDeviceClick,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.padding(paddingValues)
        )
    }
}

/**
 * Main content for the main screen.
 */
@Composable
fun MainContent(
    uiState: MainUiState,
    onDeviceClick: (String) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is MainUiState.Loading -> {
            LoadingScreen(modifier = modifier)
        }
        is MainUiState.Error -> {
            ErrorScreen(
                message = uiState.message,
                onRetry = onRefresh,
                modifier = modifier
            )
        }
        is MainUiState.Success -> {
            if (uiState.devices.isEmpty()) {
                EmptyStateScreen(modifier = modifier)
            } else {
                DeviceList(
                    devices = uiState.devices,
                    onDeviceClick = onDeviceClick,
                    modifier = modifier
                )
            }
        }
    }
}

/**
 * Loading screen.
 */
@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Error screen.
 */
@Composable
fun ErrorScreen(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        Icon(
            Icons.Outlined.BluetoothDisabled,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        FloatingActionButton(onClick = onRetry) {
            Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh))
        }
    }
}

/**
 * Empty state screen (no devices).
 */
@Composable
fun EmptyStateScreen(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        Icon(
            Icons.Outlined.Bluetooth,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.no_devices),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.no_devices_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * List of devices with their battery levels.
 */
@Composable
fun DeviceList(
    devices: List<Device>,
    onDeviceClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        items(devices) { device ->
            DeviceCard(
                device = device,
                onClick = { onDeviceClick(device.id) }
            )
        }
    }
}

/**
 * Card for a single device.
 */
@Composable
fun DeviceCard(
    device: Device,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
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
            
            Spacer(modifier = Modifier.size(16.dp))
            
            // Device info
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = device.alias ?: device.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Device type and connection status
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = device.type.name.lowercase(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (!device.isConnected) {
                        Spacer(modifier = Modifier.size(8.dp))
                        Icon(
                            Icons.Outlined.BluetoothDisabled,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.size(16.dp))
            
            // Battery level
            BatteryLevelDisplay(
                level = device.currentBatteryLevel,
                isCharging = device.isCharging,
                alertState = device.alertState
            )
        }
    }
}

/**
 * Device icon based on type.
 */
@Composable
fun DeviceIcon(
    device: Device,
    modifier: Modifier = Modifier
) {
    val icon = when (device.type) {
        DeviceType.HEADPHONS -> Icons.Outlined.BatteryFull
        DeviceType.SPEAKER -> Icons.Outlined.BatteryFull
        DeviceType.SMARTWATCH -> Icons.Outlined.BatteryFull
        DeviceType.KEYBOARD -> Icons.Outlined.BatteryFull
        DeviceType.MOUSE -> Icons.Outlined.BatteryFull
        DeviceType.GAME_CONTROLLER -> Icons.Outlined.BatteryFull
        DeviceType.HEARING_AID -> Icons.Outlined.BatteryFull
        DeviceType.MEDICAL_DEVICE -> Icons.Outlined.BatteryFull
        DeviceType.OTHER -> Icons.Outlined.Bluetooth
    }
    
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = modifier,
        tint = when (device.currentBatteryLevel) {
            null -> MaterialTheme.colorScheme.onSurfaceVariant
            in 0..20 -> BatteryCritical
            in 21..50 -> BatteryLow
            in 51..80 -> BatteryMedium
            else -> BatteryHigh
        }
    )
}

/**
 * Battery level display with color coding.
 */
@Composable
fun BatteryLevelDisplay(
    level: Int?,
    isCharging: Boolean?,
    alertState: AlertState
) {
    Column(
        horizontalAlignment = Alignment.End
    ) {
        if (level != null) {
            Text(
                text = "$level%",
                style = MaterialTheme.typography.titleMedium,
                color = when {
                    alertState is AlertState.Low -> MaterialTheme.colorScheme.error
                    isCharging == true -> MaterialTheme.colorScheme.primary
                    level <= 20 -> BatteryCritical
                    level <= 50 -> BatteryLow
                    level <= 80 -> BatteryMedium
                    else -> BatteryHigh
                }
            )
        } else {
            Icon(
                Icons.Outlined.HelpOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
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
    }
}

/**
 * UI state for the main screen.
 */
sealed class MainUiState {
    object Loading : MainUiState()
    data class Error(val message: String) : MainUiState()
    data class Success(val devices: List<Device>) : MainUiState()
}

/**
 * Preview for MainScreen.
 */
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    BatteryGuardianTheme {
        MainScreen(
            onDeviceClick = {},
            onSettingsClick = {}
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
                name = "Test Device",
                alias = null,
                type = DeviceType.HEADPHONES,
                manufacturer = "Test",
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
