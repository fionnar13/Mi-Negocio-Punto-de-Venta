package com.elfrikiamv.minegocio_puntodeventa.ui.screens

// AddProductScreen.kt

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(navController: NavController) {
    val viewModel: InventoryViewModel = viewModel()
    val context = LocalContext.current

    // Variables para almacenar los valores del formulario
    var id by remember { mutableStateOf<String?>(null) }
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var providerPrice by remember { mutableStateOf("") }
    var salePrice by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) } // Estado de carga para el botón

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
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Regresar",
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clickable { navController.navigateUp() }
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
                    // Campo Código de Barras
                    OutlinedTextField(
                        value = barcode,
                        onValueChange = { newValue ->
                            barcode = newValue
                            if (newValue.isNotBlank()) {
                                viewModel.checkProductExists(newValue) { product ->
                                    if (product != null) {
                                        // Mostrar un Toast
                                        Toast.makeText(
                                            context,
                                            "Producto ya existente en el inventario, recuperando datos.",
                                            Toast.LENGTH_LONG
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
                            }
                        },
                        label = { Text("Código de barras") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = barcode.isBlank() // Muestra error si está vacío
                    )
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
                                modifier = Modifier.size(24.dp)
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