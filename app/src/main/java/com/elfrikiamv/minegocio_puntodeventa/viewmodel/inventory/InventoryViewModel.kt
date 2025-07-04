package com.elfrikiamv.minegocio_puntodeventa.viewmodel.inventory

// InventoryViewModel.kt

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elfrikiamv.minegocio_puntodeventa.model.inventory.ProductFirebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

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

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val filteredProducts: StateFlow<List<ProductFirebase>> = _searchQuery
        .combine(_products) { query, products ->
            val result = if (query.isBlank()) products
            else products.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.barcode.contains(query, ignoreCase = true)
            }
            // Ordenar también los resultados filtrados
            result.sortedBy { it.name.lowercase() }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Nuevo campo para el estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _visibleItemCount = MutableStateFlow(50)
    val visibleItemCount: StateFlow<Int> = _visibleItemCount

    private val _visibleSearchItemCount = MutableStateFlow(50)
    val visibleSearchItemCount: StateFlow<Int> = _visibleSearchItemCount

    // Agregado para guardar referencia al listener
    private var inventoryListener: ListenerRegistration? = null

    // Estado del Bottom Sheet
    val showBottomSheet = mutableStateOf(false)

    // Campo para los detalles del producto
    val productDetails = mutableStateOf<ProductFirebase?>(null)

    // campo para el mensaje de error
    val errorMessage = mutableStateOf("")

    // Cargar los productos al iniciar el ViewModel
    init {
        _isLoading.value = true
        val userEmail = auth.currentUser?.email
        if (userEmail == null) {
            Log.e(TAG, "Usuario no autenticado. No se pueden cargar los datos.")
            _isLoading.value = false
        } else {
            Log.d(TAG, "Usuario autenticado: $userEmail")
            _isLoading.value = false
            startListeningToProducts(userEmail)
        }
    }

    // Función para cargar los productos desde Firestore
    private fun startListeningToProducts(userEmail: String) {
        _isLoading.value = true

        inventoryListener = db.collection("users")
            .document(userEmail)
            .collection("userMyBusinesses")
            .document(DEFAULT_BUSINESS)
            .collection("userMyInventories")
            .document(DEFAULT_INVENTORIES)
            .collection("userInventory")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) {
                    Log.e(TAG, "Error al escuchar cambios en inventario: ${e?.message}")
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                // Convertir StateFlow actual en Map mutable para modificar por cambio incremental
                val currentProducts = _products.value.associateBy { it.id }.toMutableMap()

                // Procesar cambios incrementales
                for (change in snapshot.documentChanges) {
                    val product = change.document.toObject(ProductFirebase::class.java)
                        .copy(id = change.document.id)
                    when (change.type) {
                        DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                            currentProducts[product.id] = product
                        }

                        DocumentChange.Type.REMOVED -> {
                            currentProducts.remove(product.id)
                        }
                    }
                }

                // Actualizar el StateFlow ordenado alfabéticamente
                _products.value = currentProducts.values.sortedBy { it.name.lowercase() }
                _isLoading.value = false
            }
    }

    // Cancelar listener en onCleared
    override fun onCleared() {
        super.onCleared()
        inventoryListener?.remove()
        Log.d(TAG, "Listener de inventario eliminado en onCleared()")
    }

    val groupedProducts: StateFlow<Map<Char, List<ProductFirebase>>> = products
        .map { productList ->
            productList.groupBy { product ->
                product.name.firstOrNull()?.uppercaseChar() ?: '#'
            }.toSortedMap()
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    fun loadMoreItems(totalItems: Int) {
        if (_visibleItemCount.value < totalItems) {
            _visibleItemCount.value = (_visibleItemCount.value + 50).coerceAtMost(totalItems)
        }
    }

    fun loadMoreSearchItems(totalItems: Int) {
        if (_visibleSearchItemCount.value < totalItems) {
            _visibleSearchItemCount.value =
                (_visibleSearchItemCount.value + 50).coerceAtMost(totalItems)
        }
    }

    private fun resetVisibleSearchItemCount() {
        _visibleSearchItemCount.value = 50
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        resetVisibleSearchItemCount()
    }

}