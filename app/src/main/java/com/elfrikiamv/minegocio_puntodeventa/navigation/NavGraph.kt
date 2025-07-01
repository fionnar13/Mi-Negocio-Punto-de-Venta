package com.elfrikiamv.minegocio_puntodeventa.navigation

// NavGraph.kt

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.MainScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.activity.ActivityScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.activity.DetailsTicketScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.home.bestSellersList.BestSellersScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.home.expensesList.AddExpenseScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.home.expensesList.ExpensesListScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.home.lowInventoryList.LowInventoryScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.home.missingList.AddMissingScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.home.missingList.MissingListScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.home.outOfStockList.OutOfStockScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.inventory.AddProductScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.inventory.DetailsInventoryScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.inventory.DetailsProductScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.inventory.InventoryScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.inventory.ScanProductScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.login.LoginScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.login.RegisterScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.scan.ScanScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.shopping.ShoppingScreen

@Composable
fun NavGraph(navController: NavHostController) {
    // Configuración del NavHost con la ruta inicial como "login"
    NavHost(navController = navController, startDestination = Screen.Login.route) {

        // Ruta para la pantalla de Login
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }

        // Ruta para la pantalla de Registro
        composable(Screen.Register.route) {
            RegisterScreen(navController)
        }
        // Ruta para la pantalla principal
        composable(Screen.Main.route) {
            MainScreen()
        }

        // Ruta para la pantalla de Inventario
        composable(Screen.Inventory.route) {
            InventoryScreen(navController)
        }

        // Ruta para la pantalla de Agregar Producto
        composable(
            route = Screen.AddProduct.route + "?barcode={barcode}",
            arguments = listOf(navArgument("barcode") { type = NavType.StringType })
        ) { backStackEntry ->
            val barcode = backStackEntry.arguments?.getString("barcode")
            AddProductScreen(navController = navController, barcodeDetails = barcode)
        }

        // Ruta para la pantalla de escanear producto para agregarlo al inventario
        composable(Screen.ScanAddProduct.route) {
            ScanProductScreen(navController)
        }
        // Ruta para la pantalla de escanear producto
        composable(Screen.ScanProduct.route) {
            ScanScreen()
        }

        // Ruta para la pantalla de escanear producto
        composable(Screen.Activity.route) {
            ActivityScreen(navController)
        }

        // Ruta para la pantalla de escanear producto
        composable(Screen.Shopping.route) {
            ShoppingScreen()
        }

        // Ruta para la pantalla de detalles producto
        composable(
            route = Screen.DetailsProduct.route + "?barcode={barcode}",
            arguments = listOf(navArgument("barcode") { type = NavType.StringType })
        ) { backStackEntry ->
            val barcode = backStackEntry.arguments?.getString("barcode")
            DetailsProductScreen(navController = navController, barcode = barcode)
        }

        // Ruta para la pantalla de detalles del inventario
        composable(Screen.DetailsInventory.route) {
            DetailsInventoryScreen(navController)
        }

        // Ruta para la pantalla de detalles del ticket
        composable(
            route = Screen.DetailsTicket.route + "?ticketId={ticketId}",
            arguments = listOf(navArgument("ticketId") { type = NavType.StringType })
        ) { backStackEntry ->
            val ticketId = backStackEntry.arguments?.getString("ticketId")
            DetailsTicketScreen(ticketId = ticketId)
        }

        // Ruta para la pantalla con los productos mas vendidos
        composable(Screen.BestSellersProducts.route) {
            BestSellersScreen()
        }

        // Ruta para la pantalla con los productos con bajo inventario
        composable(Screen.LowInventoryProducts.route) {
            LowInventoryScreen()
        }

        // Ruta para la pantalla con los productos agotados
        composable(Screen.OutOfStockProducts.route) {
            OutOfStockScreen()
        }

        // Ruta para la pantalla con los productos faltantes
        composable(Screen.MissingListProducts.route) {
            MissingListScreen()
        }

        // Ruta para la pantalla con los detalles de los gastos
        composable(Screen.ExpensesListScreen.route) {
            ExpensesListScreen()
        }

        // Ruta para la pantalla para agregar gastos
        composable(Screen.AddExpense.route) {
            AddExpenseScreen(navController)
        }

        // Ruta para la pantalla para agregar faltante
        composable(Screen.AddMissing.route) {
            AddMissingScreen(navController)
        }

    }
}