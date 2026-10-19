package com.example.telemetry

import android.content.Context
import android.os.Build
import android.os.PowerManager
import java.io.File

data class ThermalSample(
    val statusLabel: String,
    val statusCode: Int,
    val cpuTempC: Float?,
    val cpuSensorName: String?,
    val isCpuTempAccessible: Boolean,
    val cpuAccessReason: String
)

class ThermalMonitor(private val context: Context) {

    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    private var currentThermalStatus: Int = 0
    private var cpuThermalZonePath: String? = null
    private var detectedCpuSensorType: String? = null
    private var hasSearchedZones = false

    init {
        registerThermalListener()
        detectCpuThermalZone()
    }

    private fun registerThermalListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && powerManager != null) {
            try {
                currentThermalStatus = powerManager.currentThermalStatus
                powerManager.addThermalStatusListener { status ->
                    currentThermalStatus = status
                }
            } catch (e: Exception) {
                // Fallback if permission or service error occurs
                currentThermalStatus = 0
            }
        }
    }

    private fun detectCpuThermalZone() {
        if (hasSearchedZones) return
        hasSearchedZones = true

        try {
            val thermalDir = File("/sys/class/thermal")
            if (thermalDir.exists() && thermalDir.canRead()) {
                val zones = thermalDir.listFiles { f -> f.name.startsWith("thermal_zone") } ?: emptyArray()
                
                // Prioritize known Qualcomm Snapdragon 888 and ARM Big/LITTLE CPU sensor names
                val priorityKeywords = listOf("cpu", "soc-max", "tsens_tz_sensor", "gold", "prime", "silver")
                
                for (keyword in priorityKeywords) {
                    for (zone in zones) {
                        try {
                            val typeFile = File(zone, "type")
                            if (typeFile.exists() && typeFile.canRead()) {
                                val type = typeFile.readText().trim().lowercase()
                                if (type.contains(keyword)) {
                                    val tempFile = File(zone, "temp")
                                    if (tempFile.exists() && tempFile.canRead()) {
                                        cpuThermalZonePath = tempFile.absolutePath
                                        detectedCpuSensorType = type
                                        return
                                    }
                                }
                            }
                        } catch (_: Exception) {
                            // SELinux or permission check
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Unprivileged access to /sys/class/thermal may be blocked by SELinux on Android 14
        }
    }

    fun readCurrentSample(): ThermalSample {
        val statusLabel = when (currentThermalStatus) {
            0 -> "NORMAL"
            1 -> "LIGHT"
            2 -> "MODERATE"
            3 -> "SEVERE THROTTLE"
            4 -> "CRITICAL THROTTLE"
            5 -> "EMERGENCY"
            6 -> "SHUTDOWN"
            else -> "NORMAL"
        }

        var cpuTemp: Float? = null
        var reason = "Accessible"

        if (cpuThermalZonePath != null) {
            try {
                val tempFile = File(cpuThermalZonePath!!)
                if (tempFile.canRead()) {
                    val rawStr = tempFile.readText().trim()
                    val rawVal = rawStr.toFloatOrNull()
                    if (rawVal != null) {
                        cpuTemp = if (rawVal > 1000f) rawVal / 1000f else rawVal
                    }
                }
            } catch (e: Exception) {
                cpuTemp = null
                reason = "SELinux read error: ${e.message}"
            }
        }

        if (cpuTemp == null) {
            reason = "CPU thermal sysfs is protected by Android 14 SELinux policy. Requires Shizuku or ADB shell."
        }

        return ThermalSample(
            statusLabel = statusLabel,
            statusCode = currentThermalStatus,
            cpuTempC = cpuTemp,
            cpuSensorName = detectedCpuSensorType,
            isCpuTempAccessible = cpuTemp != null,
            cpuAccessReason = reason
        )
    }
}
