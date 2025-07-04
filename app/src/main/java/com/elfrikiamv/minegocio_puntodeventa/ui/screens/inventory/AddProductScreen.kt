package com.elfrikiamv.minegocio_puntodeventa.ui.screens.inventory

// AddProductScreen.kt

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.elfrikiamv.minegocio_puntodeventa.R
import com.elfrikiamv.minegocio_puntodeventa.navigation.Screen
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.inventory.AddProductViewModel
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.inventory.CheckProductExistsViewModel
import java.util.Locale

@Composable
fun AddProductScreen(
    navController: NavHostController,
    viewModel: AddProductViewModel,
    checkProductExistsViewModel: CheckProductExistsViewModel
) {

    //val viewModel: InventoryViewModel = viewModel()
    val context = LocalContext.current
    val TAG = "AddProductScreen"

    // Variables para almacenar los valores del formulario
    val id by viewModel.id
    val name by viewModel.name
    val quantity by viewModel.quantity
    val barcode by viewModel.barcode
    val description by viewModel.description
    val errorMessage by viewModel.errorMessage

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
    //var salePriceInCents by remember { mutableLongStateOf(0L) }

    // Inicializamos priceInCents a partir de salePrice si ya tiene un valor válido
    val providerPriceInCents by viewModel.providerPriceInCents
    //var providerPriceInCents by remember { mutableLongStateOf(0L) }

    // Función para formatear el precio en centavos a un string con formato 0.00
    fun formatPrice(cents: Long): String {
        return String.format(Locale.US, "%.2f", cents / 100.00)
    }

    fun isValidDescription(input: String): Boolean {
        return input.matches(Regex("^[A-Za-z0-9 ]{0,42}\$"))
    }

    // Observar el estado de isLoading
    val isLoading by viewModel.isLoading.collectAsState()

    // Verificación automática cuando el código de barras cambia
    val currentBarcode by rememberUpdatedState(barcode) // Evitar problemas de estado obsoleto

    // Manejo del TextField para mantener el cursor al final  Precio
    var salePriceTextFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                formatPrice(salePriceInCents),
                TextRange(formatPrice(salePriceInCents).length)
            )
        )
    }

    /*LaunchedEffect(viewModel) {
        viewModel.clearAddProductFields()
    }*/

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

    LaunchedEffect(currentBarcode) {

        if (currentBarcode.isNotBlank()) {
            checkProductExists(checkProductExistsViewModel, viewModel, TAG, context)
        }
        Log.d(TAG, "Código de barras recibido: $barcode")
        Log.d(TAG, "Código de barras recibido de detalles: barcodeDetails")
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
                                text = "Llena todos los campos para agregar el producto al inventario.",
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
                                    onValueChange = { newValue ->
                                        if (isValidBarcode(newValue)) {
                                            // Esto disparará LaunchedEffect automáticamente
                                            viewModel.barcode.value = newValue
                                        }
                                    },
                                    /*onValueChange = { newValue ->
                                        // Esto disparará LaunchedEffect automáticamente
                                        barcode = newValue
                                    },*/
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
                                IconButton(
                                    onClick = {
                                        // Lógica para abrir la cámara o navegar a una pantalla de escaneo
                                        navController.navigate(Screen.ScanAddProduct.route)
                                    },
                                    //modifier = Modifier.size(48.dp) // Tamaño del ícono
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.baseline_camera_alt_24), // Usa un ícono de cámara
                                        contentDescription = "Abrir cámara",
                                        modifier = Modifier.size(24.dp) // Tamaño del ícono
                                    )
                                }
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
                            /*OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Nombre del producto") },
                                modifier = Modifier.fillMaxWidth(),
                                isError = name.isBlank()
                            )*/

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
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

                            /*OutlinedTextField(
                                value = providerPrice,
                                onValueChange = {
                                    if (isValidPrice(it)) {
                                        providerPrice = it
                                    }
                                },
                                label = { Text("Precio proveedor") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = providerPrice.isBlank(),
                                supportingText = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Text("${providerPrice.length}/xd")
                                    }
                                }
                            )*/

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
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }
    }
}

fun checkProductExists(
    //currentBarcode: String,
    checkProductExistsViewModel: CheckProductExistsViewModel,
    viewModel: AddProductViewModel,
    TAG: String,
    context: Context
) {
    try {
        checkProductExistsViewModel.checkProductExists(viewModel.barcode.value) { product ->
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
                viewModel.clearAddProductFields()
            }
        }
    } catch (e: Exception) {
        viewModel.errorMessage.value = "Hubo un error al verificar el producto."
        Log.e(TAG, "Error al verificar producto: $e")
    }
}