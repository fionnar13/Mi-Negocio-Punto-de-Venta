package com.elfrikiamv.minegocio_puntodeventa.ui.screens.home.expensesList

// ExpensesListScreen.kt

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
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.elfrikiamv.minegocio_puntodeventa.R
import com.elfrikiamv.minegocio_puntodeventa.model.home.expenses.ExpenseFirebase
import com.elfrikiamv.minegocio_puntodeventa.navigation.Screen
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.home.expenses.ExpensesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesDetailsScreen(navController: NavController) {

    val viewModel: ExpensesViewModel = viewModel()

    val expensesDetails by viewModel.expensesDetailsList.collectAsState()

    // Agrupar los gastos por mes y año
    val groupedExpenses = viewModel.groupExpensesByYearAndMonth(expensesDetails)

    // Observar el estado de isLoading
    val isLoading by viewModel.isLoading.collectAsState()

    // Contenido de la pantalla que muestra ExpensesDetailsScreen
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ExpensesDetailsScreen") },
                navigationIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_arrow_back_ios_new_24),
                        contentDescription = "Regresar",
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clickable {
                                navController.navigate(Screen.Main.route) {
                                    popUpTo(Screen.Main.route) { inclusive = true }
                                }
                            }
                    )
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    // Lógica para agregar un gasto
                    onAddExpense(navController = navController)
                },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_add_24),
                        contentDescription = "Agregar gasto"
                    )
                },
                text = { Text("Agregar gasto") }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                // Mostrar el LinearProgressIndicator mientras se cargan los datos
                if (isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                if (expensesDetails.isEmpty() && !isLoading) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No hay gastos disponibles.")
                        Text("):")
                    }
                } else {
                    //val groupedExpenses = viewModel.groupExpensesByYearAndMonth(expensesDetails)

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(end = 16.dp, start = 16.dp, top = 16.dp)
                    ) {
                        groupedExpenses.forEach { group ->
                            item {
                                Text(
                                    text = "${group.month} - ${group.year}",
                                    //style = MaterialTheme.typography.titleMedium,
                                    //modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            items(group.expenses) { expense ->
                                ExpensesDetailsCard(expense)
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(72.dp))
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun ExpensesDetailsCard(expenses: ExpenseFirebase) {

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
        //.clickable { onExpenseDetails() }
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
                    Text(text = "id. ${expenses.expenseId}")
                }

                Column {
                    Row(
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = expenses.date,
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
                            text = expenses.time,
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
                        painter = painterResource(id = R.drawable.baseline_payments_24),
                        contentDescription = "payment icon",
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = expenses.paymentMethod,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
                Text(
                    text = "Total: $${expenses.amountExpense}",
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