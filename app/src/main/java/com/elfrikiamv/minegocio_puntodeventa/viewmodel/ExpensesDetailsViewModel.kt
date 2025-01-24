package com.elfrikiamv.minegocio_puntodeventa.viewmodel

// ExpensesDetailsViewModel.kt

import android.util.Log
import androidx.lifecycle.ViewModel
import com.elfrikiamv.minegocio_puntodeventa.model.ExpensesFirebase
import com.elfrikiamv.minegocio_puntodeventa.model.TicketFirebase
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.BestSellersViewModel.Companion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ExpensesDetailsViewModel : ViewModel() {

    companion object {
        private const val TAG = "ExpensesDetailsViewModel"
        private const val DEFAULT_BUSINESS = "defaultBusiness"
        private const val DEFAULT_EXPENSES = "defaultExpenses"
    }
    // Instancias de Firebase
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // MutableStateFlow para almacenar la lista de gastos
    private val _expensesDetailsList = MutableStateFlow<List<ExpensesFirebase>>(emptyList())
    val expensesDetailsList: StateFlow<List<ExpensesFirebase>> = _expensesDetailsList // StateFlow expuesto a la vista

    // Nuevo campo para el estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        val userEmail = auth.currentUser?.email
        if (userEmail == null) {
            Log.e(TAG, "Usuario no autenticado. No se pueden cargar los datos.")
        } else {
            Log.d(TAG, "Usuario autenticado: $userEmail")
            loadExpensesFromFirebase(userEmail)
        }
    }

    // Función para cargar los tickets desde Firebase
    private fun loadExpensesFromFirebase(userEmail: String) {

        _isLoading.value = true
        db.collection("users")
            .document(userEmail)
            .collection("userMyBusinesses")
            .document(DEFAULT_BUSINESS)
            .collection("userMyExpenses")
            .document(DEFAULT_EXPENSES)
            .collection("userExpenses")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Error al obtener los gastos: $e")
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val fetchedExpenses = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(ExpensesFirebase::class.java)
                    }
                    _expensesDetailsList.value = fetchedExpenses
                    Log.d(TAG, "Gastos cargados: ${_expensesDetailsList.value}")
                    _isLoading.value = false
                } else {
                    Log.d(TAG, "No se encontraron gastos")
                    _isLoading.value = false
                }
            }
    }
}