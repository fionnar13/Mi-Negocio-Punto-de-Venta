package com.elfrikiamv.minegocio_puntodeventa.data.entity

// BankEntity.kt — بانک

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * بانک/حساب بانکی فروشگاه.
 *
 * @param bank نام بانک · @param card شماره کارت · @param account شماره حساب · @param sheba شماره شبا
 */
@Entity(
    tableName = "banks",
    indices = [Index("bank")]
)
data class BankEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bank: String = "",
    val card: String = "",
    val account: String = "",
    val sheba: String = ""
)
