package com.elfrikiamv.minegocio_puntodeventa.viewmodel.inventory

// CheckProductExistsViewModel.kt

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.elfrikiamv.minegocio_puntodeventa.model.inventory.ProductFirebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CheckProductExistsViewModel : ViewModel() {

    // Constantes para las colecciones y documentos por defecto
    companion object {
        private const val TAG = "CheckProductExistsViewModel"
        private const val DEFAULT_BUSINESS = "defaultBusiness"
        private const val DEFAULT_INVENTORIES = "defaultInventory"
    }

    // Firebase Authentication para obtener el usuario actual
    private val auth = FirebaseAuth.getInstance()

    // Firestore para manejar la base de datos
    private val db = FirebaseFirestore.getInstance()

    // Nuevo campo para el estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Agregado para guardar referencia al listener
    private var inventoryListener: ListenerRegistration? = null

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
            //startListeningToProducts(userEmail)
        }
    }

    // Cancelar listener en onCleared
    override fun onCleared() {
        super.onCleared()
        inventoryListener?.remove()
        Log.d(TAG, "Listener de inventario eliminado en onCleared()")
    }

    //verificar si el producto existe en Firestore
    fun checkProductExists(barcode: String, callback: (ProductFirebase?) -> Unit) {

        //_isLoading.value = true
        val userEmail = auth.currentUser?.email ?: return

        db.collection("users")
            .document(userEmail)
            .collection("userMyBusinesses")
            .document(DEFAULT_BUSINESS)
            .collection("userMyInventories")
            .document(DEFAULT_INVENTORIES)
            .collection("userInventory")
            .whereEqualTo("barcode", barcode)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    callback(null) // Producto no encontrado
                    _isLoading.value = false
                } else {
                    _isLoading.value = true
                    val product = documents.documents.first().toObject(ProductFirebase::class.java)
                    callback(product)
                    _isLoading.value = false
                }
            }
            .addOnFailureListener {
                callback(null) // En caso de error
                _isLoading.value = false
            }
    }

    fun deleteProduct(productId: String) {

        _isLoading.value = true
        val userEmail = auth.currentUser?.email ?: return

        db.collection("users")
            .document(userEmail)
            .collection("userMyBusinesses")
            .document(DEFAULT_BUSINESS)
            .collection("userMyInventories")
            .document(DEFAULT_INVENTORIES)
            .collection("userInventory")
            .document(productId)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "Producto eliminado con éxito: $productId")
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al eliminar el producto: $productId", e)
                _isLoading.value = false
            }
    }

}