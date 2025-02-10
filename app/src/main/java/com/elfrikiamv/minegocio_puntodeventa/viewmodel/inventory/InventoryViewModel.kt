package com.elfrikiamv.minegocio_puntodeventa.viewmodel.inventory

//InventoryViewModel.kt

import android.util.Log
import androidx.lifecycle.ViewModel
import com.elfrikiamv.minegocio_puntodeventa.model.inventory.ProductFirebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class InventoryViewModel : ViewModel() {

    // Constantes para las colecciones y documentos por defecto
    companion object {
        private const val TAG = "InventoryViewModel"
        private const val DEFAULT_BUSINESS = "defaultBusiness"
        private const val DEFAULT_INVENTORIES = "defaultInventory"
    }

    // Firebase Authentication para obtener el usuario actual
    private val auth = FirebaseAuth.getInstance()

    // Firestore para manejar la base de datos
    private val db = FirebaseFirestore.getInstance()

    // StateFlow que almacenará los productos del inventario
    private val _products = MutableStateFlow<List<ProductFirebase>>(emptyList())
    val products: StateFlow<List<ProductFirebase>> = _products

    // Nuevo campo para el estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Cargar los productos al iniciar el ViewModel
    init {
        val userEmail = auth.currentUser?.email
        if (userEmail == null) {
            Log.e(TAG, "Usuario no autenticado. No se pueden cargar los datos.")
        } else {
            Log.d(TAG, "Usuario autenticado: $userEmail")
            loadProducts(userEmail)
        }
    }

    // Función para cargar los productos desde Firestore
    private fun loadProducts(userEmail: String) {

        _isLoading.value = true
        db.collection("users")
            .document(userEmail)
            .collection("userMyBusinesses")
            .document(DEFAULT_BUSINESS)
            .collection("userMyInventories")
            .document(DEFAULT_INVENTORIES)
            .collection("userInventory")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) {
                    // Manejar error
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                // Convertir los documentos a objetos Product
                val productsList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(ProductFirebase::class.java)?.copy(id = doc.id)
                }
                _products.value = productsList
                _isLoading.value = false
            }
    }

    // Función para agregar un producto a Firestore
    fun addOrUpdateProduct(
        id: String?, // ID del producto (null para nuevos productos)
        name: String,
        quantity: Int,
        barcode: String,
        providerPrice: Double,
        salePrice: Double,
        description: String
    ) {
        val userEmail = auth.currentUser?.email ?: return

        // Si no se proporciona un ID, generar uno nuevo
        val productId = id ?: System.currentTimeMillis().toString()

        val product = ProductFirebase(
            id = productId,
            name = name,
            quantity = quantity,
            barcode = barcode,
            providerPrice = providerPrice,
            salePrice = salePrice,
            description = description
        )

        // Guardar o actualizar el producto en Firestore
        db.collection("users")
            .document(userEmail)
            .collection("userMyBusinesses")
            .document(DEFAULT_BUSINESS)
            .collection("userMyInventories")
            .document(DEFAULT_INVENTORIES)
            .collection("userInventory")
            .document(productId)
            .set(product)
    }

    //verificar si el producto existe en Firestore
    fun checkProductExists(barcode: String, callback: (ProductFirebase?) -> Unit) {
        val userEmail = auth.currentUser?.email ?: return

        db.collection("users")
            .document(userEmail)
            .collection("userMyBusinesses")
            .document(DEFAULT_BUSINESS)
            .collection("userMyInventories")
            .document(DEFAULT_INVENTORIES)
            .collection("userInventory")
            .whereEqualTo("barcode", barcode)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    callback(null) // Producto no encontrado
                } else {
                    val product = documents.documents.first().toObject(ProductFirebase::class.java)
                    callback(product)
                }
            }
            .addOnFailureListener {
                callback(null) // En caso de error
            }
    }

}