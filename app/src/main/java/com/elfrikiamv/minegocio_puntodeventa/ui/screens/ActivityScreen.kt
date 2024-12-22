package com.elfrikiamv.minegocio_puntodeventa.ui.screens

// ActivityScreen.kt

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elfrikiamv.minegocio_puntodeventa.model.Ticket
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.ActivityViewModel

@Composable
fun ActivityScreen(
    modifier: Modifier = Modifier,
    viewModel: ActivityViewModel = viewModel()
) {
    // Observar la lista de tickets desde el ViewModel
    val tickets = viewModel.tickets.collectAsState()

    // Mostrar los tickets en una lista
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(tickets.value) { ticket ->
            TicketCard(ticket = ticket)
        }
    }
}

// Composable para mostrar los detalles de un ticket
@Composable
fun TicketCard(ticket: Ticket) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "ID: ${ticket.ticketId}")
            Text(text = "Fecha y Hora: ${ticket.dateTime}")
            Text(text = "Total: ${ticket.totalPrice}")
            Text(text = "Productos:")
            ticket.products.forEach { product ->
                // Accede a las propiedades usando las claves del mapa
                val name = product["name"] as? String ?: "Desconocido"
                val quantity = product["quantity"] as? Int ?: 0
                val price = product["price"] as? Double ?: 0.0

                Text(
                    text = "- $name x$quantity @${price}",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}