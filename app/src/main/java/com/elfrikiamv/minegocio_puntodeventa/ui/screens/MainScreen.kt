package com.elfrikiamv.minegocio_puntodeventa.ui.screens

//MainScreen.kt

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
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
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.home.HomeScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.home.bestSellersList.BestSellersScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.home.expensesList.AddExpenseScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.home.expensesList.ExpensesListScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.home.expensesList.onAddExpense
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.home.lowInventoryList.LowInventoryScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.home.missingList.AddMissingScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.home.missingList.MissingListScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.home.missingList.onAddMissing
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.home.outOfStockList.OutOfStockScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.inventory.AddProductScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.inventory.DetailsInventoryScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.inventory.DetailsProductScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.inventory.InventoryScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.inventory.ScanProductScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.scan.ScanScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.shopping.ShoppingScreen

@OptIn(ExperimentalMaterial3Api::class)
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
        topBar = {
            when (currentRoute) {
                Screen.Main.route -> TopAppBar(title = { Text("Inicio") })
                Screen.Activity.route -> TopAppBar(title = { Text("Actividad") })
                Screen.ScanProduct.route -> TopAppBar(title = { Text("Escanear Código de Barras") })
                Screen.Shopping.route -> TopAppBar(title = { Text("Carrito de Compras") })
                Screen.Inventory.route -> TopAppBar(title = { Text("Inventario") })
                Screen.ExpensesListScreen.route -> TopAppBar(
                    title = { Text("Mis gastos") },
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

                Screen.MissingListProducts.route -> TopAppBar(
                    title = { Text("Mis faltantes") },
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

                Screen.AddExpense.route -> TopAppBar(
                    title = { Text("Agregar Gasto") },
                    navigationIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_arrow_back_ios_new_24),
                            contentDescription = "Regresar",
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .clickable {
                                    navController.navigate(Screen.ExpensesListScreen.route) {
                                        popUpTo(Screen.ExpensesListScreen.route) {
                                            inclusive = true
                                        }
                                    }
                                }
                        )
                    }
                )

                Screen.AddMissing.route -> TopAppBar(
                    title = { Text("Agregar faltante") },
                    navigationIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_arrow_back_ios_new_24),
                            contentDescription = "Regresar",
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .clickable {
                                    navController.navigate(Screen.MissingListProducts.route) {
                                        popUpTo(Screen.MissingListProducts.route) {
                                            inclusive = true
                                        }
                                    }
                                }
                        )
                    }
                )

                Screen.LowInventoryProducts.route -> TopAppBar(
                    title = { Text("LowInventoryScreen") },
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

                Screen.OutOfStockProducts.route -> TopAppBar(
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

                Screen.BestSellersProducts.route -> TopAppBar(
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

                Screen.DetailsTicket.route + "?ticketId={ticketId}" -> TopAppBar(
                    title = { Text("Detalles del ticket") },
                    navigationIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_arrow_back_ios_new_24),
                            contentDescription = "Regresar",
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .clickable {
                                    navController.navigate(Screen.Activity.route) {
                                        popUpTo(Screen.Activity.route) { inclusive = true }
                                    }
                                }
                        )
                    }
                )

                Screen.DetailsProduct.route + "?barcode={barcode}" -> TopAppBar(
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

                Screen.AddProduct.route + "?barcode={barcode}" -> TopAppBar(
                    title = { Text("Añadir Producto") },
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

                Screen.ScanAddProduct.route -> TopAppBar(
                    title = { Text("Escanear Producto") },
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

                else -> {}  // Oculta la TopAppBar en otras pantallas
            }
        },
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
        floatingActionButton = {
            when (currentRoute) {
                Screen.ExpensesListScreen.route -> {
                    ExtendedFloatingActionButton(
                        onClick = {
                            // Lógica para agregar un gasto
                            onAddExpense(navController = navController)
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_add_24),
                                contentDescription = "Agregar gasto"
                            )
                        },
                        text = { Text("Agregar gasto") }
                    )
                }

                Screen.MissingListProducts.route -> {
                    ExtendedFloatingActionButton(
                        onClick = {
                            // Lógica para agregar un gasto
                            onAddMissing(navController = navController)
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_add_24),
                                contentDescription = "Agregar faltante"
                            )
                        },
                        text = { Text("Agregar faltante") }
                    )
                }

                Screen.DetailsProduct.route + "?barcode={barcode}" -> {}

                Screen.AddExpense.route -> {}
                Screen.AddMissing.route -> {}
                Screen.Inventory.route -> {}

                else -> {}
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
                composable(Screen.ScanProduct.route) { ScanScreen() }
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
                    DetailsTicketScreen(ticketId = ticketId)
                }
                composable(Screen.BestSellersProducts.route) { BestSellersScreen() }
                composable(Screen.LowInventoryProducts.route) { LowInventoryScreen() }
                composable(Screen.OutOfStockProducts.route) { OutOfStockScreen() }
                composable(Screen.MissingListProducts.route) { MissingListScreen() }
                composable(Screen.ExpensesListScreen.route) { ExpensesListScreen() }
                composable(Screen.AddExpense.route) { AddExpenseScreen(navController) }
                composable(Screen.AddMissing.route) { AddMissingScreen(navController) }
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