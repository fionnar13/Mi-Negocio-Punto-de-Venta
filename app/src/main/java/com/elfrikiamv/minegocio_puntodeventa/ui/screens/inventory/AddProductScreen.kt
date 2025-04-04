package com.elfrikiamv.minegocio_puntodeventa.ui.screens.inventory

// AddProductScreen.kt

import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.elfrikiamv.minegocio_puntodeventa.R
import com.elfrikiamv.minegocio_puntodeventa.navigation.Screen
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.inventory.InventoryViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(navController: NavController, barcodeDetails: String?) {

    val viewModel: InventoryViewModel = viewModel()
    val context = LocalContext.current
    val TAG = "AddProductScreen"

    // Variables para almacenar los valores del formulario
    var id by remember { mutableStateOf<String?>(null) }
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var barcode by remember {
        mutableStateOf(
            barcodeDetails.takeIf { !it.isNullOrEmpty() }
                ?: navController.previousBackStackEntry?.savedStateHandle?.get<String>(
                    "barcode"
                ) ?: ""
        )
    }
    var providerPrice by remember { mutableStateOf("") }
    //var salePrice by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

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

    /*fun isValidPrice(input: String): Boolean {
        return input.matches(Regex("^[0-9]+(\\.[0-9]{1,2})?$"))
    }*/

    // Inicializamos priceInCents a partir de salePrice si ya tiene un valor válido
    var salePriceInCents by remember { mutableLongStateOf(0L) }

    // Inicializamos priceInCents a partir de salePrice si ya tiene un valor válido
    var providerPriceInCents by remember { mutableLongStateOf(0L) }

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
            TextFieldValue(formatPrice(salePriceInCents), TextRange(formatPrice(salePriceInCents).length))
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
            TextFieldValue(formatPrice(providerPriceInCents), TextRange(formatPrice(providerPriceInCents).length))
        )
    }

    // Sincronizar cuando priceInCents cambia externamente (ej. al escanear código) Precio proveedor
    LaunchedEffect(providerPriceInCents) {
        val formatted = formatPrice(providerPriceInCents)
        providerPriceTextFieldValue = TextFieldValue(formatted, TextRange(formatted.length))
    }

    LaunchedEffect(currentBarcode) {

        if (currentBarcode.isNotBlank()) {

            try {
                viewModel.checkProductExists(currentBarcode) { product ->
                    if (product != null) {
                        // Mostrar un Toast
                        Toast.makeText(
                            context,
                            "Recuperando datos del producto.",
                            Toast.LENGTH_SHORT
                        ).show()
                        id = product.id
                        name = product.name
                        quantity = product.quantity.toString()
                        //providerPrice = product.providerPrice.toString()
                        //salePrice = product.salePrice.toString()
                        //priceInCents = product.salePrice.toLong()
                        providerPriceInCents = (product.providerPrice * 100.00).toLong()
                        salePriceInCents = (product.salePrice * 100.00).toLong()
                        description = product.description
                    } else {
                        id = null
                        name = ""
                        quantity = ""
                        //providerPrice = ""
                        //salePrice = ""
                        providerPriceInCents = 0L
                        salePriceInCents = 0L
                        description = ""
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Hubo un error al verificar el producto."
                Log.e(TAG, "Error al verificar producto: $e")
            }
        }
        Log.d(TAG, "Código de barras recibido: $barcode")
        Log.d(TAG, "Código de barras recibido de detalles: $barcodeDetails")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Añadir Producto") },
                navigationIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_arrow_back_ios_new_24),
                        contentDescription = "Regresar",
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clickable {
                                navController.navigate(Screen.Inventory.route) {
                                    popUpTo(Screen.Inventory.route) { inclusive = true }
                                }
                            }
                    )
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (barcode.isBlank() || name.isBlank() || quantity.isBlank() || providerPriceInCents <= 0 || salePriceInCents <= 0 || description.isBlank()) {
                        errorMessage = "Todos los campos son obligatorios."
                    } else if (!isLoading) {
                        // Lógica para guardar el producto
                        //val providerPriceValue = providerPrice.toDoubleOrNull() ?: 0.0
                        //val salePriceValue = salePrice.toDoubleOrNull() ?: 0.0
                        val quantityValue = quantity.toIntOrNull() ?: 0

                        viewModel.addOrUpdateProduct(
                            id = id,
                            name = name,
                            quantity = quantityValue,
                            barcode = barcode,
                            //providerPrice = providerPriceValue,
                            //salePrice = salePriceValue,
                            providerPrice = providerPriceInCents / 100.0,
                            salePrice = salePriceInCents / 100.0,
                            description = description
                        )
                        // Limpiar campos después de guardar
                        id = null
                        barcode = ""
                        name = ""
                        quantity = ""
                        //providerPrice = ""
                        //salePrice = ""
                        providerPriceInCents = 0L
                        salePriceInCents = 0L
                        description = ""

                        // Mostrar mensaje de éxito
                        Toast.makeText(context, "Producto guardado", Toast.LENGTH_SHORT).show()
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_save_24),
                        contentDescription = "Guardar Producto"
                    )
                },
                text = { Text("Guardar Producto") },
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
                                                    barcode = newValue
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
                                                quantity = it
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
                                                Text("${quantity.length}/3")
                                            }
                                        }
                                    )

                                    // Campo Precio Proveedor

                                    OutlinedTextField(
                                        value = providerPriceTextFieldValue,
                                        onValueChange = { newValue ->
                                            val cleanInput = newValue.text.filter { it.isDigit() }
                                            val newCents = cleanInput.toLongOrNull() ?: 0L
                                            providerPriceInCents =
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
                                            salePriceInCents =
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
                                                description = it
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
    )
}