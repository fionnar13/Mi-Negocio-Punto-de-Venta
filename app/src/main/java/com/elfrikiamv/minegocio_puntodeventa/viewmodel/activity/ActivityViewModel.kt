package com.elfrikiamv.minegocio_puntodeventa.viewmodel.activity

// ActivityViewModel.kt

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import com.elfrikiamv.minegocio_puntodeventa.model.shopping.TicketFirebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// ViewModel para manejar la lógica de ActivityScreen
class ActivityViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "ActivityViewModel"
        private const val DEFAULT_BUSINESS = "defaultBusiness"
        private const val DEFAULT_TICKETS = "defaultTickets"
        private const val PAGE_SIZE = 420
    }

    private val auth = FirebaseAuth.getInstance() // Instancia de Firebase Auth
    private val db = FirebaseFirestore.getInstance() // Instancia de Firestore

    // StateFlow para los tickets agrupados por año y mes
    private val _groupedTickets = MutableStateFlow<List<GroupedTickets>>(emptyList())
    val groupedTickets: StateFlow<List<GroupedTickets>> = _groupedTickets

    // campo para el estado de carga
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
    private val _searchResults = MutableStateFlow<List<TicketFirebase>>(emptyList())
    val searchResults: StateFlow<List<TicketFirebase>> = _searchResults

    // Listener de búsqueda activo
    private var searchListener: ListenerRegistration? = null

    // Guardar referencia al listener
    private var ticketsListener: ListenerRegistration? = null

    init {
        val userEmail = auth.currentUser?.email
        if (userEmail == null) {
            Log.e(TAG, "Usuario no autenticado. No se pueden cargar los datos.")
        } else {
            Log.d(TAG, "Usuario autenticado: $userEmail")
            loadNextPage()
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query

        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        searchTicketByIdOrDate(query)
    }

    private fun searchTicketByIdOrDate(query: String) {
        val userEmail = auth.currentUser?.email ?: return
        _isLoading.value = true

        val ticketsRef = getTicketsRef(userEmail)

        // Cancelar listeners anteriores para evitar fugas y llamadas duplicadas
        searchListener?.remove()

        // Mapa mutable para mantener los resultados combinados sin duplicados
        val combinedResults = mutableMapOf<String, TicketFirebase>()

        // Función para actualizar el StateFlow cuando ambos listeners hayan cargado al menos una vez
        fun updateResults() {
            _searchResults.value = combinedResults.values.toList()
            _isLoading.value = false
        }

        // Flag para saber si ya recibimos datos al menos una vez en cada listener
        var ticketIdReady = false
        var dateReady = false

        // Listener para búsqueda por ticketId con prefijo (rango Unicode)
        val ticketIdListener = ticketsRef
            .whereGreaterThanOrEqualTo("ticketId", query)
            .whereLessThanOrEqualTo("ticketId", query + '\uf8ff')
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error buscando por ticketId: $error")
                    _searchResults.value = emptyList()
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                // Procesar los cambios incrementales para mantener combinedResults actualizado
                snapshot?.documentChanges?.forEach { change ->
                    val ticket = change.document.toObject(TicketFirebase::class.java)
                    when (change.type) {
                        DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> combinedResults[ticket.ticketId] =
                            ticket

                        DocumentChange.Type.REMOVED -> combinedResults.remove(ticket.ticketId)
                    }
                }

                ticketIdReady = true
                if (ticketIdReady && dateReady) {
                    updateResults()
                }
            }

        // Listener para búsqueda por date con prefijo (rango Unicode)
        val dateListener = ticketsRef
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
                    val ticket = change.document.toObject(TicketFirebase::class.java)
                    when (change.type) {
                        DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> combinedResults[ticket.ticketId] =
                            ticket

                        DocumentChange.Type.REMOVED -> combinedResults.remove(ticket.ticketId)
                    }
                }

                dateReady = true
                if (ticketIdReady && dateReady) {
                    updateResults()
                }
            }

        // Guardar la referencia combinada para cancelar ambos listeners juntos
        searchListener = object : ListenerRegistration {
            override fun remove() {
                ticketIdListener.remove()
                dateListener.remove()
            }
        }
    }

    // Paginación mejorada: carga siguiente página desde Firestore
    fun loadNextPage() {
        val userEmail = auth.currentUser?.email ?: return
        if (_isLoading.value || endReached) return

        _isLoading.value = true

        var query = getTicketsRef(userEmail)
            .orderBy("date", Query.Direction.DESCENDING)
            .orderBy("time", Query.Direction.DESCENDING)
            .limit(PAGE_SIZE.toLong())

        lastSnapshot?.let { query = query.startAfter(it) }

        query.get().addOnSuccessListener { snap ->
            val list = snap.documents.mapNotNull { it.toObject(TicketFirebase::class.java) }
            if (list.size < PAGE_SIZE) endReached = true
            lastSnapshot = snap.documents.lastOrNull()

            val combined = _groupedTickets.value.flatMap { it.tickets } + list
            _groupedTickets.value = groupTicketsByYearAndMonth(combined)

            _isLoading.value = false

            // Cuando termina carga inicial, activa realtime listener una sola vez
            if (ticketsListener == null) {
                startRealtimeUpdates()
            }

        }.addOnFailureListener {
            Log.e(TAG, "Error cargando gastos: $it")
            _isLoading.value = false
        }
    }

    private fun startRealtimeUpdates() {
        val userEmail = auth.currentUser?.email ?: return
        ticketsListener = getTicketsRef(userEmail)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error en realtime listener: $error")
                    return@addSnapshotListener
                }

                snapshot?.let {
                    // Obtener gastos actuales como Map
                    val currentTickets = _groupedTickets.value.flatMap { it.tickets }
                        .associateBy { it.ticketId }
                        .toMutableMap()

                    // Procesar cada cambio en documentos
                    for (change in it.documentChanges) {
                        val ticket = change.document.toObject(TicketFirebase::class.java)
                        when (change.type) {
                            DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                                currentTickets[ticket.ticketId] = ticket
                            }

                            DocumentChange.Type.REMOVED -> {
                                currentTickets.remove(ticket.ticketId)
                            }
                        }
                    }

                    // Volver a agrupar y emitir el nuevo listado actualizado
                    val mergedTickets = currentTickets.values.toList()
                    _groupedTickets.value = groupTicketsByYearAndMonth(mergedTickets)
                }
            }
    }


    private fun getTicketsRef(userEmail: String): CollectionReference {
        return db.collection("users")
            .document(userEmail)
            .collection("userMyBusinesses")
            .document(DEFAULT_BUSINESS)
            .collection("userMyTickets")
            .document(DEFAULT_TICKETS)
            .collection("userTickets")
    }

    // Agrupación y ordenación
    data class GroupedTickets(
        val year: String,
        val month: String,
        val tickets: List<TicketFirebase>
    )

    private fun groupTicketsByYearAndMonth(tickets: List<TicketFirebase>): List<GroupedTickets> {
        val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        val sorted = tickets.sortedByDescending { formatter.parse("${it.date} ${it.time}") }
        val grouped = sorted.groupBy {
            val cal =
                Calendar.getInstance().apply { time = formatter.parse("${it.date} ${it.time}")!! }
            Pair(
                cal.get(Calendar.YEAR).toString(),
                cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())!!
            )
        }
        return grouped.map { (ym, list) ->
            GroupedTickets(ym.first, ym.second, list)
        }.sortedWith(
            compareByDescending<GroupedTickets> { it.year.toInt() }
                .thenByDescending { monthNameToNumber(it.month) }
        )
    }

    private fun monthNameToNumber(monthName: String): Int =
        SimpleDateFormat("MMMM", Locale.getDefault()).parse(monthName)?.let {
            Calendar.getInstance().apply { time = it }.get(Calendar.MONTH)
        } ?: 0

    override fun onCleared() {
        super.onCleared()
        ticketsListener?.remove()
        searchListener?.remove()
    }

    //verificar si el ticket existe en Firestore
    fun checkTicketExists(ticketId: String, callback: (TicketFirebase?) -> Unit) {

        _isLoading.value = true // Mostrar indicador de carga
        val userEmail = auth.currentUser?.email ?: return

        db.collection("users")
            .document(userEmail)
            .collection("userMyBusinesses")
            .document(DEFAULT_BUSINESS)
            .collection("userMyTickets")
            .document(DEFAULT_TICKETS)
            .collection("userTickets")

            //.collection("tickets")
            .whereEqualTo("ticketId", ticketId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    callback(null) // Producto no encontrado
                    _isLoading.value = false // Ocultar indicador de carga
                } else {
                    val fetchedTicket =
                        documents.documents.first().toObject(TicketFirebase::class.java)
                    callback(fetchedTicket)
                    _isLoading.value = false // Ocultar indicador de carga
                }
            }
            .addOnFailureListener {
                callback(null) // En caso de error
                _isLoading.value = false // Ocultar indicador de carga
            }
    }

    // Generar un archivo PDF para el ticket
    fun generatePDF(context: Context, ticket: TicketFirebase): File {

        _isLoading.value = true // Mostrar indicador de carga
        // Enable Android asset loading
        PDFBoxResourceLoader.init(context)

        val document = PDDocument()
        val page = PDPage(PDRectangle.A4)
        document.addPage(page)

        try {
            val contentStream = PDPageContentStream(document, page)

            // Configurar fuente y tamaño
            val font = PDType1Font.HELVETICA
            val fontSize = 12f
            val yPosition = page.mediaBox.height - 50 // Posición inicial en Y

            contentStream.beginText()
            contentStream.setFont(font, fontSize)
            contentStream.newLineAtOffset(50f, yPosition)

            // Agregar encabezado
            contentStream.showText("Detalles del Ticket")
            contentStream.newLineAtOffset(0f, -20f)
            contentStream.showText("Ticket ID: ${ticket.ticketId}")
            contentStream.newLineAtOffset(0f, -15f)
            contentStream.showText("Fecha: ${ticket.date}")
            contentStream.newLineAtOffset(0f, -15f)
            contentStream.showText("Hora: ${ticket.time}")
            contentStream.newLineAtOffset(0f, -20f)

            // Agregar productos
            contentStream.showText("Productos:")
            contentStream.newLineAtOffset(0f, -15f)
            ticket.products.forEachIndexed { index, product ->
                val productText =
                    "${index + 1}. (${product["barcode"]}) ${product["name"]} - ${product["quantity"]} x $${product["salePrice"]}"
                contentStream.showText(productText)
                contentStream.newLineAtOffset(0f, -15f)
            }

            // Agregar totales
            contentStream.newLineAtOffset(0f, -20f)
            contentStream.showText("Total de Productos: ${ticket.totalProducts}")
            contentStream.newLineAtOffset(0f, -15f)
            contentStream.showText("Total Precio: $${ticket.totalPrice}")
            contentStream.newLineAtOffset(0f, -15f)
            contentStream.showText("Pago Recibido: $${ticket.amountReceived}")
            contentStream.newLineAtOffset(0f, -15f)
            contentStream.showText("Cambio: $${ticket.change}")

            contentStream.endText()
            contentStream.close()

            // Guardar archivo en caché
            val file = File(context.cacheDir, "ticket_${ticket.ticketId}.pdf")
            document.save(file)
            document.close()

            _isLoading.value = false // Ocultar indicador de carga
            return file

        } catch (e: Exception) {
            document.close()
            _isLoading.value = false // Ocultar indicador de carga
            throw e
        }
    }

    // Enviar ticket por correo
    fun sendEmail(context: Context, email: String, file: File) {

        _isLoading.value = true // Mostrar indicador de carga
        Log.d("ActivityViewModel", "Preparando para enviar el ticket por correo.")
        Log.d(
            "ActivityViewModel",
            "Archivo a adjuntar: ${file.absolutePath}, Email destinatario: $email"
        )

        try {
            val fileUri = FileProvider.getUriForFile(
                context,
                "com.elfrikiamv.minegocio_puntodeventa.fileprovider",
                file
            )
            Log.d("ActivityViewModel", "URI del archivo generado: $fileUri")

            val emailIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                putExtra(Intent.EXTRA_SUBJECT, "Detalles del Ticket")
                putExtra(Intent.EXTRA_TEXT, "Adjunto encontrarás los detalles del ticket.")
                putExtra(Intent.EXTRA_STREAM, fileUri)
                setPackage("com.google.android.gm") // Use Gmail app
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            Log.d("ActivityViewModel", "Intent de correo creado correctamente.")
            context.startActivity(Intent.createChooser(emailIntent, "Compartir archivo"))
            Log.d("ActivityViewModel", "Correo enviado exitosamente.")
            _isLoading.value = false // Ocultar indicador de carga

        } catch (e: Exception) {
            Log.e("ActivityViewModel", "Error al enviar el correo: ${e.message}")
            _isLoading.value = false // Ocultar indicador de carga
        }
    }

}