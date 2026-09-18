package com.elfrikiamv.minegocio_puntodeventa.utils

// ReceiptText.kt — متن سادهٔ فاکتور (برای اشتراک‌گذاری)

import com.elfrikiamv.minegocio_puntodeventa.data.entity.PurchaseEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.SaleEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.SettingsEntity
import com.elfrikiamv.minegocio_puntodeventa.ui.components.unitLabel

/**
 * ساخت متن سادهٔ فاکتور فروش/خرید برای اشتراک‌گذاری
 * (تلگرام/واتساپ/بله/عمومی) — با ارقام فارسی و چیدمان متنی ساده.
 */
object ReceiptText {

    /** خط جداکننده. */
    private val SEP = "─".repeat(26)

    /** متن فاکتور فروش. */
    fun sale(
        sale: SaleEntity,
        customerName: String,
        visitorName: String,
        settings: SettingsEntity
    ): String = buildString {
        appendLine(settings.storeName.ifBlank { "سازمان فروشگاه" })
        if (settings.storePhone.isNotBlank()) appendLine("تلفن: ${settings.storePhone}")
        appendLine(SEP)
        appendLine("فاکتور فروش شماره ${NumberUtils.toPersian(sale.no)}")
        appendLine("تاریخ: ${PersianFormat.displayDate(sale.date)} · ساعت: ${PersianFormat.displayTime(sale.time)}")
        if (customerName.isNotBlank()) appendLine("مشتری: $customerName")
        if (visitorName.isNotBlank()) appendLine("ویزیتور: $visitorName")
        appendLine(SEP)
        sale.items.forEach { item ->
            appendLine(
                "${item.name} · ${PersianFormat.qty(item.qty)} ${unitLabel(item.unit)} × " +
                    PersianFormat.amount(item.price) + " = " + PersianFormat.amount(item.total)
            )
        }
        appendLine(SEP)
        appendLine("جمع: ${PersianFormat.amount(sale.total)} ${settings.currency}")
        if (sale.discount > 0.0) appendLine("تخفیف: ${PersianFormat.amount(sale.discount)} ${settings.currency}")
        appendLine("قابل پرداخت: ${PersianFormat.amount(sale.grand)} ${settings.currency}")
        appendLine("پرداخت‌شده: ${PersianFormat.amount(sale.paid)} ${settings.currency}")
        if (sale.grand - sale.paid > 0.009) {
            appendLine("مانده: ${PersianFormat.amount(sale.grand - sale.paid)} ${settings.currency}")
        }
        if (sale.pays.isNotEmpty()) {
            appendLine(SEP)
            sale.pays.forEach { p ->
                appendLine("${p.method}: ${PersianFormat.amount(p.amount)}")
            }
        }
        appendLine(SEP)
        append("با تشکر از خرید شما 🙏")
    }

    /** متن فاکتور خرید. */
    fun purchase(
        purchase: PurchaseEntity,
        supplierName: String,
        settings: SettingsEntity
    ): String = buildString {
        appendLine(settings.storeName.ifBlank { "سازمان فروشگاه" })
        appendLine(SEP)
        appendLine("فاکتور خرید شماره ${NumberUtils.toPersian(purchase.no)}")
        appendLine("تاریخ: ${PersianFormat.displayDate(purchase.date)}")
        if (supplierName.isNotBlank()) appendLine("تامین‌کننده: $supplierName")
        appendLine(SEP)
        purchase.items.forEach { item ->
            appendLine(
                "${item.name} · ${PersianFormat.qty(item.qty)} ${unitLabel(item.unit)} × " +
                    PersianFormat.amount(item.price) + " = " + PersianFormat.amount(item.total)
            )
        }
        appendLine(SEP)
        appendLine("جمع: ${PersianFormat.amount(purchase.total)} ${settings.currency}")
        if (purchase.discount > 0.0) appendLine("تخفیف: ${PersianFormat.amount(purchase.discount)} ${settings.currency}")
        appendLine("قابل پرداخت: ${PersianFormat.amount(purchase.grand)} ${settings.currency}")
        appendLine("پرداخت‌شده: ${PersianFormat.amount(purchase.paid)} ${settings.currency}")
        if (purchase.grand - purchase.paid > 0.009) {
            appendLine("بدهی: ${PersianFormat.amount(purchase.grand - purchase.paid)} ${settings.currency}")
        }
        appendLine(SEP)
        append("ثبت شد در «سازمان فروشگاه»")
    }
}
