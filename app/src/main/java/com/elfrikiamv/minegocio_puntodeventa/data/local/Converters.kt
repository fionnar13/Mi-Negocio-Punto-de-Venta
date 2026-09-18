package com.elfrikiamv.minegocio_puntodeventa.data.local

// Converters.kt — مبدل‌های نوع Room برای نقشه‌ها و لیست‌ها (با Gson)

import androidx.room.TypeConverter
import com.elfrikiamv.minegocio_puntodeventa.data.entity.Payment
import com.elfrikiamv.minegocio_puntodeventa.data.entity.PurchaseItem
import com.elfrikiamv.minegocio_puntodeventa.data.entity.SaleItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * مبدل‌های نوع Room:
 * - `Map<String, Double>` ↔ JSON (برای conv و sellUnits کالا)
 * - `List<SaleItem>` / `List<PurchaseItem>` / `List<Payment>` ↔ JSON
 */
class Converters {

    private val gson = Gson()

    // ---- Map<String, Double> ----

    @TypeConverter
    fun fromStringMap(map: Map<String, Double>?): String =
        gson.toJson(map ?: emptyMap<String, Double>())

    @TypeConverter
    fun toStringMap(value: String?): Map<String, Double> {
        if (value.isNullOrBlank()) return emptyMap()
        val type = object : TypeToken<Map<String, Double>>() {}.type
        return runCatching { gson.fromJson<Map<String, Double>>(value, type) }
            .getOrNull() ?: emptyMap()
    }

    // ---- List<SaleItem> ----

    @TypeConverter
    fun fromSaleItems(items: List<SaleItem>?): String =
        gson.toJson(items ?: emptyList<SaleItem>())

    @TypeConverter
    fun toSaleItems(value: String?): List<SaleItem> {
        if (value.isNullOrBlank()) return emptyList()
        val type = object : TypeToken<List<SaleItem>>() {}.type
        return runCatching { gson.fromJson<List<SaleItem>>(value, type) }
            .getOrNull() ?: emptyList()
    }

    // ---- List<PurchaseItem> ----

    @TypeConverter
    fun fromPurchaseItems(items: List<PurchaseItem>?): String =
        gson.toJson(items ?: emptyList<PurchaseItem>())

    @TypeConverter
    fun toPurchaseItems(value: String?): List<PurchaseItem> {
        if (value.isNullOrBlank()) return emptyList()
        val type = object : TypeToken<List<PurchaseItem>>() {}.type
        return runCatching { gson.fromJson<List<PurchaseItem>>(value, type) }
            .getOrNull() ?: emptyList()
    }

    // ---- List<Payment> ----

    @TypeConverter
    fun fromPayments(payments: List<Payment>?): String =
        gson.toJson(payments ?: emptyList<Payment>())

    @TypeConverter
    fun toPayments(value: String?): List<Payment> {
        if (value.isNullOrBlank()) return emptyList()
        val type = object : TypeToken<List<Payment>>() {}.type
        return runCatching { gson.fromJson<List<Payment>>(value, type) }
            .getOrNull() ?: emptyList()
    }
}
