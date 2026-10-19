package com.example.telemetry

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build

data class BatterySample(
    val percentage: Int,
    val temperatureC: Float,
    val voltageMv: Int,
    val currentMa: Int?,
    val isCharging: Boolean
)

class BatteryMonitor(private val context: Context) {

    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager

    fun readCurrentSample(): BatterySample {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val intent = context.registerReceiver(null, filter)

        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val rawTemp = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
        val voltage = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1

        val pct = if (level >= 0 && scale > 0) {
            ((level.toFloat() / scale.toFloat()) * 100).toInt().coerceIn(0, 100)
        } else {
            batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 0
        }

        // Battery temperature is reported in tenths of a degree Celsius (e.g., 345 = 34.5°C)
        val tempC = if (rawTemp > 0) {
            rawTemp / 10.0f
        } else {
            25.0f
        }

        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val currentNowUa = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        val currentMa = if (currentNowUa != null && currentNowUa != Int.MIN_VALUE && currentNowUa != 0) {
            // Some devices report in microamperes (uA), others in milliamperes (mA)
            if (kotlin.math.abs(currentNowUa) > 10000) {
                currentNowUa / 1000
            } else {
                currentNowUa
            }
        } else {
            null
        }

        return BatterySample(
            percentage = pct,
            temperatureC = tempC,
            voltageMv = if (voltage > 0) voltage else 0,
            currentMa = currentMa,
            isCharging = isCharging
        )
    }
}
