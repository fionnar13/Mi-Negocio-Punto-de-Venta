package com.elfrikiamv.minegocio_puntodeventa.model

// Ticket.kt

// Modelo para representar un ticket
data class Ticket(
    val ticketId: String = "",        // ID único
    val dateTime: String = "",        // Fecha y hora
    val products: List<Map<String, Any>> = emptyList(), // Productos en formato de mapa
    val totalPrice: Double = 0.0      // Precio total
)

