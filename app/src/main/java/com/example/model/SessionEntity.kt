package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gaming_sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val startTime: Long,
    val endTime: Long,
    val durationSeconds: Long,
    val gamePackageName: String,
    val gameTitle: String,
    val avgFps: Float,
    val minFps: Float,
    val maxFps: Float,
    val avgBatteryTempC: Float,
    val maxBatteryTempC: Float,
    val startBatteryPct: Int,
    val endBatteryPct: Int,
    val maxThermalStatus: String,
    val fpsSource: String,
    val sampleCount: Int = 0,
    val notes: String = ""
)
