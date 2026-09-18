package com.elfrikiamv.minegocio_puntodeventa.data.entity

// SessionEntity.kt — نشست کاری (شift صندوق)

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * نشست کاری/شیفت صندوق.
 *
 * @param userId کاربر بازکنندهٔ نشست.
 * @param startTime زمان شروع (میلی‌ثانیه از epoch).
 * @param startCash موجودی نقدی اولیهٔ کشو.
 * @param endTime زمان بستن شیفت (null = باز).
 */
@Entity(
    tableName = "sessions",
    indices = [Index("userId")]
)
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long? = null,
    val startTime: Long = 0L,
    val startCash: Double = 0.0,
    val endTime: Long? = null
)
