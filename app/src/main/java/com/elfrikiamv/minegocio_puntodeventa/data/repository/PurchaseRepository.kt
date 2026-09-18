package com.elfrikiamv.minegocio_puntodeventa.data.repository

// PurchaseRepository.kt

import com.elfrikiamv.minegocio_puntodeventa.data.dao.PurchaseDao
import com.elfrikiamv.minegocio_puntodeventa.data.entity.PurchaseEntity
import kotlinx.coroutines.flow.Flow

/**
 * مخزن فاکتورهای خرید.
 */
class PurchaseRepository(private val dao: PurchaseDao) {

    fun getAll(): Flow<List<PurchaseEntity>> = dao.getAll()

    fun getByDate(date: String): Flow<List<PurchaseEntity>> = dao.getByDate(date)

    fun getByDateRange(from: String, to: String): Flow<List<PurchaseEntity>> =
        dao.getByDateRange(from, to)

    fun getBySupplierId(supplierId: Long): Flow<List<PurchaseEntity>> =
        dao.getBySupplierId(supplierId)

    suspend fun getById(id: Long): PurchaseEntity? = dao.getById(id)

    suspend fun getByNo(no: String): PurchaseEntity? = dao.getByNo(no)

    suspend fun insert(purchase: PurchaseEntity): Long = dao.insert(purchase)

    suspend fun insertAll(purchases: List<PurchaseEntity>): List<Long> = dao.insertAll(purchases)

    suspend fun update(purchase: PurchaseEntity) = dao.update(purchase)

    suspend fun delete(purchase: PurchaseEntity) = dao.delete(purchase)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun deleteAll() = dao.deleteAll()

    /** جمع خرید نهایی در یک بازهٔ تاریخ. */
    suspend fun totalGrandBetween(from: String, to: String): Double =
        dao.totalGrandBetween(from, to)

    /** آخرین فاکتورها (برای چاپ/اشتراک‌گذاری). */
    suspend fun getRecentOnce(limit: Int): List<PurchaseEntity> = dao.getRecentOnce(limit)
}
