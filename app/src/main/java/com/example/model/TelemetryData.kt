package com.example.model

enum class FpsSource(val displayName: String, val isElevated: Boolean) {
    DISPLAY_CHOREOGRAPHER("Display Refresh Rate", false),
    SHIZUKU_ELEVATED("Elevated SurfaceFlinger (Shizuku)", true),
    ADB_SHELL("Elevated dumpsys (ADB)", true),
    UNAVAILABLE("Not Measured", false)
}

data class TelemetryData(
    val fps: Float? = null,
    val displayHz: Float = 60f,
    val fpsSource: FpsSource = FpsSource.DISPLAY_CHOREOGRAPHER,
    val batteryPercent: Int = 100,
    val batteryTempC: Float = 30.0f,
    val batteryVoltageMv: Int = 4000,
    val batteryCurrentMa: Int? = null,
    val isCharging: Boolean = false,
    val thermalStatus: String = "NORMAL",
    val thermalStatusCode: Int = 0,
    val cpuTempC: Float? = null,
    val cpuUsagePercent: Float? = null,
    val ramUsedMb: Long = 0L,
    val ramTotalMb: Long = 0L,
    val ramAvailableMb: Long = 0L,
    val ramUsagePercent: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
) {
    val isCpuTempAvailable: Boolean
        get() = cpuTempC != null

    val isCpuUsageAvailable: Boolean
        get() = cpuUsagePercent != null

    val isElevatedFpsActive: Boolean
        get() = fpsSource.isElevated && fps != null
}
