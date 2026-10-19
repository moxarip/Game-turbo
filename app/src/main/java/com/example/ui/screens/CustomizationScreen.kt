package com.example.ui.screens

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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HudPreset
import com.example.service.FloatingHudContent
import com.example.ui.MainViewModel
import com.example.ui.components.CyberCard
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.HudAccentNames
import com.example.ui.theme.HudAccentOptions
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CustomizationScreen(viewModel: MainViewModel) {
    val config by viewModel.configState.collectAsState()
    val telemetry by viewModel.liveTelemetry.collectAsState()
    val currentAccent = HudAccentOptions.getOrElse(config.accentColorIndex) { CyberGreen }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Live Floating HUD Preview Card
            CyberCard(borderColor = currentAccent, glowGradient = true) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "LIVE HUD PREVIEW",
                        color = currentAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Embedded Floating HUD preview
                    FloatingHudContent(
                        telemetry = telemetry,
                        config = config,
                        onDragDelta = { _, _ -> },
                        onToggleCollapse = { },
                        onClose = { }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Scale: ${config.scalePercent}% • Opacity: ${config.alphaPercent}%",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Layout Preset Selector
        item {
            CyberCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "DISPLAY MODE PRESET",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    HudPreset.values().forEach { preset ->
                        val isSelected = config.preset == preset
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) currentAccent.copy(alpha = 0.2f) else DarkSurfaceVariant)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) currentAccent else DarkBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.setPreset(preset) }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = preset.label,
                                        color = if (isSelected) Color.White else TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = preset.description,
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(currentAccent)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Theme Accent Color Picker
        item {
            CyberCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "NEON ACCENT COLOR",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        HudAccentOptions.forEachIndexed { index, color ->
                            val isSelected = config.accentColorIndex == index
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { viewModel.setAccentColor(index) }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (isSelected) 2.5.dp else 0.dp,
                                            color = Color.White,
                                            shape = CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = HudAccentNames[index].split(" ").first(),
                                    color = if (isSelected) Color.White else TextSecondary,
                                    fontSize = 9.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        // Size & Transparency Sliders
        item {
            CyberCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "SIZE & TRANSPARENCY",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Scale Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("HUD Scale", color = TextPrimary, fontSize = 12.sp)
                        Text("${config.scalePercent}%", color = currentAccent, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                    Slider(
                        value = config.scalePercent.toFloat(),
                        onValueChange = { viewModel.setScale(it.toInt()) },
                        valueRange = 70f..130f,
                        steps = 5,
                        colors = SliderDefaults.colors(
                            thumbColor = currentAccent,
                            activeTrackColor = currentAccent,
                            inactiveTrackColor = DarkBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Opacity Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Background Opacity", color = TextPrimary, fontSize = 12.sp)
                        Text("${config.alphaPercent}%", color = currentAccent, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                    Slider(
                        value = config.alphaPercent.toFloat(),
                        onValueChange = { viewModel.setAlpha(it.toInt()) },
                        valueRange = 30f..100f,
                        steps = 6,
                        colors = SliderDefaults.colors(
                            thumbColor = currentAccent,
                            activeTrackColor = currentAccent,
                            inactiveTrackColor = DarkBorder
                        )
                    )
                }
            }
        }

        // Telemetry Update Interval
        item {
            CyberCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "TELEMETRY REFRESH INTERVAL",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val intervals = listOf(
                        500L to "500ms (High Performance)",
                        1000L to "1000ms (Balanced - Recommended)",
                        2000L to "2000ms (Low Power Mode)"
                    )

                    intervals.forEach { (ms, label) ->
                        val isSelected = config.updateIntervalMs == ms
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) currentAccent.copy(alpha = 0.2f) else DarkSurfaceVariant)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) currentAccent else DarkBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.setUpdateInterval(ms) }
                                .padding(12.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // Indicator Toggles
        item {
            CyberCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "INDIVIDUAL METRIC TOGGLES",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    ToggleItem("Show FPS / Display Hz", config.showFps) { viewModel.toggleShowFps(it) }
                    ToggleItem("Show Battery %", config.showBatteryPct) { viewModel.toggleShowBatteryPct(it) }
                    ToggleItem("Show Battery Temperature", config.showBatteryTemp) { viewModel.toggleShowBatteryTemp(it) }
                    ToggleItem("Show System Thermal Status", config.showThermalStatus) { viewModel.toggleShowThermalStatus(it) }
                    ToggleItem("Show CPU Temperature (if accessible)", config.showCpuTemp) { viewModel.toggleShowCpuTemp(it) }
                    ToggleItem("Show CPU Usage (if accessible)", config.showCpuUsage) { viewModel.toggleShowCpuUsage(it) }
                    ToggleItem("Show RAM Usage", config.showRam) { viewModel.toggleShowRam(it) }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ToggleItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextPrimary, fontSize = 12.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = CyberGreen,
                uncheckedThumbColor = Color.LightGray,
                uncheckedTrackColor = DarkBorder
            )
        )
    }
}
