package com.elfrikiamv.minegocio_puntodeventa.ui.screens

//InventoryScreen.kt

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.InventoryViewModel
import com.elfrikiamv.minegocio_puntodeventa.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(navController: NavController, modifier: Modifier = Modifier) {
    val viewModel: InventoryViewModel = viewModel()
    val products by viewModel.products.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventario") },
                actions = {
                    // Botón que redirige a la pantalla de añadir producto
                    Button(onClick = { navController.navigate("addProduct") }) {
                        Text("Añadir Producto")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(contentPadding = innerPadding) {
            // Muestra los productos en formato de cards
            items(products.size) { index ->
                val product = products[index]
                ProductCard(product)
            }
        }
    }
}

@Composable
fun ProductCard(product: Product) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        onClick = {
            // Lógica para editar o borrar el producto puede ir aquí
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = product.name, style = MaterialTheme.typography.titleMedium)
            Text(text = "Cantidad: ${product.quantity}")
            Text(text = "Código de barras: ${product.barcode}")
        }
    }
}