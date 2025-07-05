package com.elfrikiamv.minegocio_puntodeventa.viewmodel.inventory

// AddProductViewModel.kt

import android.util.Log
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.elfrikiamv.minegocio_puntodeventa.model.inventory.ProductFirebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AddProductViewModel : ViewModel() {

    // Constantes para las colecciones y documentos por defecto
    companion object {
        private const val TAG = "AddProductViewModel"
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

    // Nuevo campo para el estado de carga
    val productFound = MutableStateFlow(true)
    val isProductFound: StateFlow<Boolean> = productFound

    // Agregado para guardar referencia al listener
    private var inventoryListener: ListenerRegistration? = null

    // Mensaje de error
    val errorMessage = mutableStateOf("")

    // Mensaje de producto encontrado
    val isProductFoundMessage = mutableStateOf("")

    // Campos del formulario
    val id = mutableStateOf<String?>(null)
    val name = mutableStateOf("")
    val quantity = mutableStateOf("")
    val barcode = mutableStateOf("")
    val description = mutableStateOf("")

    // Inicializamos priceInCents a partir de salePrice si ya tiene un valor válido
    val salePriceInCents = mutableLongStateOf(0L)

    // Inicializamos priceInCents a partir de salePrice si ya tiene un valor válido
    val providerPriceInCents = mutableLongStateOf(0L)

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

    // Función para limpiar los campos del formulario para guardar
    fun clearAddProductSaveFields() {
        id.value = null
        barcode.value = ""
        name.value = ""
        quantity.value = ""
        providerPriceInCents.longValue = 0L
        salePriceInCents.longValue = 0L
        description.value = ""
        errorMessage.value = ""
    }

    // Función para limpiar los campos del formulario
    fun clearAddProductFields() {
        id.value = null
        name.value = ""
        quantity.value = ""
        providerPriceInCents.longValue = 0L
        salePriceInCents.longValue = 0L
        description.value = ""
        errorMessage.value = ""
    }

    // Cancelar listener en onCleared
    override fun onCleared() {
        super.onCleared()
        inventoryListener?.remove()
        clearAddProductSaveFields()
        Log.d(TAG, "Listener de inventario eliminado en onCleared()")
    }

    // Función para agregar un producto a Firestore
    fun addOrUpdateProduct(
        id: String?, // ID del producto (null para nuevos productos)
        name: String,
        quantity: Int,
        barcode: String,
        providerPrice: Double,
        salePrice: Double,
        description: String
    ) {

        _isLoading.value = true
        val userEmail = auth.currentUser?.email ?: return

        // Si no se proporciona un ID, generar uno nuevo
        val productId = id ?: System.currentTimeMillis().toString()

        val product = ProductFirebase(
            id = productId,
            name = name,
            quantity = quantity,
            barcode = barcode,
            providerPrice = providerPrice,
            salePrice = salePrice,
            description = description
        )

        // Guardar o actualizar el producto en Firestore
        db.collection("users")
            .document(userEmail)
            .collection("userMyBusinesses")
            .document(DEFAULT_BUSINESS)
            .collection("userMyInventories")
            .document(DEFAULT_INVENTORIES)
            .collection("userInventory")
            .document(productId)
            .set(product)
            .addOnSuccessListener {
                _isLoading.value = false
            }
            .addOnFailureListener {
                Log.e(TAG, "Error al agregar o actualizar producto", it)
                _isLoading.value = false
            }

    }

}