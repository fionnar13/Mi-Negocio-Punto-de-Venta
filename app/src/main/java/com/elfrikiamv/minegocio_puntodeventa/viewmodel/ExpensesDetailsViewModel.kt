package com.elfrikiamv.minegocio_puntodeventa.viewmodel

// ExpensesDetailsViewModel.kt

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.elfrikiamv.minegocio_puntodeventa.database.AppDatabase
import com.elfrikiamv.minegocio_puntodeventa.model.ExpenseEntity
import com.elfrikiamv.minegocio_puntodeventa.model.ExpensesFirebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ExpensesDetailsViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "ExpensesDetailsViewModel"
        private const val DEFAULT_BUSINESS = "defaultBusiness"
        private const val DEFAULT_EXPENSES = "defaultExpenses"
    }

    private val expenseDao = AppDatabase.getDatabase(application).expenseDao()

    // Instancias de Firebase
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // MutableStateFlow para almacenar la lista de gastos
    private val _expensesDetailsList = MutableStateFlow<List<ExpensesFirebase>>(emptyList())
    val expensesDetailsList: StateFlow<List<ExpensesFirebase>> =
        _expensesDetailsList // StateFlow expuesto a la vista

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

    fun confirmExpense(
        paymentConcept: String,
        paymentMethod: String,
        description: String,
        amountExpense: Double,
        amountGiven: Double,
        /*context: Context,
        userEmail: String*/
    ) {
        viewModelScope.launch {
            // Obtener información de fecha y hora
            val currentDateTimeExpenseId = System.currentTimeMillis()
            val currentDateTime = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

            val dateFormatted = dateFormat.format(currentDateTime.time)
            val timeFormatted = timeFormat.format(currentDateTime.time)

            val change = amountGiven - amountExpense

            // Crear el ticket
            val expense = ExpenseEntity(
                expenseId = currentDateTimeExpenseId.toString(),
                date = dateFormatted,
                time = timeFormatted,
                paymentConcept = paymentConcept,
                paymentMethod = paymentMethod,
                amountExpense = amountExpense,
                amountGiven = amountGiven,
                change = change,
                description = description
            )

            // Guardar en Room
            expenseDao.insertExpense(expense)
            Log.d("ShoppingViewModel", "Ticket guardado en Room: $expense")

            // Limpiar carrito
            /*productDao.deleteAllProducts()
            loadCartProducts()*/

            // Subir a Firebase
            uploadExpenseToFirebase(expense)
        }
    }

    private fun uploadExpenseToFirebase(
        expenseEntity: ExpenseEntity,
        /*userEmail: String*/
    ) {
        val userEmail = auth.currentUser?.email ?: return

        db.collection("users")
            .document(userEmail)
            .collection("userMyBusinesses")
            .document(DEFAULT_BUSINESS)
            .collection("userMyExpenses")
            .document(DEFAULT_EXPENSES)
            .collection("userExpenses")

            .document(expenseEntity.expenseId)
            .set(expenseEntity)
            .addOnSuccessListener {
                viewModelScope.launch {

                    // compartir ticket
                    /*val ticketId = ticketEntity.ticketId
                    sharePDF(ticketId, context, email)*/
                    // Eliminar ticket de Room
                    expenseDao.deleteExpenseById(expenseEntity.expenseId)
                }
            }
            .addOnFailureListener { e ->
                Log.e("ShoppingViewModel", "Error al subir el gasto a Firebase: $e")
            }
    }
}