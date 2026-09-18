package com.elfrikiamv.minegocio_puntodeventa.data.entity

// SaleEntity.kt — فاکتور فروش (+ قلم‌های فروش و پرداخت‌ها)

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * فاکتور فروش.
 *
 * @param no شماره فاکتور (از Counter).
 * @param date تاریخ شمسی (مثل ۱۴۰۵/۰۶/۲۷ — صفرپرشده برای مرتب‌سازی متنی).
 * @param time ساعت (مثل 14:30).
 * @param items اقلام فاکتور · @param pays پرداخت‌های فاکتور.
 * @param total جمع اقلام · @param discount تخفیف · @param grand مبلغ نهایی.
 * @param paid مبلغ پرداخت‌شده (باقی‌مانده = grand - paid برای مشتری دفتری).
 * @param commission پورسانت ویزیتور.
 */
@Entity(
    tableName = "sales",
    indices = [
        Index("no"),
        Index("date"),
        Index("customerId"),
        Index("visitorId"),
        Index("sessionId"),
        Index("userId")
    ]
)
data class SaleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val no: String = "",
    val date: String = "",
    val time: String = "",
    val customerId: Long? = null,
    val visitorId: Long? = null,
    val sessionId: Long? = null,
    val userId: Long? = null,
    val terminalId: Long? = null,
    val items: List<SaleItem> = emptyList(),
    val total: Double = 0.0,
    val discount: Double = 0.0,
    val grand: Double = 0.0,
    val paid: Double = 0.0,
    val pays: List<Payment> = emptyList(),
    val commission: Double = 0.0
)

/**
 * قلم فاکتور فروش (درون [SaleEntity.items] ذخیره می‌شود).
 *
 * @param productId شناسهٔ کالا · @param name نام کالا در لحظهٔ فروش
 * @param unit واحد فروش · @param qty تعداد · @param price قیمت واحد
 * @param discount تخفیف قلم · @param total مبلغ قلم
 */
data class SaleItem(
    val productId: Long = 0,
    val name: String = "",
    val unit: String = "",
    val qty: Double = 0.0,
    val price: Double = 0.0,
    val discount: Double = 0.0,
    val total: Double = 0.0
)

/**
 * پرداخت فاکتور (درون [SaleEntity.pays] ذخیره می‌شود).
 *
 * @param method روش پرداخت: [Payment.METHOD_CASH]، [Payment.METHOD_CARD] یا [Payment.METHOD_TRANSFER].
 * @param bankId بانک مربوطه (برای کارت/انتقال).
 * @param ref شماره پیگیری/مرجع.
 */
data class Payment(
    val method: String = METHOD_CASH,
    val amount: Double = 0.0,
    val bankId: Long? = null,
    val ref: String? = null
) {
    companion object {
        /** پرداخت نقدی. */
        const val METHOD_CASH = "نقدی"

        /** پرداخت با کارت‌خوان. */
        const val METHOD_CARD = "کارت"

        /** انتقال بانکی. */
        const val METHOD_TRANSFER = "انتقال"

        /** چک. */
        const val METHOD_CHEQUE = "چک"
    }
}
