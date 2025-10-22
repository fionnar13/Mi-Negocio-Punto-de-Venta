package com.elfrikiamv.minegocio_puntodeventa.viewmodel.home.expenses

// ExpensesViewModel.kt

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.elfrikiamv.minegocio_puntodeventa.database.AppDatabase
import com.elfrikiamv.minegocio_puntodeventa.model.home.expenses.ExpenseEntity
import com.elfrikiamv.minegocio_puntodeventa.model.home.expenses.ExpenseFirebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ExpensesViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "ExpensesDetailsViewModel"
        private const val DEFAULT_BUSINESS = "defaultBusiness"
        private const val DEFAULT_EXPENSES = "defaultExpenses"
    }

    // Instancias de Room
    private val expenseDao = AppDatabase.getDatabase(application).expenseDao()

    // Instancias de Firebase
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // StateFlow para los expenses agrupados por año y mes
    private val _groupedExpenses = MutableStateFlow<List<GroupedExpenses>>(emptyList())
    val groupedExpenses = _groupedExpenses.asStateFlow()

    // Nuevo campo para el estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Estado para query de búsqueda
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Estado para resultados de búsqueda
    private val _searchResults = MutableStateFlow<List<ExpenseFirebase>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    // Listener de búsqueda activo
    private var searchListener: ListenerRegistration? = null

    // Guardar referencia al listener
    private var expensesListener: ListenerRegistration? = null

    // Mapa de expenses
    private val allExpensesMap = mutableMapOf<String, ExpenseFirebase>()

    private val monthStart: Timestamp
    private val monthEnd: Timestamp

    // Estados para formulario de gastos
    val concept = mutableStateOf("")
    val paymentMethod = mutableStateOf("")
    val description = mutableStateOf("")
    val expensePriceInCents = mutableLongStateOf(0L)
    val expenseAmountGivenInCents = mutableLongStateOf(0L)
    val errorMessage = mutableStateOf("")

    // StateFlow para el switch de todos los gastos
    private val _showAllExpenses = MutableStateFlow(false)
    val showAllExpenses = _showAllExpenses.asStateFlow()

    // Campo para los detalles del gasto
    val expenseDetails = mutableStateOf<ExpenseFirebase?>(null)

    init {
        _isLoading.value = true

        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        monthStart = Timestamp(calendar.time)

        calendar.add(Calendar.MONTH, 1)
        monthEnd = Timestamp(calendar.time)

        val userEmail = auth.currentUser?.email
        if (userEmail == null) {
            Log.e(TAG, "Usuario no autenticado. No se pueden cargar los datos.")
            _isLoading.value = false
        } else {
            Log.d(TAG, "Usuario autenticado: $userEmail")
            //loadExpensesFromFirebase(userEmail)
            _isLoading.value = false
            listenForExpenses(userEmail)
        }
    }

    private fun getExpensesRef(userEmail: String): CollectionReference {
        return db.collection("users")
            .document(userEmail)
            .collection("userMyBusinesses")
            .document(DEFAULT_BUSINESS)
            .collection("userMyExpenses")
            .document(DEFAULT_EXPENSES)
            .collection("userExpenses")
    }

    fun onShowAllExpensesChange(showAll: Boolean) {
        if (_showAllExpenses.value == showAll) return // Evitar recargas innecesarias
        _showAllExpenses.value = showAll
        auth.currentUser?.email?.let {
            listenForExpenses(it)
        }
    }

    private fun listenForExpenses(userEmail: String) {
        _isLoading.value = true
        expensesListener?.remove()
        allExpensesMap.clear()
        _groupedExpenses.value = emptyList()

        var query: Query = getExpensesRef(userEmail)
            .orderBy("date", Query.Direction.DESCENDING)
            .orderBy("time", Query.Direction.DESCENDING)

        if (!_showAllExpenses.value) {
            query = query.whereGreaterThanOrEqualTo("timestamp", monthStart)
                .whereLessThan("timestamp", monthEnd)
        }

        expensesListener = query.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                Log.e(TAG, "Error escuchando mis gastos: $error")
                _isLoading.value = false
                return@addSnapshotListener
            }

            for (change in snapshot.documentChanges) {
                val expenseId = change.document.id
                when (change.type) {
                    DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                        val expense = change.document.toObject(ExpenseFirebase::class.java)
                            .copy(expenseId = expenseId)
                        allExpensesMap[expenseId] = expense
                    }

                    DocumentChange.Type.REMOVED -> {
                        allExpensesMap.remove(expenseId)
                    }
                }
            }
            _groupedExpenses.value = groupExpensesByYearAndMonth(allExpensesMap.values.toList())
            _isLoading.value = false
        }
    }

    // verificar si el gasto existe en Firestore
    fun checkExpenseExists(expenseId: String, callback: (ExpenseFirebase?) -> Unit) {

        _isLoading.value = true // Mostrar indicador de carga
        val userEmail = auth.currentUser?.email ?: return

        getExpensesRef(userEmail)
            .whereEqualTo("expenseId", expenseId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    callback(null) // Producto no encontrado
                    _isLoading.value = false // Ocultar indicador de carga
                } else {
                    val fetchedExpense =
                        documents.documents.first().toObject(ExpenseFirebase::class.java)
                    callback(fetchedExpense)
                    _isLoading.value = false // Ocultar indicador de carga
                }
            }
            .addOnFailureListener {
                callback(null) // En caso de error
                _isLoading.value = false // Ocultar indicador de carga
            }
    }

    // Función para limpiar los campos del formulario
    fun clearExpenseFields() {
        concept.value = ""
        paymentMethod.value = ""
        description.value = ""
        expensePriceInCents.longValue = 0L
        expenseAmountGivenInCents.longValue = 0L
        errorMessage.value = ""
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isNotBlank()) {
            searchExpenses(query)
        } else {
            // Cancelar listener de búsqueda si la consulta está vacía
            searchListener?.remove()
            _searchResults.value = emptyList()
            _isLoading.value = false
        }
    }

    private fun searchExpenses(query: String) {
        val userEmail = auth.currentUser?.email ?: return
        _isLoading.value = true

        searchListener?.remove()

        val expensesRef = getExpensesRef(userEmail)
        val combinedResults = mutableMapOf<String, ExpenseFirebase>()

        fun updateUiWithResults() {
            _searchResults.value = combinedResults.values.toList()
        }

        val expenseIdListener = expensesRef
            .whereGreaterThanOrEqualTo("expenseId", query)
            .whereLessThanOrEqualTo("expenseId", query + '\uf8ff')
            .addSnapshotListener { snapshot, error ->
                _isLoading.value = false
                if (error != null) {
                    Log.e(TAG, "Error buscando por expenseId: $error")
                    return@addSnapshotListener
                }

                snapshot?.documentChanges?.forEach { change ->
                    val expense = change.document.toObject(ExpenseFirebase::class.java)
                    when (change.type) {
                        DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> combinedResults[expense.expenseId] =
                            expense

                        DocumentChange.Type.REMOVED -> combinedResults.remove(expense.expenseId)
                    }
                }
                updateUiWithResults()
            }

        val dateListener = expensesRef
            .whereGreaterThanOrEqualTo("date", query)
            .whereLessThanOrEqualTo("date", query + '\uf8ff')
            .addSnapshotListener { snapshot, error ->
                _isLoading.value = false
                if (error != null) {
                    Log.e(TAG, "Error buscando por date: $error")
                    return@addSnapshotListener
                }

                snapshot?.documentChanges?.forEach { change ->
                    val expense = change.document.toObject(ExpenseFirebase::class.java)
                    when (change.type) {
                        DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> combinedResults[expense.expenseId] =
                            expense

                        DocumentChange.Type.REMOVED -> combinedResults.remove(expense.expenseId)
                    }
                }
                updateUiWithResults()
            }

        searchListener = object : ListenerRegistration {
            override fun remove() {
                expenseIdListener.remove()
                dateListener.remove()
            }
        }
    }

    data class GroupedExpenses(
        val year: String,
        val month: String,
        val expenses: List<ExpenseFirebase>
    )

    private fun groupExpensesByYearAndMonth(expenses: List<ExpenseFirebase>): List<GroupedExpenses> {
        val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        val sorted = expenses.sortedByDescending { formatter.parse("${it.date} ${it.time}") }
        val grouped = sorted.groupBy {
            val cal =
                Calendar.getInstance().apply { time = formatter.parse("${it.date} ${it.time}")!! }
            Pair(
                cal.get(Calendar.YEAR).toString(),
                cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())!!
            )
        }
        return grouped.map { (ym, list) ->
            GroupedExpenses(ym.first, ym.second, list)
        }.sortedWith(
            compareByDescending<GroupedExpenses> { it.year.toInt() }
                .thenByDescending { monthNameToNumber(it.month) }
        )
    }

    private fun monthNameToNumber(monthName: String): Int =
        SimpleDateFormat("MMMM", Locale.getDefault()).parse(monthName)?.let {
            Calendar.getInstance().apply { time = it }.get(Calendar.MONTH)
        } ?: 0

    override fun onCleared() {
        super.onCleared()
        expensesListener?.remove()
        searchListener?.remove()
    }

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
            val currentDateTimeMillis = System.currentTimeMillis()
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
                description = description,
                timestamp = currentDateTimeMillis
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

        // Convertimos ExpenseEntity → ExpenseFirebase con Timestamp
        val expenseFirebase = ExpenseFirebase(
            expenseId = expenseEntity.expenseId,
            date = expenseEntity.date,
            time = expenseEntity.time,
            paymentConcept = expenseEntity.paymentConcept,
            paymentMethod = expenseEntity.paymentMethod,
            amountExpense = expenseEntity.amountExpense,
            amountGiven = expenseEntity.amountGiven,
            change = expenseEntity.change,
            description = expenseEntity.description,
            timestamp = Timestamp(Date(expenseEntity.timestamp))
        )

        db.collection("users")
            .document(userEmail)
            .collection("userMyBusinesses")
            .document(DEFAULT_BUSINESS)
            .collection("userMyExpenses")
            .document(DEFAULT_EXPENSES)
            .collection("userExpenses")
            .document(expenseFirebase.expenseId)
            .set(expenseFirebase)
            .addOnSuccessListener {
                viewModelScope.launch {
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