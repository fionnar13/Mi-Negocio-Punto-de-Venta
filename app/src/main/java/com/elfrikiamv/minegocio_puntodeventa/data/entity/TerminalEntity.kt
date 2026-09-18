package com.elfrikiamv.minegocio_puntodeventa.data.entity

// TerminalEntity.kt — پایانه

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * پایانه/دستگاه صندوق (برای تفکیک فروش چند صندوق).
 */
@Entity(tableName = "terminals")
data class TerminalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = ""
)
