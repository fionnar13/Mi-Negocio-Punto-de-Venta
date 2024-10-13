package com.elfrikiamv.minegocio_puntodeventa.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
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

@Composable
fun HomeScreen() {
    // Estado para el índice seleccionado en la barra de navegación
    var selectedIndex by remember { mutableIntStateOf(0) }

    // Lista de elementos de navegación
    val items = listOf(
        NavigationItem("Inicio", Icons.Default.Home),
        NavigationItem("Actividad", Icons.Default.Menu),
        NavigationItem("Scan QR", Icons.Default.Add),
        NavigationItem("Carrito", Icons.Default.ShoppingCart),
        NavigationItem("Inventario", Icons.Default.Edit)
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
            0 -> InicioScreen(modifier = Modifier.padding(innerPadding))
            1 -> ActividadScreen(modifier = Modifier.padding(innerPadding))
            2 -> ScanQRScreen(modifier = Modifier.padding(innerPadding))
            3 -> CarritoScreen(modifier = Modifier.padding(innerPadding))
            4 -> InventarioScreen(modifier = Modifier.padding(innerPadding))
        }
    }
}

// Clase de datos para los elementos de navegación
data class NavigationItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)