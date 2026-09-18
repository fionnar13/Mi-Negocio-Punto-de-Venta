package com.elfrikiamv.minegocio_puntodeventa.utils

// JalaliDateUtils.kt — Utilidades del calendario Jalali (persa)

import saman.zamani.persiandate.PersianDate

/**
 * Utilidades para trabajar con el calendario Jalali (شمسی / خورشیدی).
 *
 * Envuelve la biblioteca [PersianDate](https://github.com/samanzamani/PersianDate)
 * (`com.github.samanzamani:PersianDate`) ofreciendo una API idiomática en Kotlin,
 * nombres de meses y días en persa y formatos con dígitos persas.
 */
object JalaliDateUtils {

    /** Nombres de los meses del calendario Jalali (1 = فروردین … 12 = اسفند). */
    val MONTH_NAMES: Array<String> = arrayOf(
        "فروردین", "اردیبهشت", "خرداد",
        "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر",
        "دی", "بهمن", "اسفند"
    )

    /** Nombres de los días de la semana (0 = شنبه … 6 = جمعه). */
    val DAY_NAMES: Array<String> = arrayOf(
        "شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه",
        "چهارشنبه", "پنجشنبه", "جمعه"
    )

    /** Fecha y hora actual en el calendario Jalali. */
    fun now(): PersianDate = PersianDate()

    /** Crea una fecha Jalali a partir de un timestamp en milisegundos. */
    fun fromTimestamp(millis: Long): PersianDate = PersianDate(millis)

    /** Crea una fecha a partir de componentes Jalali (año, mes, día). */
    fun fromJalali(jalaliYear: Int, jalaliMonth: Int, jalaliDay: Int): PersianDate =
        PersianDate().apply {
            setShYear(jalaliYear)
            setShMonth(jalaliMonth)
            setShDay(jalaliDay)
            initJalaliDate()
        }

    /** Crea una fecha a partir de componentes gregorianos (año, mes, día). */
    fun fromGregorian(gregorianYear: Int, gregorianMonth: Int, gregorianDay: Int): PersianDate {
        val jalali = toJalali(gregorianYear, gregorianMonth, gregorianDay)
        return fromJalali(jalali[0], jalali[1], jalali[2])
    }

    /** Convierte una fecha gregoriana a componentes Jalali: `[año, mes, día]`. */
    fun toJalali(gregorianYear: Int, gregorianMonth: Int, gregorianDay: Int): IntArray =
        PersianDate().toJalali(gregorianYear, gregorianMonth, gregorianDay)

    /** Convierte componentes Jalali a fecha gregoriana: `[año, mes, día]`. */
    fun toGregorian(jalaliYear: Int, jalaliMonth: Int, jalaliDay: Int): IntArray =
        PersianDate().toGregorian(jalaliYear, jalaliMonth, jalaliDay)

    /** Indica si un año Jalali es bisiesto (esfand de 30 días). */
    fun isLeapJalaliYear(jalaliYear: Int): Boolean = fromJalali(jalaliYear, 1, 1).isLeap()

    /** Nombre del mes Jalali (1..12), o `null` si el mes es inválido. */
    fun monthName(month: Int): String? = MONTH_NAMES.getOrNull(month - 1)

    /** Nombre del día de la semana (0 = شنبه … 6 = جمعه), o `null` si es inválido. */
    fun dayName(dayOfWeek: Int): String? = DAY_NAMES.getOrNull(dayOfWeek)

    /**
     * Fecha corta con dígitos persas.
     * Ejemplo: `"۱۴۰۵/۰۶/۲۷"`
     */
    fun formatShort(date: PersianDate): String {
        val year = NumberUtils.toPersian(date.getShYear())
        val month = NumberUtils.toPersian(date.getShMonth().toString().padStart(2, '0'))
        val day = NumberUtils.toPersian(date.getShDay().toString().padStart(2, '0'))
        return "$year/$month/$day"
    }

    /**
     * Fecha larga con día de la semana y dígitos persas.
     * Ejemplo: `"پنجشنبه ۲۷ شهریور ۱۴۰۵"`
     */
    fun formatLong(date: PersianDate): String {
        val day = NumberUtils.toPersian(date.getShDay())
        val year = NumberUtils.toPersian(date.getShYear())
        return "${date.dayName()} $day ${date.monthName()} $year"
    }

    /** Fecha corta de hoy con dígitos persas. Ejemplo: `"۱۴۰۵/۰۶/۲۷"` */
    fun todayShort(): String = formatShort(now())

    /** Fecha larga de hoy. Ejemplo: `"پنجشنبه ۲۷ شهریور ۱۴۰۵"` */
    fun todayLong(): String = formatLong(now())
}
