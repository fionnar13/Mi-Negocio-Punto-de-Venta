package com.elfrikiamv.minegocio_puntodeventa.ui.screens.home

// AddExpense.kt

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import com.elfrikiamv.minegocio_puntodeventa.navigation.Screen
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.ExpensesDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpense(navController: NavController) {
    val viewModel: ExpensesDetailsViewModel = viewModel()

    // Variables para capturar los valores del formulario
    var name by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var amountExpense by remember { mutableStateOf("") }
    var amountGiven by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

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
                    if (name.isBlank() || paymentMethod.isBlank() || description.isBlank() || amountExpense.isBlank() || amountGiven.isBlank()) {
                        errorMessage = "Todos los campos son obligatorios."
                    } else {
                        // Guardar gasto en Room y subir a Firebase
                        viewModel.confirmExpense(
                            paymentConcept = name,
                            paymentMethod = paymentMethod,
                            description = description,
                            amountExpense = amountExpense.toDouble(),
                            amountGiven = amountGiven.toDouble(),
                            /*context = navController.context,
                            userEmail = viewModel.auth.currentUser?.email ?: ""*/
                        )
                        navController.navigate(Screen.ExpensesDetails.route)
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_save_24),
                        contentDescription = "Guardar gasto"
                    )
                },
                text = { Text("Guardar Gasto") }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (errorMessage.isNotBlank()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del gasto") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = paymentMethod,
                    onValueChange = { paymentMethod = it },
                    label = { Text("Método de pago") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountExpense,
                    onValueChange = { amountExpense = it },
                    label = { Text("Monto del gasto") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = amountExpense.toDoubleOrNull() == null
                )
                OutlinedTextField(
                    value = amountGiven,
                    onValueChange = { amountGiven = it },
                    label = { Text("Monto entregado") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = amountGiven.toDoubleOrNull() == null
                )
            }
        }
    )
}
