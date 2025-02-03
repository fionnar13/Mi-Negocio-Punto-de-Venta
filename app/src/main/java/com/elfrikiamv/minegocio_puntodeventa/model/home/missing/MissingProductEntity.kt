package com.elfrikiamv.minegocio_puntodeventa.model.home.missing

// MissingProductEntity.kt

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "missing")
data class MissingProductEntity(
    @PrimaryKey val missingId: String,    // ID basado en fecha y hora actual

    //val missingId: String = "",        // Identificador único del producto
    val date: String = "",            // campo para la fecha
    val time: String = "",          // campo para la hora
    val name: String = "",          // Nombre del producto
    val quantity: Int = 0,          // Cantidad disponible
    //val barcode: String = "",       // Código de barras del producto
    val providerPrice: Double = 0.0, // Precio del proveedor
    val totalPrice: Double = 0.0,      // Precio total
    //val salePrice: Double = 0.0,    // Precio de venta
    val description: String = ""    // Descripción del producto
)