package com.elfrikiamv.minegocio_puntodeventa.viewmodel.home.missing

// MissingProductsViewModel.kt

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.elfrikiamv.minegocio_puntodeventa.database.AppDatabase
import com.elfrikiamv.minegocio_puntodeventa.model.home.expenses.ExpenseFirebase
import com.elfrikiamv.minegocio_puntodeventa.model.home.missing.MissingProductEntity
import com.elfrikiamv.minegocio_puntodeventa.model.home.missing.MissingProductFirebase
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.home.expenses.ExpensesViewModel
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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.collections.set

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

    // StateFlow para los missing agrupados por año y mes
    private val _groupedMissing = MutableStateFlow<List<GroupedMissing>>(emptyList())
    val groupedMissing = _groupedMissing.asStateFlow()

    // Nuevo campo para el estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Estado para query de búsqueda
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Estado para resultados de búsqueda
    private val _searchResults = MutableStateFlow<List<MissingProductFirebase>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    // Listener de búsqueda activo
    private var searchListener: ListenerRegistration? = null

    // Guardar referencia al listener
    private var missingListener: ListenerRegistration? = null

    // Mapa de faltantes
    private val allMissingMap = mutableMapOf<String, MissingProductFirebase>()

    private val monthStart: Timestamp
    private val monthEnd: Timestamp

    val name = mutableStateOf("")
    val quantity = mutableStateOf("")
    val totalPrice = mutableStateOf("")
    val description = mutableStateOf("")
    val errorMessage = mutableStateOf("")
    val productPriceInCents = mutableLongStateOf(0L)

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
            _isLoading.value = false
            listenForMissing(userEmail)
        }
    }

    private fun getMissingRef(userEmail: String): CollectionReference {
        return db.collection("users")
            .document(userEmail)
            .collection("userMyBusinesses")
            .document(DEFAULT_BUSINESS)
            .collection("userMyMissing")
            .document(DEFAULT_MISSING)
            .collection("userMissing")
    }

    private fun listenForMissing(userEmail: String) {
        _isLoading.value = true

        missingListener = getMissingRef(userEmail)
            .orderBy("date", Query.Direction.DESCENDING)
            .orderBy("time", Query.Direction.DESCENDING)
            .whereGreaterThanOrEqualTo("timestamp", monthStart)
            .whereLessThan("timestamp", monthEnd)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Log.e(TAG, "Error escuchando mis faltantes: $error")
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                for (change in snapshot.documentChanges) {
                    val missingId = change.document.id
                    when (change.type) {
                        DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                            val missing = change.document.toObject(MissingProductFirebase::class.java)
                                .copy(missingId = missingId)
                            allMissingMap[missingId] = missing
                        }

                        DocumentChange.Type.REMOVED -> {
                            allMissingMap.remove(missingId)
                        }
                    }
                }
                _groupedMissing.value = groupMissingByYearAndMonth(allMissingMap.values.toList())
                _isLoading.value = false
            }
    }

    // Función para limpiar los campos del formulario
    fun clearMissingFields() {
        name.value = ""
        quantity.value = ""
        totalPrice.value = ""
        description.value = ""
        errorMessage.value = ""
        productPriceInCents.longValue = 0L
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isNotBlank()) {
            searchMissing(query)
        } else {
            // Cancelar listener de búsqueda si la consulta está vacía
            searchListener?.remove()
            _searchResults.value = emptyList()
            _isLoading.value = false
        }
    }

    private fun searchMissing(query: String) {
        val userEmail = auth.currentUser?.email ?: return
        _isLoading.value = true

        searchListener?.remove()

        val missingRef = getMissingRef(userEmail)
        val combinedResults = mutableMapOf<String, MissingProductFirebase>()

        fun updateUiWithResults() {
            _searchResults.value = combinedResults.values.toList()
        }

        val missingIdListener = missingRef
            .whereGreaterThanOrEqualTo("missingId", query)
            .whereLessThanOrEqualTo("missingId", query + '\uf8ff')
            .addSnapshotListener { snapshot, error ->
                _isLoading.value = false
                if (error != null) {
                    Log.e(TAG, "Error buscando por missingId: $error")
                    return@addSnapshotListener
                }

                snapshot?.documentChanges?.forEach { change ->
                    val missing = change.document.toObject(MissingProductFirebase::class.java)
                    when (change.type) {
                        DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> combinedResults[missing.missingId] =
                            missing

                        DocumentChange.Type.REMOVED -> combinedResults.remove(missing.missingId)
                    }
                }
                updateUiWithResults()
            }

        val dateListener = missingRef
            .whereGreaterThanOrEqualTo("date", query)
            .whereLessThanOrEqualTo("date", query + '\uf8ff')
            .addSnapshotListener { snapshot, error ->
                _isLoading.value = false
                if (error != null) {
                    Log.e(TAG, "Error buscando por date: $error")
                    return@addSnapshotListener
                }

                snapshot?.documentChanges?.forEach { change ->
                    val missing = change.document.toObject(MissingProductFirebase::class.java)
                    when (change.type) {
                        DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> combinedResults[missing.missingId] =
                            missing

                        DocumentChange.Type.REMOVED -> combinedResults.remove(missing.missingId)
                    }
                }
                updateUiWithResults()
            }

        searchListener = object : ListenerRegistration {
            override fun remove() {
                missingIdListener.remove()
                dateListener.remove()
            }
        }
    }

    // Clase para agrupar los faltantes por año y mes
    data class GroupedMissing(
        val year: String,
        val month: String,
        val missing: List<MissingProductFirebase>
    )

    private fun groupMissingByYearAndMonth(missing: List<MissingProductFirebase>): List<GroupedMissing> {
        val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        val sorted = missing.sortedByDescending { formatter.parse("${it.date} ${it.time}") }
        val grouped = sorted.groupBy {
            val cal =
                Calendar.getInstance().apply { time = formatter.parse("${it.date} ${it.time}")!! }
            Pair(
                cal.get(Calendar.YEAR).toString(),
                cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())!!
            )
        }
        return grouped.map { (ym, list) ->
            GroupedMissing(ym.first, ym.second, list)
        }.sortedWith(
            compareByDescending<GroupedMissing> { it.year.toInt() }
                .thenByDescending { monthNameToNumber(it.month) }
        )
    }

    private fun monthNameToNumber(monthName: String): Int =
        SimpleDateFormat("MMMM", Locale.getDefault()).parse(monthName)?.let {
            Calendar.getInstance().apply { time = it }.get(Calendar.MONTH)
        } ?: 0

    override fun onCleared() {
        super.onCleared()
        missingListener?.remove()
        searchListener?.remove()
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
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
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