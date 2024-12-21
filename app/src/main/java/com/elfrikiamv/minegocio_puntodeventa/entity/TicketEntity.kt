package com.elfrikiamv.minegocio_puntodeventa.entity

// TicketEntity.kt

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tickets")
data class TicketEntity(
    @PrimaryKey val ticketId: String,    // ID basado en fecha y hora actual
    val dateTime: String,              // Fecha y hora actual
    val products: List<ProductEntity>,  // Lista de productos
    val totalPrice: Double          // Precio total del ticket
)
