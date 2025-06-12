package com.elfrikiamv.minegocio_puntodeventa.viewmodel.home.expenses

// ExpensesViewModel.kt

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.elfrikiamv.minegocio_puntodeventa.database.AppDatabase
import com.elfrikiamv.minegocio_puntodeventa.model.home.expenses.ExpenseEntity
import com.elfrikiamv.minegocio_puntodeventa.model.home.expenses.ExpenseFirebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ExpensesViewModel(application: Application) : AndroidViewModel(application) {

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
    private val _expensesDetailsList = MutableStateFlow<List<ExpenseFirebase>>(emptyList())
    val expensesDetailsList: StateFlow<List<ExpenseFirebase>> =
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
                        doc.toObject(ExpenseFirebase::class.java)
                    }
                    _expensesDetailsList.value = fetchedExpenses
                    Log.d(TAG, "Gastos cargados: ${_expensesDetailsList.value}")
                    _isLoading.value = false
                } else {
                    Log.d(TAG, "No se encontraron gastos")
                    _expensesDetailsList.value = emptyList()
                    _isLoading.value = false
                }
            }
    }

    fun confirmExpense(
        paymentConcept: String,
        paymentMethod: String,
        description: String,
        amountExpense: Double,
        amountGiven: Double
    ) {

        _isLoading.value = true
        viewModelScope.launch {
            // Obtener información de fecha y hora
            val currentDateTimeExpenseId = System.currentTimeMillis()
            val currentDateTime = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
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
            Log.d(TAG, "Ticket guardado en Room: $expense")

            // Limpiar carrito
            /*productDao.deleteAllProducts()
            loadCartProducts()*/

            // Subir a Firebase
            uploadExpenseToFirebase(expense)
            _isLoading.value = false
        }
    }

    private fun uploadExpenseToFirebase(expenseEntity: ExpenseEntity) {

        _isLoading.value = true
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
                    _isLoading.value = false
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al subir el gasto a Firebase: $e")
                _isLoading.value = false
            }
    }

    // función para agrupar y ordenar
    data class GroupedExpenses(
        val year: String,
        val month: String,
        val expenses: List<ExpenseFirebase>
    )

    fun groupExpensesByYearAndMonth(expenses: List<ExpenseFirebase>): List<GroupedExpenses> {
        val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        val grouped = expenses.sortedByDescending { formatter.parse("${it.date} ${it.time}") }
            .groupBy { expense ->
                val date = formatter.parse("${expense.date} ${expense.time}")
                val calendar = Calendar.getInstance()
                calendar.time = date!!
                val year = calendar.get(Calendar.YEAR).toString()
                val month =
                    calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())!!
                Pair(year, month)
            }

        return grouped.map { (yearMonth, expensesList) ->
            GroupedExpenses(
                year = yearMonth.first,
                month = yearMonth.second,
                expenses = expensesList
            )
        }.sortedWith(
            compareByDescending<GroupedExpenses> { it.year.toInt() }
                .thenByDescending { monthNameToNumber(it.month) }
        )
    }

    private fun monthNameToNumber(monthName: String): Int {
        return SimpleDateFormat("MMMM", Locale.getDefault()).parse(monthName)?.let {
            val calendar = Calendar.getInstance()
            calendar.time = it
            calendar.get(Calendar.MONTH)
        } ?: 0
    }

}