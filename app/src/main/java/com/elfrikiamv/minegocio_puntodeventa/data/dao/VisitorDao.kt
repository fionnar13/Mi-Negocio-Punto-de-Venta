package com.elfrikiamv.minegocio_puntodeventa.data.dao

// VisitorDao.kt

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.elfrikiamv.minegocio_puntodeventa.data.entity.VisitorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(visitor: VisitorEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(visitors: List<VisitorEntity>): List<Long>

    @Update
    suspend fun update(visitor: VisitorEntity)

    @Delete
    suspend fun delete(visitor: VisitorEntity)

    @Query("DELETE FROM visitors WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM visitors")
    suspend fun deleteAll()

    @Query("SELECT * FROM visitors WHERE id = :id")
    suspend fun getById(id: Long): VisitorEntity?

    @Query("SELECT * FROM visitors ORDER BY name")
    fun getAll(): Flow<List<VisitorEntity>>

    @Query("SELECT * FROM visitors WHERE type = :type ORDER BY name")
    fun getByType(type: String): Flow<List<VisitorEntity>>

    /** فهرست کامل ویزیتورها (یک‌بار، برای گزارش‌ها). */
    @Query("SELECT * FROM visitors")
    suspend fun getAllOnce(): List<VisitorEntity>
}
