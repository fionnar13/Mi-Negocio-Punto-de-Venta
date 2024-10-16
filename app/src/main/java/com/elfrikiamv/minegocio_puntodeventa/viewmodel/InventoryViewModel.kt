package com.elfrikiamv.minegocio_puntodeventa.viewmodel

//InventoryViewModel.kt

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import com.elfrikiamv.minegocio_puntodeventa.model.Product
import kotlinx.coroutines.flow.StateFlow

class InventoryViewModel : ViewModel() {
    // Firebase Authentication para obtener el usuario actual
    private val auth = FirebaseAuth.getInstance()

    // Firestore para manejar la base de datos
    private val db = FirebaseFirestore.getInstance()

    // StateFlow que almacenará los productos del inventario
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    init {
        // Cargar los productos al iniciar el ViewModel
        loadProducts()
    }

    // Función para cargar los productos desde Firestore
    private fun loadProducts() {
        val userEmail = auth.currentUser?.email ?: return

        db.collection("inventories")
            .document(userEmail)
            .collection("products")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) {
                    // Manejar error
                    return@addSnapshotListener
                }

                // Convertir los documentos a objetos Product
                val productsList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Product::class.java)?.copy(id = doc.id)
                }
                _products.value = productsList
            }
    }

    // Función para agregar un producto a Firestore
    fun addProduct(name: String, quantity: Int, barcode: String) {
        val userEmail = auth.currentUser?.email ?: return

        val newProduct = Product(name = name, quantity = quantity, barcode = barcode)

        // Guardar el producto en Firestore
        db.collection("inventories")
            .document(userEmail)
            .collection("products")
            .add(newProduct)
    }
}