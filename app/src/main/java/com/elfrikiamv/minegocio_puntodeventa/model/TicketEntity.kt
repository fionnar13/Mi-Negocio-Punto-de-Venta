package com.elfrikiamv.minegocio_puntodeventa.model

// TicketEntity.kt

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tickets")
data class TicketEntity(
    @PrimaryKey val ticketId: String,    // ID basado en fecha y hora actual
    //val dateTime: String,              // Fecha y hora actual
    val products: List<ProductEntity>,  // Lista de productos
    val totalPrice: Double,             // Precio total del ticket

    val totalProducts: Int = 0, // Nuevo campo con valor por defecto 0
    val amountReceived: Double = 0.0, // Nuevo campo con valor por defecto 0.0
    val change: Double = 0.0, // Nuevo campo con valor por defecto 0.0// Precio total del ticket
    val date: String = "", // Nuevo campo para la fecha
    val time: String = ""  // Nuevo campo para la hora
)