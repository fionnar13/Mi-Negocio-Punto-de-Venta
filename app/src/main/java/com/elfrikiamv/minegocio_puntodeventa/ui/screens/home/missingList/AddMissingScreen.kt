package com.elfrikiamv.minegocio_puntodeventa.ui.screens.home.missingList

// AddMissingScreen.kt

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.home.missing.MissingProductsViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMissingScreen(navController: NavController) {

    val viewModel: MissingProductsViewModel = viewModel()

    // Variables para capturar los valores del formulario
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var totalPrice by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    // Funciones de validación
    fun isValidName(input: String): Boolean {
        return input.matches(Regex("^[A-Za-z0-9 ]{0,17}\$"))
    }

    fun isValidQuantity(input: String): Boolean {
        return input.matches(Regex("^[0-9]{0,3}\$"))
    }

    // Función para formatear el precio en centavos a un string con formato 0.00
    fun formatPrice(cents: Long): String {
        return String.format(Locale.US, "%.2f", cents / 100.00)
    }

    fun isValidDescription(input: String): Boolean {
        return input.matches(Regex("^[A-Za-z0-9 ]{0,42}\$"))
    }

    // Inicializamos productPriceInCents a partir de salePrice si ya tiene un valor válido
    var productPriceInCents by remember { mutableLongStateOf(0L) }

    // Manejo del TextField del precio del producto para mantener el cursor al final
    var productPriceTextFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                formatPrice(productPriceInCents),
                TextRange(formatPrice(productPriceInCents).length)
            )
        )
    }
    // Sincronizar cuando productPriceInCents cambia externamente
    LaunchedEffect(productPriceInCents) {
        val formatted = formatPrice(productPriceInCents)
        productPriceTextFieldValue = TextFieldValue(formatted, TextRange(formatted.length))
    }

    // variable reactiva que calcule el precio total
    val totalCalculated by remember(quantity, productPriceInCents) {
        mutableStateOf(
            if (quantity.isNotBlank() && quantity.toIntOrNull() != null) {
                val qty = quantity.toInt()
                val totalCents = qty * productPriceInCents
                String.format(Locale.US, "%.2f", totalCents / 100.0)
            } else {
                "0.00"
            }
        )
    }

    LaunchedEffect(totalCalculated) {
        totalPrice = totalCalculated
    }

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
                    if (name.isBlank() || (quantity <= 0.toString() || quantity.isBlank()) || description.isBlank() || productPriceInCents <= 0 || totalPrice <= 0.toString()) {
                        errorMessage = "Todos los campos son obligatorios."
                    } else if (!isLoading) {
                        // Guardar gasto en Room y subir a Firebase
                        viewModel.confirmMissing(
                            name = name,
                            quantity = quantity.toInt(),
                            providerPrice = productPriceInCents / 100.0,
                            totalPrice = totalPrice.toDouble(),
                            description = description
                        )
                        // Limpiar campos después de guardar
                        name = ""
                        quantity = ""
                        productPriceInCents = 0L
                        totalPrice = "0.00"
                        description = ""
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
                                text = "Llena todos los campos para agregar el producto faltante.",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.fillMaxWidth()
                            )
                            // Campo Nombre del Producto faltante
                            OutlinedTextField(
                                value = name,
                                onValueChange = {
                                    if (isValidName(it)) {
                                        name = it
                                    }
                                },
                                label = { Text("Nombre del producto") },
                                modifier = Modifier.fillMaxWidth(),
                                isError = name.isBlank(),
                                supportingText = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Text("${name.length}/17")
                                    }
                                }
                            )

                            // Campo Cantidad de Piezas faltante
                            OutlinedTextField(
                                value = quantity,
                                onValueChange = {
                                    if (isValidQuantity(it)) {
                                        quantity = it
                                    }
                                },
                                //onValueChange = { quantity = it },
                                label = { Text("Cantidad de piezas") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = quantity.isBlank(),
                                supportingText = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Text("Máximo: 999 piezas")
                                    }
                                }
                            )

                            // Campo Precio por pieza faltante

                            OutlinedTextField(
                                value = productPriceTextFieldValue,
                                onValueChange = { newValue ->
                                    val cleanInput = newValue.text.filter { it.isDigit() }
                                    val newCents = cleanInput.toLongOrNull() ?: 0L
                                    productPriceInCents =
                                        newCents.coerceAtMost(9999999L) // Límite: 99,999.99

                                    val formatted = formatPrice(productPriceInCents)
                                    productPriceTextFieldValue = TextFieldValue(
                                        text = formatted,
                                        selection = TextRange(formatted.length) // cursor al final
                                    )
                                },
                                label = { Text("Precio por pieza") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = productPriceInCents == 0L,
                                supportingText = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Text("Máximo: $99,999.99")
                                    }
                                }
                            )

                            // Campo Precio total faltante

                            Text(
                                text = "Total: $$totalCalculated",
                                //style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 5.dp)
                            )

                            // Campo Descripción del Producto faltante

                            OutlinedTextField(
                                value = description,
                                onValueChange = {
                                    if (isValidDescription(it)) {
                                        description = it
                                    }
                                },
                                label = { Text("Descripción") },
                                modifier = Modifier.fillMaxWidth(),
                                isError = description.isBlank(),
                                supportingText = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Text("${description.length}/42")
                                    }
                                }
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