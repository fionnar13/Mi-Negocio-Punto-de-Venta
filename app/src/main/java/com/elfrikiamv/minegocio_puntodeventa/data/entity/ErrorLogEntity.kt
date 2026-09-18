package com.elfrikiamv.minegocio_puntodeventa.data.entity

// ErrorLogEntity.kt — لاگ خطاها و رویدادهای برنامه (تب دیباگر)

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * رکورد لاگ برنامه.
 *
 * @param ts زمان رویداد (میلی‌ثانیه از epoch).
 * @param level سطح: ERROR / WARN / INFO / DEBUG.
 * @param scope دامنهٔ وقوع (نام کلاس/ماژول/نخ).
 * @param message پیام کوتاه خوانا.
 * @param technical جزئیات فنی (متن کامل خطا/استک‌تریس).
 * @param context زمینه (نام صفحه/عملیات جاری).
 * @param userAgent مشخصات دستگاه و نسخهٔ برنامه.
 * @param url نشانی مرتبط (مثل نشانی سرور WebSocket).
 */
@Entity(
    tableName = "error_logs",
    indices = [Index("ts"), Index("level")]
)
data class ErrorLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ts: Long = 0L,
    val level: String = "INFO",
    val scope: String = "",
    val message: String = "",
    val technical: String = "",
    val context: String = "",
    val userAgent: String = "",
    val url: String = ""
) {
    companion object {
        const val LEVEL_ERROR = "ERROR"
        const val LEVEL_WARN = "WARN"
        const val LEVEL_INFO = "INFO"
        const val LEVEL_DEBUG = "DEBUG"
    }
}
