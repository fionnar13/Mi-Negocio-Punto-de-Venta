package com.elfrikiamv.minegocio_puntodeventa.data.entity

// UserEntity.kt — کاربر

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * کاربر برنامه (صندوقدار، مدیر و …).
 *
 * @param role نقش کاربر · @param avatar مسیر/نشانی تصویر · @param token توکن نشست
 */
@Entity(
    tableName = "users",
    indices = [Index("name")]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    val role: String = "",
    val avatar: String? = null,
    val token: String = ""
)
