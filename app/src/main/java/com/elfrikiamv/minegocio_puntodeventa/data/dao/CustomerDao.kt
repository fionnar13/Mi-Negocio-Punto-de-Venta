package com.elfrikiamv.minegocio_puntodeventa.data.dao

// CustomerDao.kt

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.elfrikiamv.minegocio_puntodeventa.data.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customer: CustomerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(customers: List<CustomerEntity>): List<Long>

    @Update
    suspend fun update(customer: CustomerEntity)

    @Delete
    suspend fun delete(customer: CustomerEntity)

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM customers")
    suspend fun deleteAll()

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getById(id: Long): CustomerEntity?

    @Query("SELECT * FROM customers ORDER BY name")
    fun getAll(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE phone = :phone LIMIT 1")
    suspend fun findByPhone(phone: String): CustomerEntity?

    @Query(
        "SELECT * FROM customers WHERE name LIKE '%' || :query || '%' " +
            "OR phone LIKE '%' || :query || '%' ORDER BY name"
    )
    fun search(query: String): Flow<List<CustomerEntity>>

    /** فهرست کامل مشتریان (یک‌بار، برای گزارش‌ها). */
    @Query("SELECT * FROM customers")
    suspend fun getAllOnce(): List<CustomerEntity>
}
