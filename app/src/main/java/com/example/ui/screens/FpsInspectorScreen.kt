package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.CyberCard
import com.example.ui.components.FpsExplanationCard
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.io.File

@Composable
fun FpsInspectorScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val telemetry by viewModel.liveTelemetry.collectAsState()
    val isShizukuInstalled by viewModel.isShizukuInstalled.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Architectural Explanation Card
            FpsExplanationCard(
                currentSource = telemetry.fpsSource,
                isShizukuAvailable = isShizukuInstalled
            )
        }

        // Live Active Measurement Pipeline
        item {
            CyberCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "ACTIVE TELEMETRY PIPELINE",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    PipelineRow(
                        title = "Reported Source",
                        value = telemetry.fpsSource.displayName,
                        statusColor = if (telemetry.isElevatedFpsActive) NeonCyan else CyberGreen
                    )

                    PipelineRow(
                        title = "Hardware Display Hz",
                        value = "%.1f Hz (Snapdragon 888 / AMOLED)".format(telemetry.displayHz),
                        statusColor = CyberGreen
                    )

                    PipelineRow(
                        title = "Shizuku Service Status",
                        value = if (isShizukuInstalled) "Installed on Device" else "Not Detected",
                        statusColor = if (isShizukuInstalled) NeonCyan else Color(0xFF94A3B8)
                    )

                    PipelineRow(
                        title = "Battery Temp Telemetry",
                        value = "%.1f°C (Official Android BatteryManager)".format(telemetry.batteryTempC),
                        statusColor = CyberGreen
                    )

                    PipelineRow(
                        title = "CPU Temp Telemetry",
                        value = if (telemetry.cpuTempC != null) "%.1f°C (Kernel Sysfs)".format(telemetry.cpuTempC) else "Restricted (SELinux Sandbox)",
                        statusColor = if (telemetry.cpuTempC != null) CyberGreen else Color(0xFFFFB300)
                    )
                }
            }
        }

        // Elevated ADB Shell Setup Guide
        item {
            CyberCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "ADB Terminal Commands",
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "ELEVATED METRICS VIA ADB / SHIZUKU",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "For power users wanting direct SurfaceFlinger frame dumping without root, connect phone to PC or Wireless Debugging and run:",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    AdbCommandSnippet(
                        command = "adb shell pm grant com.aistudio.gamecoreprohud.hud android.permission.DUMP",
                        context = context
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    AdbCommandSnippet(
                        command = "adb shell pm grant com.aistudio.gamecoreprohud.hud android.permission.PACKAGE_USAGE_STATS",
                        context = context
                    )
                }
            }
        }

        // Snapdragon 888 Thermal Zone Explorer
        item {
            CyberCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeviceHub,
                            contentDescription = "Thermal Explorer",
                            tint = CyberGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "HARDWARE THERMAL ZONE AUDITOR",
                            color = CyberGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Real hardware thermal zone detection results on this device:",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val zoneReports = inspectThermalZones()
                    zoneReports.take(6).forEach { report ->
                        ThermalZoneRow(report)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PipelineRow(title: String, value: String, statusColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, color = TextSecondary, fontSize = 11.sp)
        Text(
            text = value,
            color = statusColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun AdbCommandSnippet(command: String, context: Context) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF070B14))
            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
            .clickable {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("ADB Command", command)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Command copied to clipboard", Toast.LENGTH_SHORT).show()
            }
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = command,
                color = Color(0xFF81D4FA),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy",
                tint = Color.LightGray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

data class ZoneReport(val zoneName: String, val type: String, val status: String, val isAccessible: Boolean)

private fun inspectThermalZones(): List<ZoneReport> {
    val reports = mutableListOf<ZoneReport>()
    try {
        val thermalDir = File("/sys/class/thermal")
        if (thermalDir.exists()) {
            val zones = thermalDir.listFiles { f -> f.name.startsWith("thermal_zone") } ?: emptyArray()
            zones.take(8).forEach { zone ->
                val typeFile = File(zone, "type")
                val tempFile = File(zone, "temp")
                val type = if (typeFile.canRead()) typeFile.readText().trim() else "Unknown"
                val temp = if (tempFile.canRead()) tempFile.readText().trim() else "SELinux Blocked"
                reports.add(
                    ZoneReport(
                        zoneName = zone.name,
                        type = type,
                        status = temp,
                        isAccessible = tempFile.canRead()
                    )
                )
            }
        }
    } catch (_: Exception) {
    }

    if (reports.isEmpty()) {
        reports.add(
            ZoneReport(
                zoneName = "thermal_zone0..N",
                type = "Qualcomm Snapdragon 888 TSENS",
                status = "Protected by Android 14 SELinux",
                isAccessible = false
            )
        )
    }

    return reports
}

@Composable
private fun ThermalZoneRow(report: ZoneReport) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(DarkSurfaceVariant)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = report.zoneName,
                color = TextPrimary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = report.type,
                color = TextSecondary,
                fontSize = 10.sp
            )
        }
        Text(
            text = report.status,
            color = if (report.isAccessible) CyberGreen else Color(0xFFFFB300),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
