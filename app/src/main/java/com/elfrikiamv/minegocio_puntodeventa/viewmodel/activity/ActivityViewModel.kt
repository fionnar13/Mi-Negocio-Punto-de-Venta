package com.elfrikiamv.minegocio_puntodeventa.viewmodel.activity

// ActivityViewModel.kt

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import com.elfrikiamv.minegocio_puntodeventa.model.shopping.TicketFirebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
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
import kotlinx.coroutines.flow.asStateFlow
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
    }

    private val auth = FirebaseAuth.getInstance() // Instancia de Firebase Auth
    private val db = FirebaseFirestore.getInstance() // Instancia de Firestore

    // StateFlow para los tickets agrupados por año y mes
    private val _groupedTickets = MutableStateFlow<List<GroupedTickets>>(emptyList())
    val groupedTickets = _groupedTickets.asStateFlow()

    // campo para el estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Estado para query de búsqueda
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Estado para resultados de búsqueda
    private val _searchResults = MutableStateFlow<List<TicketFirebase>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    // StateFlow para el switch de todos los tickets
    private val _showAllTickets = MutableStateFlow(false)
    val showAllTickets = _showAllTickets.asStateFlow()

    // Listener de búsqueda activo
    private var searchListener: ListenerRegistration? = null

    // Guardar referencia al listener
    private var ticketsListener: ListenerRegistration? = null

    // Mapa de tickets
    private val allTicketsMap = mutableMapOf<String, TicketFirebase>()

    private val monthStart: Timestamp
    private val monthEnd: Timestamp

    // ID del banner Admo
    private val _bannerID = MutableStateFlow("ca-app-pub-3940256099942544/9214589741")
    val bannerID = _bannerID.asStateFlow()

    // Cargar los tickets del mes actual al iniciar el ViewModel
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
            /*Log.e(TAG, "Usuario no autenticado. No se pueden cargar los datos.")*/
            _isLoading.value = false
        } else {
            /*Log.d(TAG, "Usuario autenticado: $userEmail")*/
            _isLoading.value = false
            listenForTickets(userEmail)
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

    fun onShowAllTicketsChange(showAll: Boolean) {
        if (_showAllTickets.value == showAll) return // Evitar recargas innecesarias
        _showAllTickets.value = showAll
        auth.currentUser?.email?.let {
            listenForTickets(it)
        }
    }

    private fun listenForTickets(userEmail: String) {
        _isLoading.value = true
        ticketsListener?.remove() // Cancelar el listener anterior
        allTicketsMap.clear() // Limpiar el mapa de tickets
        _groupedTickets.value =
            emptyList() // Limpiar la lista de tickets para la actualización de la UI


        var query: Query = getTicketsRef(userEmail)
            .orderBy("date", Query.Direction.DESCENDING)
            .orderBy("time", Query.Direction.DESCENDING)

        // Solo aplicar el filtro de fecha si no se quieren mostrar todos los tickets
        if (!_showAllTickets.value) {
            query = query.whereGreaterThanOrEqualTo("timestamp", monthStart)
                .whereLessThan("timestamp", monthEnd)
        }

        ticketsListener = query.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                /*Log.e(TAG, "Error escuchando tickets: $error")*/
                _isLoading.value = false
                return@addSnapshotListener
            }

            for (change in snapshot.documentChanges) {
                val ticketId = change.document.id
                when (change.type) {
                    DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                        val ticket = change.document.toObject(TicketFirebase::class.java)
                            .copy(ticketId = ticketId)
                        allTicketsMap[ticketId] = ticket
                    }

                    DocumentChange.Type.REMOVED -> {
                        allTicketsMap.remove(ticketId)
                    }
                }
            }
            _groupedTickets.value = groupTicketsByYearAndMonth(allTicketsMap.values.toList())
            _isLoading.value = false
        }
    }

    // Actualizar el query de búsqueda
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isNotBlank()) {
            searchTickets(query)
        } else {
            // Cancelar listener de búsqueda si la consulta está vacía
            searchListener?.remove()
            _searchResults.value = emptyList()
            _isLoading.value = false
        }
    }

    private fun searchTickets(query: String) {
        val userEmail = auth.currentUser?.email ?: return
        _isLoading.value = true

        searchListener?.remove()

        val ticketsRef = getTicketsRef(userEmail)
        val combinedResults = mutableMapOf<String, TicketFirebase>()

        fun updateUiWithResults() {
            _searchResults.value = combinedResults.values.toList()
        }

        val ticketIdListener = ticketsRef
            .whereGreaterThanOrEqualTo("ticketId", query)
            .whereLessThanOrEqualTo("ticketId", query + '\uf8ff')
            .addSnapshotListener { snapshot, error ->
                _isLoading.value = false
                if (error != null) {
                    /*Log.e(TAG, "Error buscando por ticketId: $error")*/
                    return@addSnapshotListener
                }

                snapshot?.documentChanges?.forEach { change ->
                    val ticket = change.document.toObject(TicketFirebase::class.java)
                    when (change.type) {
                        DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> combinedResults[ticket.ticketId] =
                            ticket

                        DocumentChange.Type.REMOVED -> combinedResults.remove(ticket.ticketId)
                    }
                }
                updateUiWithResults()
            }

        val dateListener = ticketsRef
            .whereGreaterThanOrEqualTo("date", query)
            .whereLessThanOrEqualTo("date", query + '\uf8ff')
            .addSnapshotListener { snapshot, error ->
                _isLoading.value = false
                if (error != null) {
                    /*Log.e(TAG, "Error buscando por date: $error")*/
                    return@addSnapshotListener
                }

                snapshot?.documentChanges?.forEach { change ->
                    val ticket = change.document.toObject(TicketFirebase::class.java)
                    when (change.type) {
                        DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> combinedResults[ticket.ticketId] =
                            ticket

                        DocumentChange.Type.REMOVED -> combinedResults.remove(ticket.ticketId)
                    }
                }
                updateUiWithResults()
            }

        searchListener = object : ListenerRegistration {
            override fun remove() {
                ticketIdListener.remove()
                dateListener.remove()
            }
        }
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
        _isLoading.value = true
        val userEmail = auth.currentUser?.email ?: return

        getTicketsRef(userEmail)
            .whereEqualTo("ticketId", ticketId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    callback(null)
                } else {
                    val fetchedTicket =
                        documents.documents.first().toObject(TicketFirebase::class.java)
                    callback(fetchedTicket)
                }
                _isLoading.value = false
            }
            .addOnFailureListener {
                callback(null)
                _isLoading.value = false
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
        /*Log.d("ActivityViewModel", "Preparando para enviar el ticket por correo.")*/
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
            /*Log.d("ActivityViewModel", "URI del archivo generado: $fileUri")*/

            val emailIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                putExtra(Intent.EXTRA_SUBJECT, "Detalles del Ticket")
                putExtra(Intent.EXTRA_TEXT, "Adjunto encontrarás los detalles del ticket.")
                putExtra(Intent.EXTRA_STREAM, fileUri)
                setPackage("com.google.android.gm") // Use Gmail app
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            /*Log.d("ActivityViewModel", "Intent de correo creado correctamente.")*/
            context.startActivity(Intent.createChooser(emailIntent, "Compartir archivo"))
            /*Log.d("ActivityViewModel", "Correo enviado exitosamente.")*/
            _isLoading.value = false // Ocultar indicador de carga

        } catch (e: Exception) {
            /*Log.e("ActivityViewModel", "Error al enviar el correo: ${e.message}")*/
            _isLoading.value = false // Ocultar indicador de carga
        }
    }

}