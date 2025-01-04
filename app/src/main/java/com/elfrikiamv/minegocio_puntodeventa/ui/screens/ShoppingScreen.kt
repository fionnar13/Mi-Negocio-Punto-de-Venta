package com.elfrikiamv.minegocio_puntodeventa.ui.screens

// ShoppingScreen.kt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elfrikiamv.minegocio_puntodeventa.R
import com.elfrikiamv.minegocio_puntodeventa.model.ProductFirebase
import com.elfrikiamv.minegocio_puntodeventa.navigation.Screen
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.ShoppingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingScreen(modifier: Modifier = Modifier) {
    val viewModel: ShoppingViewModel = viewModel() // Obtenemos el ViewModel
    val products by viewModel.cartProducts.collectAsState() // Observamos el estado del carrito

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Carrito de Compras") }) // Barra superior
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Lista de productos en el carrito
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {
                items(products) { product ->
                    ProductTicket(
                        product = product,
                        onIncreaseQuantity = { viewModel.increaseCartProductQuantity(product.barcode) },
                        onDecreaseQuantity = { viewModel.decreaseCartProductQuantity(product.barcode) },
                        onDelete = { viewModel.deleteCartProduct(product.barcode) }
                    )
                }
            }

            // Botón para confirmar el ticket
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
fun ProductTicket(
    product: ProductFirebase,
    onIncreaseQuantity: () -> Unit, // Callback para incrementar cantidad
    onDecreaseQuantity: () -> Unit, // Callback para disminuir cantidad
    onDelete: () -> Unit // Callback para eliminar producto
) {
    val total = product.quantity * product.salePrice // Total del producto

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)) {
            Text("Producto: ${product.name}", style = MaterialTheme.typography.titleMedium)
            Text("Cantidad: ${product.quantity}")
            Text("Precio Unitario: $${product.salePrice}")
            Text("Total: $${total}")

            //Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                    //.padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Botón para disminuir cantidad
                IconButton(
                    onClick = onDecreaseQuantity,
                    modifier = Modifier.weight(1f),
                    enabled = false
                    //contentPadding = PaddingValues(12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_remove_24), // Usa un ícono de cámara
                        contentDescription = "Disminuir Cantidad",
                        modifier = Modifier.size(24.dp) // Tamaño del ícono
                    )
                }
                //Spacer(modifier = Modifier.width(8.dp))
                // Botón para incrementar cantidad
                IconButton(
                    onClick = onIncreaseQuantity,
                    modifier = Modifier.weight(1f),
                    enabled = false
                    //contentPadding = PaddingValues(12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_add_24), // Usa un ícono de cámara
                        contentDescription = "Incrementar Cantidad",
                        modifier = Modifier.size(24.dp) // Tamaño del ícono
                    )
                }
                //Spacer(modifier = Modifier.width(8.dp))
                // Botón para eliminar producto
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    //contentPadding = PaddingValues(12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_delete_24), // Usa un ícono de cámara
                        contentDescription = "Eliminar",
                        modifier = Modifier.size(24.dp) // Tamaño del ícono
                    )
                }
            }
        }
    }
}
