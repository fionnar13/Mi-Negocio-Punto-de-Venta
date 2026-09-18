package com.elfrikiamv.minegocio_puntodeventa.data.repository

// UserRepository.kt

import com.elfrikiamv.minegocio_puntodeventa.data.dao.UserDao
import com.elfrikiamv.minegocio_puntodeventa.data.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * مخزن کاربران.
 */
class UserRepository(private val dao: UserDao) {

    fun getAll(): Flow<List<UserEntity>> = dao.getAll()

    suspend fun getById(id: Long): UserEntity? = dao.getById(id)

    suspend fun insert(user: UserEntity): Long = dao.insert(user)

    suspend fun insertAll(users: List<UserEntity>): List<Long> = dao.insertAll(users)

    suspend fun update(user: UserEntity) = dao.update(user)

    suspend fun delete(user: UserEntity) = dao.delete(user)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun deleteAll() = dao.deleteAll()

    /** فهرست کامل کاربران (یک‌بار، برای گزارش‌ها). */
    suspend fun getAllOnce(): List<com.elfrikiamv.minegocio_puntodeventa.data.entity.UserEntity> =
        dao.getAllOnce()
}
