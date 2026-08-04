package com.batteryguardian.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.batteryguardian.domain.repository.BatteryLevel
import com.batteryguardian.ui.theme.BatteryCritical
import com.batteryguardian.ui.theme.BatteryHigh
import com.batteryguardian.ui.theme.BatteryLow
import com.batteryguardian.ui.theme.BatteryMedium
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Composable for displaying battery level history as a line chart.
 */
@Composable
fun BatteryHistoryChart(
    batteryLevels: List<BatteryLevel>,
    modifier: Modifier = Modifier,
    height: Dp = 200.dp,
    showTimeLabels: Boolean = true,
    showLevelLabels: Boolean = true
) {
    if (batteryLevels.isEmpty()) {
        EmptyChart(modifier = modifier)
        return
    }

    // Sort by timestamp
    val sortedLevels = remember(batteryLevels) {
        batteryLevels.sortedBy { it.timestamp }
    }

    // Calculate dimensions
    val spacing = with(LocalDensity.current) { 40.dp.toPx() }
    val textHeight = with(LocalDensity.current) { 20.dp.toPx() }

    Column(modifier = modifier) {
        if (showLevelLabels) {
            // Y-axis labels
            Column(modifier = Modifier.padding(start = spacing)) {
                Text(
                    text = "100%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = "75%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = "50%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = "25%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = "0%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Chart canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .padding(start = spacing, end = 8.dp, top = 8.dp, bottom = 8.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Calculate data points
            val dataPoints = sortedLevels.mapIndexed { index, level ->
                val x = (index.toFloat() / (sortedLevels.size - 1)) * canvasWidth
                val y = canvasHeight - (level.level.toFloat() / 100 * canvasHeight)
                Offset(x, y)
            }

            if (dataPoints.isNotEmpty()) {
                // Draw the line
                val path = Path().apply {
                    dataPoints.forEachIndexed { index, point ->
                        if (index == 0) {
                            moveTo(point.x, point.y)
                        } else {
                            lineTo(point.x, point.y)
                        }
                    }
                }

                drawPath(
                    path = path,
                    color = MaterialTheme.colorScheme.primary,
                    style = Stroke(width = 4f)
                )

                // Draw data points
                dataPoints.forEach { point ->
                    drawCircle(
                        color = MaterialTheme.colorScheme.primary,
                        radius = 6f,
                        center = point
                    )
                }

                // Draw horizontal lines
                drawLine(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    start = Offset(0f, canvasHeight * 0.25f),
                    end = Offset(canvasWidth, canvasHeight * 0.25f),
                    strokeWidth = 1f
                )
                drawLine(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    start = Offset(0f, canvasHeight * 0.5f),
                    end = Offset(canvasWidth, canvasHeight * 0.5f),
                    strokeWidth = 1f
                )
                drawLine(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    start = Offset(0f, canvasHeight * 0.75f),
                    end = Offset(canvasWidth, canvasHeight * 0.75f),
                    strokeWidth = 1f
                )

                // Draw threshold lines
                drawLine(
                    color = BatteryCritical.copy(alpha = 0.3f),
                    start = Offset(0f, canvasHeight * 0.8f), // 20%
                    end = Offset(canvasWidth, canvasHeight * 0.8f),
                    strokeWidth = 2f
                )
                drawLine(
                    color = BatteryLow.copy(alpha = 0.3f),
                    start = Offset(0f, canvasHeight * 0.9f), // 10%
                    end = Offset(canvasWidth, canvasHeight * 0.9f),
                    strokeWidth = 2f
                )
            }
        }

        if (showTimeLabels) {
            // X-axis labels
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = formatTime(sortedLevels.first().timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = spacing)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = formatTime(sortedLevels.last().timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Empty chart placeholder.
 */
@Composable
fun EmptyChart(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Text(
            text = "No battery history available",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Format timestamp for display.
 */
private fun formatTime(timestamp: Instant): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    return timestamp.atZone(ZoneId.systemDefault()).format(formatter)
}

/**
 * Format date for display.
 */
fun formatDate(timestamp: Instant): String {
    val formatter = DateTimeFormatter.ofPattern("MMM dd")
    return timestamp.atZone(ZoneId.systemDefault()).format(formatter)
}

/**
 * Get color for a battery level.
 */
@Composable
fun getBatteryColor(level: Int?): Color {
    return when (level) {
        null -> MaterialTheme.colorScheme.onSurfaceVariant
        in 0..20 -> BatteryCritical
        in 21..50 -> BatteryLow
        in 51..80 -> BatteryMedium
        else -> BatteryHigh
    }
}
