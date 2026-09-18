package com.elfrikiamv.minegocio_puntodeventa.data.dao

// CounterDao.kt — جدول تک‌ردیفی (id = 1)

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.elfrikiamv.minegocio_puntodeventa.data.entity.CounterEntity

@Dao
interface CounterDao {

    /** ذخیره/مقداردهی اولیهٔ شمارنده‌ها (ردیف با id = 1). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(counter: CounterEntity)

    @Update
    suspend fun update(counter: CounterEntity)

    @Query("SELECT * FROM counters WHERE id = 1")
    suspend fun get(): CounterEntity?

    /** شمارهٔ فاکتور فروش بعدی را اتمیک افزایش می‌دهد. */
    @Query("UPDATE counters SET saleNo = saleNo + 1 WHERE id = 1")
    suspend fun incrementSaleNo()

    /** شمارهٔ فاکتور خرید بعدی را اتمیک افزایش می‌دهد. */
    @Query("UPDATE counters SET purchNo = purchNo + 1 WHERE id = 1")
    suspend fun incrementPurchNo()

    @Query("DELETE FROM counters")
    suspend fun deleteAll()
}
