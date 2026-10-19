package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.model.HudConfig
import com.example.model.HudPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HudPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("gamecore_hud_prefs", Context.MODE_PRIVATE)

    private val _configFlow = MutableStateFlow(loadConfig())
    val configFlow: StateFlow<HudConfig> = _configFlow.asStateFlow()

    fun getConfig(): HudConfig = _configFlow.value

    private fun loadConfig(): HudConfig {
        val presetStr = prefs.getString("preset", HudPreset.GAMING.name) ?: HudPreset.GAMING.name
        val preset = try {
            HudPreset.valueOf(presetStr)
        } catch (e: Exception) {
            HudPreset.GAMING
        }

        return HudConfig(
            preset = preset,
            scalePercent = prefs.getInt("scale_percent", 100),
            alphaPercent = prefs.getInt("alpha_percent", 85),
            accentColorIndex = prefs.getInt("accent_color_index", 0),
            updateIntervalMs = prefs.getLong("update_interval_ms", 1000L),
            showFps = prefs.getBoolean("show_fps", true),
            showBatteryPct = prefs.getBoolean("show_bat_pct", true),
            showBatteryTemp = prefs.getBoolean("show_bat_temp", true),
            showThermalStatus = prefs.getBoolean("show_thermal", true),
            showCpuTemp = prefs.getBoolean("show_cpu_temp", false),
            showCpuUsage = prefs.getBoolean("show_cpu_usage", false),
            showRam = prefs.getBoolean("show_ram", true),
            isCollapsed = prefs.getBoolean("is_collapsed", false),
            posX = prefs.getInt("pos_x", 50),
            posY = prefs.getInt("pos_y", 150),
            autoStartOnGames = prefs.getBoolean("auto_start", false),
            selectedGamePackages = prefs.getStringSet("selected_games", emptySet()) ?: emptySet()
        )
    }

    fun updateConfig(config: HudConfig) {
        prefs.edit()
            .putString("preset", config.preset.name)
            .putInt("scale_percent", config.scalePercent)
            .putInt("alpha_percent", config.alphaPercent)
            .putInt("accent_color_index", config.accentColorIndex)
            .putLong("update_interval_ms", config.updateIntervalMs)
            .putBoolean("show_fps", config.showFps)
            .putBoolean("show_bat_pct", config.showBatteryPct)
            .putBoolean("show_bat_temp", config.showBatteryTemp)
            .putBoolean("show_thermal", config.showThermalStatus)
            .putBoolean("show_cpu_temp", config.showCpuTemp)
            .putBoolean("show_cpu_usage", config.showCpuUsage)
            .putBoolean("show_ram", config.showRam)
            .putBoolean("is_collapsed", config.isCollapsed)
            .putInt("pos_x", config.posX)
            .putInt("pos_y", config.posY)
            .putBoolean("auto_start", config.autoStartOnGames)
            .putStringSet("selected_games", config.selectedGamePackages)
            .apply()
        _configFlow.value = config
    }

    fun updatePosition(x: Int, y: Int) {
        prefs.edit()
            .putInt("pos_x", x)
            .putInt("pos_y", y)
            .apply()
        _configFlow.value = _configFlow.value.copy(posX = x, posY = y)
    }

    fun toggleCollapsed() {
        val current = _configFlow.value.isCollapsed
        val next = !current
        prefs.edit().putBoolean("is_collapsed", next).apply()
        _configFlow.value = _configFlow.value.copy(isCollapsed = next)
    }
}
