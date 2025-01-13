package com.elfrikiamv.minegocio_puntodeventa.viewmodel

// BestSellersViewModel.kt

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BestSellersViewModel : ViewModel() {

    companion object {
        private const val TAG = "BestSellersViewModel"
        private const val DEFAULT_BUSINESS = "defaultBusiness"
        private const val DEFAULT_TICKETS = "defaultTickets"
        private const val BEST_SELLERS_LIMIT = 6
    }

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Nuevo campo para los productos más vendidos
    private val _bestSellers = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val bestSellers: StateFlow<List<Map<String, Any>>> = _bestSellers

    init {
        val userEmail = auth.currentUser?.email
        if (userEmail == null) {
            Log.e(TAG, "Usuario no autenticado. No se pueden cargar los datos.")
        } else {
            Log.d(TAG, "Usuario autenticado: $userEmail")
            loadBestSellers(userEmail)
        }
    }

    // Función para cargar los productos más vendidos
    private fun loadBestSellers(userEmail: String) {
        Log.d(TAG, "Cargando productos más vendidos para el usuario: $userEmail")

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
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.e(TAG, "Snapshot vacío o nulo al cargar los productos más vendidos.")
                    return@addSnapshotListener
                }

                Log.d(TAG, "Procesando ${snapshot.documents.size} tickets.")
                val productSales = mutableMapOf<String, MutableMap<String, Any>>()

                snapshot.documents.forEach { ticket ->
                    val products =
                        (ticket.get("products") as? List<*>) // Cast inicial a una lista genérica
                            ?.filterIsInstance<Map<String, Any>>() // Filtra solo los elementos que sean del tipo Map<String, Any>
                            ?: emptyList()
                    products.forEach { product ->
                        val productName = product["name"] as? String ?: "Desconocido"
                        val barcode = product["barcode"] as? String ?: "Sin código"
                        val quantity = (product["quantity"] as? Long)?.toInt() ?: 0

                        val productEntry = productSales.getOrPut(productName) {
                            mutableMapOf(
                                "name" to productName,
                                "barcode" to barcode,
                                "quantitySold" to 0
                            )
                        }

                        productEntry["quantitySold"] =
                            (productEntry["quantitySold"] as Int) + quantity
                    }
                }

                val sortedProducts = productSales.values
                    .sortedByDescending { it["quantitySold"] as Int }
                    .take(BEST_SELLERS_LIMIT)

                Log.d(TAG, "Productos más vendidos: $sortedProducts")
                _bestSellers.value = sortedProducts
            }
    }
}