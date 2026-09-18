package com.elfrikiamv.minegocio_puntodeventa.data.repository

// CustomerRepository.kt

import com.elfrikiamv.minegocio_puntodeventa.data.dao.CustomerDao
import com.elfrikiamv.minegocio_puntodeventa.data.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

/**
 * مخزن مشتریان.
 */
class CustomerRepository(private val dao: CustomerDao) {

    fun getAll(): Flow<List<CustomerEntity>> = dao.getAll()

    fun search(query: String): Flow<List<CustomerEntity>> = dao.search(query)

    suspend fun getById(id: Long): CustomerEntity? = dao.getById(id)

    suspend fun findByPhone(phone: String): CustomerEntity? = dao.findByPhone(phone)

    suspend fun insert(customer: CustomerEntity): Long = dao.insert(customer)

    suspend fun insertAll(customers: List<CustomerEntity>): List<Long> = dao.insertAll(customers)

    suspend fun update(customer: CustomerEntity) = dao.update(customer)

    suspend fun delete(customer: CustomerEntity) = dao.delete(customer)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun deleteAll() = dao.deleteAll()

    /** فهرست کامل مشتریان (یک‌بار، برای گزارش‌ها). */
    suspend fun getAllOnce(): List<com.elfrikiamv.minegocio_puntodeventa.data.entity.CustomerEntity> =
        dao.getAllOnce()
}
