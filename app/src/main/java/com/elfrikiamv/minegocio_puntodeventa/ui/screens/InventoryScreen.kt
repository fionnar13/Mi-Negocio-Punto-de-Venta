package com.elfrikiamv.minegocio_puntodeventa.ui.screens

// InventoryScreen.kt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.elfrikiamv.minegocio_puntodeventa.model.ProductFirebase
import com.elfrikiamv.minegocio_puntodeventa.navigation.Screen
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(navController: NavController, modifier: Modifier = Modifier) {
    val viewModel: InventoryViewModel = viewModel()
    val products by viewModel.products.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventario") }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Tarjeta de resumen del inventario
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Resumen del Inventario",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Productos totales: ${products.size}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Botones para añadir productos y escanear
                Text(
                    text = "¿Cómo quieres añadir el producto?",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { navController.navigate(Screen.AddProduct.route) },
                        modifier = Modifier.weight(1f),
                        //contentPadding = PaddingValues(12.dp)
                    ) {
                        //Icon(Icons.Filled.Add, contentDescription = "Añadir")
                        //Spacer(modifier = Modifier.width(8.dp))
                        Text("Manual")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { navController.navigate(Screen.ScanProduct.route) },
                        modifier = Modifier.weight(1f),
                        //contentPadding = PaddingValues(12.dp)
                    ) {
                        //Icon(Icons.Filled.Add, contentDescription = "Escanear")
                        //Spacer(modifier = Modifier.width(8.dp))
                        Text("Escanear")
                    }
                }

                // Divider entre los botones y la lista de productos
                HorizontalDivider(
                    modifier = Modifier.padding(bottom = 8.dp),
                    thickness = 1.dp
                )

                // Lista de productos
                if (products.isEmpty()) {
                    Box(
                        //modifier = Modifier.fillMaxSize(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f), // Ocupa espacio para mantener el diseño consistente
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No hay productos en el inventario ):",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        modifier = Modifier
                            .padding(bottom = 72.dp)
                            .fillMaxWidth()
                            .weight(1f), // Permite que la lista ocupe el espacio restante
                    ) {
                        items(products) { product ->
                            ProductCard(product = product) {
                                // Acción para editar el producto
                                //navController.navigate("editProduct/${product.id}")
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun ProductCard(product: ProductFirebase, onEdit: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
        //.clickable { onEdit() }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Cantidad: ${product.quantity}",
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = "Código de barras: ${product.barcode}",
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}