package com.elfrikiamv.minegocio_puntodeventa.ui.screens.home

// OutOfStockScreen.kt

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.elfrikiamv.minegocio_puntodeventa.R
import com.elfrikiamv.minegocio_puntodeventa.navigation.Screen
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.HomeViewModel
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.ProductDetailsByIdViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutOfStockScreen(navController: NavController) {

    val outOfStockViewModel: ProductDetailsByIdViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel()

    val outOfStockIds by homeViewModel.outStockProducts.collectAsState()
    val outOfStockProducts by outOfStockViewModel.products.collectAsState()

    // Observar el estado de isLoading
    val isLoading by outOfStockViewModel.isLoading.collectAsState()

    // Cuando se actualice la lista de IDs, cargar los detalles de productos
    LaunchedEffect(outOfStockIds) {
        val ids = outOfStockIds.mapNotNull { it["id"] as? String }
        if (ids.isNotEmpty()) {
            outOfStockViewModel.loadProductDetailsByIds(ids)
        }
    }

    // Contenido de la pantalla que muestra OutOfStockScreen
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("OutOfStockScreen") },
                navigationIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_arrow_back_ios_new_24),
                        contentDescription = "Regresar",
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clickable {
                                navController.navigate(Screen.Main.route) {
                                    popUpTo(Screen.Main.route) { inclusive = true }
                                }
                            }
                    )
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                // Mostrar el LinearProgressIndicator mientras se cargan los datos
                if (isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                if (outOfStockProducts.isEmpty() && !isLoading) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No hay productos disponibles.")
                        Text("):")
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(16.dp)
                    ) {
                        items(outOfStockProducts) { product ->
                            OutOfStockCard(product)
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun OutOfStockCard(product: Map<String, Any>) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(text = "Nombre: ${product["name"]}")
            Text(text = "Código de barras: ${product["barcode"]}")
            Text(text = "ID: ${product["id"]}")
        }
    }
}