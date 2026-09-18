package com.elfrikiamv.minegocio_puntodeventa.data.repository

// SupplierRepository.kt

import com.elfrikiamv.minegocio_puntodeventa.data.dao.SupplierDao
import com.elfrikiamv.minegocio_puntodeventa.data.entity.SupplierEntity
import kotlinx.coroutines.flow.Flow

/**
 * مخزن تامین‌کنندگان.
 */
class SupplierRepository(private val dao: SupplierDao) {

    fun getAll(): Flow<List<SupplierEntity>> = dao.getAll()

    fun search(query: String): Flow<List<SupplierEntity>> = dao.search(query)

    suspend fun getById(id: Long): SupplierEntity? = dao.getById(id)

    suspend fun findByPhone(phone: String): SupplierEntity? = dao.findByPhone(phone)

    suspend fun insert(supplier: SupplierEntity): Long = dao.insert(supplier)

    suspend fun insertAll(suppliers: List<SupplierEntity>): List<Long> = dao.insertAll(suppliers)

    suspend fun update(supplier: SupplierEntity) = dao.update(supplier)

    suspend fun delete(supplier: SupplierEntity) = dao.delete(supplier)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun deleteAll() = dao.deleteAll()
}
