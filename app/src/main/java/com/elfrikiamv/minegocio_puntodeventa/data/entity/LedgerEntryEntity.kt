package com.elfrikiamv.minegocio_puntodeventa.data.entity

// LedgerEntryEntity.kt — سند حساب (دفتر کل)

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * سند دفتر کل. هر رویداد مالی یک سند ثبت می‌کند.
 *
 * @param t نوع سند: [TYPE_SALE]، [TYPE_PURCHASE]، [TYPE_RECEIPT] (دریافت از مشتری)،
 *          [TYPE_PAY_SUP] (پرداخت به تامین‌کننده)، [TYPE_EXPENSE] یا [TYPE_INCOME].
 * @param refId شناسهٔ سند مرجع (فاکتور/هزینه و …).
 * @param debit بدهکار · @param credit بستانکار
 * @param cashIn نقدی دریافتی (ستون `in`) · @param cashOut نقدی پرداختی (ستون `out`)
 * @param bankIn بانکی دریافتی · @param bankOut بانکی پرداختی
 *
 * نکته: نام ستون‌های `in` و `out` با @ColumnInfo تنظیم شده است چون `in`
 * در Kotlin کلمهٔ رزرو شده است؛ در کوئری‌های SQL باید با بک‌تیک نوشته شوند.
 */
@Entity(
    tableName = "ledger_entries",
    indices = [
        Index("date"),
        Index("t"),
        Index("refId"),
        Index("customerId"),
        Index("supplierId"),
        Index("sessionId")
    ]
)
data class LedgerEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String = "",
    val t: String = TYPE_SALE,
    val refId: Long? = null,
    val customerId: Long? = null,
    val supplierId: Long? = null,
    val userId: Long? = null,
    val sessionId: Long? = null,
    val debit: Double = 0.0,
    val credit: Double = 0.0,
    @ColumnInfo(name = "in") val cashIn: Double = 0.0,
    @ColumnInfo(name = "out") val cashOut: Double = 0.0,
    val bankIn: Double = 0.0,
    val bankOut: Double = 0.0,
    val note: String = ""
) {
    companion object {
        /** فروش. */
        const val TYPE_SALE = "sale"

        /** خرید. */
        const val TYPE_PURCHASE = "purchase"

        /** دریافت از مشتری. */
        const val TYPE_RECEIPT = "receipt"

        /** پرداخت به تامین‌کننده. */
        const val TYPE_PAY_SUP = "paySup"

        /** هزینه. */
        const val TYPE_EXPENSE = "expense"

        /** درآمد دیگر. */
        const val TYPE_INCOME = "income"
    }
}
