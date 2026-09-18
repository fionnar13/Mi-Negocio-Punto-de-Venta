package com.elfrikiamv.minegocio_puntodeventa.data.repository

// EventRepository.kt

import com.elfrikiamv.minegocio_puntodeventa.data.dao.EventDao
import com.elfrikiamv.minegocio_puntodeventa.data.entity.EventEntity
import kotlinx.coroutines.flow.Flow

/**
 * مخزن رویدادهای تقویم.
 */
class EventRepository(private val dao: EventDao) {

    fun getAll(): Flow<List<EventEntity>> = dao.getAll()

    fun getByDate(date: String): Flow<List<EventEntity>> = dao.getByDate(date)

    fun getByDateRange(from: String, to: String): Flow<List<EventEntity>> =
        dao.getByDateRange(from, to)

    suspend fun getById(id: Long): EventEntity? = dao.getById(id)

    suspend fun insert(event: EventEntity): Long = dao.insert(event)

    suspend fun insertAll(events: List<EventEntity>): List<Long> = dao.insertAll(events)

    suspend fun update(event: EventEntity) = dao.update(event)

    suspend fun delete(event: EventEntity) = dao.delete(event)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun deleteAll() = dao.deleteAll()
}
