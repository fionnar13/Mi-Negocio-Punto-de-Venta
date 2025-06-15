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
import com.google.firebase.firestore.DocumentSnapshot
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
        private const val PAGE_SIZE = 22
    }

    // Instancias de Room
    private val expenseDao = AppDatabase.getDatabase(application).expenseDao()

    // Instancias de Firebase
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Cargar gastos desde Firebase
    /*private val _expenses = MutableStateFlow<List<ExpenseFirebase>>(emptyList())
    val expenses: StateFlow<List<ExpenseFirebase>> = _expenses*/

    // StateFlow para los expenses agrupados por año y mes
    private val _groupedExpenses = MutableStateFlow<List<GroupedExpenses>>(emptyList())
    val groupedExpenses: StateFlow<List<GroupedExpenses>> = _groupedExpenses

    // Nuevo campo para el estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Estados de Firebase
    private var lastSnapshot: DocumentSnapshot? = null
    private var endReached = false

    init {
        val userEmail = auth.currentUser?.email
        if (userEmail == null) {
            Log.e(TAG, "Usuario no autenticado. No se pueden cargar los datos.")
        } else {
            Log.d(TAG, "Usuario autenticado: $userEmail")
            //loadExpensesFromFirebase(userEmail)
            loadNextPage()
        }
    }

    fun loadNextPage() {
        val userEmail = auth.currentUser?.email ?: return
        if (_isLoading.value || endReached) return

        _isLoading.value = true

        var query = db.collection("users")
            .document(userEmail)
            .collection("userMyBusinesses")
            .document(DEFAULT_BUSINESS)
            .collection("userMyExpenses")
            .document(DEFAULT_EXPENSES)
            .collection("userExpenses")
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .orderBy("time", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(PAGE_SIZE.toLong())

        lastSnapshot?.let { query = query.startAfter(it) }

        /*query.get().addOnSuccessListener { snap ->
            val list = snap.documents.mapNotNull { it.toObject(ExpenseFirebase::class.java) }
            if (list.size < PAGE_SIZE) endReached = true
            lastSnapshot = snap.documents.lastOrNull()
            _expenses.value += list
            _isLoading.value = false
        }.addOnFailureListener {
            Log.e(TAG, "Error cargando gastos: $it")
            _isLoading.value = false
        }*/
        query.get().addOnSuccessListener { snap ->
            val list = snap.documents.mapNotNull { it.toObject(ExpenseFirebase::class.java) }
            if (list.size < PAGE_SIZE) endReached = true
            lastSnapshot = snap.documents.lastOrNull()

            // Genera la agrupación reactiva combinando lo que ya había con lo nuevo
            val allExpenses = _groupedExpenses.value.flatMap { it.expenses } + list
            _groupedExpenses.value = groupExpensesByYearAndMonth(allExpenses)

            _isLoading.value = false
        }.addOnFailureListener {
            Log.e(TAG, "Error cargando gastos: $it")
            _isLoading.value = false
        }
    }

    // Agrupación y ordenación
    data class GroupedExpenses(
        val year: String,
        val month: String,
        val expenses: List<ExpenseFirebase>
    )

    private fun groupExpensesByYearAndMonth(expenses: List<ExpenseFirebase>): List<GroupedExpenses> {
        val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        val sorted = expenses.sortedByDescending { formatter.parse("${it.date} ${it.time}") }
        val grouped = sorted.groupBy {
            val cal = Calendar.getInstance().apply {
                time = formatter.parse("${it.date} ${it.time}")!!
            }
            val yr = cal.get(Calendar.YEAR).toString()
            val mo = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())!!
            Pair(yr, mo)
        }
        return grouped
            .map { (ym, list) -> GroupedExpenses(ym.first, ym.second, list) }
            .sortedWith(compareByDescending<GroupedExpenses> { it.year.toInt() }
                .thenByDescending { monthNameToNumber(it.month) })
    }

    private fun monthNameToNumber(monthName: String): Int =
        SimpleDateFormat("MMMM", Locale.getDefault())
            .parse(monthName)?.let {
                Calendar.getInstance().apply { time = it }.get(Calendar.MONTH)
            } ?: 0

    // guardar gastos en bd room
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

    // Subir gastos a Firebase
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
}