package com.elfrikiamv.minegocio_puntodeventa.ui.screens.activity

// ActivityScreen.kt

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.elfrikiamv.minegocio_puntodeventa.R
import com.elfrikiamv.minegocio_puntodeventa.model.shopping.TicketFirebase
import com.elfrikiamv.minegocio_puntodeventa.navigation.Screen
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.activity.ActivityViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ActivityScreen(navController: NavController) {

    val viewModel: ActivityViewModel = viewModel()
    val grouped by viewModel.groupedTickets.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Estado para controlar posición del scroll
    val listState = rememberLazyListState()

    // Observar búsqueda y resultados en tiempo real
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    //val searchListState = rememberLazyListState()

    var isSearchActive by remember { mutableStateOf(false) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }
            .collect { layoutInfo ->
                val totalItems = layoutInfo.totalItemsCount
                val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                if (lastVisibleItem >= totalItems - 4 && !isLoading && !viewModel.isEndReached) {
                    viewModel.loadNextPage()
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi actividad") }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                // Mostrar indicador de carga mientras los datos están cargándose
                if (isLoading && grouped.isEmpty()) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {

                    /*val onActiveChange: (Boolean) -> Unit = { isSearchActive = it }*/
                    val onActiveChange: (Boolean) -> Unit = {
                        isSearchActive = it
                        if (!it) viewModel.updateSearchQuery("")
                    }

                    SearchBar(
                        inputField = {
                            SearchBarDefaults.InputField(
                                query = searchQuery,
                                onQueryChange = { viewModel.updateSearchQuery(it) },
                                onSearch = { isSearchActive = false },
                                expanded = isSearchActive,
                                onExpandedChange = onActiveChange,
                                enabled = true,
                                placeholder = { Text("Buscar por ID o fecha") },
                                leadingIcon = {
                                    if (isSearchActive) {
                                        IconButton(
                                            onClick = { isSearchActive = false }
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.baseline_arrow_back_ios_new_24),
                                                contentDescription = "Volver"
                                            )
                                        }
                                    } else {
                                        Icon(
                                            painter = painterResource(id = R.drawable.baseline_search_24),
                                            contentDescription = "Buscar"
                                        )
                                    }
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.baseline_close_24),
                                                contentDescription = "Borrar búsqueda"
                                            )
                                        }
                                    }
                                },
                                colors = SearchBarDefaults.inputFieldColors(),
                                interactionSource = null,
                            )
                        },
                        expanded = isSearchActive,
                        onExpandedChange = onActiveChange,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        shape = SearchBarDefaults.inputFieldShape,
                        colors = SearchBarDefaults.colors(),
                        tonalElevation = SearchBarDefaults.TonalElevation,
                        shadowElevation = SearchBarDefaults.ShadowElevation,
                        windowInsets = SearchBarDefaults.windowInsets,
                        content = {

                            if (isLoading) {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            }

                            if (searchQuery.isEmpty() && !isLoading) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    //verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "rules of search.",
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                    )
                                }
                            } else if (searchResults.isEmpty() && searchQuery.isNotEmpty() && !isLoading) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "No se encontraron coincidencias",
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                    )
                                    Text(
                                        "):",
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                    )
                                }
                            } else {
                                LazyColumn(
                                    //state = searchListState,
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .padding(end = 16.dp, start = 16.dp, top = 16.dp)
                                ) {
                                    // Resultados de la búsqueda en su propio LazyColumn dentro del SearchBar
                                    items(searchResults) { ticket ->
                                        TicketCard(ticket = ticket) {
                                            navController.navigate(Screen.DetailsTicket.route + "?ticketId=${ticket.ticketId}")
                                        }
                                    }

                                    /*items(searchResults) { exp ->
                                        ExpensesDetailsCard(exp)
                                    }*/

                                    item {
                                        Spacer(modifier = Modifier.height(72.dp))
                                    }
                                }
                            }
                        },
                    )
                }

                if (searchResults.isEmpty() && !isLoading && searchQuery.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            //.weight(1f)
                            .padding(end = 16.dp, start = 16.dp, top = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "No se encontraron coincidencias.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Text(
                            "):",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                } else if (searchResults.isNotEmpty() && !isLoading && searchQuery.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            //.weight(1f)
                            .padding(end = 16.dp, start = 16.dp, top = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Resultados de la búsqueda:")
                    }
                    LazyColumn(
                        //state = searchListState,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(end = 16.dp, start = 16.dp, top = 16.dp)
                    ) {
                        // Resultados de la búsqueda en su propio LazyColumn
                        items(searchResults) { ticket ->
                            TicketCard(ticket = ticket) {
                                navController.navigate(Screen.DetailsTicket.route + "?ticketId=${ticket.ticketId}")
                            }
                        }
                        /*items(searchResults) { exp ->
                            ExpensesDetailsCard(exp)
                        }*/

                        item {
                            Spacer(modifier = Modifier.height(72.dp))
                        }
                    }
                } else if (grouped.isEmpty() && !isLoading) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No hay tickets disponibles.")
                        Text("):")
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(end = 16.dp, start = 16.dp, top = 16.dp)
                    ) {
                        grouped.forEach { gr ->
                            stickyHeader {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.background)
                                        .padding(start = 16.dp)
                                ) {
                                    Text(
                                        text = "${gr.month} - ${gr.year}",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            }
                            items(gr.tickets) { ticket ->
                                Log.d(
                                    "ActivityScreen",
                                    "Renderizando ticket: ${ticket.ticketId}"
                                )
                                TicketCard(ticket = ticket) {
                                    navController.navigate(Screen.DetailsTicket.route + "?ticketId=${ticket.ticketId}")
                                }
                            }
                        }
                        // Indicador de carga al final
                        item {
                            AnimatedVisibility(
                                visible = isLoading,
                                enter = fadeIn() + slideInVertically { it / 2 },
                                exit = fadeOut() + slideOutVertically { it / 2 }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                        // Mensaje de fin de lista
                        item {
                            if (!isLoading && viewModel.isEndReached) {
                                Text(
                                    text = "No hay más tickets disponibles.",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurface.copy(
                                            alpha = 0.8f
                                        )
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

// Composable para mostrar los detalles de un ticket
@Composable
fun TicketCard(ticket: TicketFirebase, onTicketDetails: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTicketDetails() }
        //.padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                ) {
                    Text(text = "Venta realizada")
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(text = "id. ${ticket.ticketId}")
                }

                Column {
                    Row(
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = ticket.date,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_calendar_today_24),
                            contentDescription = "calendar icon",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        modifier = Modifier.align(Alignment.End)
                    ) {

                        Text(
                            text = ticket.time,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_access_time_24),
                            contentDescription = "clock icon",
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(bottom = 8.dp, top = 8.dp),
                thickness = 1.dp
            )

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {

                Row(
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_shopping_bag_24),
                        contentDescription = "bag icon",
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${ticket.totalProducts} productos",
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
                Text(
                    text = "Total: $${ticket.totalPrice}",
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
    }
}