package com.elfrikiamv.minegocio_puntodeventa.data.dao

// BankDao.kt

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.elfrikiamv.minegocio_puntodeventa.data.entity.BankEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BankDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bank: BankEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(banks: List<BankEntity>): List<Long>

    @Update
    suspend fun update(bank: BankEntity)

    @Delete
    suspend fun delete(bank: BankEntity)

    @Query("DELETE FROM banks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM banks")
    suspend fun deleteAll()

    @Query("SELECT * FROM banks WHERE id = :id")
    suspend fun getById(id: Long): BankEntity?

    @Query("SELECT * FROM banks ORDER BY bank")
    fun getAll(): Flow<List<BankEntity>>
}
