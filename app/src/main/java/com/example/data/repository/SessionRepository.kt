package com.example.data.repository

import com.example.data.db.SessionDao
import com.example.model.SessionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class SessionRepository(private val sessionDao: SessionDao) {

    val allSessions: Flow<List<SessionEntity>> = sessionDao.getAllSessions()
        .flowOn(Dispatchers.IO)

    val sessionCount: Flow<Int> = sessionDao.getSessionCount()
        .flowOn(Dispatchers.IO)

    suspend fun getSessionById(id: Long): SessionEntity? = withContext(Dispatchers.IO) {
        sessionDao.getSessionById(id)
    }

    suspend fun saveSession(session: SessionEntity): Long = withContext(Dispatchers.IO) {
        sessionDao.insertSession(session)
    }

    suspend fun deleteSession(id: Long) = withContext(Dispatchers.IO) {
        sessionDao.deleteSessionById(id)
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        sessionDao.clearAllSessions()
    }
}
