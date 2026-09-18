package com.elfrikiamv.minegocio_puntodeventa.data.entity

// PurchaseEntity.kt — فاکتور خرید (+ قلم‌های خرید)

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * فاکتور خرید از تامین‌کننده.
 *
 * @param no شماره فاکتور (از Counter).
 * @param date تاریخ شمسی (صفرپرشده).
 * @param items اقلام فاکتور.
 * @param total جمع اقلام · @param discount تخفیف · @param grand مبلغ نهایی · @param paid پرداخت‌شده.
 */
@Entity(
    tableName = "purchases",
    indices = [Index("no"), Index("date"), Index("supplierId")]
)
data class PurchaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val no: String = "",
    val date: String = "",
    val supplierId: Long? = null,
    val items: List<PurchaseItem> = emptyList(),
    val total: Double = 0.0,
    val discount: Double = 0.0,
    val grand: Double = 0.0,
    val paid: Double = 0.0
)

/**
 * قلم فاکتور خرید (درون [PurchaseEntity.items] ذخیره می‌شود).
 */
data class PurchaseItem(
    val productId: Long = 0,
    val name: String = "",
    val unit: String = "",
    val qty: Double = 0.0,
    val price: Double = 0.0,
    val total: Double = 0.0
)
