package com.elfrikiamv.minegocio_puntodeventa.ui.screens.home

// BestSellersScreen.kt

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.elfrikiamv.minegocio_puntodeventa.R
import com.elfrikiamv.minegocio_puntodeventa.navigation.Screen
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.BestSellersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BestSellersScreen(navController: NavController) {
    val viewModel: BestSellersViewModel = viewModel()
    val bestSellers by viewModel.bestSellers.collectAsState()

    // Contenido de la pantalla que muestra los productos más vendidos
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Productos más vendidos") },
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(bestSellers) { product ->
                        ProductCard(
                            name = product["name"] as String,
                            barcode = product["barcode"] as String,
                            quantitySold = product["quantitySold"] as Int
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun ProductCard(name: String, barcode: String, quantitySold: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Nombre: $name", modifier = Modifier.padding(bottom = 4.dp))
            Text(text = "Código de barras: $barcode", modifier = Modifier.padding(bottom = 4.dp))
            Text(text = "Cantidad vendida: $quantitySold")
        }
    }
}