package com.elfrikiamv.minegocio_puntodeventa.viewmodel.home.bestSellers

// BestSellersProductsViewModel.kt

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BestSellersProductsViewModel : ViewModel() {

    companion object {
        private const val TAG = "BestSellersViewModel"
        private const val DEFAULT_BUSINESS = "defaultBusiness"
        private const val DEFAULT_TICKETS = "defaultTickets"
        private const val BEST_SELLERS_LIMIT = 7
    }

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Nuevo campo para los productos más vendidos
    private val _bestSellers = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val bestSellers: StateFlow<List<Map<String, Any>>> = _bestSellers

    // Nuevo campo para el estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var ticketsListener: ListenerRegistration? = null

    init {
        val userEmail = auth.currentUser?.email
        if (userEmail == null) {
            Log.e(TAG, "Usuario no autenticado. No se pueden cargar los datos.")
        } else {
            Log.d(TAG, "Usuario autenticado: $userEmail")
            startListeningToBestSellers(userEmail)
        }
    }

    // Función para cargar los productos más vendidos
    private fun startListeningToBestSellers(userEmail: String) {
        _isLoading.value = true

        ticketsListener = db.collection("users")
            .document(userEmail)
            .collection("userMyBusinesses")
            .document(DEFAULT_BUSINESS)
            .collection("userMyTickets")
            .document(DEFAULT_TICKETS)
            .collection("userTickets")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error al escuchar cambios en tickets: ${error.message}")
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.e(TAG, "Snapshot nulo en tickets.")
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                Log.d(TAG, "Procesando ${snapshot.documents.size} tickets.")
                processTicketDocuments(snapshot.documents)
            }
    }

    private fun processTicketDocuments(documents: List<DocumentSnapshot>) {
        val productSales = mutableMapOf<String, MutableMap<String, Any>>()

        documents.forEach { ticket ->
            val products = (ticket.get("products") as? List<*>)
                ?.filterIsInstance<Map<String, Any>>() ?: emptyList()

            products.forEach { product ->
                val productName = product["name"] as? String ?: "Desconocido"
                val barcode = product["barcode"] as? String ?: "Sin código"
                val quantity = (product["quantity"] as? Long)?.toInt() ?: 0

                val entry = productSales.getOrPut(productName) {
                    mutableMapOf(
                        "name" to productName,
                        "barcode" to barcode,
                        "quantitySold" to 0
                    )
                }

                entry["quantitySold"] = (entry["quantitySold"] as Int) + quantity
            }
        }

        val sortedProducts = productSales.values
            .sortedByDescending { it["quantitySold"] as Int }
            .take(BEST_SELLERS_LIMIT)

        Log.d(TAG, "Productos más vendidos: $sortedProducts")
        _bestSellers.value = sortedProducts
        _isLoading.value = false
    }

    override fun onCleared() {
        super.onCleared()
        ticketsListener?.remove()
        Log.d(TAG, "Listener de tickets eliminado en onCleared()")
    }
}