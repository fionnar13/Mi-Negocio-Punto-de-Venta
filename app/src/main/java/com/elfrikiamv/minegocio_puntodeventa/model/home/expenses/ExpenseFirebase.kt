package com.elfrikiamv.minegocio_puntodeventa.model.home.expenses

// ExpenseFirebase.kt

data class ExpenseFirebase(
    val expenseId: String = "", // ID único
    //val name: String = "",

    val date: String = "", // Nuevo campo para la fecha
    val time: String = "",  // Nuevo campo para la hora
    val paymentConcept: String = "",
    val paymentMethod: String = "",
    val amountExpense: Double = 0.0, // Nuevo campo con valor por defecto 0.0
    val amountGiven: Double = 0.0, // Nuevo campo con valor por defecto 0.0
    val change: Double = 0.0, // Nuevo campo con valor por defecto 0.0// Precio total del ticket
    val description: String = ""
)