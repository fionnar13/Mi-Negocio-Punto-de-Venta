package com.elfrikiamv.minegocio_puntodeventa.model.home.expenses

// ExpenseEntity.kt

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val expenseId: String,    // ID basado en fecha y hora actual
    //val name: String = "",

    val date: String = "", // Nuevo campo para la fecha
    val time: String = "",  // Nuevo campo para la hora
    val paymentConcept: String = "",
    val paymentMethod: String = "",
    val amountExpense: Double = 0.0, // Nuevo campo con valor por defecto 0.0
    val amountGiven: Double = 0.0, // Nuevo campo con valor por defecto 0.0
    val change: Double = 0.0, // Nuevo campo con valor por defecto 0.0// Precio total del ticket
    val description: String = "",
    val timestamp: Long = 0L // Campo para almacenar el timestamp
)
