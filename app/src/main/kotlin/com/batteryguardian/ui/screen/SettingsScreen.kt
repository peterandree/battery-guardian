package com.batteryguardian.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Battery0Bar
import androidx.compose.material.icons.outlined.Battery1Bar
import androidx.compose.material.icons.outlined.Battery2Bar
import androidx.compose.material.icons.outlined.Battery3Bar
import androidx.compose.material.icons.outlined.Battery4Bar
import androidx.compose.material.icons.outlined.Battery5Bar
import androidx.compose.material.icons.outlined.Battery6Bar
import androidx.compose.material.icons.outlined.BatteryFull
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.batteryguardian.R
import com.batteryguardian.domain.model.BatteryDisplayFormat
import com.batteryguardian.domain.model.NotificationPriority
import com.batteryguardian.domain.model.UserPreferences
import com.batteryguardian.ui.theme.BatteryGuardianTheme

/**
 * Settings screen for Battery Guardian.
 * 
 * Allows users to configure:
 * - Alert thresholds
 * - Polling interval
 * - Hysteresis band
 * - Notification settings
 * - Appearance settings
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        SettingsContent(
            uiState = uiState,
            onToggleDarkTheme = { viewModel.toggleDarkTheme() },
            onSetPollingInterval = { viewModel.setPollingInterval(it) },
            onSetLowThreshold = { viewModel.setLowThreshold(it) },
            onSetMediumThreshold = { viewModel.setMediumThreshold(it) },
            onSetCriticalThreshold = { viewModel.setCriticalThreshold(it) },
            onSetHysteresisBand = { viewModel.setHysteresisBand(it) },
            onToggleNotifications = { viewModel.toggleNotifications() },
            onSetNotificationPriority = { viewModel.setNotificationPriority(it) },
            onSetBatteryDisplayFormat = { viewModel.setBatteryDisplayFormat(it) },
            modifier = Modifier.padding(paddingValues)
        )
    }
}

/**
 * Content for the settings screen.
 */
@Composable
fun SettingsContent(
    uiState: SettingsUiState,
    onToggleDarkTheme: () -> Unit,
    onSetPollingInterval: (Int) -> Unit,
    onSetLowThreshold: (Int) -> Unit,
    onSetMediumThreshold: (Int) -> Unit,
    onSetCriticalThreshold: (Int) -> Unit,
    onSetHysteresisBand: (Int) -> Unit,
    onToggleNotifications: () -> Unit,
    onSetNotificationPriority: (NotificationPriority) -> Unit,
    onSetBatteryDisplayFormat: (BatteryDisplayFormat) -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is SettingsUiState.Loading -> {
            // Loading state
        }
        is SettingsUiState.Success -> {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Monitoring settings section
                SettingsSection(title = stringResource(R.string.settings_monitoring)) {
                    PollingIntervalSetting(
                        currentValue = uiState.preferences.pollingInterval,
                        onValueChange = onSetPollingInterval
                    )
                }
                
                // Alerts settings section
                SettingsSection(title = stringResource(R.string.settings_alerts)) {
                    ThresholdSetting(
                        title = stringResource(R.string.settings_low_threshold),
                        currentValue = uiState.preferences.lowThreshold,
                        onValueChange = onSetLowThreshold
                    )
                    
                    ThresholdSetting(
                        title = stringResource(R.string.settings_medium_threshold),
                        currentValue = uiState.preferences.mediumThreshold,
                        onValueChange = onSetMediumThreshold
                    )
                    
                    ThresholdSetting(
                        title = stringResource(R.string.settings_critical_threshold),
                        currentValue = uiState.preferences.criticalThreshold,
                        onValueChange = onSetCriticalThreshold
                    )
                    
                    HysteresisSetting(
                        currentValue = uiState.preferences.hysteresisBand,
                        onValueChange = onSetHysteresisBand
                    )
                    
                    NotificationSettings(
                        notificationsEnabled = uiState.preferences.notificationsEnabled,
                        notificationPriority = uiState.preferences.notificationPriority,
                        onToggleNotifications = onToggleNotifications,
                        onSetNotificationPriority = onSetNotificationPriority
                    )
                }
                
                // Appearance settings section
                SettingsSection(title = stringResource(R.string.settings_appearance)) {
                    DarkThemeSetting(
                        darkTheme = uiState.preferences.darkTheme,
                        onToggle = onToggleDarkTheme
                    )
                    
                    BatteryDisplaySetting(
                        currentFormat = uiState.preferences.batteryDisplayFormat,
                        onSetFormat = onSetBatteryDisplayFormat
                    )
                }
            }
        }
    }
}

/**
 * Settings section with a title and content.
 */
@Composable
fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

/**
 * Polling interval setting.
 */
@Composable
fun PollingIntervalSetting(
    currentValue: Int,
    onValueChange: (Int) -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.settings_polling_interval),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = stringResource(R.string.settings_polling_interval_summary),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            val options = listOf(
                Triple(1, R.string.settings_minutes_1, 1),
                Triple(5, R.string.settings_minutes_5, 5),
                Triple(15, R.string.settings_minutes_15, 15)
            )
            
            options.forEach { (value, labelRes, minutes) ->
                val selected = currentValue == minutes
                SettingOption(
                    label = stringResource(labelRes),
                    selected = selected,
                    onClick = { onValueChange(minutes) }
                )
            }
        }
    }
}

/**
 * Threshold setting for battery alerts.
 */
@Composable
fun ThresholdSetting(
    title: String,
    currentValue: Int,
    onValueChange: (Int) -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Slider(
                value = currentValue.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = 5f..100f,
                steps = 19,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "5%",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "$currentValue%",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "100%",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * Hysteresis setting.
 */
@Composable
fun HysteresisSetting(
    currentValue: Int,
    onValueChange: (Int) -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.settings_hysteresis_band),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = stringResource(R.string.settings_hysteresis_summary),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Slider(
                value = currentValue.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = 1f..10f,
                steps = 9,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "1%",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "$currentValue%",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "10%",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * Notification settings.
 */
@Composable
fun NotificationSettings(
    notificationsEnabled: Boolean,
    notificationPriority: NotificationPriority,
    onToggleNotifications: () -> Unit,
    onSetNotificationPriority: (NotificationPriority) -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.settings_notifications_enabled),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = onToggleNotifications
                )
            }
            
            Text(
                text = stringResource(R.string.settings_notification_priority),
                style = MaterialTheme.typography.bodyLarge
            )
            
            val options = listOf(
                NotificationPriority.LOW to stringResource(R.string.settings_minutes_1),
                NotificationPriority.DEFAULT to "Default",
                NotificationPriority.HIGH to "High",
                NotificationPriority.URGENT to "Urgent"
            )
            
            options.forEach { (priority, label) ->
                val selected = notificationPriority == priority
                SettingOption(
                    label = label,
                    selected = selected,
                    onClick = { onSetNotificationPriority(priority) }
                )
            }
        }
    }
}

/**
 * Dark theme setting.
 */
@Composable
fun DarkThemeSetting(
    darkTheme: Boolean,
    onToggle: () -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_dark_theme),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = darkTheme,
                onCheckedChange = onToggle
            )
        }
    }
}

/**
 * Battery display format setting.
 */
@Composable
fun BatteryDisplaySetting(
    currentFormat: BatteryDisplayFormat,
    onSetFormat: (BatteryDisplayFormat) -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_battery_display),
                style = MaterialTheme.typography.bodyLarge
            )
            
            val options = listOf(
                BatteryDisplayFormat.PERCENTAGE to "Percentage",
                BatteryDisplayFormat.ICON to "Icon",
                BatteryDisplayFormat.BOTH to "Both"
            )
            
            options.forEach { (format, label) ->
                val selected = currentFormat == format
                SettingOption(
                    label = label,
                    selected = selected,
                    onClick = { onSetFormat(format) }
                )
            }
        }
    }
}

/**
 * Setting option (radio button).
 */
@Composable
fun SettingOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(8.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * UI state for the settings screen.
 */
sealed class SettingsUiState {
    object Loading : SettingsUiState()
    data class Success(val preferences: UserPreferences) : SettingsUiState()
}

/**
 * Preview for SettingsScreen.
 */
@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    BatteryGuardianTheme {
        SettingsScreen(onBack = {})
    }
}
