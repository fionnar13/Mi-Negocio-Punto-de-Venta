package com.elfrikiamv.minegocio_puntodeventa.data.entity

// ProductEntity.kt — کالا

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * کالا/محصول.
 *
 * @param conv ضریب تبدیل واحدها به واحد پایه (مثلاً {"کارتن": 12.0}).
 * @param buyU واحد خرید · @param buyC قیمت خرید
 * @param sellU واحد فروش · @param sellC قیمت فروش
 * @param sellUnits قیمت فروش به تفکیک واحد (مثلاً {"عدد": 25000.0, "کارتن": 280000.0}).
 * @param stock موجودی (بر حسب واحد پایه؛ Double برای پشتیبانی از کیلوگرم و …).
 * @param min حداقل موجودی هشدار.
 * @param soldCount تعداد فروش‌شده (بر حسب واحد پایه).
 */
@Entity(
    tableName = "products",
    indices = [Index("barcode"), Index("name"), Index("cat")]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    val barcode: String = "",
    val code: String = "",
    val cat: String = "",
    val subCat: String = "",
    val brand: String = "",
    val img: String? = null,
    val baseUnit: String = "",
    val conv: Map<String, Double> = emptyMap(),
    val buyU: String = "",
    val buyC: Double = 0.0,
    val sellU: String = "",
    val sellC: Double = 0.0,
    val sellUnits: Map<String, Double> = emptyMap(),
    val stock: Double = 0.0,
    val min: Double = 0.0,
    val soldCount: Double = 0.0
)
