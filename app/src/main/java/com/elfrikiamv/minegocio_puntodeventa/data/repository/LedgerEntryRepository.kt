package com.elfrikiamv.minegocio_puntodeventa.data.repository

// LedgerEntryRepository.kt

import com.elfrikiamv.minegocio_puntodeventa.data.dao.CustomerTotals
import com.elfrikiamv.minegocio_puntodeventa.data.dao.LedgerEntryDao
import com.elfrikiamv.minegocio_puntodeventa.data.dao.SupplierTotals
import com.elfrikiamv.minegocio_puntodeventa.data.entity.LedgerEntryEntity
import kotlinx.coroutines.flow.Flow

/**
 * مخزن اسناد دفتر کل.
 */
class LedgerEntryRepository(private val dao: LedgerEntryDao) {

    fun getAll(): Flow<List<LedgerEntryEntity>> = dao.getAll()

    fun getByType(t: String): Flow<List<LedgerEntryEntity>> = dao.getByType(t)

    fun getByDate(date: String): Flow<List<LedgerEntryEntity>> = dao.getByDate(date)

    fun getByDateRange(from: String, to: String): Flow<List<LedgerEntryEntity>> =
        dao.getByDateRange(from, to)

    fun getByCustomerId(customerId: Long): Flow<List<LedgerEntryEntity>> =
        dao.getByCustomerId(customerId)

    fun getBySupplierId(supplierId: Long): Flow<List<LedgerEntryEntity>> =
        dao.getBySupplierId(supplierId)

    fun getBySessionId(sessionId: Long): Flow<List<LedgerEntryEntity>> =
        dao.getBySessionId(sessionId)

    suspend fun getById(id: Long): LedgerEntryEntity? = dao.getById(id)

    suspend fun getByRef(t: String, refId: Long): List<LedgerEntryEntity> =
        dao.getByRef(t, refId)

    suspend fun insert(entry: LedgerEntryEntity): Long = dao.insert(entry)

    suspend fun insertAll(entries: List<LedgerEntryEntity>): List<Long> = dao.insertAll(entries)

    suspend fun update(entry: LedgerEntryEntity) = dao.update(entry)

    suspend fun delete(entry: LedgerEntryEntity) = dao.delete(entry)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun deleteAll() = dao.deleteAll()

    /** مانده حساب مشتری (بدهکار منهای بستانکار؛ مثبت = بدهی مشتری). */
    suspend fun customerBalance(customerId: Long): Double =
        dao.totalDebitByCustomer(customerId) - dao.totalCreditByCustomer(customerId)

    /** مانده حساب تامین‌کننده (بستانکار منهای بدهکار؛ مثبت = بدهی ما). */
    suspend fun supplierBalance(supplierId: Long): Double =
        dao.totalCreditBySupplier(supplierId) - dao.totalDebitBySupplier(supplierId)

    suspend fun totalDebitByCustomer(customerId: Long): Double =
        dao.totalDebitByCustomer(customerId)

    suspend fun totalCreditByCustomer(customerId: Long): Double =
        dao.totalCreditByCustomer(customerId)

    suspend fun totalDebitBySupplier(supplierId: Long): Double =
        dao.totalDebitBySupplier(supplierId)

    suspend fun totalCreditBySupplier(supplierId: Long): Double =
        dao.totalCreditBySupplier(supplierId)

    suspend fun totalCashInBySession(sessionId: Long): Double =
        dao.totalCashInBySession(sessionId)

    suspend fun totalCashOutBySession(sessionId: Long): Double =
        dao.totalCashOutBySession(sessionId)

    suspend fun totalBankInBySession(sessionId: Long): Double =
        dao.totalBankInBySession(sessionId)

    suspend fun totalBankOutBySession(sessionId: Long): Double =
        dao.totalBankOutBySession(sessionId)

    // ---- حساب‌ها و مانده‌ها ----

    /** جمع بدهکار/بستانکار به تفکیک مشتری. */
    fun totalsByCustomer(): Flow<List<CustomerTotals>> = dao.totalsByCustomer()

    /** جمع بدهکار/بستانکار به تفکیک تامین‌کننده. */
    fun totalsBySupplier(): Flow<List<SupplierTotals>> = dao.totalsBySupplier()

    /** مانده نقدی صندوق. */
    fun totalCashBalance(): Flow<Double> = dao.totalCashBalance()

    /** مانده بانکی. */
    fun totalBankBalance(): Flow<Double> = dao.totalBankBalance()

    /** کل مطالبات از مشتریان. */
    fun totalReceivables(): Flow<Double> = dao.totalReceivables()

    /** کل بدهی به تامین‌کنندگان. */
    fun totalPayables(): Flow<Double> = dao.totalPayables()

    /** اسناد با فیلتر نوع/متن/بازهٔ تاریخ. */
    fun filtered(type: String, query: String, from: String, to: String): Flow<List<LedgerEntryEntity>> =
        dao.filtered(type, query, from, to)
}
