package com.elfrikiamv.minegocio_puntodeventa.viewmodel.home

// ProductDetailsByIdViewModel.kt

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
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

    // Guardar referencias a los listeners para cancelarlos cuando sea necesario
    private val productListeners = mutableListOf<ListenerRegistration>()

    // Busca los detalles de los productos por su lista de IDs.
    fun loadProductDetailsByIds(productIds: List<String>) {
        _isLoading.value = true
        val userEmail = auth.currentUser?.email ?: return

        if (productIds.isEmpty()) {
            Log.d(TAG, "No hay IDs para buscar productos.")
            _products.value = emptyList()
            _isLoading.value = false
            return
        }

        // Cancelar listeners previos antes de agregar nuevos
        productListeners.forEach { it.remove() }
        productListeners.clear()

        val newProducts = mutableMapOf<String, Map<String, Any>>()

        productIds.forEach { id ->
            val listener = db.collection("users")
                .document(userEmail)
                .collection("userMyBusinesses")
                .document(DEFAULT_BUSINESS)
                .collection("userMyInventories")
                .document(DEFAULT_INVENTORY)
                .collection("userInventory")
                .document(id)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error al escuchar cambios de producto $id: ${error.message}")
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val product = mapOf(
                            "id" to snapshot.id,
                            "name" to snapshot.getString("name").orEmpty(),
                            "barcode" to snapshot.getString("barcode").orEmpty(),
                            "quantity" to ((snapshot["quantity"] as? Number)?.toInt() ?: 0)
                        )

                        newProducts[id] = product
                    } else {
                        // Si se eliminó, lo quitamos del mapa
                        newProducts.remove(id)
                    }

                    // Actualizamos StateFlow con la lista actualizada
                    _products.value = newProducts.values.toList()
                    _isLoading.value = false
                }

            productListeners.add(listener)
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Quitar listeners al destruir ViewModel
        productListeners.forEach { it.remove() }
        productListeners.clear()
    }
}