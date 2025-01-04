package com.elfrikiamv.minegocio_puntodeventa.viewmodel

// ShoppingViewModel.kt

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.elfrikiamv.minegocio_puntodeventa.database.AppDatabase
import com.elfrikiamv.minegocio_puntodeventa.model.ProductEntity
import com.elfrikiamv.minegocio_puntodeventa.model.ProductFirebase
import com.elfrikiamv.minegocio_puntodeventa.model.TicketEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.suspendCoroutine

// ViewModel para manejar el carrito de compras usando el patrón MVVM
class ShoppingViewModel(application: Application) : AndroidViewModel(application) {

    private val productDao = AppDatabase.getDatabase(application).productDao()
    private val ticketDao = AppDatabase.getDatabase(application).ticketDao()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _cartProducts = MutableStateFlow<List<ProductFirebase>>(emptyList())
    val cartProducts: StateFlow<List<ProductFirebase>> = _cartProducts

    init {
        loadCartProducts()
    }

    fun addToCart(product: ProductFirebase) {
        val productEntity = ProductEntity(
            barcode = product.barcode,
            name = product.name,
            quantity = product.quantity,
            price = product.salePrice
        )

        viewModelScope.launch {
            val existingProduct = productDao.getProductByBarcode(product.barcode)
            if (existingProduct == null) {
                // Nuevo producto
                productDao.insertProduct(productEntity)
                updateFirebaseQuantity(product.barcode, -product.quantity) // Restar en Firebase
            } else {
                // Producto existente
                val updatedQuantity = existingProduct.quantity + product.quantity
                productDao.updateProduct(
                    existingProduct.copy(quantity = updatedQuantity)
                )
                updateFirebaseQuantity(product.barcode, -product.quantity) // Ajustar en Firebase
            }
            loadCartProducts()
        }
    }

    fun deleteCartProduct(barcode: String) {
        viewModelScope.launch {
            val product = productDao.getProductByBarcode(barcode)
            if (product != null) {
                productDao.deleteProduct(product)
                updateFirebaseQuantity(barcode, product.quantity) // Revertir en Firebase
                loadCartProducts()
            }
        }
    }

    private fun updateFirebaseQuantity(barcode: String, quantityChange: Int) {
        val userEmail = auth.currentUser?.email ?: return
        db.collection("users")
            .document(userEmail)
            .collection("inventories")
            .whereEqualTo("barcode", barcode)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents.first()
                    val currentQuantity = document.getLong("quantity") ?: 0
                    val newQuantity = (currentQuantity + quantityChange).coerceAtLeast(0)
                    db.collection("users")
                        .document(userEmail)
                        .collection("inventories")
                        .document(document.id)
                        .update("quantity", newQuantity)
                        .addOnSuccessListener {
                            Log.d("ShoppingViewModel", "Cantidad actualizada en Firebase: $newQuantity")
                        }
                        .addOnFailureListener { e ->
                            Log.e("ShoppingViewModel", "Error al actualizar Firebase: $e")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("ShoppingViewModel", "Error al consultar Firebase: $e")
            }
    }

    fun confirmTicket() {
        viewModelScope.launch {
            val currentDateTime = System.currentTimeMillis()
            val dateTimeFormatted = SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault()
            ).format(Date(currentDateTime))

            val products = productDao.getAllProducts()
            if (products.isEmpty()) {
                Log.e("ShoppingViewModel", "No hay productos en el carrito.")
                return@launch
            }

            val totalPrice = products.sumOf { it.price * it.quantity }
            val ticket = TicketEntity(
                ticketId = currentDateTime.toString(),
                dateTime = dateTimeFormatted,
                products = products,
                totalPrice = totalPrice
            )

            // Insertamos el ticket en la base de datos
            ticketDao.insertTicket(ticket)
            /*products.forEach { product ->
                updateFirebaseQuantity(product.barcode, product.quantity) // Revertir en Firebase
            }*/
            productDao.deleteAllProducts()
            loadCartProducts()
            uploadTicketToFirebase(ticket)
        }
    }

    private fun loadCartProducts() {
        viewModelScope.launch {
            val products = productDao.getAllProducts().map {
                ProductFirebase(
                    barcode = it.barcode,
                    name = it.name,
                    quantity = it.quantity,
                    salePrice = it.price
                )
            }
            _cartProducts.value = products
        }
    }

    private fun uploadTicketToFirebase(ticketEntity: TicketEntity) {
        val userEmail = auth.currentUser?.email ?: return

        db.collection("users")
            .document(userEmail)
            .collection("tickets")
            .document(ticketEntity.ticketId)
            .set(ticketEntity)
            .addOnSuccessListener {
                viewModelScope.launch {
                    ticketDao.deleteTicketById(ticketEntity.ticketId)
                }
            }
            .addOnFailureListener { e ->
                Log.e("ShoppingViewModel", "Error al subir el ticket a Firebase: $e")
            }
    }

    private suspend fun getFirebaseStock(barcode: String): Int = suspendCoroutine { continuation ->
        val userEmail = auth.currentUser?.email
        if (userEmail == null) {
            continuation.resumeWith(Result.success(0))
            return@suspendCoroutine
        }

        db.collection("users")
            .document(userEmail)
            .collection("inventories")
            .whereEqualTo("barcode", barcode)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val stock = documents.documents.first().getLong("quantity")?.toInt() ?: 0
                    continuation.resumeWith(Result.success(stock))
                } else {
                    continuation.resumeWith(Result.success(0)) // Si no hay documentos, devolvemos 0
                }
            }
            .addOnFailureListener { e ->
                Log.e("ShoppingViewModel", "Error al consultar stock en Firebase: $e")
                continuation.resumeWith(Result.success(0)) // En caso de error, devolvemos 0
            }
    }

    fun increaseCartProductQuantity(barcode: String) {
        viewModelScope.launch {
            val existingProduct = productDao.getProductByBarcode(barcode)
            if (existingProduct != null) {
                val currentStock = getFirebaseStock(barcode) // Consultar existencias en Firebase
                if (existingProduct.quantity < currentStock) {
                    val newQuantity = existingProduct.quantity + 1
                    productDao.updateProduct(existingProduct.copy(quantity = newQuantity))
                    updateFirebaseQuantity(barcode, -1) // Reducir 1 en Firebase
                    loadCartProducts()
                } else {
                    Log.e("ShoppingViewModel", "No hay suficiente stock en Firebase para incrementar.")
                }
            }
        }
    }

    fun decreaseCartProductQuantity(barcode: String) {
        viewModelScope.launch {
            val existingProduct = productDao.getProductByBarcode(barcode)
            if (existingProduct != null && existingProduct.quantity > 1) {
                val newQuantity = existingProduct.quantity - 1
                productDao.updateProduct(existingProduct.copy(quantity = newQuantity))
                updateFirebaseQuantity(barcode, 1) // Incrementar 1 en Firebase
                loadCartProducts()
            } else {
                Log.e("ShoppingViewModel", "No se puede disminuir más la cantidad.")
            }
        }
    }

}