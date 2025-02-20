package com.elfrikiamv.minegocio_puntodeventa.ui.screens.inventory

// DetailsProductScreen.kt

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.elfrikiamv.minegocio_puntodeventa.R
import com.elfrikiamv.minegocio_puntodeventa.model.inventory.ProductFirebase
import com.elfrikiamv.minegocio_puntodeventa.navigation.Screen
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.inventory.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsProductScreen(navController: NavHostController, barcode: String?) {
    val viewModel: InventoryViewModel = viewModel()
    var productDetails by remember { mutableStateOf<ProductFirebase?>(null) }

    var errorMessage by remember { mutableStateOf("") }
    val TAG = "DetailsProductScreen"

    // Observar el estado de isLoading
    val isLoading by viewModel.isLoading.collectAsState()

    // Cargar los detalles del producto
    LaunchedEffect(barcode) {

        if (!barcode.isNullOrEmpty()) {
            try {
                viewModel.checkProductExists(barcode) { product ->
                    productDetails = product
                }
                Log.d(TAG, "Código de barras recibido: $barcode")
            } catch (e: Exception) {
                errorMessage = "Hubo un error al cargar los datos del producto."
                Log.e(TAG, "Error al cargar los datos del producto: $e")
            }

        } else {
            errorMessage = "El código de barras no es válido."
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
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (productDetails == null) {
                        errorMessage = "Hubo un error al cargar los datos del producto."
                    } else if (!isLoading) {
                        navController.navigate(Screen.AddProduct.route + "?barcode=${productDetails!!.barcode}")
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_edit_24),
                        contentDescription = "Editar Producto"
                    )
                },
                text = { Text("Editar Producto") }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxWidth()
            ) {
                if (productDetails == null) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(16.dp)
                    ) {
                        item {
                            DetailsProductCard(productDetails!!, errorMessage)
                        }
                        item {
                            Spacer(modifier = Modifier.height(72.dp))
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun DetailsProductCard(productDetails: ProductFirebase, errorMessage: String) {

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row {
                Text(
                    text = "Nombre: ",
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier
                        .alignByBaseline()
                        .alpha(0.8f)
                )
                Text(
                    text = productDetails.name,
                    modifier = Modifier.alignByBaseline()
                )
            }
            Row {
                Text(
                    text = "Código de barras: ",
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier
                        .alignByBaseline()
                        .alpha(0.8f)
                )
                Text(
                    text = productDetails.barcode,
                    modifier = Modifier.alignByBaseline()
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(bottom = 8.dp, top = 8.dp),
                thickness = 1.dp
            )
            Row {
                Text(
                    text = "Cantidad: ",
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier
                        .alignByBaseline()
                        .alpha(0.8f)
                )
                Text(
                    text = "${productDetails.quantity}",
                    modifier = Modifier.alignByBaseline()
                )
            }
            Row {
                Text(
                    text = "Precio proveedor: ",
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier
                        .alignByBaseline()
                        .alpha(0.8f)
                )
                Text(
                    text = "${productDetails.providerPrice}",
                    modifier = Modifier.alignByBaseline()
                )
            }
            Row {
                Text(
                    text = "Precio venta: ",
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier
                        .alignByBaseline()
                        .alpha(0.8f)
                )
                Text(
                    text = "${productDetails.salePrice}",
                    modifier = Modifier.alignByBaseline()
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(bottom = 8.dp, top = 8.dp),
                thickness = 1.dp
            )
            Row {
                Text(
                    text = "Descripción: ",
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier
                        .alignByBaseline()
                        .alpha(0.8f)
                )
                Text(
                    text = productDetails.description,
                    modifier = Modifier.alignByBaseline()
                )
            }
            if (errorMessage.isNotBlank()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
