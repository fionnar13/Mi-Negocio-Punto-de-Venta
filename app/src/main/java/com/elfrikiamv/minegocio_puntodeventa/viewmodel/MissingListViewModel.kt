package com.elfrikiamv.minegocio_puntodeventa.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.elfrikiamv.minegocio_puntodeventa.model.MissingProductFirebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MissingListViewModel : ViewModel() {

    companion object {
        private const val TAG = "MissingListViewModel"
        private const val DEFAULT_BUSINESS = "defaultBusiness"
        private const val DEFAULT_MISSING = "defaultMissing"
    }

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
}