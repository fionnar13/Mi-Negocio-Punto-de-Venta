package com.elfrikiamv.minegocio_puntodeventa.viewmodel.shopping

// ShoppingViewModel.kt

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.elfrikiamv.minegocio_puntodeventa.database.AppDatabase
import com.elfrikiamv.minegocio_puntodeventa.model.inventory.ProductFirebase
import com.elfrikiamv.minegocio_puntodeventa.model.shopping.ProductEntity
import com.elfrikiamv.minegocio_puntodeventa.model.shopping.TicketEntity
import com.elfrikiamv.minegocio_puntodeventa.model.shopping.TicketFirebase
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.activity.ActivityViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
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
    val cartProducts = _cartProducts.asStateFlow()

    // StateFlow para el total de la compra
    private val _totalPurchase = MutableStateFlow(0.0)
    val totalPurchase = _totalPurchase.asStateFlow()

    private val userMyBusinesses = "defaultBusiness"
    private val userMyInventories = "defaultInventory"

    private val userMyTickets = "defaultTickets"

    // Estado del diálogo de correo electrónico
    val showEmailDialog = mutableStateOf(false)

    // campo para el estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    val amountReceived = mutableStateOf("")

    init {
        viewModelScope.launch {
            productDao.getAllProducts()
                .map { entities ->
                    entities.map {
                        ProductFirebase(
                            barcode = it.barcode,
                            name = it.name,
                            quantity = it.quantity,
                            salePrice = it.salePrice,
                            providerPrice = it.providerPrice
                        )
                    }
                }
                .collect { firebaseProducts ->
                    _cartProducts.value = firebaseProducts
                    _totalPurchase.value = firebaseProducts.sumOf { it.quantity * it.salePrice }
                }
        }
    }

    fun onConfirmPurchaseClicked(
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
        onEmptyCart: (String) -> Unit
    ) {
        val amount = amountReceived.value.toDoubleOrNull()

        if (isLoading.value) {
            onError("Ya se está procesando una compra.")
            return
        } else if (cartProducts.value.isEmpty()) {
            onEmptyCart("El carrito está vacío.")
            return
        } else if (amount == null || amount < totalPurchase.value) {
            onError("El pago recibido debe ser mayor o igual al total de la compra.")
            return
        } else {
            // Validation passed
            showEmailDialog.value = true
            onSuccess("Ticket guardado")
        }

    }

    fun addToCart(product: ProductFirebase) {
        val productEntity = ProductEntity(
            barcode = product.barcode,
            name = product.name,
            quantity = product.quantity,
            salePrice = product.salePrice,
            providerPrice = product.providerPrice
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
        }
    }

    fun deleteCartProduct(barcode: String) {
        viewModelScope.launch {
            val product = productDao.getProductByBarcode(barcode)
            if (product != null) {
                productDao.deleteProduct(product)
                updateFirebaseQuantity(barcode, product.quantity) // Revertir en Firebase
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
        amountReceived: Double,
        context: Context,
        email: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            // Obtener información de fecha y hora
            val currentDateTimeMillis = System.currentTimeMillis()
            val currentDateTimeTicketId = System.currentTimeMillis()
            val currentDateTime = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

            val dateFormatted = dateFormat.format(currentDateTime.time)
            val timeFormatted = timeFormat.format(currentDateTime.time)

            // Obtener la lista actual de productos del DAO
            val products = productDao.getAllProducts().first()
            if (products.isEmpty()) {
                Log.e("ShoppingViewModel", "No hay productos en el carrito.")
                _isLoading.value = false
                return@launch
            }

            val totalProducts = products.sumOf { it.quantity }
            val totalPrice = products.sumOf { it.salePrice * it.quantity }
            val change = amountReceived - totalPrice

            // Crear el ticket
            val ticket = TicketEntity(
                ticketId = currentDateTimeTicketId.toString(),
                date = dateFormatted,
                time = timeFormatted,
                products = products,
                totalPrice = totalPrice,
                totalProducts = totalProducts,
                amountReceived = amountReceived,
                change = change,
                timestamp = currentDateTimeMillis
            )

            // Guardar en Room (temporalmente)
            ticketDao.insertTicket(ticket)
            Log.d("ShoppingViewModel", "Ticket guardado en Room: $ticket")

            // Limpiar carrito de Room
            productDao.deleteAllProducts()

            // Subir a Firebase
            uploadTicketToFirebase(ticket, context, email)
        }
    }

    private fun uploadTicketToFirebase(
        ticketEntity: TicketEntity,
        context: Context,
        email: String
    ) {
        val userEmail = auth.currentUser?.email ?: run {
            _isLoading.value = false
            return
        }

        // Convertir productos a Map<String, Any>
        val productsList = ticketEntity.products.map { product ->
            mapOf(
                "barcode" to product.barcode,
                "name" to product.name,
                "quantity" to product.quantity,
                "providerPrice" to product.providerPrice,
                "salePrice" to product.salePrice
            )
        }

        // Convertir TicketEntity → TicketFirebase con Timestamp
        val ticketFirebase = TicketFirebase(
            ticketId = ticketEntity.ticketId,
            products = productsList,
            totalPrice = ticketEntity.totalPrice,
            totalProducts = ticketEntity.totalProducts,
            amountReceived = ticketEntity.amountReceived,
            change = ticketEntity.change,
            date = ticketEntity.date,
            time = ticketEntity.time,
            timestamp = Timestamp(Date(ticketEntity.timestamp))
        )

        db.collection("users")
            .document(userEmail)
            .collection("userMyBusinesses")
            .document(userMyBusinesses)
            .collection("userMyTickets")
            .document(userMyTickets)
            .collection("userTickets")
            .document(ticketFirebase.ticketId)
            .set(ticketFirebase)
            .addOnSuccessListener {
                viewModelScope.launch {
                    // compartir ticket
                    sharePDF(ticketEntity.ticketId, context, email)
                    // Eliminar ticket de Room
                    ticketDao.deleteTicketById(ticketEntity.ticketId)
                    _isLoading.value = false
                }
            }
            .addOnFailureListener { e ->
                Log.e("ShoppingViewModel", "Error al subir el ticket a Firebase: $e")
                _isLoading.value = false
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
            } else {
                Log.e("ShoppingViewModel", "No se puede disminuir más la cantidad.")
            }
        }
    }

}
