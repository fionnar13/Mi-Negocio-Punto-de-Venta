package com.elfrikiamv.minegocio_puntodeventa.viewmodel.home

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
            loadTotalTicketsSold(userEmail)
            loadTotalTransactions(userEmail)
            loadTotalExpenses(userEmail)
            loadTotalProfitEarned(userEmail)
        }
    }

    // Función para cargar el número total de transacciones (tickets)
    private fun loadTotalTransactions(userEmail: String) {

        _isLoading.value = true // Mostrar indicador de carga
        Log.d(TAG, "Cargando número total de transacciones para el usuario: $userEmail")

        db.collection("users")
            .document(userEmail)
            .collection("userMyBusinesses")
            .document(DEFAULT_BUSINESS)
            .collection("userMyTickets")
            .document(DEFAULT_TICKETS)
            .collection("userTickets")
            .addSnapshotListener { snapshot, e ->
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

                Log.d(TAG, "Número total de tickets procesados: ${snapshot.documents.size}")
                _totalTransactions.value = snapshot.documents.size // Actualiza el flujo
                _isLoading.value = false // Ocultar indicador de carga
            }
    }

    // Saca el total de tickets vendidos para el usuario
    private fun loadTotalTicketsSold(userEmail: String) {

        _isLoading.value = true // Mostrar indicador de carga
        Log.d(TAG, "Cargando total de ventas de tickets para el usuario: $userEmail")

        db.collection("users")
            .document(userEmail)
            .collection("userMyBusinesses")
            .document(DEFAULT_BUSINESS)
            .collection("userMyTickets")
            .document(DEFAULT_TICKETS)
            .collection("userTickets")
            .addSnapshotListener { snapshot, e ->
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

                Log.d(TAG, "Procesando ${snapshot.documents.size} documentos de tickets.")
                val total = snapshot.documents.sumOf { doc ->
                    doc.getDouble("totalPrice") ?: 0.0
                }

                Log.d(TAG, "Total de ventas calculado: $total")
                _totalTicketsSold.value = total
                _isLoading.value = false // Ocultar indicador de carga
            }
    }

    // Saca los productos con stock bajo para el usuario
    private fun loadLowStockProducts(userEmail: String) {

        _isLoading.value = true // Mostrar indicador de carga
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
                    _isLoading.value = false // Ocultar indicador de carga en caso de error
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.e(TAG, "Snapshot vacío o nulo al cargar productos con stock bajo.")
                    _isLoading.value = false // Ocultar indicador de carga en caso de error
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
                _isLoading.value = false // Ocultar indicador de carga
            }
    }

    // Saca los productos sin stock para el usuario
    private fun loadOutStockProducts(userEmail: String) {

        _isLoading.value = true // Mostrar indicador de carga
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
                    _isLoading.value = false // Ocultar indicador de carga en caso de error
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.e(TAG, "Snapshot vacío o nulo al cargar productos sin stock.")
                    _isLoading.value = false // Ocultar indicador de carga en caso de error
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
                _isLoading.value = false // Ocultar indicador de carga
            }
    }

    // Calcula el valor total del inventario multiplicando `salePrice` por `quantity` para cada producto.
    private fun loadTotalSalePrice(userEmail: String) {

        _isLoading.value = true // Mostrar indicador de carga
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
                    _isLoading.value = false // Ocultar indicador de carga en caso de error
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.e(TAG, "Snapshot vacío o nulo al cargar valor total del inventario.")
                    _isLoading.value = false // Ocultar indicador de carga en caso de error
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
                _isLoading.value = false // Ocultar indicador de carga
            }
    }

    // Calcula la cantidad total de productos en el inventario.
    private fun loadTotalQuantity(userEmail: String) {

        _isLoading.value = true // Mostrar indicador de carga
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
                    _isLoading.value = false // Ocultar indicador de carga en caso de error
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.e(TAG, "Snapshot vacío o nulo al cargar cantidad total de productos.")
                    _isLoading.value = false // Ocultar indicador de carga en caso de error
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
                _isLoading.value = false // Ocultar indicador de carga
            }
    }

    // Calcula el total de gastos
    private fun loadTotalExpenses(userEmail: String) {
        _isLoading.value = true
        Log.d(TAG, "Cargando total de gastos para el usuario: $userEmail")

        db.collection("users")
            .document(userEmail)
            .collection("userMyBusinesses")
            .document(DEFAULT_BUSINESS)
            .collection("userMyExpenses")
            .document(DEFAULT_EXPENSES)
            .collection("userExpenses")
            .addSnapshotListener { snapshot, e ->
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

                Log.d(TAG, "Procesando ${snapshot.documents.size} documentos de gastos.")
                val total = snapshot.documents.sumOf { doc ->
                    doc.getDouble("amountExpense") ?: 0.0
                }

                Log.d(TAG, "Total de gastos calculado: $total")
                _totalExpenses.value = total
                _isLoading.value = false
            }
    }

    // Calcula el total de ganancias
    private fun loadTotalProfitEarned(userEmail: String) {
        _isLoading.value = true
        Log.d(TAG, "Cargando ganancias totales para el usuario: $userEmail")

        db.collection("users")
            .document(userEmail)
            .collection("userMyBusinesses")
            .document(DEFAULT_BUSINESS)
            .collection("userMyTickets")
            .document(DEFAULT_TICKETS)
            .collection("userTickets")
            .addSnapshotListener { snapshot, e ->
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

                Log.d(TAG, "Procesando ${snapshot.documents.size} tickets para ganancias.")

                var totalProfit = 0.0

                snapshot.documents.forEach { ticketDoc ->
                    val productsList = ticketDoc.get("products")
                    if (productsList is List<*>) {
                        val products = productsList.filterIsInstance<Map<String, Any>>()
                        products.forEach { product ->
                            val salePrice = product["salePrice"] as? Double ?: 0.0
                            val providerPrice = product["providerPrice"] as? Double ?: 0.0
                            val quantity = (product["quantity"] as? Long)?.toInt() ?: 0
                            val productProfit = (salePrice - providerPrice) * quantity
                            totalProfit += productProfit
                            Log.d(TAG, "Ganancia producto: $productProfit")
                        }
                    } else {
                        Log.e(TAG, "El campo 'products' no es una lista o está mal formado.")
                    }
                }

                Log.d(TAG, "Ganancia total calculada: $totalProfit")
                _totalProfitEarned.value = totalProfit
                _isLoading.value = false
            }
    }

}