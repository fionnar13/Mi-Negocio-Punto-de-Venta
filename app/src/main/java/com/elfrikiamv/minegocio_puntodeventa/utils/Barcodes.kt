package com.elfrikiamv.minegocio_puntodeventa.utils

// Barcodes.kt — بارکدهای وزنی (پیشوند 20 یا 28)

/**
 * ابزار کار با بارکدهای وزنی (Embedded Weight Barcodes).
 *
 * قالب رایج EAN-13 ترازو:
 * `PP CCCCC WWWWW X`
 * - PP: پیشوند 20 یا 28 (بارکد وزنی)
 * - CCCCC: کد کالا (۵ رقم؛ منطبق بر فیلد `code` کالا)
 * - WWWWW: وزن به گرم (۵ رقم؛ مثلاً 00350 = 0/350 کیلوگرم)
 * - X: رقم کنترل (نادیده گرفته می‌شود)
 */
object Barcodes {

    /** نتیجهٔ تجزیهٔ بارکد وزنی. */
    data class WeightBarcode(
        val code: String,
        val weightKg: Double
    )

    /**
     * اگر بارکد از نوع وزنی باشد، کد کالا و وزن (کیلوگرم) را برمی‌گرداند.
     * در غیر این صورت null.
     */
    fun parseWeightBarcode(barcode: String): WeightBarcode? {
        val b = barcode.trim()
        if (b.length != 13 || b.any { !it.isDigit() }) return null
        if (!b.startsWith("20") && !b.startsWith("28")) return null

        val code = b.substring(2, 7)
        val grams = b.substring(7, 12).toLongOrNull() ?: return null
        val weightKg = grams / 1000.0
        if (weightKg <= 0.0) return null

        return WeightBarcode(code = code, weightKg = weightKg)
    }
}
