package com.elfrikiamv.minegocio_puntodeventa.viewmodel.home.expenses

// ExpensesViewModel.kt

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.elfrikiamv.minegocio_puntodeventa.database.AppDatabase
import com.elfrikiamv.minegocio_puntodeventa.model.home.expenses.ExpenseEntity
import com.elfrikiamv.minegocio_puntodeventa.model.home.expenses.ExpenseFirebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
        private const val PAGE_SIZE = 420
    }

    // Instancias de Room
    private val expenseDao = AppDatabase.getDatabase(application).expenseDao()

    // Instancias de Firebase
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // StateFlow para los expenses agrupados por año y mes
    private val _groupedExpenses = MutableStateFlow<List<GroupedExpenses>>(emptyList())
    val groupedExpenses: StateFlow<List<GroupedExpenses>> = _groupedExpenses

    // Nuevo campo para el estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Estados de Firebase
    private var lastSnapshot: DocumentSnapshot? = null
    private var endReached = false

    val isEndReached: Boolean
        get() = endReached

    // Estado para query de búsqueda
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // Estado para resultados de búsqueda
    private val _searchResults = MutableStateFlow<List<ExpenseFirebase>>(emptyList())
    val searchResults: StateFlow<List<ExpenseFirebase>> = _searchResults

    // Listener de búsqueda activo
    private var searchListener: ListenerRegistration? = null

    // Guardar referencia al listener
    private var expensesListener: ListenerRegistration? = null

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

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query

        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        searchExpensesByIdOrDate(query)
    }

    private fun searchExpensesByIdOrDate(query: String) {
        val userEmail = auth.currentUser?.email ?: return
        _isLoading.value = true

        val expensesRef = getExpensesRef(userEmail)

        // Cancelar listeners anteriores para evitar fugas y llamadas duplicadas
        searchListener?.remove()

        // Mapa mutable para mantener los resultados combinados sin duplicados
        val combinedResults = mutableMapOf<String, ExpenseFirebase>()

        // Función para actualizar el StateFlow cuando ambos listeners hayan cargado al menos una vez
        fun updateResults() {
            _searchResults.value = combinedResults.values.toList()
            _isLoading.value = false
        }

        // Flag para saber si ya recibimos datos al menos una vez en cada listener
        var expenseIdReady = false
        var dateReady = false

        // Listener para búsqueda por expenseId con prefijo (rango Unicode)
        val expenseIdListener = expensesRef
            .whereGreaterThanOrEqualTo("expenseId", query)
            .whereLessThanOrEqualTo("expenseId", query + '\uf8ff')
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error buscando por expenseId: $error")
                    _searchResults.value = emptyList()
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                // Procesar los cambios incrementales para mantener combinedResults actualizado
                snapshot?.documentChanges?.forEach { change ->
                    val expense = change.document.toObject(ExpenseFirebase::class.java)
                    when (change.type) {
                        DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> combinedResults[expense.expenseId] =
                            expense

                        DocumentChange.Type.REMOVED -> combinedResults.remove(expense.expenseId)
                    }
                }

                expenseIdReady = true
                if (expenseIdReady && dateReady) {
                    updateResults()
                }
            }

        // Listener para búsqueda por date con prefijo (rango Unicode)
        val dateListener = expensesRef
            .whereGreaterThanOrEqualTo("date", query)
            .whereLessThanOrEqualTo("date", query + '\uf8ff')
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error buscando por date: $error")
                    _searchResults.value = emptyList()
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                // Procesar los cambios incrementales para mantener combinedResults actualizado
                snapshot?.documentChanges?.forEach { change ->
                    val expense = change.document.toObject(ExpenseFirebase::class.java)
                    when (change.type) {
                        DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> combinedResults[expense.expenseId] =
                            expense

                        DocumentChange.Type.REMOVED -> combinedResults.remove(expense.expenseId)
                    }
                }

                dateReady = true
                if (expenseIdReady && dateReady) {
                    updateResults()
                }
            }

        // Guardar la referencia combinada para cancelar ambos listeners juntos
        searchListener = object : ListenerRegistration {
            override fun remove() {
                expenseIdListener.remove()
                dateListener.remove()
            }
        }
    }


    fun loadNextPage() {
        val userEmail = auth.currentUser?.email ?: return
        if (_isLoading.value || endReached) return

        _isLoading.value = true

        var query = getExpensesRef(userEmail)
            .orderBy("date", Query.Direction.DESCENDING)
            .orderBy("time", Query.Direction.DESCENDING)
            .limit(PAGE_SIZE.toLong())

        lastSnapshot?.let { query = query.startAfter(it) }

        query.get().addOnSuccessListener { snap ->
            val list = snap.documents.mapNotNull { it.toObject(ExpenseFirebase::class.java) }
            if (list.size < PAGE_SIZE) endReached = true
            lastSnapshot = snap.documents.lastOrNull()

            val combined = _groupedExpenses.value.flatMap { it.expenses } + list
            _groupedExpenses.value = groupExpensesByYearAndMonth(combined)

            _isLoading.value = false

            // Cuando termina carga inicial, activa realtime listener una sola vez
            if (expensesListener == null) {
                startRealtimeUpdates()
            }

        }.addOnFailureListener {
            Log.e(TAG, "Error cargando gastos: $it")
            _isLoading.value = false
        }
    }

    private fun startRealtimeUpdates() {
        val userEmail = auth.currentUser?.email ?: return
        expensesListener = getExpensesRef(userEmail)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error en realtime listener: $error")
                    return@addSnapshotListener
                }

                snapshot?.let {
                    // Obtener gastos actuales como Map
                    val currentExpenses = _groupedExpenses.value.flatMap { it.expenses }
                        .associateBy { it.expenseId }
                        .toMutableMap()

                    // Procesar cada cambio en documentos
                    for (change in it.documentChanges) {
                        val expense = change.document.toObject(ExpenseFirebase::class.java)
                        when (change.type) {
                            DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                                currentExpenses[expense.expenseId] = expense
                            }

                            DocumentChange.Type.REMOVED -> {
                                currentExpenses.remove(expense.expenseId)
                            }
                        }
                    }

                    // Volver a agrupar y emitir el nuevo listado actualizado
                    val mergedExpenses = currentExpenses.values.toList()
                    _groupedExpenses.value = groupExpensesByYearAndMonth(mergedExpenses)
                }
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