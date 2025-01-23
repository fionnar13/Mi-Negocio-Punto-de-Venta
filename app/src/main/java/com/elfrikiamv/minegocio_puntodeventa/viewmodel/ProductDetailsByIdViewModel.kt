package com.elfrikiamv.minegocio_puntodeventa.viewmodel

// ProductDetailsByIdViewModel.kt

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProductDetailsByIdViewModel : ViewModel() {

    companion object {
        private const val TAG = "ProductDetailsByIdViewModel"
        private const val DEFAULT_BUSINESS = "defaultBusiness"
        private const val DEFAULT_INVENTORY = "defaultInventory"
    }

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _products = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val products: StateFlow<List<Map<String, Any>>> = _products

    // Nuevo campo para el estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Busca los detalles de los productos por su lista de IDs.
    fun loadProductDetailsByIds(productIds: List<String>) {

        _isLoading.value = true
        val userEmail = auth.currentUser?.email ?: return
        if (productIds.isEmpty()) {
            Log.d(TAG, "No hay IDs para buscar productos.")
            _isLoading.value = false
            return
        }

        db.collection("users")
            .document(userEmail)
            .collection("userMyBusinesses")
            .document(DEFAULT_BUSINESS)
            .collection("userMyInventories")
            .document(DEFAULT_INVENTORY)
            .collection("userInventory")
            .whereIn("id", productIds) // Realiza la búsqueda por IDs
            .get()
            .addOnSuccessListener { snapshot ->
                val products = snapshot.documents.mapNotNull { doc ->
                    mapOf(
                        "id" to doc.id,
                        "name" to doc.getString("name").orEmpty(),
                        "barcode" to doc.getString("barcode").orEmpty(),
                        "quantity" to ((doc["quantity"] as? Number)?.toInt() ?: 0)
                    )
                }
                _products.value = products
                _isLoading.value = false
                Log.d(TAG, "Productos cargados: $products")
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                Log.e(TAG, "Error al cargar detalles de productos: ${e.message}")
            }
    }
}