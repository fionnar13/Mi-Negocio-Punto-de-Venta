package com.elfrikiamv.minegocio_puntodeventa.viewmodel.home.missing

// MissingProductsViewModel.kt

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.elfrikiamv.minegocio_puntodeventa.database.AppDatabase
import com.elfrikiamv.minegocio_puntodeventa.model.home.missing.MissingProductEntity
import com.elfrikiamv.minegocio_puntodeventa.model.home.missing.MissingProductFirebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MissingProductsViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "MissingListViewModel"
        private const val DEFAULT_BUSINESS = "defaultBusiness"
        private const val DEFAULT_MISSING = "defaultMissing"
    }

    private val missingDao = AppDatabase.getDatabase(application).missingProductDao()

    // Instancias de Firebase
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // MutableStateFlow para almacenar la lista de faltantes
    private val _missingList = MutableStateFlow<List<MissingProductFirebase>>(emptyList())
    val missingList: StateFlow<List<MissingProductFirebase>> = _missingList

    // Nuevo campo para el estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        val userEmail = auth.currentUser?.email
        if (userEmail == null) {
            Log.e(TAG, "Usuario no autenticado. No se pueden cargar los datos.")
        } else {
            Log.d(TAG, "Usuario autenticado: $userEmail")
            loadMissingFromFirebase(userEmail)
        }
    }

    // Función para cargar la lista de faltantes desde Firebase
    private fun loadMissingFromFirebase(userEmail: String) {

        _isLoading.value = true
        db.collection("users")
            .document(userEmail)
            .collection("userMyBusinesses")
            .document(DEFAULT_BUSINESS)
            .collection("userMyMissing")
            .document(DEFAULT_MISSING)
            .collection("userMissing")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Error al obtener los faltantes: $e")
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val fetchedMissing = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(MissingProductFirebase::class.java)
                    }
                    _missingList.value = fetchedMissing
                    Log.d(TAG, "faltantes cargados: ${_missingList.value}")
                    _isLoading.value = false
                } else {
                    Log.d(TAG, "No se encontraron faltantes")
                    _isLoading.value = false
                }
            }
    }

    fun confirmMissing(
        name: String,
        quantity: Int,
        description: String,
        providerPrice: Double,
        totalPrice: Double
    ) {

        _isLoading.value = true
        viewModelScope.launch {
            // Obtener información de fecha y hora
            val currentDateTimeMissingId = System.currentTimeMillis()
            val currentDateTime = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

            val dateFormatted = dateFormat.format(currentDateTime.time)
            val timeFormatted = timeFormat.format(currentDateTime.time)

            // Crear el ticket
            val missing = MissingProductEntity(
                missingId = currentDateTimeMissingId.toString(),
                date = dateFormatted,
                time = timeFormatted,
                name = name,
                quantity = quantity,
                providerPrice = providerPrice,
                totalPrice = totalPrice,
                description = description
            )

            // Guardar en Room
            missingDao.insertMissing(missing)
            Log.d("ShoppingViewModel", "Ticket guardado en Room: $missing")

            // Limpiar carrito
            /*productDao.deleteAllProducts()
            loadCartProducts()*/

            // Subir a Firebase
            uploadMissingToFirebase(missing)
            _isLoading.value = false
        }
    }

    private fun uploadMissingToFirebase(missingEntity: MissingProductEntity) {

        _isLoading.value = true
        val userEmail = auth.currentUser?.email ?: return

        db.collection("users")
            .document(userEmail)
            .collection("userMyBusinesses")
            .document(DEFAULT_BUSINESS)
            .collection("userMyMissing")
            .document(DEFAULT_MISSING)
            .collection("userMissing")

            .document(missingEntity.missingId)
            .set(missingEntity)
            .addOnSuccessListener {
                viewModelScope.launch {

                    // compartir ticket
                    /*val ticketId = ticketEntity.ticketId
                    sharePDF(ticketId, context, email)*/
                    // Eliminar ticket de Room
                    missingDao.deleteMissingById(missingEntity.missingId)
                    _isLoading.value = false
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al subir el gasto a Firebase: $e")
                _isLoading.value = false
            }
    }
}