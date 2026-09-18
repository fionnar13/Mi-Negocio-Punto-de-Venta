package com.elfrikiamv.minegocio_puntodeventa.data.dao

// EventDao.kt

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.elfrikiamv.minegocio_puntodeventa.data.entity.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: EventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<EventEntity>): List<Long>

    @Update
    suspend fun update(event: EventEntity)

    @Delete
    suspend fun delete(event: EventEntity)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM events")
    suspend fun deleteAll()

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getById(id: Long): EventEntity?

    @Query("SELECT * FROM events ORDER BY date DESC, time DESC")
    fun getAll(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE date = :date ORDER BY time")
    fun getByDate(date: String): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE date BETWEEN :from AND :to ORDER BY date, time")
    fun getByDateRange(from: String, to: String): Flow<List<EventEntity>>
}
