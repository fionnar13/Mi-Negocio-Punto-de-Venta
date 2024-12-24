package com.elfrikiamv.minegocio_puntodeventa.viewmodel

// ActivityViewModel.kt

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.elfrikiamv.minegocio_puntodeventa.model.TicketFirebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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
}
