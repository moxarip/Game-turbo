package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.GameCoreApp
import com.example.model.HudConfig
import com.example.model.HudPreset
import com.example.model.SessionEntity
import com.example.model.TelemetryData
import com.example.service.GameOverlayService
import com.example.telemetry.TelemetryCollector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class InstalledGame(
    val packageName: String,
    val appName: String,
    val isMonitored: Boolean
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = GameCoreApp.hudPreferences
    private val sessionRepo = GameCoreApp.sessionRepository
    private val telemetryCollector = TelemetryCollector(application, viewModelScope)

    val configState: StateFlow<HudConfig> = preferences.configFlow

    val liveTelemetry: StateFlow<TelemetryData> = telemetryCollector.telemetryFlow

    val isHudRunning: StateFlow<Boolean> = GameOverlayService.isServiceRunning

    val allSessions: StateFlow<List<SessionEntity>> = sessionRepo.allSessions
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _installedGames = MutableStateFlow<List<InstalledGame>>(emptyList())
    val installedGames: StateFlow<List<InstalledGame>> = _installedGames.asStateFlow()

    private val _fpsHistory = MutableStateFlow<List<Float>>(emptyList())
    val fpsHistory: StateFlow<List<Float>> = _fpsHistory.asStateFlow()

    private val _tempHistory = MutableStateFlow<List<Float>>(emptyList())
    val tempHistory: StateFlow<List<Float>> = _tempHistory.asStateFlow()

    private val _hasOverlayPermission = MutableStateFlow(false)
    val hasOverlayPermission: StateFlow<Boolean> = _hasOverlayPermission.asStateFlow()

    private val _isShizukuInstalled = MutableStateFlow(false)
    val isShizukuInstalled: StateFlow<Boolean> = _isShizukuInstalled.asStateFlow()

    init {
        checkPermissions()
        loadInstalledGames()
        telemetryCollector.start()

        // Track live history buffer for in-app real-time graph
        viewModelScope.launch {
            liveTelemetry.collect { telemetry ->
                val fpsVal = telemetry.fps ?: telemetry.displayHz
                _fpsHistory.value = (_fpsHistory.value + fpsVal).takeLast(30)
                _tempHistory.value = (_tempHistory.value + telemetry.batteryTempC).takeLast(30)
            }
        }
    }

    fun checkPermissions() {
        val context = getApplication<Application>()
        _hasOverlayPermission.value = Settings.canDrawOverlays(context)

        val pm = context.packageManager
        _isShizukuInstalled.value = try {
            pm.getPackageInfo("moe.shizuku.privileged.api", 0) != null
        } catch (_: Exception) {
            false
        }
    }

    fun toggleHud() {
        val context = getApplication<Application>()
        if (isHudRunning.value) {
            GameOverlayService.stop(context)
        } else {
            if (Settings.canDrawOverlays(context)) {
                GameOverlayService.start(context)
            }
        }
    }

    fun setPreset(preset: HudPreset) {
        val current = configState.value
        preferences.updateConfig(current.copy(preset = preset))
    }

    fun setScale(scalePercent: Int) {
        val current = configState.value
        preferences.updateConfig(current.copy(scalePercent = scalePercent))
    }

    fun setAlpha(alphaPercent: Int) {
        val current = configState.value
        preferences.updateConfig(current.copy(alphaPercent = alphaPercent))
    }

    fun setAccentColor(colorIndex: Int) {
        val current = configState.value
        preferences.updateConfig(current.copy(accentColorIndex = colorIndex))
    }

    fun setUpdateInterval(intervalMs: Long) {
        val current = configState.value
        preferences.updateConfig(current.copy(updateIntervalMs = intervalMs))
        telemetryCollector.setInterval(intervalMs)
    }

    fun toggleShowFps(show: Boolean) {
        val current = configState.value
        preferences.updateConfig(current.copy(showFps = show))
    }

    fun toggleShowBatteryPct(show: Boolean) {
        val current = configState.value
        preferences.updateConfig(current.copy(showBatteryPct = show))
    }

    fun toggleShowBatteryTemp(show: Boolean) {
        val current = configState.value
        preferences.updateConfig(current.copy(showBatteryTemp = show))
    }

    fun toggleShowThermalStatus(show: Boolean) {
        val current = configState.value
        preferences.updateConfig(current.copy(showThermalStatus = show))
    }

    fun toggleShowCpuTemp(show: Boolean) {
        val current = configState.value
        preferences.updateConfig(current.copy(showCpuTemp = show))
    }

    fun toggleShowCpuUsage(show: Boolean) {
        val current = configState.value
        preferences.updateConfig(current.copy(showCpuUsage = show))
    }

    fun toggleShowRam(show: Boolean) {
        val current = configState.value
        preferences.updateConfig(current.copy(showRam = show))
    }

    fun toggleGameMonitored(packageName: String) {
        val currentSelected = configState.value.selectedGamePackages.toMutableSet()
        if (currentSelected.contains(packageName)) {
            currentSelected.remove(packageName)
        } else {
            currentSelected.add(packageName)
        }
        preferences.updateConfig(configState.value.copy(selectedGamePackages = currentSelected))
        loadInstalledGames()
    }

    fun clearAllSessions() {
        viewModelScope.launch {
            sessionRepo.clearAll()
        }
    }

    fun deleteSession(id: Long) {
        viewModelScope.launch {
            sessionRepo.deleteSession(id)
        }
    }

    private fun loadInstalledGames() {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolvedApps = pm.queryIntentActivities(mainIntent, 0)
            val selected = preferences.getConfig().selectedGamePackages

            val games = resolvedApps
                .filter { it.activityInfo.packageName != context.packageName }
                .map { resolveInfo ->
                    val pkg = resolveInfo.activityInfo.packageName
                    val name = resolveInfo.loadLabel(pm).toString()
                    InstalledGame(
                        packageName = pkg,
                        appName = name,
                        isMonitored = selected.contains(pkg)
                    )
                }
                .sortedByDescending { it.isMonitored }

            _installedGames.value = games
        }
    }

    fun requestOverlayPermission(context: Context) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    override fun onCleared() {
        super.onCleared()
        telemetryCollector.stop()
    }
}
