package com.elfrikiamv.minegocio_puntodeventa.data.entity

// VisitorEntity.kt — ویزیتور

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * ویزیتور (بازاریاب).
 *
 * @param type نوع پورسانت: [TYPE_PERCENT] (درصدی)، [TYPE_CARTON] (کارتنی) یا [TYPE_FIXED] (ثابت).
 * @param value مقدار پورسانت (برحسب نوع: درصد، مبلغ هر کارتن یا مبلغ ثابت).
 */
@Entity(
    tableName = "visitors",
    indices = [Index("name")]
)
data class VisitorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    val type: String = TYPE_PERCENT,
    val value: Double = 0.0
) {
    companion object {
        /** پورسانت درصدی از فروش. */
        const val TYPE_PERCENT = "percent"

        /** پورسانت به ازای هر کارتن. */
        const val TYPE_CARTON = "carton"

        /** پورسانت مبلغ ثابت. */
        const val TYPE_FIXED = "fixed"
    }
}
