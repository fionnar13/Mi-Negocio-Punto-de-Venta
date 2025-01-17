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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.elfrikiamv.minegocio_puntodeventa.R
import com.elfrikiamv.minegocio_puntodeventa.navigation.Screen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.activity.ActivityScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.activity.DetailsTicketScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.home.BestSellersScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.home.ExpensesDetailsScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.home.HomeScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.home.LowInventoryScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.home.MissingListScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.home.OutOfStockScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.inventory.AddProductScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.inventory.DetailsInventoryScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.inventory.DetailsProductScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.inventory.InventoryScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.inventory.ScanProductScreen

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val items = listOf(
        NavigationItem("Inicio", Screen.Main.route, painterResource(R.drawable.baseline_store_24)),
        NavigationItem(
            "Actividad",
            Screen.Activity.route,
            painterResource(R.drawable.baseline_format_list_bulleted_24)
        ),
        NavigationItem(
            "Escanear",
            Screen.ScanProduct.route,
            painterResource(R.drawable.baseline_qr_code_scanner_24)
        ),
        NavigationItem(
            "Carrito",
            Screen.Shopping.route,
            painterResource(R.drawable.baseline_shopping_cart_24)
        ),
        NavigationItem(
            "Inventario",
            Screen.Inventory.route,
            painterResource(R.drawable.baseline_dashboard_customize_24)
        )
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Main.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        },
        content = { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screen.Main.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(Screen.Main.route) { HomeScreen(navController = navController) }
                composable(Screen.Activity.route) { ActivityScreen(navController = navController) }
                composable(Screen.ScanProduct.route) { ScanScreen(navController = navController) }
                composable(Screen.Shopping.route) { ShoppingScreen() }
                composable(Screen.Inventory.route) { InventoryScreen(navController = navController) }
                composable(
                    route = Screen.AddProduct.route + "?barcode={barcode}",
                    arguments = listOf(navArgument("barcode") { type = NavType.StringType })
                ) { backStackEntry ->
                    val barcode = backStackEntry.arguments?.getString("barcode")
                    AddProductScreen(navController = navController, barcodeDetails = barcode)
                }
                composable(Screen.ScanAddProduct.route) { ScanProductScreen(navController = navController) }
                composable(
                    route = Screen.DetailsProduct.route + "?barcode={barcode}",
                    arguments = listOf(navArgument("barcode") { type = NavType.StringType })
                ) { backStackEntry ->
                    val barcode = backStackEntry.arguments?.getString("barcode")
                    DetailsProductScreen(navController = navController, barcode = barcode)
                }
                composable(Screen.DetailsInventory.route) { DetailsInventoryScreen(navController = navController) }
                composable(
                    route = Screen.DetailsTicket.route + "?ticketId={ticketId}",
                    arguments = listOf(navArgument("ticketId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val ticketId = backStackEntry.arguments?.getString("ticketId")
                    DetailsTicketScreen(navController = navController, ticketId = ticketId)
                }
                composable(Screen.BestSellersProducts.route) { BestSellersScreen(navController) }
                composable(Screen.LowInventoryProducts.route) { LowInventoryScreen(navController) }
                composable(Screen.OutOfStockProducts.route) { OutOfStockScreen(navController) }
                composable(Screen.MissingListProducts.route) { MissingListScreen(navController) }
                composable(Screen.ExpensesDetails.route) { ExpensesDetailsScreen(navController) }
            }
        }
    )
}

// Clase de datos para los elementos de navegación
data class NavigationItem(
    val label: String,
    val route: String,
    val icon: Painter
)