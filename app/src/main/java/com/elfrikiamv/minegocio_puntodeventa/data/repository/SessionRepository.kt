package com.elfrikiamv.minegocio_puntodeventa.data.repository

// SessionRepository.kt

import com.elfrikiamv.minegocio_puntodeventa.data.dao.SessionDao
import com.elfrikiamv.minegocio_puntodeventa.data.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * مخزن نشست‌های کاری (شیفت صندوق).
 */
class SessionRepository(private val dao: SessionDao) {

    fun getAll(): Flow<List<SessionEntity>> = dao.getAll()

    fun getByUserId(userId: Long): Flow<List<SessionEntity>> = dao.getByUserId(userId)

    suspend fun getById(id: Long): SessionEntity? = dao.getById(id)

    /** آخرین نشست (نشست جاری). */
    suspend fun getLatest(): SessionEntity? = dao.getLatest()

    suspend fun insert(session: SessionEntity): Long = dao.insert(session)

    suspend fun insertAll(sessions: List<SessionEntity>): List<Long> = dao.insertAll(sessions)

    suspend fun update(session: SessionEntity) = dao.update(session)

    suspend fun delete(session: SessionEntity) = dao.delete(session)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun deleteAll() = dao.deleteAll()

    /** فهرست کامل نشست‌ها (یک‌بار، برای گزارش‌ها). */
    suspend fun getAllOnce(): List<com.elfrikiamv.minegocio_puntodeventa.data.entity.SessionEntity> =
        dao.getAllOnce()

    /** بستن شیفت جاری (اگر باز باشد). */
    suspend fun closeSession() {
        dao.getLatest()?.let { s ->
            if (s.endTime == null) dao.close(s.id, System.currentTimeMillis())
        }
    }
}
