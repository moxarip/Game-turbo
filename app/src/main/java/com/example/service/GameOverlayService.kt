package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.GameCoreApp
import com.example.MainActivity
import com.example.R
import com.example.data.preferences.HudPreferences
import com.example.model.SessionEntity
import com.example.model.TelemetryData
import com.example.telemetry.TelemetryCollector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class GameOverlayService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var windowManager: WindowManager
    private lateinit var hudPreferences: HudPreferences
    private lateinit var telemetryCollector: TelemetryCollector

    private var composeView: ComposeView? = null
    private var windowLayoutParams: WindowManager.LayoutParams? = null
    private val lifecycleOwner = OverlayLifecycleOwner()

    // Session analytics tracking
    private var sessionStartTime: Long = 0L
    private var sessionStartBatteryPct: Int = 100
    private val recordedFpsSamples = mutableListOf<Float>()
    private val recordedTempSamples = mutableListOf<Float>()
    private var worstThermalStatus = "NORMAL"

    companion object {
        const val ACTION_START_HUD = "com.example.action.START_HUD"
        const val ACTION_STOP_HUD = "com.example.action.STOP_HUD"
        const val ACTION_TOGGLE_COLLAPSE = "com.example.action.TOGGLE_COLLAPSE"

        private const val NOTIFICATION_CHANNEL_ID = "gamecore_hud_channel"
        private const val NOTIFICATION_ID = 1001

        val isServiceRunning = MutableStateFlow(false)

        fun start(context: Context) {
            val intent = Intent(context, GameOverlayService::class.java).apply {
                action = ACTION_START_HUD
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, GameOverlayService::class.java).apply {
                action = ACTION_STOP_HUD
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        hudPreferences = HudPreferences(this)
        telemetryCollector = TelemetryCollector(this, serviceScope)

        createNotificationChannel()
        lifecycleOwner.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_HUD -> {
                stopHud()
                return START_NOT_STICKY
            }
            ACTION_TOGGLE_COLLAPSE -> {
                hudPreferences.toggleCollapsed()
                return START_STICKY
            }
            ACTION_START_HUD, null -> {
                if (!Settings.canDrawOverlays(this)) {
                    stopSelf()
                    return START_NOT_STICKY
                }
                startForegroundNotification()
                showFloatingHud()
                startSessionRecording()
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun startForegroundNotification() {
        val stopIntent = Intent(this, GameOverlayService::class.java).apply {
            action = ACTION_STOP_HUD
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openAppIntent = Intent(this, MainActivity::class.java)
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_content))
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                getString(R.string.notification_action_stop),
                stopPendingIntent
            )
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        isServiceRunning.value = true
    }

    private fun showFloatingHud() {
        if (composeView != null) return

        val config = hudPreferences.getConfig()
        telemetryCollector.setInterval(config.updateIntervalMs)
        telemetryCollector.setPreferElevatedFps(config.useElevatedFpsIfAvailable)
        telemetryCollector.start()

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        windowLayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = config.posX
            y = config.posY
        }

        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeViewModelStoreOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)

            setContent {
                val currentConfig by hudPreferences.configFlow.collectAsState()
                val telemetry by telemetryCollector.telemetryFlow.collectAsState()

                // Track analytics while HUD is alive
                trackTelemetrySample(telemetry)

                FloatingHudContent(
                    telemetry = telemetry,
                    config = currentConfig,
                    onDragDelta = { dx, dy ->
                        windowLayoutParams?.let { params ->
                            params.x += dx.toInt()
                            params.y += dy.toInt()
                            windowManager.updateViewLayout(this@apply, params)
                            hudPreferences.updatePosition(params.x, params.y)
                        }
                    },
                    onToggleCollapse = {
                        hudPreferences.toggleCollapsed()
                    },
                    onClose = {
                        stopHud()
                    }
                )
            }
        }

        try {
            windowManager.addView(composeView, windowLayoutParams)
        } catch (e: Exception) {
            stopSelf()
        }
    }

    private fun startSessionRecording() {
        sessionStartTime = System.currentTimeMillis()
        val initialTelemetry = telemetryCollector.getLatestSnapshot()
        sessionStartBatteryPct = initialTelemetry.batteryPercent
        recordedFpsSamples.clear()
        recordedTempSamples.clear()
        worstThermalStatus = "NORMAL"
    }

    private fun trackTelemetrySample(telemetry: TelemetryData) {
        val fpsVal = telemetry.fps ?: telemetry.displayHz
        if (fpsVal > 0) {
            recordedFpsSamples.add(fpsVal)
        }
        if (telemetry.batteryTempC > 0) {
            recordedTempSamples.add(telemetry.batteryTempC)
        }
        if (telemetry.thermalStatusCode > 0) {
            worstThermalStatus = telemetry.thermalStatus
        }
    }

    private fun stopHud() {
        finalizeSessionRecording()

        telemetryCollector.stop()

        if (composeView != null) {
            try {
                windowManager.removeView(composeView)
            } catch (_: Exception) {
            }
            composeView = null
        }

        isServiceRunning.value = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun finalizeSessionRecording() {
        val endTime = System.currentTimeMillis()
        val durationSeconds = ((endTime - sessionStartTime) / 1000L).coerceAtLeast(1L)
        val latestTelemetry = telemetryCollector.getLatestSnapshot()

        if (recordedFpsSamples.isNotEmpty() || recordedTempSamples.isNotEmpty()) {
            val avgFps = if (recordedFpsSamples.isNotEmpty()) {
                recordedFpsSamples.average().toFloat()
            } else {
                latestTelemetry.displayHz
            }
            val minFps = recordedFpsSamples.minOrNull() ?: avgFps
            val maxFps = recordedFpsSamples.maxOrNull() ?: avgFps

            val avgTemp = if (recordedTempSamples.isNotEmpty()) {
                recordedTempSamples.average().toFloat()
            } else {
                latestTelemetry.batteryTempC
            }
            val maxTemp = recordedTempSamples.maxOrNull() ?: avgTemp

            val session = SessionEntity(
                startTime = sessionStartTime,
                endTime = endTime,
                durationSeconds = durationSeconds,
                gamePackageName = "Active Session",
                gameTitle = "Gaming Session",
                avgFps = avgFps,
                minFps = minFps,
                maxFps = maxFps,
                avgBatteryTempC = avgTemp,
                maxBatteryTempC = maxTemp,
                startBatteryPct = sessionStartBatteryPct,
                endBatteryPct = latestTelemetry.batteryPercent,
                maxThermalStatus = worstThermalStatus,
                fpsSource = latestTelemetry.fpsSource.displayName,
                sampleCount = recordedFpsSamples.size,
                notes = "Auto-logged by GameCore Pro HUD"
            )

            serviceScope.launch(Dispatchers.IO) {
                try {
                    GameCoreApp.database.sessionDao().insertSession(session)
                } catch (_: Exception) {
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Keep HUD within screen bounds on orientation change (portrait <-> landscape)
        windowLayoutParams?.let { params ->
            params.x = params.x.coerceIn(0, 1000)
            params.y = params.y.coerceIn(0, 1500)
            composeView?.let { view ->
                try {
                    windowManager.updateViewLayout(view, params)
                } catch (_: Exception) {
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleOwner.onDestroy()
        serviceScope.cancel()
        isServiceRunning.value = false
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
