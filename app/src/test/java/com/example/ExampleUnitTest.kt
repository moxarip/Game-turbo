package com.example

import com.example.model.FpsSource
import com.example.model.HudConfig
import com.example.model.HudPreset
import com.example.model.TelemetryData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun telemetryData_honesty_defaults() {
        val telemetry = TelemetryData()
        assertNull(telemetry.fps)
        assertEquals(60f, telemetry.displayHz, 0.01f)
        assertEquals(FpsSource.DISPLAY_CHOREOGRAPHER, telemetry.fpsSource)
        assertFalse(telemetry.isElevatedFpsActive)
        assertFalse(telemetry.isCpuTempAvailable)
        assertFalse(telemetry.isCpuUsageAvailable)
    }

    @Test
    fun telemetryData_elevatedFps_flag() {
        val standard = TelemetryData(
            fps = 120f,
            fpsSource = FpsSource.DISPLAY_CHOREOGRAPHER
        )
        assertFalse(standard.isElevatedFpsActive)

        val elevated = TelemetryData(
            fps = 58.5f,
            fpsSource = FpsSource.SHIZUKU_ELEVATED
        )
        assertTrue(elevated.isElevatedFpsActive)
    }

    @Test
    fun hudConfig_presets_behavior() {
        val miniConfig = HudConfig(preset = HudPreset.MINI)
        assertFalse(miniConfig.effectiveShowBatteryTemp())
        assertFalse(miniConfig.effectiveShowCpuTemp())
        assertFalse(miniConfig.effectiveShowRam())

        val gamingConfig = HudConfig(preset = HudPreset.GAMING)
        assertTrue(gamingConfig.effectiveShowBatteryTemp())
        assertTrue(gamingConfig.effectiveShowThermalStatus())

        val advancedConfig = HudConfig(preset = HudPreset.ADVANCED)
        assertTrue(advancedConfig.effectiveShowBatteryTemp())
        assertTrue(advancedConfig.effectiveShowCpuTemp())
        assertTrue(advancedConfig.effectiveShowRam())
    }

    @Test
    fun hudConfig_scaleAndAlphaLimits() {
        val config = HudConfig(scalePercent = 100, alphaPercent = 85)
        assertEquals(100, config.scalePercent)
        assertEquals(85, config.alphaPercent)
    }
}
