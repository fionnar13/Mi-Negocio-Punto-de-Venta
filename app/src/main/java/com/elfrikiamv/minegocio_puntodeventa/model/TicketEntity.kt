package com.elfrikiamv.minegocio_puntodeventa.model

// TicketEntity.kt

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tickets")
data class TicketEntity(
    @PrimaryKey val ticketId: String,    // ID basado en fecha y hora actual
    val dateTime: String,              // Fecha y hora actual
    val products: List<ProductEntity>,  // Lista de productos
    val totalPrice: Double          // Precio total del ticket
) {
    fun toTicketEntity(): Ticket {
        return Ticket(
            ticketId = this.ticketId,  // Usamos el mismo ticketId
            dateTime = this.dateTime,  // Usamos la misma fecha y hora
            // Convertimos la lista de productos (de ProductEntity a Map) para Firestore
            products = this.products.map { product ->
                mapOf(
                    "barcode" to product.barcode,
                    "name" to product.name,
                    "quantity" to product.quantity,
                    "price" to product.price
                )
            },
            totalPrice = this.totalPrice  // Usamos el mismo precio total
        )
    }

}