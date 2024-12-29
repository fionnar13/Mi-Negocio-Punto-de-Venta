package com.elfrikiamv.minegocio_puntodeventa.ui.screens

//AddProductScreen.kt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(navController: NavController) {
    val viewModel: InventoryViewModel = viewModel()

    // Variables para almacenar los valores del formulario
    var id by remember { mutableStateOf<String?>(null) } // ID del producto (para actualizaciones)
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var providerPrice by remember { mutableStateOf("") }
    var salePrice by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Calcular si todos los campos están llenos
    val isFormValid = name.isNotBlank() &&
            quantity.isNotBlank() &&
            barcode.isNotBlank() &&
            providerPrice.isNotBlank() &&
            salePrice.isNotBlank() &&
            description.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Añadir Producto") })
        }
    ) { innerPadding ->
        // Formulario para añadir un producto
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Campo de código de barras con lógica para buscar en Firebase
            TextField(
                value = barcode,
                onValueChange = { newValue ->
                    barcode = newValue
                    // Buscar producto en Firebase si el código no está vacío
                    if (newValue.isNotBlank()) {
                        viewModel.checkProductExists(newValue) { product ->
                            if (product != null) {
                                // Si el producto existe, llenar los demás campos
                                id = product.id // Asignar el ID del producto existente
                                name = product.name
                                quantity = product.quantity.toString()
                                providerPrice = product.providerPrice.toString()
                                salePrice = product.salePrice.toString()
                                description = product.description
                            } else {
                                // Si no se encuentra, limpiar los demás campos
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
                label = { Text("Código de barras") }
            )

            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del producto") }
            )
            TextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("Cantidad") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            TextField(
                value = providerPrice,
                onValueChange = { providerPrice = it },
                label = { Text("Precio proveedor") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            TextField(
                value = salePrice,
                onValueChange = { salePrice = it },
                label = { Text("Precio de venta") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción del producto") }
            )

            Button(
                onClick = {
                    // Validar datos antes de guardar
                    val providerPriceValue = providerPrice.toDoubleOrNull() ?: 0.0
                    val salePriceValue = salePrice.toDoubleOrNull() ?: 0.0
                    val quantityValue = quantity.toIntOrNull() ?: 0

                    viewModel.addOrUpdateProduct(
                        id = id, // Enviar el ID si existe, null si es nuevo
                        name = name,
                        quantity = quantityValue,
                        barcode = barcode,
                        providerPrice = providerPriceValue,
                        salePrice = salePriceValue,
                        description = description
                    )
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isFormValid // Botón habilitado solo si el formulario es válido
            ) {
                Text("Guardar Producto")
            }
        }
    }
}