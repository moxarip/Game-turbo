package com.example.telemetry

import android.content.Context
import com.example.model.TelemetryData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TelemetryCollector(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val batteryMonitor = BatteryMonitor(context)
    private val thermalMonitor = ThermalMonitor(context)
    private val memoryMonitor = MemoryMonitor(context)
    val fpsManager = FpsManager(context)

    private val _telemetryFlow = MutableStateFlow(TelemetryData())
    val telemetryFlow: StateFlow<TelemetryData> = _telemetryFlow.asStateFlow()

    private var pollingJob: Job? = null
    private var updateIntervalMs: Long = 1000L
    private var preferElevatedFps: Boolean = true

    fun setInterval(intervalMs: Long) {
        updateIntervalMs = intervalMs.coerceIn(250L, 5000L)
    }

    fun setPreferElevatedFps(prefer: Boolean) {
        preferElevatedFps = prefer
    }

    fun start() {
        if (pollingJob?.isActive == true) return

        fpsManager.start()

        pollingJob = scope.launch(Dispatchers.Default) {
            while (isActive) {
                val batterySample = batteryMonitor.readCurrentSample()
                val thermalSample = thermalMonitor.readCurrentSample()
                val memorySample = memoryMonitor.readCurrentSample()
                val fpsSnapshot = fpsManager.getCurrentSnapshot(preferElevatedFps)

                val telemetry = TelemetryData(
                    fps = fpsSnapshot.fps,
                    displayHz = fpsSnapshot.displayHz,
                    fpsSource = fpsSnapshot.source,
                    batteryPercent = batterySample.percentage,
                    batteryTempC = batterySample.temperatureC,
                    batteryVoltageMv = batterySample.voltageMv,
                    batteryCurrentMa = batterySample.currentMa,
                    isCharging = batterySample.isCharging,
                    thermalStatus = thermalSample.statusLabel,
                    thermalStatusCode = thermalSample.statusCode,
                    cpuTempC = thermalSample.cpuTempC,
                    cpuUsagePercent = null, // Honestly reported as null when SELinux prevents reading
                    ramUsedMb = memorySample.usedMb,
                    ramTotalMb = memorySample.totalMb,
                    ramAvailableMb = memorySample.availableMb,
                    ramUsagePercent = memorySample.usagePercent,
                    timestamp = System.currentTimeMillis()
                )

                _telemetryFlow.value = telemetry
                delay(updateIntervalMs)
            }
        }
    }

    fun stop() {
        pollingJob?.cancel()
        pollingJob = null
        fpsManager.stop()
    }

    fun getLatestSnapshot(): TelemetryData = _telemetryFlow.value
}
