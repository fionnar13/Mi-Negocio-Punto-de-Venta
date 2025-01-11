package com.elfrikiamv.minegocio_puntodeventa.viewmodel

// HomeViewModel.kt

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val userEmail = auth.currentUser?.email
    private val userMyBusinesses = "defaultBusiness"
    private val userMyInventories = "defaultInventory"

    private val _totalQuantity = MutableStateFlow(0)
    val totalQuantity: StateFlow<Int> = _totalQuantity

    private val _totalSalePrice = MutableStateFlow(0.0)
    val totalSalePrice: StateFlow<Double> = _totalSalePrice

    init {
        if (userEmail != null) {
            loadTotalQuantity(userEmail)
            loadTotalSalePrice(userEmail)
        }
    }

    // Calcula el valor total del inventario multiplicando `salePrice` por `quantity` para cada producto.
    private fun loadTotalSalePrice(userEmail: String) {
        Log.d("HomeViewModel", "Cargando valor total del inventario para el usuario: $userEmail")

        db.collection("users")
            .document(userEmail)
            .collection("userMyBusinesses")
            .document(userMyBusinesses)
            .collection("userMyInventories")
            .document(userMyInventories)
            .collection("userInventory")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("HomeViewModel", "Error al escuchar cambios en Firestore: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.e(
                        "HomeViewModel",
                        "Snapshot vacío o nulo al cargar valor total del inventario."
                    )
                    return@addSnapshotListener
                }

                Log.d(
                    "HomeViewModel",
                    "Procesando ${snapshot.documents.size} documentos para valor total."
                )
                val total = snapshot.documents.sumOf { doc ->
                    val quantity = doc.getLong("quantity")?.toInt() ?: 0
                    val salePrice = doc.getDouble("salePrice") ?: 0.0
                    val productValue = quantity * salePrice
                    Log.d(
                        "HomeViewModel",
                        "Producto procesado - ID: ${doc.id}, quantity: $quantity, salePrice: $salePrice, value: $productValue"
                    )
                    productValue
                }

                Log.d("HomeViewModel", "Valor total del inventario calculado: $total")
                _totalSalePrice.value = total
            }
    }

    // Calcula la cantidad total de productos en el inventario.
    private fun loadTotalQuantity(userEmail: String) {
        Log.d("HomeViewModel", "Cargando cantidad total de productos para el usuario: $userEmail")

        db.collection("users")
            .document(userEmail)
            .collection("userMyBusinesses")
            .document(userMyBusinesses)
            .collection("userMyInventories")
            .document(userMyInventories)
            .collection("userInventory")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("HomeViewModel", "Error al escuchar cambios en Firestore: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.e(
                        "HomeViewModel",
                        "Snapshot vacío o nulo al cargar cantidad total de productos."
                    )
                    return@addSnapshotListener
                }

                Log.d(
                    "HomeViewModel",
                    "Procesando ${snapshot.documents.size} documentos para cantidad total."
                )
                val total = snapshot.documents.sumOf { doc ->
                    val quantity = doc.getLong("quantity")?.toInt() ?: 0
                    Log.d(
                        "HomeViewModel",
                        "Producto procesado - ID: ${doc.id}, quantity: $quantity"
                    )
                    quantity
                }

                Log.d("HomeViewModel", "Cantidad total calculada: $total")
                _totalQuantity.value = total
            }
    }
}