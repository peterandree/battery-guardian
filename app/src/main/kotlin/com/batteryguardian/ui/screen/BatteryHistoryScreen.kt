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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.batteryguardian.R
import com.batteryguardian.domain.model.Device
import com.batteryguardian.domain.model.DeviceType
import com.batteryguardian.domain.repository.BatteryLevel
import com.batteryguardian.domain.usecase.GetDeviceHistoryUseCase
import com.batteryguardian.ui.component.BatteryHistoryChart
import com.batteryguardian.ui.theme.BatteryGuardianTheme
import com.batteryguardian.util.formatDate
import com.batteryguardian.util.formatTime
import java.time.Instant

/**
 * Screen for displaying battery level history for a device.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryHistoryScreen(
    deviceId: String,
    onBack: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: BatteryHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Load battery history when screen is composed
    LaunchedEffect(deviceId) {
        viewModel.loadBatteryHistory(deviceId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Battery History") },
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
        BatteryHistoryContent(
            uiState = uiState,
            onTimeRangeSelected = { timeRange ->
                viewModel.setTimeRange(timeRange)
            },
            modifier = Modifier.padding(paddingValues)
        )
    }
}

/**
 * Content for the battery history screen.
 */
@Composable
fun BatteryHistoryContent(
    uiState: BatteryHistoryUiState,
    onTimeRangeSelected: (TimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Time range selector
        TimeRangeSelector(
            selectedRange = uiState.selectedTimeRange,
            onSelected = onTimeRangeSelected
        )

        // Statistics card
        if (uiState is BatteryHistoryUiState.Success) {
            BatteryStatisticsCard(
                statistics = uiState.statistics,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Chart
        when (uiState) {
            is BatteryHistoryUiState.Loading -> {
                // Loading placeholder
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text("Loading battery history...")
                    }
                }
            }
            is BatteryHistoryUiState.Error -> {
                // Error placeholder
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(uiState.message)
                    }
                }
            }
            is BatteryHistoryUiState.Success -> {
                BatteryHistoryChart(
                    batteryLevels = uiState.batteryLevels,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Battery level list
                BatteryLevelList(
                    batteryLevels = uiState.batteryLevels,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Time range selector for battery history.
 */
@Composable
fun TimeRangeSelector(
    selectedRange: TimeRange,
    onSelected: (TimeRange) -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.padding(8.dp)
        ) {
            TimeRange.values().forEach { range ->
                TimeRangeButton(
                    range = range,
                    isSelected = range == selectedRange,
                    onClick = { onSelected(range) }
                )
            }
        }
    }
}

/**
 * Button for selecting a time range.
 */
@Composable
fun TimeRangeButton(
    range: TimeRange,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .then(if (isSelected) {
                Modifier
            } else {
                Modifier
            })
    ) {
        Text(
            text = range.getDisplayString(),
            color = textColor,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .then(if (isSelected) {
                    Modifier
                        .then(Modifier)
                } else {
                    Modifier
                })
        )
    }
}

/**
 * Card showing battery statistics.
 */
@Composable
fun BatteryStatisticsCard(
    statistics: BatteryStatistics,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.titleMedium
            )
            
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                StatisticItem(
                    label = "Average",
                    value = "%.1f%%".format(statistics.averageLevel),
                    modifier = Modifier.weight(1f)
                )
                
                StatisticItem(
                    label = "Min",
                    value = "${statistics.minLevel}%",
                    modifier = Modifier.weight(1f)
                )
                
                StatisticItem(
                    label = "Max",
                    value = "${statistics.maxLevel}%",
                    modifier = Modifier.weight(1f)
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                StatisticItem(
                    label = "Drain Rate",
                    value = "%.1f%%/h".format(statistics.averageDrainRate),
                    modifier = Modifier.weight(1f)
                )
                
                StatisticItem(
                    label = "Cycles",
                    value = statistics.totalDischargeCycles.toString(),
                    modifier = Modifier.weight(1f)
                )
                
                StatisticItem(
                    label = "Avg Discharge",
                    value = statistics.averageDischargeTime.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Single statistic item.
 */
@Composable
fun StatisticItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * List of battery level readings.
 */
@Composable
fun BatteryLevelList(
    batteryLevels: List<BatteryLevel>,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Battery Level History",
                style = MaterialTheme.typography.titleMedium
            )
            
            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(batteryLevels) { level ->
                    BatteryLevelItem(
                        level = level,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * Single battery level item in the list.
 */
@Composable
fun BatteryLevelItem(
    level: BatteryLevel,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = formatTime(level.timestamp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.3f)
        )
        
        Spacer(modifier = Modifier.weight(0.1f))
        
        Text(
            text = "${level.level}%",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(0.3f)
        )
        
        Spacer(modifier = Modifier.weight(0.1f))
        
        Text(
            text = if (level.isPredicted) "Predicted" else "Measured",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.2f)
        )
    }
}

/**
 * UI state for the battery history screen.
 */
sealed class BatteryHistoryUiState {
    object Loading : BatteryHistoryUiState()
    data class Error(val message: String) : BatteryHistoryUiState()
    data class Success(
        val batteryLevels: List<BatteryLevel>,
        val statistics: BatteryStatistics,
        val selectedTimeRange: TimeRange
    ) : BatteryHistoryUiState()
}

/**
 * Battery statistics for display.
 */
data class BatteryStatistics(
    val averageLevel: Float,
    val minLevel: Int,
    val maxLevel: Int,
    val averageDrainRate: Float,
    val totalDischargeCycles: Int,
    val averageDischargeTime: java.time.Duration
)

/**
 * Time range for battery history.
 */
enum class TimeRange {
    LAST_24_HOURS,
    LAST_7_DAYS,
    LAST_30_DAYS,
    ALL_TIME
    ;

    fun getDisplayString(): String {
        return when (this) {
            LAST_24_HOURS -> "24 Hours"
            LAST_7_DAYS -> "7 Days"
            LAST_30_DAYS -> "30 Days"
            ALL_TIME -> "All Time"
        }
    }
}

/**
 * ViewModel for the battery history screen.
 */
class BatteryHistoryViewModel @Inject constructor(
    private val getDeviceHistoryUseCase: GetDeviceHistoryUseCase
) : androidx.lifecycle.ViewModel() {

    private val _uiState = androidx.lifecycle.MutableLiveData<BatteryHistoryUiState>(BatteryHistoryUiState.Loading)
    val uiState: androidx.lifecycle.LiveData<BatteryHistoryUiState> = _uiState

    private var currentDeviceId: String = ""

    /**
     * Load battery history for a device.
     */
    fun loadBatteryHistory(deviceId: String) {
        currentDeviceId = deviceId
        _uiState.value = BatteryHistoryUiState.Loading
        
        // In a real implementation, this would use coroutines and Flow
        // For now, just set a placeholder state
        _uiState.value = BatteryHistoryUiState.Success(
            batteryLevels = emptyList(),
            statistics = BatteryStatistics(
                averageLevel = 0f,
                minLevel = 0,
                maxLevel = 0,
                averageDrainRate = 0f,
                totalDischargeCycles = 0,
                averageDischargeTime = java.time.Duration.ZERO
            ),
            selectedTimeRange = TimeRange.LAST_24_HOURS
        )
    }

    /**
     * Set the selected time range.
     */
    fun setTimeRange(timeRange: TimeRange) {
        val currentState = _uiState.value
        if (currentState is BatteryHistoryUiState.Success) {
            _uiState.value = currentState.copy(selectedTimeRange = timeRange)
        }
    }
}

/**
 * Preview for BatteryHistoryScreen.
 */
@Preview(showBackground = true)
@Composable
fun BatteryHistoryScreenPreview() {
    BatteryGuardianTheme {
        BatteryHistoryScreen(
            deviceId = "test",
            onBack = {},
            onSettingsClick = {}
        )
    }
}
