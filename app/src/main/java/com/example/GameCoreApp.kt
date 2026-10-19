package com.example

import android.app.Application
import com.example.data.db.GameCoreDatabase
import com.example.data.preferences.HudPreferences
import com.example.data.repository.SessionRepository

class GameCoreApp : Application() {

    companion object {
        lateinit var instance: GameCoreApp
            private set

        lateinit var database: GameCoreDatabase
            private set

        lateinit var sessionRepository: SessionRepository
            private set

        lateinit var hudPreferences: HudPreferences
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = GameCoreDatabase.getInstance(this)
        sessionRepository = SessionRepository(database.sessionDao())
        hudPreferences = HudPreferences(this)
    }
}
