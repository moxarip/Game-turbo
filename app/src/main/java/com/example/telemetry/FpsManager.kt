package com.example.telemetry

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.Looper
import android.view.Choreographer
import android.view.Display
import com.example.model.FpsSource
import java.io.BufferedReader
import java.io.InputStreamReader

data class FpsSnapshot(
    val fps: Float?,
    val displayHz: Float,
    val source: FpsSource,
    val isShizukuAvailable: Boolean,
    val explanation: String
)

class FpsManager(private val context: Context) {

    private val choreographer = Choreographer.getInstance()
    private val mainHandler = Handler(Looper.getMainLooper())

    private var isTracking = false
    private var lastFrameTimeNanos: Long = 0L
    private var frameCount = 0
    private var lastFpsCalculationTime = 0L
    private var currentCalculatedFps: Float = 60.0f

    private var targetGamePackage: String? = null
    private var elevatedFpsActive = false
    private var isShizukuDetected = false

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!isTracking) return

            if (lastFrameTimeNanos != 0L) {
                val delta = frameTimeNanos - lastFrameTimeNanos
                if (delta > 0) {
                    frameCount++
                }
            }
            lastFrameTimeNanos = frameTimeNanos

            val now = System.currentTimeMillis()
            if (now - lastFpsCalculationTime >= 500) {
                val elapsedSeconds = (now - lastFpsCalculationTime) / 1000.0f
                if (elapsedSeconds > 0) {
                    currentCalculatedFps = (frameCount / elapsedSeconds).coerceIn(0f, 240f)
                }
                frameCount = 0
                lastFpsCalculationTime = now
            }

            choreographer.postFrameCallback(this)
        }
    }

    init {
        detectShizukuEnvironment()
    }

    private fun detectShizukuEnvironment() {
        try {
            // Check if Shizuku package is installed on device
            val pm = context.packageManager
            val shizukuInstalled = try {
                pm.getPackageInfo("moe.shizuku.privileged.api", 0) != null
            } catch (e: Exception) {
                false
            }
            isShizukuDetected = shizukuInstalled
        } catch (_: Exception) {
            isShizukuDetected = false
        }
    }

    fun start() {
        if (isTracking) return
        isTracking = true
        lastFrameTimeNanos = 0L
        frameCount = 0
        lastFpsCalculationTime = System.currentTimeMillis()
        mainHandler.post {
            choreographer.postFrameCallback(frameCallback)
        }
    }

    fun stop() {
        isTracking = false
        mainHandler.post {
            choreographer.removeFrameCallback(frameCallback)
        }
    }

    fun setTargetGame(packageName: String?) {
        targetGamePackage = packageName
    }

    fun getHardwareDisplayRefreshRate(): Float {
        return try {
            val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
            val display = displayManager?.getDisplay(Display.DEFAULT_DISPLAY)
            display?.mode?.refreshRate ?: 60.0f
        } catch (e: Exception) {
            60.0f
        }
    }

    fun getCurrentSnapshot(preferElevatedIfAvailable: Boolean): FpsSnapshot {
        val displayHz = getHardwareDisplayRefreshRate()

        // Verify if we can honestly query target app surface flinger via ADB / Shizuku
        if (preferElevatedIfAvailable && targetGamePackage != null) {
            val elevatedSample = tryReadElevatedGfxinfo(targetGamePackage!!)
            if (elevatedSample != null) {
                return FpsSnapshot(
                    fps = elevatedSample,
                    displayHz = displayHz,
                    source = FpsSource.SHIZUKU_ELEVATED,
                    isShizukuAvailable = true,
                    explanation = "Elevated SurfaceFlinger / dumpsys gfxinfo measurement active for $targetGamePackage."
                )
            }
        }

        // Honest Standard mode: we report the real hardware display refresh rate and HUD cadence
        return FpsSnapshot(
            fps = currentCalculatedFps,
            displayHz = displayHz,
            source = FpsSource.DISPLAY_CHOREOGRAPHER,
            isShizukuAvailable = isShizukuDetected,
            explanation = "Standard Android 14 mode: Display refresh rate & HUD cadence (${displayHz.toInt()} Hz). External game frame timestamps require Shizuku or ADB permission."
        )
    }

    /**
     * Checks if elevated command execution (e.g. adb shell / Shizuku binder) is available.
     * If shell execution is unprivileged (standard Android app sandbox), returns null.
     */
    private fun tryReadElevatedGfxinfo(pkg: String): Float? {
        // In a sandboxed standard Android environment, invoking `dumpsys gfxinfo` directly returns Permission Denied
        // unless granted via `adb shell pm grant ...` or Shizuku service.
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("dumpsys", "gfxinfo", pkg, "framestats"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            var frames = 0
            while (reader.readLine().also { line = it } != null) {
                if (line?.startsWith("Flags,IntendedVsync") == true) {
                    frames++
                }
            }
            process.waitFor()
            if (frames > 0) {
                frames.toFloat()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
