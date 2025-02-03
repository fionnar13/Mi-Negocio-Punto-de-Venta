package com.elfrikiamv.minegocio_puntodeventa.ui.screens.home.missingList

// AddMissingScreen.kt

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.elfrikiamv.minegocio_puntodeventa.R
import com.elfrikiamv.minegocio_puntodeventa.navigation.Screen
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.home.missing.MissingProductsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMissingScreen(navController: NavController) {

    val viewModel: MissingProductsViewModel = viewModel()

    // Variables para capturar los valores del formulario
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var providerPrice by remember { mutableStateOf("") }
    var totalPrice by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    // Observar el estado de isLoading
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar faltante") },
                navigationIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_arrow_back_ios_new_24),
                        contentDescription = "Regresar",
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clickable {
                                navController.navigate(Screen.MissingListProducts.route) {
                                    popUpTo(Screen.MissingListProducts.route) { inclusive = true }
                                }
                            }
                    )
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (name.isBlank() || quantity.isBlank() || description.isBlank() || providerPrice.isBlank() || totalPrice.isBlank()) {
                        errorMessage = "Todos los campos son obligatorios."
                    } else if (!isLoading) {
                        // Guardar gasto en Room y subir a Firebase
                        viewModel.confirmMissing(
                            name = name,
                            quantity = quantity.toInt(),
                            description = description,
                            providerPrice = providerPrice.toDouble(),
                            totalPrice = totalPrice.toDouble()
                        )
                        // Limpiar campos después de guardar
                        name = ""
                        quantity = ""
                        description = ""
                        providerPrice = ""
                        totalPrice = ""
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_save_24),
                        contentDescription = "Guardar faltante"
                    )
                },
                text = { Text("Guardar faltante") },
                /*containerColor = if (!isLoading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (!isLoading) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant*/
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Mostrar el indicador de carga si isLoading es true
                if (isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                } else {
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
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Nombre del faltante") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                isError = name.isBlank()
                            )
                            OutlinedTextField(
                                value = quantity,
                                onValueChange = { quantity = it },
                                label = { Text("Cantidad de piezas") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                isError = quantity.isBlank()
                            )
                            OutlinedTextField(
                                value = providerPrice,
                                onValueChange = { providerPrice = it },
                                label = { Text("Costo por pieza") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = providerPrice.isBlank()
                            )
                            OutlinedTextField(
                                value = totalPrice,
                                onValueChange = { totalPrice = it },
                                label = { Text("Precio total") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = totalPrice.isBlank()
                            )
                            /*Text(
                                text = "Total: $totalPrice",
                                modifier = Modifier.fillMaxWidth()
                            )*/
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = { Text("Descripción") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                isError = description.isBlank()
                            )
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
            }
        }
    )
}