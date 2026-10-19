package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HudPreset
import com.example.ui.MainViewModel
import com.example.ui.components.CyberCard
import com.example.ui.components.PerformanceGraph
import com.example.ui.components.TelemetryBadge
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.HudAccentOptions
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val isHudRunning by viewModel.isHudRunning.collectAsState()
    val telemetry by viewModel.liveTelemetry.collectAsState()
    val config by viewModel.configState.collectAsState()
    val hasOverlayPermission by viewModel.hasOverlayPermission.collectAsState()
    val isShizukuInstalled by viewModel.isShizukuInstalled.collectAsState()
    val fpsHistory by viewModel.fpsHistory.collectAsState()
    val tempHistory by viewModel.tempHistory.collectAsState()

    val currentAccent = HudAccentOptions.getOrElse(config.accentColorIndex) { CyberGreen }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isHudRunning) 1.05f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Hardware profile badge
            CyberCard(borderColor = DarkBorder) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TARGET HARDWARE PROFILE",
                            color = NeonCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Xiaomi 11T Pro • Snapdragon 888",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "120Hz AMOLED • Android 14 HyperOS / AOSP",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceElevated)
                            .border(1.dp, currentAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isHudRunning) "LIVE OVERLAY" else "STANDBY",
                            color = if (isHudRunning) CyberGreen else TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Overlay Permission Banner
        if (!hasOverlayPermission) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2A1515))
                        .border(1.5.dp, Color(0xFFFF5252), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Permission Alert",
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Overlay Permission Required",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Android requires 'Display over other apps' permission so GameCore Pro HUD can float above games.",
                            color = Color(0xFFFFCDD2),
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.requestOverlayPermission(context) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("grant_overlay_permission_button")
                        ) {
                            Text("Grant Overlay Permission", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Hero Start / Stop HUD Card
        item {
            val heroBorderColor by animateColorAsState(
                targetValue = if (isHudRunning) CyberGreen else DarkBorder,
                label = "border_anim"
            )

            CyberCard(
                borderColor = heroBorderColor,
                borderWidth = 1.5.dp,
                glowGradient = isHudRunning
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isHudRunning) "GAMING HUD ACTIVE" else "HUD READY",
                        color = if (isHudRunning) CyberGreen else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Big Action Button
                    Box(
                        modifier = Modifier
                            .scale(pulseScale)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                if (isHudRunning) {
                                    Brush.horizontalGradient(listOf(Color(0xFF880E4F), Color(0xFFD50000)))
                                } else {
                                    Brush.horizontalGradient(listOf(Color(0xFF00796B), CyberGreen))
                                }
                            )
                            .border(
                                width = 1.5.dp,
                                color = Color.White.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .clickable {
                                if (!hasOverlayPermission) {
                                    viewModel.requestOverlayPermission(context)
                                } else {
                                    viewModel.toggleHud()
                                }
                            }
                            .testTag(if (isHudRunning) "stop_hud_button" else "start_hud_button")
                            .padding(horizontal = 36.dp, vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (isHudRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = if (isHudRunning) "Stop HUD" else "Start HUD",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = if (isHudRunning) "STOP HUD OVERLAY" else "START HUD OVERLAY",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isHudRunning) {
                            "HUD is floating above active game. Drag to move, tap to minimize."
                        } else {
                            "Tap to launch floating HUD on top of any game."
                        },
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Live Telemetry Grid
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "LIVE HARDWARE TELEMETRY",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Row 1: FPS / Display Hz & Battery %
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val fpsVal = telemetry.fps ?: telemetry.displayHz
                    val fpsLabel = if (telemetry.isElevatedFpsActive) "True Game FPS" else "Display Rate"
                    TelemetryBadge(
                        title = fpsLabel,
                        value = "%.0f".format(fpsVal),
                        unit = if (telemetry.isElevatedFpsActive) "FPS" else "Hz",
                        icon = Icons.Default.Speed,
                        accentColor = currentAccent,
                        subValue = telemetry.fpsSource.displayName,
                        modifier = Modifier.weight(1f)
                    )

                    TelemetryBadge(
                        title = "Battery",
                        value = "${telemetry.batteryPercent}%",
                        unit = if (telemetry.isCharging) "⚡ Charging" else "",
                        icon = Icons.Default.Bolt,
                        accentColor = if (telemetry.batteryPercent <= 20) Color(0xFFFF5252) else CyberGreen,
                        subValue = "${telemetry.batteryVoltageMv} mV",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row 2: Battery Temp & System Thermal Level
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TelemetryBadge(
                        title = "Bat Temperature",
                        value = "%.1f".format(telemetry.batteryTempC),
                        unit = "°C",
                        icon = Icons.Default.Thermostat,
                        accentColor = if (telemetry.batteryTempC >= 42f) Color(0xFFFF1744) else CyberGreen,
                        subValue = if (telemetry.batteryTempC < 38f) "Cool State" else "Warm (Gaming)",
                        modifier = Modifier.weight(1f)
                    )

                    TelemetryBadge(
                        title = "Thermal Status",
                        value = telemetry.thermalStatus,
                        unit = "",
                        icon = Icons.Default.Security,
                        accentColor = when (telemetry.thermalStatusCode) {
                            0 -> CyberGreen
                            1 -> NeonCyan
                            2 -> Color(0xFFFFD600)
                            else -> Color(0xFFFF1744)
                        },
                        subValue = "Code: ${telemetry.thermalStatusCode}",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row 3: RAM & CPU Zone Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val usedGb = telemetry.ramUsedMb / 1024f
                    val totalGb = telemetry.ramTotalMb / 1024f
                    TelemetryBadge(
                        title = "Memory (RAM)",
                        value = "%.1f / %.1f".format(usedGb, totalGb),
                        unit = "GB",
                        icon = Icons.Default.Memory,
                        accentColor = NeonCyan,
                        subValue = "${telemetry.ramUsagePercent}% In Use",
                        modifier = Modifier.weight(1f)
                    )

                    val (cpuTitle, cpuUnit) = if (telemetry.cpuTempC != null) {
                        "%.1f°C".format(telemetry.cpuTempC) to "Real Sensor"
                    } else {
                        "SELinux" to "Req. Shizuku"
                    }
                    TelemetryBadge(
                        title = "CPU Thermal Zone",
                        value = cpuTitle,
                        unit = "",
                        icon = Icons.Default.Thermostat,
                        accentColor = if (telemetry.cpuTempC != null) currentAccent else Color(0xFF94A3B8),
                        subValue = cpuUnit,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Live Performance Graphs
        item {
            PerformanceGraph(
                title = "Live Display Cadence Curve",
                dataPoints = fpsHistory,
                lineColor = currentAccent,
                unit = "Hz",
                minExpected = 40f,
                maxExpected = 120f
            )
        }

        item {
            PerformanceGraph(
                title = "Battery Temperature Curve",
                dataPoints = tempHistory,
                lineColor = Color(0xFFFF9100),
                unit = "°C",
                minExpected = 25f,
                maxExpected = 50f
            )
        }

        // Quick Preset Selector
        item {
            CyberCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "HUD LAYOUT PRESET",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HudPreset.values().forEach { preset ->
                            val isSelected = config.preset == preset
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) currentAccent.copy(alpha = 0.2f) else DarkSurfaceVariant)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) currentAccent else DarkBorder,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.setPreset(preset) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = preset.label,
                                        color = if (isSelected) Color.White else TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
