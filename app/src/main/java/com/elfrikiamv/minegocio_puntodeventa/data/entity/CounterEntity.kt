package com.elfrikiamv.minegocio_puntodeventa.data.entity

// CounterEntity.kt — شمارنده‌های فاکتور (تک‌ردیفی، id = 1)

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * شمارندهٔ شماره فاکتورها — جدول تک‌ردیفی با شناسهٔ ثابت ۱.
 * مقدار بعدی با افزایش اتمیک ستون مربوطه گرفته می‌شود.
 */
@Entity(tableName = "counters")
data class CounterEntity(
    @PrimaryKey val id: Long = 1,
    val saleNo: Long = 0,
    val purchNo: Long = 0
)
