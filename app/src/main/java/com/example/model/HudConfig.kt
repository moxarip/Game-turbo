package com.example.model

enum class HudPreset(val label: String, val description: String) {
    MINI("Mini", "FPS & Battery %"),
    GAMING("Gaming", "FPS, Battery %, Temp & Thermal"),
    ADVANCED("Advanced", "All metrics: CPU, RAM, Battery & FPS")
}

data class HudConfig(
    val preset: HudPreset = HudPreset.GAMING,
    val scalePercent: Int = 100, // 75%, 100%, 125%
    val alphaPercent: Int = 85,  // 30% .. 100%
    val accentColorIndex: Int = 0, // Index into HudAccentOptions (0: Cyber Green)
    val updateIntervalMs: Long = 1000L, // 500ms, 1000ms, 2000ms
    val showFps: Boolean = true,
    val showBatteryPct: Boolean = true,
    val showBatteryTemp: Boolean = true,
    val showThermalStatus: Boolean = true,
    val showCpuTemp: Boolean = false,
    val showCpuUsage: Boolean = false,
    val showRam: Boolean = true,
    val isCollapsed: Boolean = false,
    val posX: Int = 50,
    val posY: Int = 150,
    val autoStartOnGames: Boolean = false,
    val selectedGamePackages: Set<String> = emptySet(),
    val useElevatedFpsIfAvailable: Boolean = true
) {
    fun effectiveShowCpuTemp(): Boolean =
        if (preset == HudPreset.MINI) false else if (preset == HudPreset.ADVANCED) true else showCpuTemp

    fun effectiveShowCpuUsage(): Boolean =
        if (preset == HudPreset.MINI) false else if (preset == HudPreset.ADVANCED) true else showCpuUsage

    fun effectiveShowRam(): Boolean =
        if (preset == HudPreset.MINI) false else if (preset == HudPreset.ADVANCED) true else showRam

    fun effectiveShowBatteryTemp(): Boolean =
        if (preset == HudPreset.MINI) false else showBatteryTemp

    fun effectiveShowThermalStatus(): Boolean =
        if (preset == HudPreset.MINI) false else showThermalStatus
}
