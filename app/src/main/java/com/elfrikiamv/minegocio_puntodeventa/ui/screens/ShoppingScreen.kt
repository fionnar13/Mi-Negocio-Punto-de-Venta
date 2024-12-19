package com.elfrikiamv.minegocio_puntodeventa.ui.screens

// ShoppingScreen.kt

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elfrikiamv.minegocio_puntodeventa.model.Product
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.ShoppingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingScreen(viewModel: ShoppingViewModel, modifier: Modifier = Modifier) {
    val products by viewModel.cartProducts.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Carrito de Compras") })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier.padding(paddingValues)
        ) {
            items(products) { product ->
                ProductTicket(product)
            }
        }
    }
}

@Composable
fun ProductTicket(product: Product) {
    val total = product.quantity * product.price

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Producto: ${product.name}", style = MaterialTheme.typography.titleMedium)
            Text("Cantidad: ${product.quantity}")
            Text("Precio Unitario: $${product.price}")
            Text("Total: $${total}")
            Text("Fecha y Hora: ${System.currentTimeMillis()}")
        }
    }
}