package com.elfrikiamv.minegocio_puntodeventa.data.entity

// ScanLogEntity.kt — لاگ اسکن بارکد

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * رکورد هر اسکن بارکد (برای گزارش‌های «لاگ اسکن» و «عملکرد اسکنرها»).
 *
 * @param date تاریخ شمسی ذخیره‌ای ("1405/06/27") برای فیلتر بازه‌ای.
 * @param time ساعت ("14:30").
 * @param timestamp میلی‌ثانیه از epoch.
 * @param device نام دستگاه (مدل گوشی/پایانه).
 * @param barcode بارکد اسکن‌شده.
 * @param success آیا کالا با این بارکد یافت شد؟
 */
@Entity(
    tableName = "scan_logs",
    indices = [Index("date"), Index("device")]
)
data class ScanLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String = "",
    val time: String = "",
    val timestamp: Long = 0L,
    val device: String = "",
    val barcode: String = "",
    val success: Boolean = false
) {
    companion object {
        /** برچسب نتیجه برای نمایش در گزارش. */
        fun resultLabel(success: Boolean): String =
            if (success) "یافت شد" else "یافت نشد"
    }
}
