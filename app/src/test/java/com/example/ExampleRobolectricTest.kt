package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.GameCoreDatabase
import com.example.data.db.SessionDao
import com.example.data.preferences.HudPreferences
import com.example.model.HudPreset
import com.example.model.SessionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var database: GameCoreDatabase
    private lateinit var sessionDao: SessionDao

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, GameCoreDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        sessionDao = database.sessionDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun readStringFromContext_matchesAppName() {
        val appName = context.getString(R.string.app_name)
        assertEquals("GameCore Pro HUD", appName)
    }

    @Test
    fun roomDatabase_insertAndRetrieveSession() = runBlocking {
        val session = SessionEntity(
            startTime = 1000L,
            endTime = 5000L,
            durationSeconds = 4L,
            gamePackageName = "com.miHoYo.GenshinImpact",
            gameTitle = "Genshin Impact",
            avgFps = 59.2f,
            minFps = 45.0f,
            maxFps = 60.0f,
            avgBatteryTempC = 37.5f,
            maxBatteryTempC = 41.2f,
            startBatteryPct = 90,
            endBatteryPct = 85,
            maxThermalStatus = "NORMAL",
            fpsSource = "Display Refresh Rate",
            sampleCount = 10,
            notes = "Test Session"
        )

        val id = sessionDao.insertSession(session)
        val retrieved = sessionDao.getSessionById(id)

        assertNotNull(retrieved)
        assertEquals("Genshin Impact", retrieved?.gameTitle)
        assertEquals(59.2f, retrieved?.avgFps ?: 0f, 0.01f)
        assertEquals(41.2f, retrieved?.maxBatteryTempC ?: 0f, 0.01f)

        val all = sessionDao.getAllSessions().first()
        assertEquals(1, all.size)

        sessionDao.deleteSessionById(id)
        val emptyList = sessionDao.getAllSessions().first()
        assertEquals(0, emptyList.size)
    }

    @Test
    fun hudPreferences_updateAndRetrievePosition() {
        val prefs = HudPreferences(context)
        prefs.updatePosition(120, 340)

        val config = prefs.getConfig()
        assertEquals(120, config.posX)
        assertEquals(340, config.posY)
    }
}
