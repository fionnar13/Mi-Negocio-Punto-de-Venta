package com.elfrikiamv.minegocio_puntodeventa.ui.screens.inventory

// AddProductScreen.kt

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.elfrikiamv.minegocio_puntodeventa.R
import com.elfrikiamv.minegocio_puntodeventa.navigation.Screen
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(navController: NavController, barcodeDetails: String?) {

    val viewModel: InventoryViewModel = viewModel()
    val context = LocalContext.current

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
    var salePrice by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) } // Estado de carga para el botón
    var errorMessage by remember { mutableStateOf<String?>(null) } // Para mostrar un error si algo sale mal

    // Verificación automática cuando el código de barras cambia
    val currentBarcode by rememberUpdatedState(barcode) // Evitar problemas de estado obsoleto

    LaunchedEffect(currentBarcode) {

        if (currentBarcode.isNotBlank()) {
            isLoading = true // Habilitar indicador de carga

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
                        providerPrice = product.providerPrice.toString()
                        salePrice = product.salePrice.toString()
                        description = product.description
                    } else {
                        id = null
                        name = ""
                        quantity = ""
                        providerPrice = ""
                        salePrice = ""
                        description = ""
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Hubo un error al verificar el producto."
                Log.e("AddProductScreen", "Error al verificar producto: $e")
            } finally {
                isLoading = false // Desactivar indicador de carga
            }
        }
        Log.d("AddProductScreen", "Código de barras recibido: $barcode")
        Log.d("AddProductScreen", "Código de barras recibido de detalles: $barcodeDetails")
    }

    // Mostrar mensaje de error si es necesario
    errorMessage?.let {
        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        errorMessage = null
    }

    // Calcular si todos los campos están llenos
    val isFormValid = name.isNotBlank() &&
            quantity.isNotBlank() &&
            barcode.isNotBlank() &&
            providerPrice.isNotBlank() &&
            salePrice.isNotBlank() &&
            description.isNotBlank()

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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
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
                                barcode = newValue // Esto disparará LaunchedEffect automáticamente
                            },
                            label = { Text("Código de barras") },
                            modifier = Modifier
                                .weight(1f) // Ajusta el ancho para que ocupe el espacio restante
                                .fillMaxWidth(),
                            isError = barcode.isBlank() // Muestra error si está vacío
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


                    /*OutlinedTextField(
                        value = barcode,
                        onValueChange = { newValue ->
                            barcode = newValue // Esto disparará LaunchedEffect automáticamente
                        },
                        label = { Text("Código de barras") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = barcode.isBlank() // Muestra error si está vacío
                    )*/
                    /*if (barcode.isBlank()) Text(
                        "Campo requerido",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )*/

                    // Campo Nombre del Producto
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre del producto") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = name.isBlank()
                    )

                    // Campo Cantidad
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Cantidad") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = quantity.isBlank()
                    )

                    // Campo Precio Proveedor
                    OutlinedTextField(
                        value = providerPrice,
                        onValueChange = { providerPrice = it },
                        label = { Text("Precio proveedor") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = providerPrice.isBlank()
                    )

                    // Campo Precio de Venta
                    OutlinedTextField(
                        value = salePrice,
                        onValueChange = { salePrice = it },
                        label = { Text("Precio de venta") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = salePrice.isBlank()
                    )

                    // Campo Descripción
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descripción del producto") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = description.isBlank()
                    )

                    // Botón Guardar Producto
                    Button(
                        onClick = {
                            isLoading = true    // activar la animación de carga
                            val providerPriceValue = providerPrice.toDoubleOrNull() ?: 0.0
                            val salePriceValue = salePrice.toDoubleOrNull() ?: 0.0
                            val quantityValue = quantity.toIntOrNull() ?: 0

                            viewModel.addOrUpdateProduct(
                                id = id,
                                name = name,
                                quantity = quantityValue,
                                barcode = barcode,
                                providerPrice = providerPriceValue,
                                salePrice = salePriceValue,
                                description = description
                            )
                            // Limpiar campos después de guardar
                            id = null
                            barcode = ""
                            name = ""
                            quantity = ""
                            providerPrice = ""
                            salePrice = ""
                            description = ""

                            // Desactivar el animación de carga
                            isLoading = false

                            // Mostrar mensaje de éxito
                            Toast.makeText(context, "Producto guardado", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        enabled = isFormValid // Activado si el formulario es válido
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.inversePrimary
                            )
                        } else {
                            Text("Guardar Producto")
                        }
                    }
                }
            }
        }
    }
}