package com.elfrikiamv.minegocio_puntodeventa.utils

// NumberUtils.kt — Conversión de dígitos latinos a persas (۰۱۲۳۴۵۶۷۸۹)

/**
 * Utilidades numéricas para la interfaz persa.
 *
 * Convierte dígitos latinos (0-9) a dígitos persas (۰۱۲۳۴۵۶۷۸۹)
 * para mostrar montos, cantidades y fechas en farsi.
 */
object NumberUtils {

    /** Dígitos persas del 0 al 9. */
    const val PERSIAN_DIGITS: String = "۰۱۲۳۴۵۶۷۸۹"

    /** Separador de miles persa (U+066C). */
    const val PERSIAN_THOUSANDS_SEPARATOR: String = "٬"

    /** Separador decimal persa (U+066B). */
    const val PERSIAN_DECIMAL_SEPARATOR: String = "٫"

    private val latinToPersian: Map<Char, Char> =
        ('0'..'9').zip(PERSIAN_DIGITS.toList()).toMap()

    private val persianToLatin: Map<Char, Char> =
        PERSIAN_DIGITS.toList().zip('0'..'9').toMap()

    /**
     * Convierte un número entero a texto con dígitos persas.
     *
     * Ejemplo: `toPersian(1405L)` -> `"۱۴۰۵"`
     */
    fun toPersian(number: Long): String = toPersian(number.toString())

    /** Sobrecarga para [Int]. Ejemplo: `toPersian(27)` -> `"۲۷"` */
    fun toPersian(number: Int): String = toPersian(number.toLong())

    /** Sobrecarga para [Double]. Ejemplo: `toPersian(12.5)` -> `"۱۲.۵"` (el punto se mantiene) */
    fun toPersian(number: Double): String = toPersian(number.toString())

    /**
     * Reemplaza todos los dígitos latinos (0-9) de un texto por dígitos persas,
     * conservando el resto de caracteres sin cambios.
     *
     * Ejemplo: `toPersian("Total: 123")` -> `"Total: ۱۲۳"`
     */
    fun toPersian(text: String): String = buildString(text.length) {
        for (char in text) append(latinToPersian[char] ?: char)
    }

    /**
     * Convierte un número a dígitos persas con separador de miles.
     *
     * Ejemplo: `toPersianGrouped(1234567L)` -> `"۱٬۲۳۴٬۵۶۷"`
     */
    fun toPersianGrouped(number: Long): String {
        val digits = number.toString().removePrefix("-")
        val grouped = buildString {
            digits.forEachIndexed { index, char ->
                if (index > 0 && (digits.length - index) % 3 == 0) {
                    append(PERSIAN_THOUSANDS_SEPARATOR)
                }
                append(char)
            }
        }
        return toPersian(if (number < 0) "-$grouped" else grouped)
    }

    /**
     * Reemplaza los dígitos persas de un texto por dígitos latinos.
     * Útil para parsear entradas del usuario antes de operar con ellas.
     *
     * Ejemplo: `toLatin("۱۲۳")` -> `"123"`
     */
    fun toLatin(text: String): String = buildString(text.length) {
        for (char in text) append(persianToLatin[char] ?: char)
    }
}
