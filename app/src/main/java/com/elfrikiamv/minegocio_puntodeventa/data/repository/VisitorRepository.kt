package com.elfrikiamv.minegocio_puntodeventa.data.repository

// VisitorRepository.kt

import com.elfrikiamv.minegocio_puntodeventa.data.dao.VisitorDao
import com.elfrikiamv.minegocio_puntodeventa.data.entity.VisitorEntity
import kotlinx.coroutines.flow.Flow

/**
 * مخزن ویزیتورها.
 */
class VisitorRepository(private val dao: VisitorDao) {

    fun getAll(): Flow<List<VisitorEntity>> = dao.getAll()

    fun getByType(type: String): Flow<List<VisitorEntity>> = dao.getByType(type)

    suspend fun getById(id: Long): VisitorEntity? = dao.getById(id)

    suspend fun insert(visitor: VisitorEntity): Long = dao.insert(visitor)

    suspend fun insertAll(visitors: List<VisitorEntity>): List<Long> = dao.insertAll(visitors)

    suspend fun update(visitor: VisitorEntity) = dao.update(visitor)

    suspend fun delete(visitor: VisitorEntity) = dao.delete(visitor)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun deleteAll() = dao.deleteAll()

    /** فهرست کامل ویزیتورها (یک‌بار، برای گزارش‌ها). */
    suspend fun getAllOnce(): List<com.elfrikiamv.minegocio_puntodeventa.data.entity.VisitorEntity> =
        dao.getAllOnce()
}
