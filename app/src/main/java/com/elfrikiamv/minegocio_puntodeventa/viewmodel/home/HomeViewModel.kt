package com.elfrikiamv.minegocio_puntodeventa.viewmodel.home

// HomeViewModel.kt

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
        private const val DEFAULT_BUSINESS = "defaultBusiness"
        private const val DEFAULT_INVENTORY = "defaultInventory"
        private const val DEFAULT_TICKETS = "defaultTickets"
        private const val DEFAULT_EXPENSES = "defaultExpenses"
        private const val LOW_STOCK_THRESHOLD = 7 // Umbral de stock bajo
        private const val OUT_STOCK_THRESHOLD = 0 // Umbral de sin stock
    }

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Nuevo campo para la cantidad total de productos en el inventario
    private val _totalQuantity = MutableStateFlow(0)
    val totalQuantity: StateFlow<Int> = _totalQuantity

    // Nuevo campo para el valor total del inventario
    private val _totalSalePrice = MutableStateFlow(0.0)
    val totalSalePrice: StateFlow<Double> = _totalSalePrice

    // Nuevo campo para los productos con stock bajo
    private val _lowStockProducts = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val lowStockProducts: StateFlow<List<Map<String, Any>>> = _lowStockProducts

    // Nuevo campo para los productos sin stock
    private val _outStockProducts = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val outStockProducts: StateFlow<List<Map<String, Any>>> = _outStockProducts

    // Nuevo campo para el total de tickets vendidos
    private val _totalTicketsSold = MutableStateFlow(0.0)
    val totalTicketsSold: StateFlow<Double> = _totalTicketsSold

    // Nuevo campo para el total de transacciones (número de tickets)
    private val _totalTransactions = MutableStateFlow(0)
    val totalTransactions: StateFlow<Int> = _totalTransactions

    // Nuevo campo para el total de gastos
    private val _totalExpenses = MutableStateFlow(0.0)
    val totalExpenses: StateFlow<Double> = _totalExpenses

    // Nuevo campo para el total de ganancias
    private val _totalProfitEarned = MutableStateFlow(0.0)
    val totalProfitEarned: StateFlow<Double> = _totalProfitEarned

    // Nuevo campo para el estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Guardar referencias de los listeners para cancelarlos después
    private val listenerRegistrations = mutableListOf<ListenerRegistration>()

    init {
        val userEmail = auth.currentUser?.email
        if (userEmail == null) {
            Log.e(TAG, "Usuario no autenticado. No se pueden cargar los datos.")
        } else {
            Log.d(TAG, "Usuario autenticado: $userEmail")
            startListeners(userEmail)
        }
    }

    private fun startListeners(userEmail: String) {
        loadTotalQuantity(userEmail)
        loadTotalSalePrice(userEmail)
        loadLowStockProducts(userEmail)
        loadOutStockProducts(userEmail)
        loadTotalTicketsSold(userEmail)
        loadTotalTransactions(userEmail)
        loadTotalExpenses(userEmail)
        loadTotalProfitEarned(userEmail)
    }

    // Función para cargar el número total de transacciones (tickets)
    private fun loadTotalTransactions(userEmail: String) {
        _isLoading.value = true
        val listener = db.collection("users").document(userEmail)
            .collection("userMyBusinesses").document(DEFAULT_BUSINESS)
            .collection("userMyTickets").document(DEFAULT_TICKETS)
            .collection("userTickets")
            .addSnapshotListener { snapshot, e ->
                //if (e != null) return@addSnapshotListener
                if (e != null) {
                    Log.e(TAG, "Error al escuchar cambios en la colección de tickets: ${e.message}")
                    _isLoading.value = false // Ocultar indicador de carga en caso de error
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.e(TAG, "Snapshot vacío o nulo al cargar el número total de transacciones.")
                    _isLoading.value = false // Ocultar indicador de carga en caso de error
                    return@addSnapshotListener
                }
                _totalTransactions.value = snapshot.size()
                _isLoading.value = false
            }
        listenerRegistrations.add(listener)
    }

    // SAca el total de ventas sumando `totalPrice` de cada ticket
    private fun loadTotalTicketsSold(userEmail: String) {
        _isLoading.value = true
        val listener = db.collection("users").document(userEmail)
            .collection("userMyBusinesses").document(DEFAULT_BUSINESS)
            .collection("userMyTickets").document(DEFAULT_TICKETS)
            .collection("userTickets")
            .addSnapshotListener { snapshot, e ->
                //if (e != null) return@addSnapshotListener
                if (e != null) {
                    Log.e(TAG, "Error al escuchar cambios en la colección de tickets: ${e.message}")
                    _isLoading.value = false // Ocultar indicador de carga en caso de error
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.e(TAG, "Snapshot vacío o nulo al cargar las ventas de tickets.")
                    _isLoading.value = false // Ocultar indicador de carga en caso de error
                    return@addSnapshotListener
                }
                val total = snapshot.documents.sumOf { it.getDouble("totalPrice") ?: 0.0 }
                _totalTicketsSold.value = total
                _isLoading.value = false // Ocultar indicador de carga
            }
        listenerRegistrations.add(listener)
    }

    // Calcula el total de gastos
    private fun loadTotalExpenses(userEmail: String) {
        _isLoading.value = true
        val listener = db.collection("users").document(userEmail)
            .collection("userMyBusinesses").document(DEFAULT_BUSINESS)
            .collection("userMyExpenses").document(DEFAULT_EXPENSES)
            .collection("userExpenses")
            .addSnapshotListener { snapshot, e ->
                //if (e != null) return@addSnapshotListener
                if (e != null) {
                    Log.e(TAG, "Error al escuchar cambios en la colección de gastos: ${e.message}")
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.e(TAG, "Snapshot vacío o nulo al cargar los gastos.")
                    _isLoading.value = false
                    return@addSnapshotListener
                }
                val total =
                    snapshot.documents.sumOf { it.getDouble("amountExpense") ?: 0.0 }
                _totalExpenses.value = total
                _isLoading.value = false
            }
        listenerRegistrations.add(listener)
    }

    // Calcula el total de ganancias
    private fun loadTotalProfitEarned(userEmail: String) {
        _isLoading.value = true
        val listener = db.collection("users").document(userEmail)
            .collection("userMyBusinesses").document(DEFAULT_BUSINESS)
            .collection("userMyTickets").document(DEFAULT_TICKETS)
            .collection("userTickets")
            .addSnapshotListener { snapshot, e ->
                //if (e != null) return@addSnapshotListener
                if (e != null) {
                    Log.e(TAG, "Error al escuchar cambios en la colección de tickets: ${e.message}")
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.e(TAG, "Snapshot vacío o nulo al cargar las ganancias.")
                    _isLoading.value = false
                    return@addSnapshotListener
                }
                var totalProfit = 0.0
                snapshot.documents.forEach { doc ->
                    (doc.get("products") as? List<*>)?.filterIsInstance<Map<String, Any>>()
                        ?.forEach { product ->
                            val sale = product["salePrice"] as? Double ?: 0.0
                            val provider = product["providerPrice"] as? Double ?: 0.0
                            val quantity = (product["quantity"] as? Long)?.toInt() ?: 0
                            totalProfit += (sale - provider) * quantity
                        }
                }
                _totalProfitEarned.value = totalProfit
                _isLoading.value = false
            }
        listenerRegistrations.add(listener)
    }

    // Saca los productos con stock bajo para el usuario
    private fun loadLowStockProducts(userEmail: String) {
        _isLoading.value = true
        val listener = db.collection("users").document(userEmail)
            .collection("userMyBusinesses").document(DEFAULT_BUSINESS)
            .collection("userMyInventories").document(DEFAULT_INVENTORY)
            .collection("userInventory")
            .addSnapshotListener { snapshot, e ->
                //if (e != null) return@addSnapshotListener
                if (e != null) {
                    Log.e(TAG, "Error al escuchar cambios en Firestore: ${e.message}")
                    _isLoading.value = false // Ocultar indicador de carga en caso de error
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.e(TAG, "Snapshot vacío o nulo al cargar productos con stock bajo.")
                    _isLoading.value = false // Ocultar indicador de carga en caso de error
                    return@addSnapshotListener
                }
                val lowStockList = snapshot.documents.filter {
                    val quantity = it.getLong("quantity")?.toInt() ?: 0
                    quantity in 1..LOW_STOCK_THRESHOLD
                }.map { mapOf("id" to it.id) }
                _lowStockProducts.value = lowStockList
                _isLoading.value = false // Ocultar indicador de carga
            }
        listenerRegistrations.add(listener)
    }

    // Saca los productos sin stock para el usuario
    private fun loadOutStockProducts(userEmail: String) {
        _isLoading.value = true
        val listener = db.collection("users").document(userEmail)
            .collection("userMyBusinesses").document(DEFAULT_BUSINESS)
            .collection("userMyInventories").document(DEFAULT_INVENTORY)
            .collection("userInventory")
            .addSnapshotListener { snapshot, e ->
                //if (e != null) return@addSnapshotListener
                if (e != null) {
                    Log.e(TAG, "Error al escuchar cambios en Firestore: ${e.message}")
                    _isLoading.value = false // Ocultar indicador de carga en caso de error
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.e(TAG, "Snapshot vacío o nulo al cargar productos sin stock.")
                    _isLoading.value = false // Ocultar indicador de carga en caso de error
                    return@addSnapshotListener
                }
                val outStockList = snapshot.documents.filter {
                    (it.getLong("quantity") ?: 0) <= OUT_STOCK_THRESHOLD
                }.map { mapOf("id" to it.id) }
                _outStockProducts.value = outStockList
                _isLoading.value = false // Ocultar indicador de carga
            }
        listenerRegistrations.add(listener)
    }

    // Calcula el valor total del inventario multiplicando `salePrice` por `quantity` para cada producto.
    private fun loadTotalSalePrice(userEmail: String) {
        _isLoading.value = true
        val listener = db.collection("users").document(userEmail)
            .collection("userMyBusinesses").document(DEFAULT_BUSINESS)
            .collection("userMyInventories").document(DEFAULT_INVENTORY)
            .collection("userInventory")
            .addSnapshotListener { snapshot, e ->
                //if (e != null) return@addSnapshotListener
                if (e != null) {
                    Log.e(TAG, "Error al escuchar cambios en Firestore: ${e.message}")
                    _isLoading.value = false // Ocultar indicador de carga en caso de error
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.e(TAG, "Snapshot vacío o nulo al cargar valor total del inventario.")
                    _isLoading.value = false // Ocultar indicador de carga en caso de error
                    return@addSnapshotListener
                }
                val total = snapshot.documents.sumOf {
                    (it.getLong("quantity")?.toInt() ?: 0) * (it.getDouble("salePrice") ?: 0.0)
                }
                _totalSalePrice.value = total
                _isLoading.value = false // Ocultar indicador de carga
            }
        listenerRegistrations.add(listener)
    }

    // Calcula la cantidad total de productos en el inventario.
    private fun loadTotalQuantity(userEmail: String) {
        _isLoading.value = true
        val listener = db.collection("users").document(userEmail)
            .collection("userMyBusinesses").document(DEFAULT_BUSINESS)
            .collection("userMyInventories").document(DEFAULT_INVENTORY)
            .collection("userInventory")
            .addSnapshotListener { snapshot, e ->
                //if (e != null) return@addSnapshotListener
                if (e != null) {
                    Log.e(TAG, "Error al escuchar cambios en Firestore: ${e.message}")
                    _isLoading.value = false // Ocultar indicador de carga en caso de error
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.e(TAG, "Snapshot vacío o nulo al cargar cantidad total de productos.")
                    _isLoading.value = false // Ocultar indicador de carga en caso de error
                    return@addSnapshotListener
                }
                val total = snapshot.documents.sumOf { it.getLong("quantity")?.toInt() ?: 0 }
                _totalQuantity.value = total
                _isLoading.value = false // Ocultar indicador de carga
            }
        listenerRegistrations.add(listener)
    }

    // Cancelamos todos los listeners cuando se destruye el ViewModel
    override fun onCleared() {
        super.onCleared()
        listenerRegistrations.forEach { it.remove() }
        listenerRegistrations.clear()
        Log.d(TAG, "Todos los listeners de HomeViewModel eliminados en onCleared()")
    }
}