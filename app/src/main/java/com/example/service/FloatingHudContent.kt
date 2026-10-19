package com.example.service

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HudConfig
import com.example.model.HudPreset
import com.example.model.TelemetryData
import com.example.ui.theme.HudAccentOptions

@Composable
fun FloatingHudContent(
    telemetry: TelemetryData,
    config: HudConfig,
    onDragDelta: (dx: Float, dy: Float) -> Unit,
    onToggleCollapse: () -> Unit,
    onClose: () -> Unit
) {
    val accentColor = HudAccentOptions.getOrElse(config.accentColorIndex) {
        HudAccentOptions[0]
    }

    val alpha = (config.alphaPercent / 100f).coerceIn(0.2f, 1.0f)
    val baseScale = (config.scalePercent / 100f).coerceIn(0.6f, 1.4f)

    val backgroundColor = Color(0xFF0A0F1D).copy(alpha = alpha)
    val cardBorderColor = accentColor.copy(alpha = 0.8f)

    if (config.isCollapsed) {
        // Collapsed mini pill mode
        Box(
            modifier = Modifier
                .wrapContentSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onDragDelta(dragAmount.x, dragAmount.y)
                    }
                }
                .clip(RoundedCornerShape(20.dp))
                .background(backgroundColor)
                .border(1.5.dp, cardBorderColor, RoundedCornerShape(20.dp))
                .clickable { onToggleCollapse() }
                .padding(horizontal = (10 * baseScale).dp, vertical = (6 * baseScale).dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy((6 * baseScale).dp)
            ) {
                // Pulse dot
                Box(
                    modifier = Modifier
                        .size((8 * baseScale).dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )

                // Live FPS or Hz
                val fpsText = if (telemetry.fps != null) {
                    "${telemetry.fps.toInt()} FPS"
                } else {
                    "${telemetry.displayHz.toInt()} HZ"
                }
                Text(
                    text = fpsText,
                    color = Color.White,
                    fontSize = (12 * baseScale).sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Text(
                    text = "•",
                    color = Color.Gray,
                    fontSize = (12 * baseScale).sp
                )

                // Battery
                Text(
                    text = "${telemetry.batteryPercent}%",
                    color = if (telemetry.batteryPercent <= 20) Color(0xFFFF5252) else accentColor,
                    fontSize = (12 * baseScale).sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )

                // Bat Temp
                Text(
                    text = "%.1f°C".format(telemetry.batteryTempC),
                    color = if (telemetry.batteryTempC >= 42f) Color(0xFFFF5252) else Color(0xFFE2E8F0),
                    fontSize = (11 * baseScale).sp,
                    fontFamily = FontFamily.Monospace
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand HUD",
                    tint = Color.LightGray,
                    modifier = Modifier.size((14 * baseScale).dp)
                )
            }
        }
    } else {
        // Expanded full HUD panel
        val panelWidth = (210 * baseScale).dp

        Box(
            modifier = Modifier
                .width(panelWidth)
                .wrapContentHeight()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onDragDelta(dragAmount.x, dragAmount.y)
                    }
                }
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
                .border(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        listOf(accentColor, accentColor.copy(alpha = 0.3f))
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding((8 * baseScale).dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Bar (Grip + Title + Minimize + Close)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenWith,
                            contentDescription = "Drag Handle",
                            tint = accentColor,
                            modifier = Modifier.size((14 * baseScale).dp)
                        )
                        Text(
                            text = "GAMECORE",
                            color = accentColor,
                            fontSize = (11 * baseScale).sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(accentColor.copy(alpha = 0.2f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = config.preset.label.uppercase(),
                                color = Color.White,
                                fontSize = (8 * baseScale).sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // Minimize/Collapse
                        Box(
                            modifier = Modifier
                                .size((22 * baseScale).dp)
                                .clickable { onToggleCollapse() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Minimize HUD",
                                tint = Color.LightGray,
                                modifier = Modifier.size((16 * baseScale).dp)
                            )
                        }

                        // Close/Stop HUD
                        Box(
                            modifier = Modifier
                                .size((22 * baseScale).dp)
                                .clickable { onClose() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close HUD",
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size((14 * baseScale).dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height((6 * baseScale).dp))

                // Telemetry Metrics Grid
                // 1. FPS / Display Hz
                if (config.showFps) {
                    val fpsValueStr = if (telemetry.fps != null) {
                        telemetry.fps.toInt().toString()
                    } else {
                        telemetry.displayHz.toInt().toString()
                    }
                    val fpsLabel = if (telemetry.isElevatedFpsActive) "FPS" else "DISP HZ"

                    MetricRow(
                        label = fpsLabel,
                        value = fpsValueStr,
                        unit = if (telemetry.isElevatedFpsActive) "fps" else "Hz",
                        valueColor = if ((telemetry.fps ?: telemetry.displayHz) >= 55f) accentColor else Color(0xFFFFB300),
                        baseScale = baseScale
                    )
                }

                // 2. Battery Percentage
                if (config.showBatteryPct) {
                    val batColor = when {
                        telemetry.batteryPercent <= 20 -> Color(0xFFFF5252)
                        telemetry.batteryPercent <= 50 -> Color(0xFFFFD600)
                        else -> Color(0xFF00E676)
                    }
                    val chargingIndicator = if (telemetry.isCharging) " ⚡" else ""
                    MetricRow(
                        label = "BAT",
                        value = "${telemetry.batteryPercent}%$chargingIndicator",
                        unit = if (telemetry.batteryCurrentMa != null) "${telemetry.batteryCurrentMa}mA" else "",
                        valueColor = batColor,
                        baseScale = baseScale
                    )
                }

                // 3. Battery Temperature
                if (config.effectiveShowBatteryTemp()) {
                    val tempColor = when {
                        telemetry.batteryTempC >= 43.0f -> Color(0xFFFF1744)
                        telemetry.batteryTempC >= 39.0f -> Color(0xFFFF9100)
                        else -> Color(0xFF69F0AE)
                    }
                    MetricRow(
                        label = "BAT TEMP",
                        value = "%.1f".format(telemetry.batteryTempC),
                        unit = "°C",
                        valueColor = tempColor,
                        baseScale = baseScale
                    )
                }

                // 4. System Thermal Level
                if (config.effectiveShowThermalStatus()) {
                    val thermalColor = when (telemetry.thermalStatusCode) {
                        0 -> Color(0xFF00E676) // Normal
                        1 -> Color(0xFF69F0AE) // Light
                        2 -> Color(0xFFFFD600) // Moderate
                        3, 4 -> Color(0xFFFF1744) // Severe / Critical
                        else -> Color(0xFFFF1744)
                    }
                    MetricRow(
                        label = "THERMAL",
                        value = telemetry.thermalStatus,
                        unit = "",
                        valueColor = thermalColor,
                        baseScale = baseScale
                    )
                }

                // 5. CPU Temperature (truthful readout; shows N/A if SELinux protected)
                if (config.effectiveShowCpuTemp()) {
                    val (cpuValue, cpuColor) = if (telemetry.cpuTempC != null) {
                        "%.1f°C".format(telemetry.cpuTempC) to accentColor
                    } else {
                        "N/A" to Color(0xFF94A3B8)
                    }
                    MetricRow(
                        label = "CPU TEMP",
                        value = cpuValue,
                        unit = if (telemetry.cpuTempC == null) "SELinux" else "",
                        valueColor = cpuColor,
                        baseScale = baseScale
                    )
                }

                // 6. CPU Usage
                if (config.effectiveShowCpuUsage()) {
                    MetricRow(
                        label = "CPU USAGE",
                        value = telemetry.cpuUsagePercent?.let { "%.0f%%".format(it) } ?: "N/A",
                        unit = if (telemetry.cpuUsagePercent == null) "Restricted" else "",
                        valueColor = Color(0xFF94A3B8),
                        baseScale = baseScale
                    )
                }

                // 7. RAM Usage
                if (config.effectiveShowRam()) {
                    val usedGb = telemetry.ramUsedMb / 1024f
                    val totalGb = telemetry.ramTotalMb / 1024f
                    MetricRow(
                        label = "RAM",
                        value = "%.1f/%.1f".format(usedGb, totalGb),
                        unit = "GB (${telemetry.ramUsagePercent}%)",
                        valueColor = if (telemetry.ramUsagePercent > 85) Color(0xFFFF5252) else Color.White,
                        baseScale = baseScale
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricRow(
    label: String,
    value: String,
    unit: String,
    valueColor: Color,
    baseScale: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = (2 * baseScale).dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color(0xFF94A3B8),
            fontSize = (10 * baseScale).sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace
        )
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                color = valueColor,
                fontSize = (12 * baseScale).sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            if (unit.isNotEmpty()) {
                Text(
                    text = unit,
                    color = Color(0xFF64748B),
                    fontSize = (9 * baseScale).sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
