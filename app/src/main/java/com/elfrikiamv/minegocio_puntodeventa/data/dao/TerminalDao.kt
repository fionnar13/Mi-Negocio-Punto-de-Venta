package com.elfrikiamv.minegocio_puntodeventa.data.dao

// TerminalDao.kt

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.elfrikiamv.minegocio_puntodeventa.data.entity.TerminalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TerminalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(terminal: TerminalEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(terminals: List<TerminalEntity>): List<Long>

    @Update
    suspend fun update(terminal: TerminalEntity)

    @Delete
    suspend fun delete(terminal: TerminalEntity)

    @Query("DELETE FROM terminals WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM terminals")
    suspend fun deleteAll()

    @Query("SELECT * FROM terminals WHERE id = :id")
    suspend fun getById(id: Long): TerminalEntity?

    @Query("SELECT * FROM terminals ORDER BY name")
    fun getAll(): Flow<List<TerminalEntity>>
}
