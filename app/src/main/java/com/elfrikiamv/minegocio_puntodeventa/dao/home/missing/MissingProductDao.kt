package com.elfrikiamv.minegocio_puntodeventa.dao.home.missing

// MissingProductDao.kt

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.elfrikiamv.minegocio_puntodeventa.model.home.missing.MissingProductEntity

@Dao
interface MissingProductDao {
    @Insert
    suspend fun insertMissing(missing: MissingProductEntity)

    @Query("SELECT * FROM missing ORDER BY missingId DESC")
    suspend fun getAllExpenses(): List<MissingProductEntity>

    @Query("DELETE FROM missing WHERE missingId = :missingId")
    suspend fun deleteMissingById(missingId: String)

}