package com.elfrikiamv.minegocio_puntodeventa.data.entity

// CustomerEntity.kt — مشتری

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * مشتری.
 *
 * @param kind نوع مشتری: [KIND_CASH] (نقدی) یا [KIND_CREDIT] (دفتری).
 */
@Entity(
    tableName = "customers",
    indices = [Index("name"), Index("phone")]
)
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    val phone: String = "",
    val kind: String = KIND_CASH
) {
    companion object {
        /** مشتری نقدی. */
        const val KIND_CASH = "نقدی"

        /** مشتری دفتری (با حساب بدهی). */
        const val KIND_CREDIT = "دفتری"
    }
}
