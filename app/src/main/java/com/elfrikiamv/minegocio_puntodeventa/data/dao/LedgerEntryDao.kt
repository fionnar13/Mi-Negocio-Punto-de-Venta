package com.elfrikiamv.minegocio_puntodeventa.data.dao

// LedgerEntryDao.kt

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.elfrikiamv.minegocio_puntodeventa.data.entity.LedgerEntryEntity
import kotlinx.coroutines.flow.Flow

/** جمع بدهکار/بستانکار هر مشتری (خروجی کوئری تجمیعی). */
data class CustomerTotals(
    val customerId: Long,
    val totalDebit: Double,
    val totalCredit: Double
)

/** جمع بدهکار/بستانکار هر تامین‌کننده (خروجی کوئری تجمیعی). */
data class SupplierTotals(
    val supplierId: Long,
    val totalDebit: Double,
    val totalCredit: Double
)

@Dao
interface LedgerEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: LedgerEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<LedgerEntryEntity>): List<Long>

    @Update
    suspend fun update(entry: LedgerEntryEntity)

    @Delete
    suspend fun delete(entry: LedgerEntryEntity)

    @Query("DELETE FROM ledger_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM ledger_entries")
    suspend fun deleteAll()

    @Query("SELECT * FROM ledger_entries WHERE id = :id")
    suspend fun getById(id: Long): LedgerEntryEntity?

    @Query("SELECT * FROM ledger_entries ORDER BY id DESC")
    fun getAll(): Flow<List<LedgerEntryEntity>>

    @Query("SELECT * FROM ledger_entries WHERE t = :t ORDER BY id DESC")
    fun getByType(t: String): Flow<List<LedgerEntryEntity>>

    /** اسناد مرجع یک رویداد مالی (مثلاً همهٔ اسناد فاکتور فروش ۱۲). */
    @Query("SELECT * FROM ledger_entries WHERE t = :t AND refId = :refId ORDER BY id DESC")
    suspend fun getByRef(t: String, refId: Long): List<LedgerEntryEntity>

    @Query("SELECT * FROM ledger_entries WHERE date = :date ORDER BY id DESC")
    fun getByDate(date: String): Flow<List<LedgerEntryEntity>>

    @Query("SELECT * FROM ledger_entries WHERE date BETWEEN :from AND :to ORDER BY id DESC")
    fun getByDateRange(from: String, to: String): Flow<List<LedgerEntryEntity>>

    @Query("SELECT * FROM ledger_entries WHERE customerId = :customerId ORDER BY id DESC")
    fun getByCustomerId(customerId: Long): Flow<List<LedgerEntryEntity>>

    @Query("SELECT * FROM ledger_entries WHERE supplierId = :supplierId ORDER BY id DESC")
    fun getBySupplierId(supplierId: Long): Flow<List<LedgerEntryEntity>>

    @Query("SELECT * FROM ledger_entries WHERE sessionId = :sessionId ORDER BY id DESC")
    fun getBySessionId(sessionId: Long): Flow<List<LedgerEntryEntity>>

    // ---- جمع‌ها (ستون‌های in/out کلمهٔ رزرو هستند و با بک‌تیک نوشته شده‌اند) ----

    @Query("SELECT COALESCE(SUM(debit), 0) FROM ledger_entries WHERE customerId = :customerId")
    suspend fun totalDebitByCustomer(customerId: Long): Double

    @Query("SELECT COALESCE(SUM(credit), 0) FROM ledger_entries WHERE customerId = :customerId")
    suspend fun totalCreditByCustomer(customerId: Long): Double

    @Query("SELECT COALESCE(SUM(debit), 0) FROM ledger_entries WHERE supplierId = :supplierId")
    suspend fun totalDebitBySupplier(supplierId: Long): Double

    @Query("SELECT COALESCE(SUM(credit), 0) FROM ledger_entries WHERE supplierId = :supplierId")
    suspend fun totalCreditBySupplier(supplierId: Long): Double

    @Query("SELECT COALESCE(SUM(`in`), 0) FROM ledger_entries WHERE sessionId = :sessionId")
    suspend fun totalCashInBySession(sessionId: Long): Double

    @Query("SELECT COALESCE(SUM(`out`), 0) FROM ledger_entries WHERE sessionId = :sessionId")
    suspend fun totalCashOutBySession(sessionId: Long): Double

    @Query("SELECT COALESCE(SUM(bankIn), 0) FROM ledger_entries WHERE sessionId = :sessionId")
    suspend fun totalBankInBySession(sessionId: Long): Double

    @Query("SELECT COALESCE(SUM(bankOut), 0) FROM ledger_entries WHERE sessionId = :sessionId")
    suspend fun totalBankOutBySession(sessionId: Long): Double

    // ---- تجمیع‌های حساب‌ها و مانده‌ها ----

    /** جمع بدهکار/بستانکار به تفکیک مشتری (برای فهرست مشتریان). */
    @Query(
        "SELECT customerId, COALESCE(SUM(debit), 0) AS totalDebit, " +
            "COALESCE(SUM(credit), 0) AS totalCredit " +
            "FROM ledger_entries WHERE customerId IS NOT NULL GROUP BY customerId"
    )
    fun totalsByCustomer(): Flow<List<CustomerTotals>>

    /** جمع بدهکار/بستانکار به تفکیک تامین‌کننده. */
    @Query(
        "SELECT supplierId, COALESCE(SUM(debit), 0) AS totalDebit, " +
            "COALESCE(SUM(credit), 0) AS totalCredit " +
            "FROM ledger_entries WHERE supplierId IS NOT NULL GROUP BY supplierId"
    )
    fun totalsBySupplier(): Flow<List<SupplierTotals>>

    /** مانده نقدی صندوق: جمع (in − out). */
    @Query("SELECT COALESCE(SUM(`in` - `out`), 0) FROM ledger_entries")
    fun totalCashBalance(): Flow<Double>

    /** مانده بانکی: جمع (bankIn − bankOut). */
    @Query("SELECT COALESCE(SUM(bankIn - bankOut), 0) FROM ledger_entries")
    fun totalBankBalance(): Flow<Double>

    /** کل مطالبات از مشتریان (فقط مانده‌های مثبت هر مشتری). */
    @Query(
        "SELECT COALESCE(SUM(bal), 0) FROM (" +
            "SELECT SUM(debit - credit) AS bal FROM ledger_entries " +
            "WHERE customerId IS NOT NULL GROUP BY customerId HAVING bal > 0)"
    )
    fun totalReceivables(): Flow<Double>

    /** کل بدهی به تامین‌کنندگان (فقط مانده‌های مثبت هر تامین‌کننده). */
    @Query(
        "SELECT COALESCE(SUM(bal), 0) FROM (" +
            "SELECT SUM(credit - debit) AS bal FROM ledger_entries " +
            "WHERE supplierId IS NOT NULL GROUP BY supplierId HAVING bal > 0)"
    )
    fun totalPayables(): Flow<Double>

    /** اسناد با فیلتر نوع/متن/بازهٔ تاریخ (رشته‌های خالی = بدون فیلتر). */
    @Query(
        "SELECT * FROM ledger_entries WHERE (:type = '' OR t = :type) " +
            "AND (:query = '' OR note LIKE '%' || :query || '%') " +
            "AND (:from = '' OR date >= :from) AND (:to = '' OR date <= :to) " +
            "ORDER BY date DESC, id DESC"
    )
    fun filtered(
        type: String,
        query: String,
        from: String,
        to: String
    ): Flow<List<LedgerEntryEntity>>
}
