package com.elfrikiamv.minegocio_puntodeventa.viewmodel

// ActivityViewModel.kt

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import com.elfrikiamv.minegocio_puntodeventa.model.TicketFirebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.io.FileOutputStream

// ViewModel para manejar la lógica de ActivityScreen
class ActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance() // Instancia de Firebase Auth
    private val db = FirebaseFirestore.getInstance() // Instancia de Firestore

    // MutableStateFlow para almacenar la lista de tickets
    private val _tickets = MutableStateFlow<List<TicketFirebase>>(emptyList())
    val tickets: StateFlow<List<TicketFirebase>> = _tickets // StateFlow expuesto a la vista

    init {
        // Cargar los tickets al inicializar el ViewModel
        loadTicketsFromFirebase()
    }

    // Función para cargar los tickets desde Firebase
    private fun loadTicketsFromFirebase() {
        val userEmail = auth.currentUser?.email
        if (userEmail.isNullOrEmpty()) {
            Log.e("ActivityViewModel", "Usuario no autenticado")
            return
        }

        // Escuchar cambios en tiempo real en la colección de tickets
        db.collection("users")
            .document(userEmail)
            .collection("tickets")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("ActivityViewModel", "Error al obtener los tickets: $e")
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val fetchedTickets = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(TicketFirebase::class.java) // Convertir cada documento a objeto Ticket
                    }
                    _tickets.value = fetchedTickets // Actualizar el StateFlow
                    Log.d("ActivityViewModel", "Tickets cargados: ${_tickets.value}")
                } else {
                    Log.d("ActivityViewModel", "No se encontraron tickets")
                }
            }
    }

    //verificar si el ticket existe en Firestore
    fun checkTicketExists(ticketId: String, callback: (TicketFirebase?) -> Unit) {
        val userEmail = auth.currentUser?.email ?: return

        db.collection("users")
            .document(userEmail)
            .collection("tickets")
            .whereEqualTo("ticketId", ticketId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    callback(null) // Producto no encontrado
                } else {
                    val fetchedTicket =
                        documents.documents.first().toObject(TicketFirebase::class.java)
                    callback(fetchedTicket)
                }
            }
            .addOnFailureListener {
                callback(null) // En caso de error
            }
    }

    // Generar un archivo PDF para el ticket
    fun generatePDF(context: Context, ticket: TicketFirebase): File {
        Log.d(
            "ActivityViewModel",
            "Iniciando la generación del PDF para el ticket: ${ticket.ticketId}"
        )

        val pdfDocument = PdfDocument()
        try {
            val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            // Escribir contenido en el PDF
            Log.d("ActivityViewModel", "Escribiendo información del ticket en el PDF.")
            canvas.drawText(
                "Ticket ID: ${ticket.ticketId}",
                10f,
                50f,
                Paint().apply { textSize = 12f })
            canvas.drawText("Fecha: ${ticket.date}", 10f, 70f, Paint().apply { textSize = 12f })
            canvas.drawText("Hora: ${ticket.time}", 10f, 90f, Paint().apply { textSize = 12f })

            ticket.products.forEachIndexed { index, product ->
                val yPosition = 110 + (index * 20)
                canvas.drawText(
                    "${index + 1}. ${product["name"]} - ${product["quantity"]} x $${product["price"]}",
                    10f, yPosition.toFloat(), Paint().apply { textSize = 12f }
                )
                Log.d(
                    "ActivityViewModel",
                    "Producto ${index + 1}: ${product["name"]}, Cantidad: ${product["quantity"]}, Precio: ${product["price"]}"
                )
            }

            canvas.drawText(
                "Total: $${ticket.totalPrice}",
                10f,
                200f,
                Paint().apply { textSize = 14f })
            pdfDocument.finishPage(page)

            // Guardar archivo en caché
            val file = File(context.cacheDir, "ticket_${ticket.ticketId}.pdf")
            Log.d("ActivityViewModel", "Guardando el archivo en caché: ${file.absolutePath}")
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()

            Log.d("ActivityViewModel", "PDF generado con éxito: ${file.absolutePath}")
            return file

        } catch (e: Exception) {
            Log.e("ActivityViewModel", "Error al generar el PDF: ${e.message}")
            pdfDocument.close()
            throw e
        }
    }

    // Enviar ticket por correo
    fun sendEmail(context: Context, email: String, file: File) {
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
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            Log.d("ActivityViewModel", "Intent de correo creado correctamente.")
            context.startActivity(Intent.createChooser(emailIntent, "Compartir archivo"))
            Log.d("ActivityViewModel", "Correo enviado exitosamente.")

        } catch (e: Exception) {
            Log.e("ActivityViewModel", "Error al enviar el correo: ${e.message}")
        }
    }

}