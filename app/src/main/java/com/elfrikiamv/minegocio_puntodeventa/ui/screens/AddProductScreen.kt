package com.elfrikiamv.minegocio_puntodeventa.ui.screens

//AddProductScreen.kt

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }

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
                value = barcode,
                onValueChange = { barcode = it },
                label = { Text("Código de barras") }
            )

            Button(
                onClick = {
                    // Guardar el producto en Firestore y volver a la pantalla de inventario
                    viewModel.addProduct(name, quantity.toInt(), barcode)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Producto")
            }
        }
    }
}