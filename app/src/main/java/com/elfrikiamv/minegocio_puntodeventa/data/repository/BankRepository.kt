package com.elfrikiamv.minegocio_puntodeventa.data.repository

// BankRepository.kt

import com.elfrikiamv.minegocio_puntodeventa.data.dao.BankDao
import com.elfrikiamv.minegocio_puntodeventa.data.entity.BankEntity
import kotlinx.coroutines.flow.Flow

/**
 * مخزن بانک‌ها.
 */
class BankRepository(private val dao: BankDao) {

    fun getAll(): Flow<List<BankEntity>> = dao.getAll()

    suspend fun getById(id: Long): BankEntity? = dao.getById(id)

    suspend fun insert(bank: BankEntity): Long = dao.insert(bank)

    suspend fun insertAll(banks: List<BankEntity>): List<Long> = dao.insertAll(banks)

    suspend fun update(bank: BankEntity) = dao.update(bank)

    suspend fun delete(bank: BankEntity) = dao.delete(bank)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun deleteAll() = dao.deleteAll()
}
