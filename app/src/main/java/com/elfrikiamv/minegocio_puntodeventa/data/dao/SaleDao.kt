package com.elfrikiamv.minegocio_puntodeventa.data.dao

// SaleDao.kt

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.elfrikiamv.minegocio_puntodeventa.data.entity.SaleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sale: SaleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sales: List<SaleEntity>): List<Long>

    @Update
    suspend fun update(sale: SaleEntity)

    @Delete
    suspend fun delete(sale: SaleEntity)

    @Query("DELETE FROM sales WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM sales")
    suspend fun deleteAll()

    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getById(id: Long): SaleEntity?

    @Query("SELECT * FROM sales WHERE no = :no LIMIT 1")
    suspend fun getByNo(no: String): SaleEntity?

    @Query("SELECT * FROM sales ORDER BY id DESC")
    fun getAll(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE date = :date ORDER BY id DESC")
    fun getByDate(date: String): Flow<List<SaleEntity>>

    /** بازهٔ تاریخ شمسی صفرپرشده (مثل ۱۴۰۵/۰۶/۰۱ تا ۱۴۰۵/۰۶/۳۱). */
    @Query("SELECT * FROM sales WHERE date BETWEEN :from AND :to ORDER BY id DESC")
    fun getByDateRange(from: String, to: String): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE customerId = :customerId ORDER BY id DESC")
    fun getByCustomerId(customerId: Long): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE visitorId = :visitorId ORDER BY id DESC")
    fun getByVisitorId(visitorId: Long): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE sessionId = :sessionId ORDER BY id DESC")
    fun getBySessionId(sessionId: Long): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE userId = :userId ORDER BY id DESC")
    fun getByUserId(userId: Long): Flow<List<SaleEntity>>

    /** آخرین فاکتورهای فروش (برای چاپ/اشتراک‌گذاری). */
    @Query("SELECT * FROM sales ORDER BY id DESC LIMIT :limit")
    suspend fun getRecentOnce(limit: Int): List<SaleEntity>

    /** فاکتورهای یک شیفت (برای گزارش پایان شیفت). */
    @Query("SELECT * FROM sales WHERE sessionId = :sessionId ORDER BY id")
    suspend fun getBySessionOnce(sessionId: Long): List<SaleEntity>

    /** جمع مبلغ نهایی فروش در یک بازهٔ تاریخ. */
    @Query("SELECT COALESCE(SUM(grand), 0) FROM sales WHERE date BETWEEN :from AND :to")
    suspend fun totalGrandBetween(from: String, to: String): Double

    // ---- کوئری‌های گزارش‌ها ----

    /** فاکتورهای یک بازه (برای تجمیع اقلام در Kotlin). */
    @Query("SELECT * FROM sales WHERE date BETWEEN :from AND :to ORDER BY id DESC")
    suspend fun getBetween(from: String, to: String): List<SaleEntity>

    /** گزارش روزانه: تاریخ، تعداد، جمع. */
    @Query(
        "SELECT date, COUNT(*) AS cnt, COALESCE(SUM(grand), 0) AS total " +
            "FROM sales WHERE date BETWEEN :from AND :to " +
            "GROUP BY date ORDER BY date DESC"
    )
    suspend fun dailySales(from: String, to: String): List<DailySaleRow>

    /** گزارش مشتری: تعداد فاکتور و جمع خرید هر مشتری. */
    @Query(
        "SELECT customerId, COUNT(*) AS cnt, COALESCE(SUM(grand), 0) AS total " +
            "FROM sales WHERE date BETWEEN :from AND :to AND customerId IS NOT NULL " +
            "GROUP BY customerId ORDER BY total DESC"
    )
    suspend fun salesByCustomer(from: String, to: String): List<PartyStat>

    /** گزارش ویزیتور: تعداد، جمع فروش و جمع پورسانت. */
    @Query(
        "SELECT visitorId, COUNT(*) AS cnt, COALESCE(SUM(grand), 0) AS total, " +
            "COALESCE(SUM(commission), 0) AS commission " +
            "FROM sales WHERE date BETWEEN :from AND :to AND visitorId IS NOT NULL " +
            "GROUP BY visitorId ORDER BY total DESC"
    )
    suspend fun salesByVisitor(from: String, to: String): List<VisitorStat>

    /** گزارش شیفت‌ها: تعداد فاکتور و جمع فروش هر نشست. */
    @Query(
        "SELECT sessionId, COUNT(*) AS cnt, COALESCE(SUM(grand), 0) AS total " +
            "FROM sales WHERE date BETWEEN :from AND :to AND sessionId IS NOT NULL " +
            "GROUP BY sessionId ORDER BY total DESC"
    )
    suspend fun salesBySession(from: String, to: String): List<SessionStat>

    /** گزارش کاربران: تعداد فاکتور و جمع فروش هر کاربر. */
    @Query(
        "SELECT userId, COUNT(*) AS cnt, COALESCE(SUM(grand), 0) AS total " +
            "FROM sales WHERE date BETWEEN :from AND :to AND userId IS NOT NULL " +
            "GROUP BY userId ORDER BY total DESC"
    )
    suspend fun salesByUser(from: String, to: String): List<UserStat>

    /** جمع پورسانت به تفکیک ویزیتور (برای فهرست ویزیتورها). */
    @Query(
        "SELECT visitorId, COALESCE(SUM(commission), 0) AS totalCommission " +
            "FROM sales WHERE visitorId IS NOT NULL GROUP BY visitorId"
    )
    fun commissionsByVisitor(): Flow<List<VisitorCommission>>
}

/** ردیف گزارش روزانه. */
data class DailySaleRow(
    val date: String,
    val cnt: Int,
    val total: Double
)

/** آمار فروش به تفکیک مشتری. */
data class PartyStat(
    val customerId: Long,
    val cnt: Int,
    val total: Double
)

/** آمار فروش به تفکیک ویزیتور (با پورسانت). */
data class VisitorStat(
    val visitorId: Long,
    val cnt: Int,
    val total: Double,
    val commission: Double
)

/** آمار فروش به تفکیک نشست. */
data class SessionStat(
    val sessionId: Long,
    val cnt: Int,
    val total: Double
)

/** آمار فروش به تفکیک کاربر. */
data class UserStat(
    val userId: Long,
    val cnt: Int,
    val total: Double
)

/** جمع پورسانت هر ویزیتور (خروجی کوئری تجمیعی). */
data class VisitorCommission(
    val visitorId: Long,
    val totalCommission: Double
)
