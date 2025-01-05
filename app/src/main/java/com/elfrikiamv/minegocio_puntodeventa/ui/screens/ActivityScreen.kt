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
import com.elfrikiamv.minegocio_puntodeventa.model.TicketFirebase
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
fun TicketCard(ticket: TicketFirebase) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "ID: ${ticket.ticketId}")
            Text(text = "Fecha y Hora: ${ticket.date}, ${ticket.time}")
            Text(text = "Productos:")
            // Iterar sobre los productos
            ticket.products.forEach { product ->
                // Acceder a las propiedades del mapa con conversiones seguras
                val name = product["name"] as? String ?: "Desconocido"
                val quantity = (product["quantity"] as? Number)?.toInt() ?: 0
                val price = (product["price"] as? Number)?.toDouble() ?: 0.0

                // Mostrar el producto
                Text(
                    text = "- $name",
                    modifier = Modifier.padding(start = 8.dp)
                )
                Text(
                    text = "$$price x$quantity = $${price * quantity}",
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            Text(text = "Total: $${ticket.totalPrice}")
        }
    }
}