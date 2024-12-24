package com.elfrikiamv.minegocio_puntodeventa.model

//Product.kt

// Clase de datos para representar un producto en el inventario
data class ProductFirebase(
    val id: String = "",         // Identificador único del producto en Firebase
    val name: String = "",       // Nombre del producto
    val quantity: Int = 0,       // Cantidad disponible del producto
    val price: Double = 0.0,     // Precio unitario del producto
    val barcode: String = ""     // Código de barras del producto
)