package com.elfrikiamv.minegocio_puntodeventa.ui.screens.home.expensesList

// AddExpenseScreen.kt

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.elfrikiamv.minegocio_puntodeventa.R
import com.elfrikiamv.minegocio_puntodeventa.navigation.Screen
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.home.expenses.ExpensesViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(navController: NavController) {
    val viewModel: ExpensesViewModel = viewModel()

    // Variables para capturar los valores del formulario
    var concept by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    //var amountExpense by remember { mutableStateOf("") }
    //var amountGiven by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    // Función para formatear el precio en centavos a un string con formato 0.00
    fun formatPrice(cents: Long): String {
        return String.format(Locale.US, "%.2f", cents / 100.00)
    }

    // Inicializamos expensePriceInCents a partir del monto del producto
    var expensePriceInCents by remember { mutableLongStateOf(0L) }

    // Manejo del expense TextField del precio del producto para mantener el cursor al final
    var expensePriceTextFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                formatPrice(expensePriceInCents),
                TextRange(formatPrice(expensePriceInCents).length)
            )
        )
    }
    // Sincronizar cuando expensePriceInCents cambia externamente
    LaunchedEffect(expensePriceInCents) {
        val formatted = formatPrice(expensePriceInCents)
        expensePriceTextFieldValue = TextFieldValue(formatted, TextRange(formatted.length))
    }

    // Inicializamos expenseAmountGivenInCents a partir del monto del producto
    var expenseAmountGivenInCents by remember { mutableLongStateOf(0L) }

    // Manejo del expense amount given TextField del precio del producto para mantener el cursor al final
    var expenseAmountGivenTextFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                formatPrice(expenseAmountGivenInCents),
                TextRange(formatPrice(expenseAmountGivenInCents).length)
            )
        )
    }
    // Sincronizar cuando expenseAmountGivenInCents cambia externamente
    LaunchedEffect(expenseAmountGivenInCents) {
        val formatted = formatPrice(expenseAmountGivenInCents)
        expenseAmountGivenTextFieldValue = TextFieldValue(formatted, TextRange(formatted.length))
    }

    // Observar el estado de isLoading
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar Gasto") },
                navigationIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_arrow_back_ios_new_24),
                        contentDescription = "Regresar",
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clickable {
                                navController.navigate(Screen.ExpensesDetails.route) {
                                    popUpTo(Screen.ExpensesDetails.route) { inclusive = true }
                                }
                            }
                    )
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (concept.isBlank() || paymentMethod.isBlank() || description.isBlank() || expensePriceInCents <= 0 || expenseAmountGivenInCents <= 0) {
                        errorMessage = "Todos los campos son obligatorios."
                    } else if (!isLoading) {
                        // Guardar gasto en Room y subir a Firebase
                        viewModel.confirmExpense(
                            paymentConcept = concept,
                            paymentMethod = paymentMethod,
                            description = description,
                            amountExpense = expensePriceInCents / 100.0,
                            //amountExpense = amountExpense.toDouble(),
                            amountGiven = expenseAmountGivenInCents / 100.0
                            //amountGiven = amountGiven.toDouble()
                        )
                        // Limpiar campos después de guardar
                        concept = ""
                        paymentMethod = ""
                        description = ""
                        expensePriceInCents = 0L
                        //amountExpense = ""
                        expenseAmountGivenInCents = 0L
                        //amountGiven = ""
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_save_24),
                        contentDescription = "Guardar gasto"
                    )
                },
                text = { Text("Guardar Gasto") },
                /*containerColor = if (!isLoading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (!isLoading) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant*/
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                //.padding(16.dp)
            ) {
                // Mostrar el indicador de carga si isLoading es true
                if (isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(end = 16.dp, start = 16.dp, top = 16.dp)
                    ) {
                        item {
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Llena todos los campos para agregar un gasto.",
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = concept,
                                        onValueChange = { concept = it },
                                        label = { Text("Concepto del gasto") },
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                        isError = concept.isBlank()
                                    )
                                    OutlinedTextField(
                                        value = paymentMethod,
                                        onValueChange = { paymentMethod = it },
                                        label = { Text("Método de pago") },
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                        isError = paymentMethod.isBlank()
                                    )
                                    OutlinedTextField(
                                        value = description,
                                        onValueChange = { description = it },
                                        label = { Text("Descripción") },
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                        isError = description.isBlank()
                                    )

                                    // Campo para el monto del gasto

                                    OutlinedTextField(
                                        value = expensePriceTextFieldValue,
                                        onValueChange = { newValue ->
                                            val cleanInput = newValue.text.filter { it.isDigit() }
                                            val newCents = cleanInput.toLongOrNull() ?: 0L
                                            expensePriceInCents =
                                                newCents.coerceAtMost(9999999L) // Límite: 99,999.99

                                            val formatted = formatPrice(expensePriceInCents)
                                            expensePriceTextFieldValue = TextFieldValue(
                                                text = formatted,
                                                selection = TextRange(formatted.length) // cursor al final
                                            )
                                        },
                                        label = { Text("¿Cuál fue el costo del gasto?") },
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        isError = expensePriceInCents == 0L,
                                        supportingText = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End
                                            ) {
                                                Text("Máximo: $99,999.99")
                                            }
                                        }
                                    )
                                    /*OutlinedTextField(
                                        value = amountExpense,
                                        onValueChange = { amountExpense = it },
                                        label = { Text("Monto del gasto") },
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        isError = amountExpense.isBlank()
                                    )*/

                                    // Campo para el monto pagado por el gasto

                                    // Campo para el monto del gasto

                                    OutlinedTextField(
                                        value = expenseAmountGivenTextFieldValue,
                                        onValueChange = { newValue ->
                                            val cleanInput = newValue.text.filter { it.isDigit() }
                                            val newCents = cleanInput.toLongOrNull() ?: 0L
                                            expenseAmountGivenInCents =
                                                newCents.coerceAtMost(9999999L) // Límite: 99,999.99

                                            val formatted = formatPrice(expenseAmountGivenInCents)
                                            expenseAmountGivenTextFieldValue = TextFieldValue(
                                                text = formatted,
                                                selection = TextRange(formatted.length) // cursor al final
                                            )
                                        },
                                        label = { Text("¿Cuál fue el monto que pagaste?") },
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        isError = expenseAmountGivenInCents == 0L,
                                        supportingText = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End
                                            ) {
                                                Text("Máximo: $99,999.99")
                                            }
                                        }
                                    )
                                    /*OutlinedTextField(
                                        value = amountGiven,
                                        onValueChange = { amountGiven = it },
                                        label = { Text("¿Cuánto pagaste por el gasto?") },
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        isError = amountGiven.isBlank()
                                    )*/
                                    if (errorMessage.isNotBlank()) {
                                        Text(
                                            text = errorMessage,
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                        )
                                    }
                                }
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
