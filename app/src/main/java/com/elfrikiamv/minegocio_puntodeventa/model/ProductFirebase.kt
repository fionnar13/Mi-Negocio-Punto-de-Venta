package com.elfrikiamv.minegocio_puntodeventa.model

//Product.kt

// Clase de datos para representar un producto en el inventario
data class ProductFirebase(
    val id: String = "",            // Identificador único del producto
    val name: String = "",          // Nombre del producto
    val quantity: Int = 0,          // Cantidad disponible
    val barcode: String = "",       // Código de barras del producto
    val providerPrice: Double = 0.0, // Precio del proveedor
    val salePrice: Double = 0.0,    // Precio de venta
    val description: String = ""    // Descripción del producto
)