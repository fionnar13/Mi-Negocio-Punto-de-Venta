package com.elfrikiamv.minegocio_puntodeventa.model

// ProductEntity.kt

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val barcode: String,  // Código de barras como ID único
    val name: String,                 // Nombre del producto
    var quantity: Int,                // Cantidad del producto
    val price: Double                 // Precio unitario
)
