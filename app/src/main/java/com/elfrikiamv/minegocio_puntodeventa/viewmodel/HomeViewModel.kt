package com.elfrikiamv.minegocio_puntodeventa.viewmodel

// HomeViewModel.kt

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
        private const val DEFAULT_BUSINESS = "defaultBusiness"
        private const val DEFAULT_INVENTORY = "defaultInventory"
        private const val LOW_STOCK_THRESHOLD = 6 // Umbral de stock bajo
        private const val OUT_STOCK_THRESHOLD = 0 // Umbral de stock bajo
    }

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _totalQuantity = MutableStateFlow(0)
    val totalQuantity: StateFlow<Int> = _totalQuantity

    private val _totalSalePrice = MutableStateFlow(0.0)
    val totalSalePrice: StateFlow<Double> = _totalSalePrice

    private val _lowStockProducts = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val lowStockProducts: StateFlow<List<Map<String, Any>>> = _lowStockProducts

    private val _outStockProducts = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val outStockProducts: StateFlow<List<Map<String, Any>>> = _outStockProducts

    init {
        val userEmail = auth.currentUser?.email
        if (userEmail == null) {
            Log.e(TAG, "Usuario no autenticado. No se pueden cargar los datos.")
        } else {
            Log.d(TAG, "Usuario autenticado: $userEmail")
            loadTotalQuantity(userEmail)
            loadTotalSalePrice(userEmail)
            loadLowStockProducts(userEmail)
            loadOutStockProducts(userEmail)
        }
    }

    private fun loadLowStockProducts(userEmail: String) {
        Log.d(TAG, "Cargando productos con stock bajo para el usuario: $userEmail")

        db.collection("users")
            .document(userEmail)
            .collection("userMyBusinesses")
            .document(DEFAULT_BUSINESS)
            .collection("userMyInventories")
            .document(DEFAULT_INVENTORY)
            .collection("userInventory")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Error al escuchar cambios en Firestore: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.e(TAG, "Snapshot vacío o nulo al cargar productos con stock bajo.")
                    return@addSnapshotListener
                }

                Log.d(TAG, "Procesando ${snapshot.documents.size} documentos para stock bajo.")
                val lowStockList = snapshot.documents.filter { doc ->
                    val quantity = doc.getLong("quantity")?.toInt() ?: 0
                    quantity in 1..LOW_STOCK_THRESHOLD
                }.mapNotNull { doc ->

                    // Validar los datos y construir el mapa de producto
                    val product = mapOf(
                        "id" to doc.id
                    )
                    Log.d(TAG, "Producto con stock bajo: $product")
                    product
                }

                Log.d(TAG, "Productos con stock bajo encontrados: ${lowStockList.size}")
                _lowStockProducts.value = lowStockList
            }
    }

    private fun loadOutStockProducts(userEmail: String) {
        Log.d(TAG, "Cargando productos sin stock para el usuario: $userEmail")

        db.collection("users")
            .document(userEmail)
            .collection("userMyBusinesses")
            .document(DEFAULT_BUSINESS)
            .collection("userMyInventories")
            .document(DEFAULT_INVENTORY)
            .collection("userInventory")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Error al escuchar cambios en Firestore: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.e(TAG, "Snapshot vacío o nulo al cargar productos sin stock.")
                    return@addSnapshotListener
                }

                Log.d(TAG, "Procesando ${snapshot.documents.size} documentos para sin stock.")
                val outStockList = snapshot.documents.filter { doc ->
                    val quantity = doc.getLong("quantity")?.toInt() ?: 0
                    quantity <= OUT_STOCK_THRESHOLD
                }.mapNotNull { doc ->

                    // Validar los datos y construir el mapa de producto
                    val product = mapOf(
                        "id" to doc.id
                    )
                    Log.d(TAG, "Producto sin stock: $product")
                    product
                }

                Log.d(TAG, "Productos sin stock encontrados: ${outStockList.size}")
                _outStockProducts.value = outStockList
            }
    }

    // Calcula el valor total del inventario multiplicando `salePrice` por `quantity` para cada producto.
    private fun loadTotalSalePrice(userEmail: String) {
        Log.d(TAG, "Cargando valor total del inventario para el usuario: $userEmail")

        db.collection("users")
            .document(userEmail)
            .collection("userMyBusinesses")
            .document(DEFAULT_BUSINESS)
            .collection("userMyInventories")
            .document(DEFAULT_INVENTORY)
            .collection("userInventory")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Error al escuchar cambios en Firestore: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.e(TAG, "Snapshot vacío o nulo al cargar valor total del inventario.")
                    return@addSnapshotListener
                }

                Log.d(TAG, "Procesando ${snapshot.documents.size} documentos para valor total.")
                val total = snapshot.documents.sumOf { doc ->
                    val quantity = doc.getLong("quantity")?.toInt() ?: 0
                    val salePrice = doc.getDouble("salePrice") ?: 0.0
                    val productValue = quantity * salePrice
                    Log.d(
                        TAG,
                        "Producto procesado - ID: ${doc.id}, quantity: $quantity, salePrice: $salePrice, value: $productValue"
                    )
                    productValue
                }

                Log.d(TAG, "Valor total del inventario calculado: $total")
                _totalSalePrice.value = total
            }
    }

    // Calcula la cantidad total de productos en el inventario.
    private fun loadTotalQuantity(userEmail: String) {
        Log.d(TAG, "Cargando cantidad total de productos para el usuario: $userEmail")

        db.collection("users")
            .document(userEmail)
            .collection("userMyBusinesses")
            .document(DEFAULT_BUSINESS)
            .collection("userMyInventories")
            .document(DEFAULT_INVENTORY)
            .collection("userInventory")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Error al escuchar cambios en Firestore: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.e(TAG, "Snapshot vacío o nulo al cargar cantidad total de productos.")
                    return@addSnapshotListener
                }

                Log.d(TAG, "Procesando ${snapshot.documents.size} documentos para cantidad total.")
                val total = snapshot.documents.sumOf { doc ->
                    val quantity = doc.getLong("quantity")?.toInt() ?: 0
                    Log.d(TAG, "Producto procesado - ID: ${doc.id}, quantity: $quantity")
                    quantity
                }

                Log.d(TAG, "Cantidad total calculada: $total")
                _totalQuantity.value = total
            }
    }
}