package com.elfrikiamv.minegocio_puntodeventa.viewmodel

// ActivityViewModel.kt

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import com.elfrikiamv.minegocio_puntodeventa.model.TicketFirebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

// ViewModel para manejar la lógica de ActivityScreen
class ActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance() // Instancia de Firebase Auth
    private val db = FirebaseFirestore.getInstance() // Instancia de Firestore

    // MutableStateFlow para almacenar la lista de tickets
    private val _tickets = MutableStateFlow<List<TicketFirebase>>(emptyList())
    val tickets: StateFlow<List<TicketFirebase>> = _tickets // StateFlow expuesto a la vista

    private val userMyBusinesses = "defaultBusiness"

    private val userMyTickets = "defaultTickets"

    // Nuevo campo para el estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        // Cargar los tickets al inicializar el ViewModel
        loadTicketsFromFirebase()
    }

    // Función para cargar los tickets desde Firebase
    private fun loadTicketsFromFirebase() {

        _isLoading.value = true // Mostrar indicador de carga
        val userEmail = auth.currentUser?.email
        if (userEmail.isNullOrEmpty()) {
            Log.e("ActivityViewModel", "Usuario no autenticado")
            return
        }

        // Escuchar cambios en tiempo real en la colección de tickets
        db.collection("users")
            .document(userEmail)
            .collection("userMyBusinesses")
            .document(userMyBusinesses)
            .collection("userMyTickets")
            .document(userMyTickets)
            .collection("userTickets")

            //.collection("tickets")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("ActivityViewModel", "Error al obtener los tickets: $e")
                    _isLoading.value = false // Ocultar indicador de carga en caso de error
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val fetchedTickets = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(TicketFirebase::class.java) // Convertir cada documento a objeto Ticket
                    }
                    _tickets.value = fetchedTickets // Actualizar el StateFlow
                    Log.d("ActivityViewModel", "Tickets cargados: ${_tickets.value}")
                    _isLoading.value = false // Ocultar indicador de carga
                } else {
                    Log.d("ActivityViewModel", "No se encontraron tickets")
                    _isLoading.value = false // Ocultar indicador de carga
                }
            }
    }

    //verificar si el ticket existe en Firestore
    fun checkTicketExists(ticketId: String, callback: (TicketFirebase?) -> Unit) {

        _isLoading.value = true // Mostrar indicador de carga
        val userEmail = auth.currentUser?.email ?: return

        db.collection("users")
            .document(userEmail)
            .collection("userMyBusinesses")
            .document(userMyBusinesses)
            .collection("userMyTickets")
            .document(userMyTickets)
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