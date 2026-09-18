package com.elfrikiamv.minegocio_puntodeventa.utils

// PersianFormat.kt — قالب‌بندی اعداد/تاریخ/زمان فارسی برای فاکتورها

import com.elfrikiamv.minegocio_puntodeventa.utils.NumberUtils.toPersian
import com.elfrikiamv.minegocio_puntodeventa.utils.NumberUtils.toPersianGrouped
import java.math.BigDecimal
import java.util.Calendar

/**
 * قالب‌بندی مشترک صفحه‌های فروش و خرید:
 * تاریخ شمسی ذخیره‌ای (لاتین، صفرپرشده برای مرتب‌سازی) و نمایش فارسی.
 */
object PersianFormat {

    /** تاریخ شمسی امروز به شکل ذخیره‌ای: "1405/06/27". */
    fun todayDateString(): String {
        val p = JalaliDateUtils.now()
        return jalaliString(p.getShYear(), p.getShMonth(), p.getShDay())
    }

    /** ساخت رشتهٔ ذخیره‌ای از اجزای Jalali. */
    fun jalaliString(year: Int, month: Int, day: Int): String =
        "%04d/%02d/%02d".format(year, month, day)

    /** ساعت جاری به شکل ذخیره‌ای: "14:30". */
    fun nowTimeString(): String {
        val c = Calendar.getInstance()
        return "%02d:%02d".format(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
    }

    /** نمایش فارسی تاریخ ذخیره‌ای: "۱۴۰۵/۰۶/۲۷". */
    fun displayDate(date: String): String = toPersian(date)

    /** نمایش فارسی ساعت ذخیره‌ای: "۱۴:۳۰". */
    fun displayTime(time: String): String = toPersian(time)

    /** نمایش فارسی شماره‌ها (تلفن/کارت/شبا): فقط ارقام فارسی می‌شوند. */
    fun displayPhone(value: String): String = toPersian(value)

    /** عدد صحیح بدون جداکننده (مثل تعداد ردیف‌ها). */
    fun plain(value: Long): String = NumberUtils.toPersian(value)

    /** حجم فایل/داده با واحد مناسب: "۲٫۴ مگابایت". */
    fun size(bytes: Long): String {
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1.0 -> "${qty(mb)} مگابایت"
            kb >= 1.0 -> "${qty(kb)} کیلوبایت"
            else -> "${NumberUtils.toPersian(bytes)} بایت"
        }
    }

    /**
     * تبدیل ورودی کاربر به عدد؛ ارقام فارسی، ممیز فارسی (٫) و
     * ویرگول پشتیبانی می‌شوند. ناموفق → 0.0
     */
    fun parse(input: String): Double =
        NumberUtils.toLatin(input.trim())
            .replace('٫', '.')
            .replace(',', '.')
            .toDoubleOrNull() ?: 0.0

    /** مبلغ با جداکنندهٔ هزارگان فارسی و حداکثر ۳ رقم اعشار: "۱۲٬۵۰۰٫۵". */
    fun amount(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return toPersian("0")
        val sign = if (value < 0) "-" else ""
        val abs = kotlin.math.abs(value)
        val whole = abs.toLong()
        val frac = BigDecimal.valueOf(abs - whole)
            .setScale(3, BigDecimal.ROUND_HALF_UP)
            .stripTrailingZeros()
        val fracText = if (frac.toDouble() <= 0.0) {
            ""
        } else {
            "٫" + toPersian(frac.toPlainString().removePrefix("0."))
        }
        return sign + toPersianGrouped(whole) + fracText
    }

    /** تعداد: عدد صحیح یا وزن (تا ۳ رقم اعشار) با ارقام فارسی. */
    fun qty(value: Double): String =
        if (value % 1.0 == 0.0) toPersianGrouped(value.toLong()) else amount(value)

    /** مقدار لاتین برای پیش‌پرکردن فیلد ورودی (بدون ارقام فارسی). */
    fun plain(value: Double): String = when {
        value % 1.0 == 0.0 -> value.toLong().toString()
        else -> BigDecimal.valueOf(value).stripTrailingZeros().toPlainString()
    }
}
