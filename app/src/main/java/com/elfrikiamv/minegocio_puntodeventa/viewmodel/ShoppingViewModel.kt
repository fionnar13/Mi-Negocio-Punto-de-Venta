package com.elfrikiamv.minegocio_puntodeventa.viewmodel

// ShoppingViewModel.kt

import android.app.Application
import android.content.Context
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
import java.util.Calendar
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

    private val userMyBusinesses = "defaultBusiness"
    private val userMyInventories = "defaultInventory"

    private val userMyTickets = "defaultTickets"

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
            .collection("userMyBusinesses")
            .document(userMyBusinesses)
            .collection("userMyInventories")
            .document(userMyInventories)
            .collection("userInventory")

            //.collection("inventories")
            .whereEqualTo("barcode", barcode)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents.first()
                    val currentQuantity = document.getLong("quantity") ?: 0
                    val newQuantity = (currentQuantity + quantityChange).coerceAtLeast(0)
                    db.collection("users")
                        .document(userEmail)
                        .collection("userMyBusinesses")
                        .document(userMyBusinesses)
                        .collection("userMyInventories")
                        .document(userMyInventories)
                        .collection("userInventory")

                        //.collection("inventories")
                        .document(document.id)
                        .update("quantity", newQuantity)
                        .addOnSuccessListener {
                            Log.d(
                                "ShoppingViewModel",
                                "Cantidad actualizada en Firebase: $newQuantity"
                            )
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

    fun confirmTicket(
        totalProducts: Int,
        totalPurchase: Double,
        amountReceived: Double,
        context: Context,
        email: String
    ) {
        viewModelScope.launch {
            // Obtener información de fecha y hora
            val currentDateTimeTicketId = System.currentTimeMillis()
            val currentDateTime = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

            val dateFormatted = dateFormat.format(currentDateTime.time)
            val timeFormatted = timeFormat.format(currentDateTime.time)

            // Obtener productos del carrito
            val products = productDao.getAllProducts()
            if (products.isEmpty()) {
                Log.e("ShoppingViewModel", "No hay productos en el carrito.")
                return@launch
            }

            val totalPrice = products.sumOf { it.price * it.quantity }
            val change = amountReceived - totalPurchase

            // Crear el ticket
            val ticket = TicketEntity(
                ticketId = currentDateTimeTicketId.toString(),
                date = dateFormatted,
                time = timeFormatted,
                products = products,
                totalPrice = totalPrice,
                totalProducts = totalProducts,
                amountReceived = amountReceived,
                change = change
            )

            // Guardar en Room
            ticketDao.insertTicket(ticket)
            Log.d("ShoppingViewModel", "Ticket guardado en Room: $ticket")

            // Limpiar carrito
            productDao.deleteAllProducts()
            loadCartProducts()

            // Subir a Firebase
            uploadTicketToFirebase(ticket, context, email)
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

    private fun uploadTicketToFirebase(
        ticketEntity: TicketEntity,
        context: Context,
        email: String
    ) {
        val userEmail = auth.currentUser?.email ?: return

        db.collection("users")
            .document(userEmail)
            .collection("userMyBusinesses")
            .document(userMyBusinesses)
            .collection("userMyTickets")
            .document(userMyTickets)
            .collection("userTickets")

            .document(ticketEntity.ticketId)
            .set(ticketEntity)
            .addOnSuccessListener {
                viewModelScope.launch {

                    // compartir ticket
                    val ticketId = ticketEntity.ticketId
                    sharePDF(ticketId, context, email)
                    // Eliminar ticket de Room
                    ticketDao.deleteTicketById(ticketEntity.ticketId)
                }
            }
            .addOnFailureListener { e ->
                Log.e("ShoppingViewModel", "Error al subir el ticket a Firebase: $e")
            }
    }

    private fun sharePDF(ticketId: String, context: Context, email: String) {

        // Obtener ticket de Firebase y generar PDF
        val activityViewModel = ActivityViewModel(getApplication())
        activityViewModel.checkTicketExists(ticketId) { firebaseTicket ->
            if (firebaseTicket != null) {
                // El ticket existe, generar el PDF
                try {
                    val pdfFile = activityViewModel.generatePDF(context, firebaseTicket)
                    Log.d("ShoppingViewModel", "PDF generado: ${pdfFile.absolutePath}")

                    // Enviar correo
                    activityViewModel.sendEmail(context, email, pdfFile)
                    Log.d("ShoppingViewModel", "Correo enviado con éxito.")
                } catch (e: Exception) {
                    Log.e("ShoppingViewModel", "Error al generar/enviar PDF: ${e.message}")
                }
            } else {
                // El ticket no existe, registrar un error o manejar el caso
                Log.e("ShoppingViewModel", "El ticket con ID $ticketId no existe en Firebase.")
            }
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
            .collection("userMyBusinesses")
            .document(userMyBusinesses)
            .collection("userMyInventories")
            .document(userMyInventories)
            .collection("userInventory")

            //.collection("inventories")
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
                    Log.e(
                        "ShoppingViewModel",
                        "No hay suficiente stock en Firebase para incrementar."
                    )
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