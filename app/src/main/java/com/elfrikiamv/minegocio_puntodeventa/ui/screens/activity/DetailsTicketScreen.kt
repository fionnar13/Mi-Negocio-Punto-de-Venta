package com.elfrikiamv.minegocio_puntodeventa.ui.screens.activity

// DetailsTicketScreen.kt

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.elfrikiamv.minegocio_puntodeventa.R
import com.elfrikiamv.minegocio_puntodeventa.model.TicketFirebase
import com.elfrikiamv.minegocio_puntodeventa.navigation.Screen
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.ActivityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsTicketScreen(navController: NavController, ticketId: String?) {
    val viewModel: ActivityViewModel = viewModel()
    var ticketDetails by remember { mutableStateOf<TicketFirebase?>(null) }

    // Cargar los detalles del ticket
    LaunchedEffect(ticketId) {
        ticketId?.let {
            viewModel.checkTicketExists(it) { fetchedTicket ->
                ticketDetails = fetchedTicket
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles del ticket") },
                navigationIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_arrow_back_ios_new_24),
                        contentDescription = "Regresar",
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clickable {
                                navController.navigate(Screen.Activity.route) {
                                    popUpTo(Screen.Activity.route) { inclusive = true }
                                }
                            }
                    )
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {

                // Mostrar los detalles del ticket
                if (ticketDetails == null) {
                    Text(
                        "Cargando detalles del ticket.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    // Mostrar detalles del producto
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = "Ticket ID: ${ticketDetails!!.ticketId}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "products: ${ticketDetails!!.products}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "totalPrice: ${ticketDetails!!.totalPrice}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "totalProducts: ${ticketDetails!!.totalProducts}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "amountReceived: ${ticketDetails!!.amountReceived}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "change: ${ticketDetails!!.change}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "date: ${ticketDetails!!.date}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "time: ${ticketDetails!!.time}",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    )
}