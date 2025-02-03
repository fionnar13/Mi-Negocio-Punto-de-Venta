package com.elfrikiamv.minegocio_puntodeventa.model.shopping

// Ticket.kt

// Modelo para representar un ticket
data class TicketFirebase(
    val ticketId: String = "",        // ID único
    //val dateTime: String = "",        // Fecha y hora
    val products: List<Map<String, Any>> = emptyList(), // Productos en formato de mapa
    val totalPrice: Double = 0.0,      // Precio total

    val totalProducts: Int = 0, // Nuevo campo con valor por defecto 0
    val amountReceived: Double = 0.0, // Nuevo campo con valor por defecto 0.0
    val change: Double = 0.0, // Nuevo campo con valor por defecto 0.0// Precio total del ticket
    val date: String = "", // Nuevo campo para la fecha
    val time: String = ""  // Nuevo campo para la hora
)