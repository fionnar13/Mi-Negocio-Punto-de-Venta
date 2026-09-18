package com.elfrikiamv.minegocio_puntodeventa.data.entity

// EventEntity.kt — رویداد تقویم

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * رویداد تقویم.
 *
 * @param date تاریخ شمسی (مثل ۱۴۰۵/۰۶/۲۷) · @param time ساعت (مثل 09:00)
 * @param color رنگ رویداد (ARGB) · @param reminder یادآور فعال/غیرفعال
 */
@Entity(
    tableName = "events",
    indices = [Index("date")]
)
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String = "",
    val date: String = "",
    val time: String = "",
    val color: Int = 0xFF6366F1.toInt(),
    val reminder: Boolean = false
)
