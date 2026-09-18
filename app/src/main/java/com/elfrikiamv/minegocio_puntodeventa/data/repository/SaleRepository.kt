package com.elfrikiamv.minegocio_puntodeventa.data.repository

// SaleRepository.kt

import com.elfrikiamv.minegocio_puntodeventa.data.dao.SaleDao
import com.elfrikiamv.minegocio_puntodeventa.data.dao.VisitorCommission
import com.elfrikiamv.minegocio_puntodeventa.data.entity.SaleEntity
import kotlinx.coroutines.flow.Flow
import com.elfrikiamv.minegocio_puntodeventa.data.dao.DailySaleRow
import com.elfrikiamv.minegocio_puntodeventa.data.dao.PartyStat
import com.elfrikiamv.minegocio_puntodeventa.data.dao.SessionStat
import com.elfrikiamv.minegocio_puntodeventa.data.dao.UserStat
import com.elfrikiamv.minegocio_puntodeventa.data.dao.VisitorStat

/**
 * مخزن فاکتورهای فروش.
 */
class SaleRepository(private val dao: SaleDao) {

    fun getAll(): Flow<List<SaleEntity>> = dao.getAll()

    fun getByDate(date: String): Flow<List<SaleEntity>> = dao.getByDate(date)

    fun getByDateRange(from: String, to: String): Flow<List<SaleEntity>> =
        dao.getByDateRange(from, to)

    fun getByCustomerId(customerId: Long): Flow<List<SaleEntity>> =
        dao.getByCustomerId(customerId)

    fun getByVisitorId(visitorId: Long): Flow<List<SaleEntity>> =
        dao.getByVisitorId(visitorId)

    fun getBySessionId(sessionId: Long): Flow<List<SaleEntity>> =
        dao.getBySessionId(sessionId)

    fun getByUserId(userId: Long): Flow<List<SaleEntity>> = dao.getByUserId(userId)

    suspend fun getById(id: Long): SaleEntity? = dao.getById(id)

    suspend fun getByNo(no: String): SaleEntity? = dao.getByNo(no)

    suspend fun insert(sale: SaleEntity): Long = dao.insert(sale)

    suspend fun insertAll(sales: List<SaleEntity>): List<Long> = dao.insertAll(sales)

    suspend fun update(sale: SaleEntity) = dao.update(sale)

    suspend fun delete(sale: SaleEntity) = dao.delete(sale)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun deleteAll() = dao.deleteAll()

    /** جمع فروش نهایی در یک بازهٔ تاریخ. */
    suspend fun totalGrandBetween(from: String, to: String): Double =
        dao.totalGrandBetween(from, to)

    /** جمع پورسانت به تفکیک ویزیتور. */
    fun commissionsByVisitor(): Flow<List<VisitorCommission>> = dao.commissionsByVisitor()

    // ---- گزارش‌ها ----

    /** فاکتورهای یک بازه (برای تجمیع اقلام). */
    suspend fun getBetween(from: String, to: String): List<SaleEntity> =
        dao.getBetween(from, to)

    /** گزارش روزانه. */
    suspend fun dailySales(from: String, to: String): List<DailySaleRow> =
        dao.dailySales(from, to)

    /** گزارش فروش به تفکیک مشتری. */
    suspend fun salesByCustomer(from: String, to: String): List<PartyStat> =
        dao.salesByCustomer(from, to)

    /** گزارش فروش به تفکیک ویزیتور. */
    suspend fun salesByVisitor(from: String, to: String): List<VisitorStat> =
        dao.salesByVisitor(from, to)

    /** گزارش فروش به تفکیک نشست. */
    suspend fun salesBySession(from: String, to: String): List<SessionStat> =
        dao.salesBySession(from, to)

    /** گزارش فروش به تفکیک کاربر. */
    suspend fun salesByUser(from: String, to: String): List<UserStat> =
        dao.salesByUser(from, to)

    /** فاکتورهای یک شیفت (برای گزارش پایان شیفت). */
    suspend fun getBySessionOnce(sessionId: Long): List<SaleEntity> =
        dao.getBySessionOnce(sessionId)

    /** آخرین فاکتورها (برای چاپ/اشتراک‌گذاری). */
    suspend fun getRecentOnce(limit: Int): List<SaleEntity> = dao.getRecentOnce(limit)
}
