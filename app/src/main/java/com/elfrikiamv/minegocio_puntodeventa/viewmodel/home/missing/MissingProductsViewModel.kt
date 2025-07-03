package com.elfrikiamv.minegocio_puntodeventa.viewmodel.home.missing

// MissingProductsViewModel.kt

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.elfrikiamv.minegocio_puntodeventa.database.AppDatabase
import com.elfrikiamv.minegocio_puntodeventa.model.home.missing.MissingProductEntity
import com.elfrikiamv.minegocio_puntodeventa.model.home.missing.MissingProductFirebase
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
import java.util.Locale

class MissingProductsViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "MissingListViewModel"
        private const val DEFAULT_BUSINESS = "defaultBusiness"
        private const val DEFAULT_MISSING = "defaultMissing"
        private const val PAGE_SIZE = 420
    }

    private val missingDao = AppDatabase.getDatabase(application).missingProductDao()

    // Instancias de Firebase
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // MutableStateFlow para almacenar la lista de faltantes
    /*private val _missingList = MutableStateFlow<List<MissingProductFirebase>>(emptyList())
    val missingList: StateFlow<List<MissingProductFirebase>> = _missingList*/

    // StateFlow para los missing agrupados por año y mes
    private val _groupedMissing = MutableStateFlow<List<GroupedMissing>>(emptyList())
    val groupedMissing: StateFlow<List<GroupedMissing>> = _groupedMissing

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
    private val _searchResults = MutableStateFlow<List<MissingProductFirebase>>(emptyList())
    val searchResults: StateFlow<List<MissingProductFirebase>> = _searchResults

    // Listener de búsqueda activo
    private var searchListener: ListenerRegistration? = null

    // Guardar referencia al listener
    private var missingListener: ListenerRegistration? = null

    val name = mutableStateOf("")
    val quantity = mutableStateOf("")
    val totalPrice = mutableStateOf("")
    val description = mutableStateOf("")
    val errorMessage = mutableStateOf("")
    val productPriceInCents = mutableLongStateOf(0L)

    init {
        val userEmail = auth.currentUser?.email
        if (userEmail == null) {
            Log.e(TAG, "Usuario no autenticado. No se pueden cargar los datos.")
        } else {
            Log.d(TAG, "Usuario autenticado: $userEmail")
            //loadMissingFromFirebase(userEmail)
            loadNextPage()
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

        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        searchMissingProductsByIdOrDate(query)
    }

    private fun searchMissingProductsByIdOrDate(query: String) {
        val userEmail = auth.currentUser?.email ?: return
        _isLoading.value = true

        val missingRef = getMissingRef(userEmail)

        // Cancelar listeners anteriores para evitar fugas y llamadas duplicadas
        searchListener?.remove()

        // Mapa mutable para mantener los resultados combinados sin duplicados
        val combinedResults = mutableMapOf<String, MissingProductFirebase>()

        // Función para actualizar el StateFlow cuando ambos listeners hayan cargado al menos una vez
        fun updateResults() {
            _searchResults.value = combinedResults.values.toList()
            _isLoading.value = false
        }

        // Flag para saber si ya recibimos datos al menos una vez en cada listener
        var missingIdReady = false
        var dateReady = false

        // Listener para búsqueda por missingId con prefijo (rango Unicode)
        val missingIdListener = missingRef
            .whereGreaterThanOrEqualTo("missingId", query)
            .whereLessThanOrEqualTo("missingId", query + '\uf8ff')
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error buscando por missingId: $error")
                    _searchResults.value = emptyList()
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                // Procesar los cambios incrementales para mantener combinedResults actualizado
                snapshot?.documentChanges?.forEach { change ->
                    val missing = change.document.toObject(MissingProductFirebase::class.java)
                    when (change.type) {
                        DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> combinedResults[missing.missingId] =
                            missing

                        DocumentChange.Type.REMOVED -> combinedResults.remove(missing.missingId)
                    }
                }

                missingIdReady = true
                if (missingIdReady && dateReady) {
                    updateResults()
                }
            }

        // Listener para búsqueda por date con prefijo (rango Unicode)
        val dateListener = missingRef
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
                    val missing = change.document.toObject(MissingProductFirebase::class.java)
                    when (change.type) {
                        DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> combinedResults[missing.missingId] =
                            missing

                        DocumentChange.Type.REMOVED -> combinedResults.remove(missing.missingId)
                    }
                }

                dateReady = true
                if (missingIdReady && dateReady) {
                    updateResults()
                }
            }

        // Guardar la referencia combinada para cancelar ambos listeners juntos
        searchListener = object : ListenerRegistration {
            override fun remove() {
                missingIdListener.remove()
                dateListener.remove()
            }
        }
    }

    // Función para cargar la lista de faltantes desde Firebase
    fun loadNextPage() {
        val userEmail = auth.currentUser?.email ?: return
        if (_isLoading.value || endReached) return

        _isLoading.value = true

        var query = getMissingRef(userEmail)
            .orderBy("date", Query.Direction.DESCENDING)
            .orderBy("time", Query.Direction.DESCENDING)
            .limit(PAGE_SIZE.toLong())

        lastSnapshot?.let { query = query.startAfter(it) }

        query.get().addOnSuccessListener { snap ->
            val list = snap.documents.mapNotNull { it.toObject(MissingProductFirebase::class.java) }
            if (list.size < PAGE_SIZE) endReached = true
            lastSnapshot = snap.documents.lastOrNull()

            val combined = _groupedMissing.value.flatMap { it.missing } + list
            _groupedMissing.value = groupMissingByYearAndMonth(combined)

            _isLoading.value = false

            // Cuando termina carga inicial, activa realtime listener una sola vez
            if (missingListener == null) {
                startRealtimeUpdates()
            }

        }.addOnFailureListener {
            Log.e(TAG, "Error cargando gastos: $it")
            _isLoading.value = false
        }
    }

    private fun startRealtimeUpdates() {
        val userEmail = auth.currentUser?.email ?: return
        missingListener = getMissingRef(userEmail)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error en realtime listener: $error")
                    return@addSnapshotListener
                }

                snapshot?.let {
                    // Obtener gastos actuales como Map
                    val currentMissing = _groupedMissing.value.flatMap { it.missing }
                        .associateBy { it.missingId }
                        .toMutableMap()

                    // Procesar cada cambio en documentos
                    for (change in it.documentChanges) {
                        val missing = change.document.toObject(MissingProductFirebase::class.java)
                        when (change.type) {
                            DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                                currentMissing[missing.missingId] = missing
                            }

                            DocumentChange.Type.REMOVED -> {
                                currentMissing.remove(missing.missingId)
                            }
                        }
                    }

                    // Volver a agrupar y emitir el nuevo listado actualizado
                    val mergedMissing = currentMissing.values.toList()
                    _groupedMissing.value = groupMissingByYearAndMonth(mergedMissing)
                }
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