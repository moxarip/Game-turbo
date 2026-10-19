package com.example.telemetry

import android.app.ActivityManager
import android.content.Context

data class MemorySample(
    val usedMb: Long,
    val totalMb: Long,
    val availableMb: Long,
    val usagePercent: Int,
    val isLowMemory: Boolean
)

class MemoryMonitor(private val context: Context) {

    private val activityManager =
        context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager

    fun readCurrentSample(): MemorySample {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memoryInfo)

        val totalBytes = memoryInfo.totalMem
        val availBytes = memoryInfo.availMem
        val usedBytes = (totalBytes - availBytes).coerceAtLeast(0)

        val totalMb = totalBytes / (1024 * 1024)
        val availMb = availBytes / (1024 * 1024)
        val usedMb = usedBytes / (1024 * 1024)

        val percent = if (totalBytes > 0) {
            ((usedBytes.toDouble() / totalBytes.toDouble()) * 100).toInt().coerceIn(0, 100)
        } else {
            0
        }

        return MemorySample(
            usedMb = usedMb,
            totalMb = totalMb,
            availableMb = availMb,
            usagePercent = percent,
            isLowMemory = memoryInfo.lowMemory
        )
    }
}
