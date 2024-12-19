package com.elfrikiamv.minegocio_puntodeventa.viewmodel

// ShoppingViewModel.kt
import androidx.lifecycle.ViewModel
import com.elfrikiamv.minegocio_puntodeventa.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// ViewModel para manejar el carrito de compras
class ShoppingViewModel : ViewModel() {

    // StateFlow para almacenar los productos agregados al carrito
    private val _cartProducts = MutableStateFlow<List<Product>>(emptyList())
    val cartProducts: StateFlow<List<Product>> = _cartProducts

    // Agrega un producto al carrito de compras. Si el producto ya existe, actualiza su cantidad.
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
}
