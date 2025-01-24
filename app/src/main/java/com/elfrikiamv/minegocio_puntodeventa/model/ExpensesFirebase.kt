package com.elfrikiamv.minegocio_puntodeventa.model

data class ExpensesFirebase(
    val expenseId: String = "", // ID único
    val name: String = "",

    val description: String = "",
    val amountExpense: Double = 0.0, // Nuevo campo con valor por defecto 0.0
    val amountGiven: Double = 0.0, // Nuevo campo con valor por defecto 0.0
    val change: Double = 0.0, // Nuevo campo con valor por defecto 0.0// Precio total del ticket
    val date: String = "", // Nuevo campo para la fecha
    val time: String = ""  // Nuevo campo para la hora
)