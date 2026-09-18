package com.elfrikiamv.minegocio_puntodeventa.data.entity

// SettingsEntity.kt — تنظیمات فروشگاه (تک‌ردیفی، id = 1)

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * تنظیمات فروشگاه — جدول تک‌ردیفی با شناسهٔ ثابت ۱.
 *
 * @param dateSystem نظام تاریخ: [DATE_JALALI] یا [DATE_GREGORIAN].
 * @param theme پوستهٔ رنگی: [THEME_DARK]، [THEME_LIGHT] یا [THEME_GOLD]
 *              (هم‌نام با AppTheme در ui/theme).
 * @param layout جهت چیدمان: [LAYOUT_RTL] یا [LAYOUT_LTR].
 * @param printFormat قالب چاپ فاکتور (مثل 58mm یا 80mm).
 */
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Long = 1,
    val storeName: String = "سازمان فروشگاه",
    val storePhone: String = "",
    val storeAddress: String = "",
    val storeLogo: String? = null,
    val storeLat: Double = 0.0,
    val storeLng: Double = 0.0,
    val storeEmail: String = "",
    val licenseNo: String = "",
    val oneDrive: String = "",
    val gDrive: String = "",
    val gSheet: String = "",
    val currency: String = "تومان",
    val dateSystem: String = DATE_JALALI,
    val theme: String = THEME_DARK,
    val layout: String = LAYOUT_RTL,
    val printFormat: String = "58mm",
    val defaultSellUnit: String = "u",
    val dupBarcode: String = DUP_BLOCK,
    val invalidBarcode: String = INVALID_WARN,
    val wsUrl: String = ""
) {
    companion object {
        const val DATE_JALALI = "jalali"
        const val DATE_GREGORIAN = "gregorian"

        const val THEME_DARK = "dark"
        const val THEME_LIGHT = "light"
        const val THEME_GOLD = "gold"

        const val LAYOUT_RTL = "rtl"
        const val LAYOUT_LTR = "ltr"

        /** سیاست بارکد تکراری. */
        const val DUP_BLOCK = "block"
        const val DUP_WARN = "warn"

        /** سیاست بارکد نامعتبر. */
        const val INVALID_WARN = "warn"
        const val INVALID_IGNORE = "ignore"
    }
}
