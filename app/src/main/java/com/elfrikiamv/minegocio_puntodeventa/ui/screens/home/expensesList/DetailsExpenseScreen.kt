package com.elfrikiamv.minegocio_puntodeventa.ui.screens.home.expensesList

// DetailsExpenseScreen.kt

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.elfrikiamv.minegocio_puntodeventa.model.home.expenses.ExpenseFirebase
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.home.expenses.ExpensesViewModel
import java.util.Locale

@Composable
fun DetailsExpenseScreen(expenseId: String?, viewModel: ExpensesViewModel) {

    // Estado para carga de datos
    val isLoading by viewModel.isLoading.collectAsState()

    val expenseDetails by viewModel.expenseDetails
    val errorMessage by viewModel.errorMessage

    val TAG = "DetailsExpenseScreen"

    // Cargar los detalles del ticket
    LaunchedEffect(expenseId) {

        if (!expenseId.isNullOrEmpty()) {
            try {
                viewModel.errorMessage.value = ""
                viewModel.checkExpenseExists(expenseId) { fetchedExpense ->
                    viewModel.expenseDetails.value = fetchedExpense
                }
                Log.d(TAG, "Id del gasto recibido: $expenseId")
            } catch (e: Exception) {
                viewModel.errorMessage.value = "Hubo un error al cargar los datos del gasto."
                Log.e(TAG, "Error al cargar los datos del gasto: $e")
            }

        } else {
            viewModel.errorMessage.value = "El Id del gasto no es válido."
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        } else if (errorMessage.isNotBlank()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
            )
        } else if (expenseDetails == null) {
            // Mensaje de error si no hay datos
            Text(
                "No se encontraron detalles del ticket.",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
            )
        } else {

            // Mostrar detalles del gasto
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
                item {
                    ExpenseDetailsCard(expenseDetails!!)
                }
                item {
                    Spacer(modifier = Modifier.height(88.dp))
                }
            }
        }
    }
}

@Composable
fun ExpenseDetailsCard(expenseDetails: ExpenseFirebase) {

    val amountExpense = String.format(Locale.getDefault(), "%.2f", expenseDetails.amountExpense)
    val amountGiven = String.format(Locale.getDefault(), "%.2f", expenseDetails.amountGiven)
    val change = String.format(Locale.getDefault(), "%.2f", expenseDetails.change)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Encabezado
            Text(
                text = "Gasto ID: ${expenseDetails.expenseId}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 8.dp)
            )

            Row {
                Text(
                    text = "Fecha: ",
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier
                        .alignByBaseline()
                        .alpha(0.8f) // Aplica opacidad del 80%
                )
                Text(
                    text = expenseDetails.date,
                    modifier = Modifier.alignByBaseline()
                )
            }
            Row {
                Text(
                    text = "Hora: ",
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier
                        .alignByBaseline()
                        .alpha(0.8f) // Aplica opacidad del 80%
                )
                Text(
                    text = expenseDetails.time,
                    modifier = Modifier.alignByBaseline()
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(bottom = 8.dp, top = 8.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            // Detalles del gasto
            Row {
                Text(
                    text = "Concepto de pago: ",
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier
                        .alignByBaseline()
                        .alpha(0.8f) // Aplica opacidad del 80%
                )
                Text(
                    text = expenseDetails.paymentConcept,
                    modifier = Modifier.alignByBaseline()
                )
            }
            Row {
                Text(
                    text = "Método de pago: ",
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier
                        .alignByBaseline()
                        .alpha(0.8f) // Aplica opacidad del 80%
                )
                Text(
                    text = expenseDetails.paymentMethod,
                    modifier = Modifier.alignByBaseline()
                )
            }
            Row {
                Text(
                    text = "Descripción: ",
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier
                        .alignByBaseline()
                        .alpha(0.8f) // Aplica opacidad del 80%
                )
                Text(
                    text = expenseDetails.description,
                    modifier = Modifier.alignByBaseline()
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(bottom = 8.dp, top = 8.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Row {
                Text(
                    text = "Monto del gasto: ",
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier
                        .alignByBaseline()
                        .alpha(0.8f) // Aplica opacidad del 80%
                )
                Text(
                    text = "$$amountExpense",
                    modifier = Modifier.alignByBaseline()
                )
            }
            Row {
                Text(
                    text = "Monto dado: ",
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier
                        .alignByBaseline()
                        .alpha(0.8f) // Aplica opacidad del 80%
                )
                Text(
                    text = "$$amountGiven",
                    modifier = Modifier.alignByBaseline()
                )
            }
            Row {
                Text(
                    text = "Cambio: ",
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier
                        .alignByBaseline()
                        .alpha(0.8f) // Aplica opacidad del 80%
                )
                Text(
                    text = "$$change",
                    modifier = Modifier.alignByBaseline()
                )
            }
        }
    }
}