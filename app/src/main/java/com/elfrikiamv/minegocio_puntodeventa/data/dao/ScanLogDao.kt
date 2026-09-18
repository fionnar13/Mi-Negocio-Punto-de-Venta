package com.elfrikiamv.minegocio_puntodeventa.data.dao

// ScanLogDao.kt — لاگ اسکن بارکد

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.elfrikiamv.minegocio_puntodeventa.data.entity.ScanLogEntity

/** دستگاه‌های اسکنر دیده‌شده (برای تب دستگاه‌ها). */
data class ScannerDevice(
    val device: String,
    val cnt: Int,
    val lastSeen: Long
)

/** آمار عملکرد هر دستگاه اسکنر. */
data class DeviceScanStat(
    val device: String,
    val cnt: Int,
    val successCnt: Int
)

@Dao
interface ScanLogDao {

    @Insert
    suspend fun insert(log: ScanLogEntity)

    @Query("DELETE FROM scan_logs")
    suspend fun deleteAll()

    /** لاگ‌های یک بازهٔ تاریخ (جدیدترین اول). */
    @Query(
        "SELECT * FROM scan_logs WHERE date BETWEEN :from AND :to " +
            "ORDER BY id DESC"
    )
    suspend fun betweenDates(from: String, to: String): List<ScanLogEntity>

    /** عملکرد اسکنرها در یک بازه: تعداد کل و موفق. */
    @Query(
        "SELECT device, COUNT(*) AS cnt, COALESCE(SUM(success), 0) AS successCnt " +
            "FROM scan_logs WHERE date BETWEEN :from AND :to " +
            "GROUP BY device ORDER BY cnt DESC"
    )
    suspend fun deviceStats(from: String, to: String): List<DeviceScanStat>

    /** فهرست دستگاه‌های اسکنر با تعداد و آخرین اسکن. */
    @Query(
        "SELECT device, COUNT(*) AS cnt, MAX(timestamp) AS lastSeen " +
            "FROM scan_logs GROUP BY device ORDER BY lastSeen DESC"
    )
    suspend fun devices(): List<ScannerDevice>
}
