package com.elfrikiamv.minegocio_puntodeventa.ui.screens.inventory

// DetailsProductScreen.kt

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.elfrikiamv.minegocio_puntodeventa.R
import com.elfrikiamv.minegocio_puntodeventa.model.ProductFirebase
import com.elfrikiamv.minegocio_puntodeventa.navigation.Screen
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsProductScreen(navController: NavHostController, barcode: String?) {
    val viewModel: InventoryViewModel = viewModel()
    var productDetails by remember { mutableStateOf<ProductFirebase?>(null) }

    // Cargar los detalles del producto
    LaunchedEffect(barcode) {
        barcode?.let {
            viewModel.checkProductExists(it) { product ->
                productDetails = product
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles del producto") },
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
            if (productDetails == null) {
                Text(
                    "Cargando detalles del producto...",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                // Mostrar detalles del producto
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
                            text = "Nombre: ${productDetails!!.name}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Cantidad: ${productDetails!!.quantity}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Código de barras: ${productDetails!!.barcode}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Precio proveedor: ${productDetails!!.providerPrice}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Precio venta: ${productDetails!!.salePrice}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Descripción: ${productDetails!!.description}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        // Botón editar Producto
                        Button(
                            onClick = {
                                // Navegar a DetailsProductScreen pasando el código de barras
                                navController.navigate(Screen.AddProduct.route + "?barcode=${productDetails!!.barcode}")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            Text("Editar Producto")
                        }
                    }
                }
            }
        }
    }
}