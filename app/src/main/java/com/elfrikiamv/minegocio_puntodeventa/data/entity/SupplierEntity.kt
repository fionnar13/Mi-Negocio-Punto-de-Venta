package com.elfrikiamv.minegocio_puntodeventa.data.entity

// SupplierEntity.kt — تامین‌کننده

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * تامین‌کننده.
 *
 * @param card شماره کارت · @param account شماره حساب · @param address آدرس
 */
@Entity(
    tableName = "suppliers",
    indices = [Index("name"), Index("phone")]
)
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    val phone: String = "",
    val card: String = "",
    val account: String = "",
    val address: String = ""
)
