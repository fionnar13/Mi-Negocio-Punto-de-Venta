package com.elfrikiamv.minegocio_puntodeventa.data.dao

// SupplierDao.kt

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.elfrikiamv.minegocio_puntodeventa.data.entity.SupplierEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(supplier: SupplierEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(suppliers: List<SupplierEntity>): List<Long>

    @Update
    suspend fun update(supplier: SupplierEntity)

    @Delete
    suspend fun delete(supplier: SupplierEntity)

    @Query("DELETE FROM suppliers WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM suppliers")
    suspend fun deleteAll()

    @Query("SELECT * FROM suppliers WHERE id = :id")
    suspend fun getById(id: Long): SupplierEntity?

    @Query("SELECT * FROM suppliers ORDER BY name")
    fun getAll(): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM suppliers WHERE phone = :phone LIMIT 1")
    suspend fun findByPhone(phone: String): SupplierEntity?

    @Query(
        "SELECT * FROM suppliers WHERE name LIKE '%' || :query || '%' " +
            "OR phone LIKE '%' || :query || '%' OR card LIKE '%' || :query || '%' " +
            "ORDER BY name"
    )
    fun search(query: String): Flow<List<SupplierEntity>>
}
