package com.elfrikiamv.minegocio_puntodeventa.ui.screens.inventory

// InventoryScreen.kt

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.elfrikiamv.minegocio_puntodeventa.R
import com.elfrikiamv.minegocio_puntodeventa.model.inventory.ProductFirebase
import com.elfrikiamv.minegocio_puntodeventa.navigation.Screen
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.inventory.InventoryViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(navController: NavController, modifier: Modifier = Modifier) {
    val viewModel: InventoryViewModel = viewModel()
    val products by viewModel.products.collectAsState()

    // Estado del Bottom Sheet
    val sheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    // Observar el estado de isLoading
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Inventario") })
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                //.padding(16.dp)
            ) {
                // Mostrar indicador de carga mientras los datos están cargándose
                if (isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                // Lista de productos
                if (products.isEmpty() && !isLoading) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "No hay productos en el inventario",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            "):",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    //HorizontalDivider(modifier = Modifier.padding(0.dp), thickness = 1.dp)
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(end = 16.dp, start = 16.dp)
                    ) {
                        items(products) { product ->
                            ProductCard(product = product) {
                                navController.navigate(Screen.DetailsProduct.route + "?barcode=${product.barcode}")
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(72.dp))
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showBottomSheet = true },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_add_24),
                        contentDescription = "Añadir Producto"
                    )
                },
                text = { Text("Añadir Producto") }
            )
        }
    )

    // Bottom Sheet con opciones
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp, bottom = 16.dp, start = 16.dp),
                //verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Text(
                    text = "¿Cómo quieres agregar el código de barras?",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = {
                        val barcodeRoute = ""
                        navController.navigate(Screen.AddProduct.route + "?barcode=${barcodeRoute}")
                        coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                            showBottomSheet = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_edit_24),
                        contentDescription = "Añadir manualmente"
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Manual")
                    Spacer(modifier = Modifier.weight(1f))
                }

                TextButton(
                    onClick = {
                        navController.navigate(Screen.ScanAddProduct.route)
                        coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                            showBottomSheet = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_camera_alt_24),
                        contentDescription = "Añadir por escaner"
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Escanear")
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ProductCard(product: ProductFirebase, onProductDetails: () -> Unit) {

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProductDetails() }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Row {
                            Text(
                                text = "Nombre: ",
                                fontStyle = FontStyle.Italic,
                                modifier = Modifier
                                    .alignByBaseline()
                                    .alpha(0.8f) // Aplica opacidad del 80%
                            )
                            Text(
                                text = product.name,
                                modifier = Modifier.alignByBaseline()
                            )
                        }
                        Row {
                            Text(
                                text = "Código de barras: ",
                                fontStyle = FontStyle.Italic,
                                modifier = Modifier
                                    .alignByBaseline()
                                    .alpha(0.8f) // Aplica opacidad del 80%
                            )
                            Text(
                                text = product.barcode,
                                modifier = Modifier.alignByBaseline()
                            )
                        }
                    }


                    HorizontalDivider(
                        modifier = Modifier.padding(bottom = 8.dp, top = 8.dp),
                        thickness = 1.dp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Row(
                            modifier = Modifier.weight(1f)
                        ) {
                            Row {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_shopping_bag_24),
                                    contentDescription = "bag icon",
                                    modifier = Modifier.size(24.dp),
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${product.quantity} ",
                                    modifier = Modifier.alignByBaseline()
                                )
                                Text(
                                    text = "piezas",
                                    fontStyle = FontStyle.Italic,
                                    modifier = Modifier
                                        .alignByBaseline()
                                        .alpha(0.8f) // Aplica opacidad del 80%
                                )
                            }
                        }
                        Row {
                            Text(
                                text = "Precio: ",
                                fontStyle = FontStyle.Italic,
                                modifier = Modifier
                                    .alignByBaseline()
                                    .alpha(0.8f) // Aplica opacidad del 80%
                            )
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_attach_money_24),
                                contentDescription = "money icon",
                                modifier = Modifier.size(24.dp),
                            )
                            Text(
                                text = "${product.salePrice}",
                                modifier = Modifier.alignByBaseline()
                            )
                        }
                    }
                }
                Column(
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    IconButton(
                        onClick = deleteProduct(product.id)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_delete_24),
                            contentDescription = "Eliminar",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun deleteProduct(productId: String): () -> Unit {

    val viewModel: InventoryViewModel = viewModel()
    return {
        viewModel.deleteProduct(productId)
    }
}