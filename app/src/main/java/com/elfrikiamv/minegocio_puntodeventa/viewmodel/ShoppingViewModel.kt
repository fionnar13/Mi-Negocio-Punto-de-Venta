package com.elfrikiamv.minegocio_puntodeventa.viewmodel

// ShoppingViewModel.kt

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.elfrikiamv.minegocio_puntodeventa.database.AppDatabase
import com.elfrikiamv.minegocio_puntodeventa.entity.ProductEntity
import com.elfrikiamv.minegocio_puntodeventa.entity.TicketEntity
import com.elfrikiamv.minegocio_puntodeventa.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ViewModel para manejar el carrito de compras usando el patrón MVVM
class ShoppingViewModel(application: Application) : AndroidViewModel(application) {

    // Instancia de ProductDao y TicketDao obtenidas de la base de datos
    private val productDao = AppDatabase.getDatabase(application)
        .productDao() // Para manejar productos en la base de datos
    private val ticketDao =
        AppDatabase.getDatabase(application).ticketDao() // Para manejar tickets en la base de datos

    // MutableStateFlow que almacena los productos del carrito (estado mutable)
    private val _cartProducts = MutableStateFlow<List<Product>>(emptyList())

    // StateFlow expuesto para que las vistas puedan observar los productos del carrito
    val cartProducts: StateFlow<List<Product>> = _cartProducts

    // Inicializa el ViewModel y carga los productos al abrir la pantalla
    init {
        loadCartProducts()
    }

    // Función para agregar un producto al carrito de compras
    fun addToCart(product: Product) {
        // Convertimos el producto de la vista en una entidad para la base de datos (ProductEntity)
        val productEntity = ProductEntity(
            barcode = product.barcode,  // Código de barras del producto
            name = product.name,        // Nombre del producto
            quantity = product.quantity, // Cantidad agregada
            price = product.price        // Precio unitario
        )

        // Ejecutamos la inserción en la base de datos en un hilo de trabajo (viewModelScope)
        viewModelScope.launch {
            // Insertamos el producto en la base de datos
            productDao.insertProduct(productEntity)
            // Recargamos los productos del carrito para reflejar la actualización
            loadCartProducts()
            // Log para verificar que el producto fue guardado correctamente
            Log.d("ShoppingViewModel", "Producto agregado al carrito: $productEntity")
        }
    }

    // Función para confirmar el ticket de compra
    fun confirmTicket() {
        viewModelScope.launch {
            // Genera un ID único basado en la fecha y hora actual
            val currentDateTime = System.currentTimeMillis()
            // Formateamos la fecha y hora en un formato legible
            val dateTimeFormatted = SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault()
            ).format(Date(currentDateTime))

            // Calculamos el precio total de los productos del carrito
            val totalPrice = productDao.getAllProducts()
                .sumOf { it.price * it.quantity } // Suma de precios por cantidad

            // Creamos el objeto TicketEntity con el precio total
            val ticket = TicketEntity(
                ticketId = currentDateTime.toString(), // ID único del ticket
                dateTime = dateTimeFormatted,           // Fecha y hora del ticket
                products = productDao.getAllProducts(), // Lista de productos del carrito
                totalPrice = totalPrice                // Precio total de los productos en el carrito
            )

            // Insertamos el ticket en la base de datos
            ticketDao.insertTicket(ticket)
            // Log para verificar que el ticket fue guardado correctamente
            Log.d("ShoppingViewModel", "Ticket insertado en la base de datos: $ticket")

            // Limpiamos los productos del carrito (después de confirmar el ticket)
            productDao.deleteAllProducts()

            // Recargamos la lista del carrito (que ahora estará vacía)
            loadCartProducts()

            // Log para verificar que los productos fueron eliminados del carrito
            Log.d(
                "ShoppingViewModel",
                "Productos eliminados del carrito después de confirmar el ticket"
            )
        }
    }

    // Función para cargar los productos del carrito desde la base de datos
    private fun loadCartProducts() {
        viewModelScope.launch {
            // Recuperamos todos los productos desde la base de datos (ProductEntity)
            val products = productDao.getAllProducts().map {
                // Convertimos los productos de la base de datos (ProductEntity) a objetos Product
                Product(
                    barcode = it.barcode,   // Código de barras del producto
                    name = it.name,         // Nombre del producto
                    quantity = it.quantity, // Cantidad del producto
                    price = it.price        // Precio unitario del producto
                )
            }
            // Actualizamos el StateFlow con la lista de productos
            _cartProducts.value = products
            // Log para verificar que los productos se cargaron correctamente
            Log.d("ShoppingViewModel", "Productos cargados del carrito: $products")
        }
    }
}