package com.elfrikiamv.minegocio_puntodeventa.viewmodel

//InventoryViewModel.kt

import androidx.lifecycle.ViewModel
import com.elfrikiamv.minegocio_puntodeventa.model.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class InventoryViewModel : ViewModel() {
    // Firebase Authentication para obtener el usuario actual
    private val auth = FirebaseAuth.getInstance()

    // Firestore para manejar la base de datos
    private val db = FirebaseFirestore.getInstance()

    // StateFlow que almacenará los productos del inventario
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    // StateFlow cartProducts
    private val _cartProducts = MutableStateFlow<List<Product>>(emptyList())
    val cartProducts: StateFlow<List<Product>> = _cartProducts

    init {
        // Cargar los productos al iniciar el ViewModel
        loadProducts()
    }

    // Función para agregar un producto al carrito
    fun addToCart(product: Product) {
        val updatedCart = _cartProducts.value.toMutableList().apply {
            val existingProduct = find { it.barcode == product.barcode }
            if (existingProduct != null) {
                remove(existingProduct)
                add(existingProduct.copy(quantity = existingProduct.quantity + product.quantity))
            } else {
                add(product)
            }
        }
        _cartProducts.value = updatedCart
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

    //verificar si el producto existe en Firestore
    fun checkProductExists(barcode: String, callback: (Product?) -> Unit) {
        val userEmail = auth.currentUser?.email ?: return

        db.collection("inventories")
            .document(userEmail)
            .collection("products")
            .whereEqualTo("barcode", barcode)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    callback(null) // Producto no encontrado
                } else {
                    val product = documents.documents.first().toObject(Product::class.java)
                    callback(product)
                }
            }
            .addOnFailureListener {
                callback(null) // En caso de error
            }
    }

}