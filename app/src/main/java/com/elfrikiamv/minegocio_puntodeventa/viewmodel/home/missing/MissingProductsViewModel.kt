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
import com.google.firebase.firestore.DocumentSnapshot
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

    // Función para cargar la lista de faltantes desde Firebase
    fun loadNextPage() {
        val userEmail = auth.currentUser?.email ?: return
        if (_isLoading.value || endReached) return

        _isLoading.value = true

        var query = db.collection("users")
            .document(userEmail)
            .collection("userMyBusinesses")
            .document(DEFAULT_BUSINESS)
            .collection("userMyMissing")
            .document(DEFAULT_MISSING)
            .collection("userMissing")
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .orderBy("time", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(PAGE_SIZE.toLong())

        lastSnapshot?.let { query = query.startAfter(it) }

        query.get().addOnSuccessListener { snap ->
            val list = snap.documents.mapNotNull { it.toObject(MissingProductFirebase::class.java) }
            if (list.size < PAGE_SIZE) endReached = true
            lastSnapshot = snap.documents.lastOrNull()

            // Genera la agrupación reactiva combinando lo que ya había con lo nuevo
            val allMissing = _groupedMissing.value.flatMap { it.missing } + list
            _groupedMissing.value = groupMissingByYearAndMonth(allMissing)

            _isLoading.value = false
        }.addOnFailureListener {
            Log.e(TAG, "Error cargando faltantes: $it")
            _isLoading.value = false
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
            val cal = Calendar.getInstance().apply {
                time = formatter.parse("${it.date} ${it.time}")!!
            }
            val yr = cal.get(Calendar.YEAR).toString()
            val mo = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())!!
            Pair(yr, mo)
        }
        return grouped
            .map { (ym, list) -> GroupedMissing(ym.first, ym.second, list) }
            .sortedWith(compareByDescending<GroupedMissing> { it.year.toInt() }
                .thenByDescending { monthNameToNumber(it.month) })
    }

    private fun monthNameToNumber(monthName: String): Int =
        SimpleDateFormat("MMMM", Locale.getDefault())
            .parse(monthName)?.let {
                Calendar.getInstance().apply { time = it }.get(Calendar.MONTH)
            } ?: 0

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