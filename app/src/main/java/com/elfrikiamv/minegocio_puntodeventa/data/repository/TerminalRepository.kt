package com.elfrikiamv.minegocio_puntodeventa.data.repository

// TerminalRepository.kt

import com.elfrikiamv.minegocio_puntodeventa.data.dao.TerminalDao
import com.elfrikiamv.minegocio_puntodeventa.data.entity.TerminalEntity
import kotlinx.coroutines.flow.Flow

/**
 * مخزن پایانه‌ها (دستگاه‌های صندوق).
 */
class TerminalRepository(private val dao: TerminalDao) {

    fun getAll(): Flow<List<TerminalEntity>> = dao.getAll()

    suspend fun getById(id: Long): TerminalEntity? = dao.getById(id)

    suspend fun insert(terminal: TerminalEntity): Long = dao.insert(terminal)

    suspend fun insertAll(terminals: List<TerminalEntity>): List<Long> = dao.insertAll(terminals)

    suspend fun update(terminal: TerminalEntity) = dao.update(terminal)

    suspend fun delete(terminal: TerminalEntity) = dao.delete(terminal)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun deleteAll() = dao.deleteAll()
}
