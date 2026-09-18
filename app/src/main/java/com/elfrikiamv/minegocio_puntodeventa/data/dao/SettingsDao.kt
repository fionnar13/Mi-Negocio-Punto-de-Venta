package com.elfrikiamv.minegocio_puntodeventa.data.dao

// SettingsDao.kt — جدول تک‌ردیفی (id = 1)

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.elfrikiamv.minegocio_puntodeventa.data.entity.SettingsEntity

@Dao
interface SettingsDao {

    /** ذخیره/جایگزینی تنظیمات (ردیف یکتا با id = 1). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: SettingsEntity)

    @Update
    suspend fun update(settings: SettingsEntity)

    @Query("SELECT * FROM settings WHERE id = 1")
    suspend fun get(): SettingsEntity?

    @Query("DELETE FROM settings")
    suspend fun deleteAll()
}
