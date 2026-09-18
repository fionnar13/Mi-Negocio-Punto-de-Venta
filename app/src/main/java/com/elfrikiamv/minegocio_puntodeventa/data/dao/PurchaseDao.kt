package com.elfrikiamv.minegocio_puntodeventa.data.dao

// PurchaseDao.kt

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.elfrikiamv.minegocio_puntodeventa.data.entity.PurchaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(purchase: PurchaseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(purchases: List<PurchaseEntity>): List<Long>

    @Update
    suspend fun update(purchase: PurchaseEntity)

    @Delete
    suspend fun delete(purchase: PurchaseEntity)

    @Query("DELETE FROM purchases WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM purchases")
    suspend fun deleteAll()

    @Query("SELECT * FROM purchases WHERE id = :id")
    suspend fun getById(id: Long): PurchaseEntity?

    @Query("SELECT * FROM purchases WHERE no = :no LIMIT 1")
    suspend fun getByNo(no: String): PurchaseEntity?

    @Query("SELECT * FROM purchases ORDER BY id DESC")
    fun getAll(): Flow<List<PurchaseEntity>>

    @Query("SELECT * FROM purchases WHERE date = :date ORDER BY id DESC")
    fun getByDate(date: String): Flow<List<PurchaseEntity>>

    @Query("SELECT * FROM purchases WHERE date BETWEEN :from AND :to ORDER BY id DESC")
    fun getByDateRange(from: String, to: String): Flow<List<PurchaseEntity>>

    @Query("SELECT * FROM purchases WHERE supplierId = :supplierId ORDER BY id DESC")
    fun getBySupplierId(supplierId: Long): Flow<List<PurchaseEntity>>

    /** جمع مبلغ نهایی خرید در یک بازهٔ تاریخ. */
    @Query("SELECT COALESCE(SUM(grand), 0) FROM purchases WHERE date BETWEEN :from AND :to")
    suspend fun totalGrandBetween(from: String, to: String): Double

    /** آخرین فاکتورهای خرید. */
    @Query("SELECT * FROM purchases ORDER BY id DESC LIMIT :limit")
    suspend fun getRecentOnce(limit: Int): List<PurchaseEntity>
}
