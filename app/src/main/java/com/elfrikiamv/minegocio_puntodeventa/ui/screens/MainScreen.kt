package com.elfrikiamv.minegocio_puntodeventa.ui.screens

//MainScreen.kt

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.elfrikiamv.minegocio_puntodeventa.R

@Composable
fun MainScreen(navController: NavController) {
    // Estado para el índice seleccionado en la barra de navegación
    var selectedIndex by remember { mutableIntStateOf(0) }

    // Lista de elementos de navegación
    val items = listOf(
        NavigationItem("Inicio", painterResource(R.drawable.baseline_store_24)),
        NavigationItem("Actividad", painterResource(R.drawable.baseline_format_list_bulleted_24)),
        NavigationItem("Escanear", painterResource(R.drawable.baseline_qr_code_scanner_24)),
        NavigationItem("Carrito", painterResource(R.drawable.baseline_shopping_cart_24)),
        NavigationItem("Inventario", painterResource(R.drawable.baseline_dashboard_customize_24))
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        // Contenido de la pantalla según el índice seleccionado
        when (selectedIndex) {
            0 -> HomeScreen(modifier = Modifier.padding(innerPadding))
            1 -> ActivityScreen(modifier = Modifier.padding(innerPadding))
            2 -> ScanScreen(  // Aquí debes pasar viewModel y navController
                viewModel = viewModel(),  // Obtén el viewModel de la manera que lo necesites
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )

            3 -> ShoppingScreen(
                viewModel = viewModel(),
                modifier = Modifier.padding(innerPadding)
            )

            4 -> InventoryScreen(
                navController = navController,  // Pasar navController aquí
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

// Clase de datos para los elementos de navegación
data class NavigationItem(
    val label: String,
    val icon: Painter
)