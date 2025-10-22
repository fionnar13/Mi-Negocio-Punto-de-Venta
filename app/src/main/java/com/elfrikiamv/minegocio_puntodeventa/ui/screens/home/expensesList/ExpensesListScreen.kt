package com.elfrikiamv.minegocio_puntodeventa.ui.screens.home.expensesList

// ExpensesListScreen.kt

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.elfrikiamv.minegocio_puntodeventa.R
import com.elfrikiamv.minegocio_puntodeventa.model.home.expenses.ExpenseFirebase
import com.elfrikiamv.minegocio_puntodeventa.navigation.Screen
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.home.expenses.ExpensesViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ExpensesListScreen(navController: NavHostController) {

    val viewModel: ExpensesViewModel = viewModel()

    val grouped by viewModel.groupedExpenses.collectAsState()

    // Observar el estado de isLoading
    val isLoading by viewModel.isLoading.collectAsState()

    // Observar búsqueda y resultados en tiempo real
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    val showAllExpenses by viewModel.showAllExpenses.collectAsState()

    var isSearchActive by remember { mutableStateOf(false) }

    // Contenido de la pantalla que muestra ExpensesDetailsScreen
    Column(
        modifier = Modifier
            //.padding(paddingValues)
            .fillMaxSize()
    ) {
        // Mostrar el LinearProgressIndicator mientras se cargan los datos
        if (isLoading && grouped.isEmpty()) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
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
                                        contentDescription = "Volver",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_search_24),
                                    contentDescription = "Buscar",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.baseline_close_24),
                                        contentDescription = "Borrar búsqueda",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
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
                windowInsets = WindowInsets(0.dp),
                content = {

                    if (searchQuery.isEmpty() && !isLoading) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "rules of search.",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    } else if (searchResults.isEmpty() && searchQuery.isNotEmpty() && !isLoading) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "No se encontraron coincidencias.",
                                fontStyle = FontStyle.Italic,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.alpha(0.8f)
                            )
                            Text(
                                "):",
                                fontStyle = FontStyle.Italic,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.alpha(0.8f)
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(end = 16.dp, start = 16.dp, top = 16.dp)
                        ) {
                            // Resultados de la búsqueda en su propio LazyColumn dentro del SearchBar
                            items(searchResults) { expense ->
                                ExpensesDetailsCard(expense = expense) {
                                    isSearchActive = false
                                    navController.navigate(Screen.DetailsExpense.route + "?expenseId=${expense.expenseId}")
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(72.dp))
                            }
                        }
                    }
                },
            )
        }

        if (searchResults.isEmpty() && searchQuery.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp, start = 16.dp, top = 16.dp),
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (showAllExpenses) "Mostrar gastos del mes." else "Mostrar todos los gastos.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Switch(
                        checked = showAllExpenses,
                        onCheckedChange = { viewModel.onShowAllExpensesChange(it) }
                    )
                }
            }
        }

        if (searchResults.isEmpty() && !isLoading && searchQuery.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp, start = 16.dp, top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "No se encontraron coincidencias.",
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.alpha(0.8f)
                )
                Text(
                    "):",
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.alpha(0.8f)
                )
            }
        } else if (searchResults.isNotEmpty() && !isLoading && searchQuery.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp, start = 16.dp, top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Resultados de la búsqueda:")
            }
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(end = 16.dp, start = 16.dp, top = 16.dp)
            ) {
                // Resultados de la búsqueda en su propio LazyColumn
                items(searchResults) { expense ->
                    ExpensesDetailsCard(expense = expense) {
                        navController.navigate(Screen.DetailsExpense.route + "?expenseId=${expense.expenseId}")
                    }
                }

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
                Text(
                    "No se han registrado gastos en este mes.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Text(
                    "):",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(end = 16.dp, start = 16.dp, top = 0.dp)
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
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    items(gr.expenses) { expense ->
                        Log.d(
                            "ExpensesDetailsScreen",
                            "Renderizando gasto: ${expense.expenseId}"
                        )

                        ExpensesDetailsCard(expense = expense) {
                            navController.navigate(Screen.DetailsExpense.route + "?expenseId=${expense.expenseId}")
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(56.dp))
                }
            }
        }
    }
}

@Composable
fun ExpensesDetailsCard(expense: ExpenseFirebase, onExpenseDetails: () -> Unit) {

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpenseDetails() }
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
                    Text(text = "Gasto realizado")
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(text = "id. ${expense.expenseId}")
                }

                Column {
                    Row(
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = expense.date,
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
                            text = expense.time,
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
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {

                Row(
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_payments_24),
                        contentDescription = "payment icon",
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = expense.paymentMethod,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
                Text(
                    text = "Total: $${expense.amountExpense}",
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
    }

}

fun onAddExpense(navController: NavController) {

    // Acción para agregar un gasto
    navController.navigate(Screen.AddExpense.route)
}