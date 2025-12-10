package com.elfrikiamv.minegocio_puntodeventa.ui.screens.inventory

// EditProductScreen.kt

import android.content.Context
import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.inventory.CheckProductExistsViewModel
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.inventory.EditProductViewModel
import java.util.Locale

@Composable
fun EditProductScreen(
    barcodeDetails: String?,
    viewModel: EditProductViewModel,
    checkProductExistsViewModel: CheckProductExistsViewModel
) {

    //val viewModel: EditProductViewModel = viewModel()
    val context = LocalContext.current
    val TAG = "AddProductScreen"

    // Variables para almacenar los valores del formulario
    val id by viewModel.id
    val name by viewModel.name
    val quantity by viewModel.quantity
    val barcode by viewModel.barcode
    val description by viewModel.description
    val errorMessage by viewModel.errorMessage

    // Manejo de enfoque
    val focusManager = LocalFocusManager.current
    //val barcodeProductFocusRequester = remember { FocusRequester() }
    val nameAddProductFocusRequester = remember { FocusRequester() }
    val quantityAddProductFocusRequester = remember { FocusRequester() }
    val providerAddPriceFocusRequester = remember { FocusRequester() }
    val salePriceAddFocusRequester = remember { FocusRequester() }
    val descriptionAddProductFocusRequester = remember { FocusRequester() }

    // Funciones de validación
    fun isValidName(input: String): Boolean {
        return input.matches(Regex("^[A-Za-z0-9 ]{0,17}\$"))
    }

    fun isValidQuantity(input: String): Boolean {
        return input.matches(Regex("^[0-9]{0,3}\$"))
    }

    fun isValidBarcode(input: String): Boolean {
        return input.matches(Regex("^[A-Za-z0-9]{0,14}$"))
    }

    // Inicializamos priceInCents a partir de salePrice si ya tiene un valor válido
    val salePriceInCents by viewModel.salePriceInCents

    // Inicializamos priceInCents a partir de salePrice si ya tiene un valor válido
    val providerPriceInCents by viewModel.providerPriceInCents

    // Función para formatear el precio en centavos a un string con formato 0.00
    fun formatPrice(cents: Long): String {
        return String.format(Locale.US, "%.2f", cents / 100.00)
    }

    fun isValidDescription(input: String): Boolean {
        return input.matches(Regex("^[A-Za-z0-9 ]{0,42}\$"))
    }

    // Observar el estado de isLoading
    val isLoading by viewModel.isLoading.collectAsState()

    // Manejo del TextField para mantener el cursor al final  Precio
    var salePriceTextFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                formatPrice(salePriceInCents),
                TextRange(formatPrice(salePriceInCents).length)
            )
        )
    }

    // Sincronizar cuando priceInCents cambia externamente (ej. al escanear código)
    LaunchedEffect(salePriceInCents) {
        val formatted = formatPrice(salePriceInCents)
        salePriceTextFieldValue = TextFieldValue(formatted, TextRange(formatted.length))
    }

    // Manejo del TextField para mantener el cursor al final  Precio proveedor
    var providerPriceTextFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                formatPrice(providerPriceInCents),
                TextRange(formatPrice(providerPriceInCents).length)
            )
        )
    }

    // Sincronizar cuando priceInCents cambia externamente (ej. al escanear código) Precio proveedor
    LaunchedEffect(providerPriceInCents) {
        val formatted = formatPrice(providerPriceInCents)
        providerPriceTextFieldValue = TextFieldValue(formatted, TextRange(formatted.length))
    }

    LaunchedEffect(barcodeDetails) {

        if (!barcodeDetails.isNullOrBlank()) {

            viewModel.barcode.value = barcodeDetails
            checkProductExists(checkProductExistsViewModel, barcodeDetails, viewModel, TAG, context)
            Log.d(TAG, "Código de barras recibido de detalles: $barcodeDetails")
            Log.d(TAG, "Código de barras recibido: $barcode")
        } else {

            viewModel.clearEditProductSaveFields()
            Log.e(TAG, "Código de barras nulo o vacío")
        }
    }

    LaunchedEffect(Unit) {
        nameAddProductFocusRequester.requestFocus()
    }

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
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(16.dp)
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
                                text = "Llena todos los campos para editar el producto al inventario.",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.fillMaxWidth()
                            )
                            // Campo Código de Barras con botón de cámara
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = barcode,
                                    enabled = false,
                                    onValueChange = { newValue ->
                                        if (isValidBarcode(newValue)) {
                                            // Esto disparará LaunchedEffect automáticamente
                                            viewModel.barcode.value = newValue
                                        }
                                    },
                                    label = { Text("Código de barras") },
                                    modifier = Modifier
                                        .weight(1f) // Ajusta el ancho para que ocupe el espacio restante
                                        .fillMaxWidth(),
                                    isError = barcode.isBlank(), // Muestra error si está vacío
                                    supportingText = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            Text("${barcode.length}/14")
                                        }
                                    }
                                )
                            }

                            // Campo Nombre del Producto
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
                                    .focusRequester(nameAddProductFocusRequester),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { quantityAddProductFocusRequester.requestFocus() }
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

                            // Campo Cantidad
                            OutlinedTextField(
                                value = quantity,
                                onValueChange = {
                                    if (isValidQuantity(it)) {
                                        viewModel.quantity.value = it
                                    }
                                },
                                //onValueChange = { quantity = it },
                                label = { Text("Cantidad") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(quantityAddProductFocusRequester),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { providerAddPriceFocusRequester.requestFocus() }
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

                            // Campo Precio Proveedor

                            OutlinedTextField(
                                value = providerPriceTextFieldValue,
                                onValueChange = { newValue ->
                                    val cleanInput = newValue.text.filter { it.isDigit() }
                                    val newCents = cleanInput.toLongOrNull() ?: 0L
                                    viewModel.providerPriceInCents.longValue =
                                        newCents.coerceAtMost(9999999L) // Límite: 99,999.99

                                    val formatted = formatPrice(providerPriceInCents)
                                    providerPriceTextFieldValue = TextFieldValue(
                                        text = formatted,
                                        selection = TextRange(formatted.length) // cursor al final
                                    )
                                },
                                label = { Text("Precio proveedor") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(providerAddPriceFocusRequester),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { salePriceAddFocusRequester.requestFocus() }
                                ),
                                singleLine = true,
                                isError = providerPriceInCents == 0L,
                                supportingText = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Text("Máximo: $99,999.99")
                                    }
                                }
                            )

                            // Campo Precio de Venta
                            OutlinedTextField(
                                value = salePriceTextFieldValue,
                                onValueChange = { newValue ->
                                    val cleanInput = newValue.text.filter { it.isDigit() }
                                    val newCents = cleanInput.toLongOrNull() ?: 0L
                                    viewModel.salePriceInCents.longValue =
                                        newCents.coerceAtMost(9999999L) // Límite: 99,999.99

                                    val formatted = formatPrice(salePriceInCents)
                                    salePriceTextFieldValue = TextFieldValue(
                                        text = formatted,
                                        selection = TextRange(formatted.length) // cursor al final
                                    )
                                },
                                label = { Text("Precio de venta") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(salePriceAddFocusRequester),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { descriptionAddProductFocusRequester.requestFocus() }
                                ),
                                singleLine = true,
                                isError = salePriceInCents == 0L,
                                supportingText = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Text("Máximo: $99,999.99")
                                    }
                                }
                            )

                            // Campo Descripción
                            OutlinedTextField(
                                value = description,
                                onValueChange = {
                                    if (isValidDescription(it)) {
                                        viewModel.description.value = it
                                    }
                                },
                                label = { Text("Descripción del producto") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(descriptionAddProductFocusRequester),
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

fun checkProductExists(
    checkProductExistsViewModel: CheckProductExistsViewModel,
    barcodeDetails: String,
    viewModel: EditProductViewModel,
    TAG: String,
    context: Context
) {
    try {
        checkProductExistsViewModel.checkProductExists(barcodeDetails) { product ->
            if (product != null) {
                // Mostrar un Toast
                Toast.makeText(
                    context,
                    "Recuperando datos del producto.",
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.id.value = product.id
                viewModel.name.value = product.name
                viewModel.quantity.value = product.quantity.toString()
                viewModel.providerPriceInCents.longValue =
                    (product.providerPrice * 100.00).toLong()
                viewModel.salePriceInCents.longValue = (product.salePrice * 100.00).toLong()
                viewModel.description.value = product.description
            } else {
                viewModel.clearEditProductFields()
            }
        }
    } catch (e: Exception) {
        viewModel.errorMessage.value = "Hubo un error al verificar el producto."
        Log.e(TAG, "Error al verificar producto: $e")
    }
}