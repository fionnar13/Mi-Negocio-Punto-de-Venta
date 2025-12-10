package com.elfrikiamv.minegocio_puntodeventa.ui.screens.home.missingList

// AddMissingScreen.kt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.requestFocus
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.home.missing.MissingProductsViewModel
import java.util.Locale

@Composable
fun AddMissingScreen(viewModel: MissingProductsViewModel) {

    //val viewModel: MissingProductsViewModel = viewModel()

    // Variables para capturar los valores del formulario
    /*var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var totalPrice by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }*/
    val name by viewModel.name
    val quantity by viewModel.quantity
    //val totalPrice by viewModel.totalPrice
    val description by viewModel.description
    val errorMessage by viewModel.errorMessage

    // Manejo de enfoque
    val focusManager = LocalFocusManager.current
    val nameMissingFocusRequester = remember { FocusRequester() }
    val quantityMissingFocusRequester = remember { FocusRequester() }
    val productPriceMissingFocusRequester = remember { FocusRequester() }
    val descriptionMissingFocusRequester = remember { FocusRequester() }

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
    //var productPriceInCents by remember { mutableLongStateOf(0L) }
    val productPriceInCents by viewModel.productPriceInCents

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
        viewModel.totalPrice.value = totalCalculated
    }

    LaunchedEffect(Unit) {
        nameMissingFocusRequester.requestFocus()
    }

    // Observar el estado de isLoading
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            //.padding(paddingValues)
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
                                text = "Llena todos los campos para agregar el producto faltante.",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.fillMaxWidth()
                            )
                            // Campo Nombre del Producto faltante
                            OutlinedTextField(
                                value = name,
                                onValueChange = {
                                    if (isValidName(it)) {
                                        viewModel.name.value = it
                                    }
                                },
                                label = { Text("Nombre del producto") },

                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(nameMissingFocusRequester),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { quantityMissingFocusRequester.requestFocus() }
                                ),
                                singleLine = true,
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
                                        viewModel.quantity.value = it
                                    }
                                },
                                //onValueChange = { quantity = it },
                                label = { Text("Cantidad de piezas") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(quantityMissingFocusRequester),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { productPriceMissingFocusRequester.requestFocus() }
                                ),
                                singleLine = true,

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
                                    viewModel.productPriceInCents.longValue =
                                        newCents.coerceAtMost(9999999L) // Límite: 99,999.99

                                    val formatted = formatPrice(productPriceInCents)
                                    productPriceTextFieldValue = TextFieldValue(
                                        text = formatted,
                                        selection = TextRange(formatted.length) // cursor al final
                                    )
                                },
                                label = { Text("Precio por pieza") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(productPriceMissingFocusRequester),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { descriptionMissingFocusRequester.requestFocus() }
                                ),
                                singleLine = true,

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
                                        viewModel.description.value = it
                                    }
                                },
                                label = { Text("Descripción") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(descriptionMissingFocusRequester),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { focusManager.clearFocus() }
                                ),
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
                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }
    }
}