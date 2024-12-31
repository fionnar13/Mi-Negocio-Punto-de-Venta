package com.elfrikiamv.minegocio_puntodeventa.ui.screens

// ShoppingScreen.kt

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elfrikiamv.minegocio_puntodeventa.model.ProductFirebase
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.ShoppingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingScreen(modifier: Modifier = Modifier) {
    val viewModel: ShoppingViewModel = viewModel()
    val products by viewModel.cartProducts.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Carrito de Compras") })
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Lista de productos
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {
                items(products) { product ->
                    ProductTicket(product)
                }
            }

            // Botón de confirmación de ticket
            Button(
                onClick = { viewModel.confirmTicket() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text("Confirmar Ticket")
            }
        }
    }
}

@Composable
fun ProductTicket(product: ProductFirebase) {
    val total = product.quantity * product.salePrice

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Producto: ${product.name}", style = MaterialTheme.typography.titleMedium)
            Text("Cantidad: ${product.quantity}")
            Text("Precio Unitario: $${product.salePrice}")
            Text("Total: $${total}")
        }
    }
}
