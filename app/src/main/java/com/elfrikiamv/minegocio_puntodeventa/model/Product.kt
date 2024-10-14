package com.elfrikiamv.minegocio_puntodeventa.model

// Clase de datos para representar un producto en el inventario
data class Product(
    val id: String = "",        // Identificador único del producto en Firebase
    val name: String = "",       // Nombre del producto
    val quantity: Int = 0,       // Cantidad disponible del producto
    val barcode: String = ""     // Código de barras del producto
)