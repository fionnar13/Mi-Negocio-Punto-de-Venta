package com.elfrikiamv.minegocio_puntodeventa.navigation

// NavGraph.kt

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.ActivityScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.LoginScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.MainScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.RegisterScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.ScanScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.ShoppingScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.inventory.AddProductScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.inventory.DetailsInventoryScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.inventory.DetailsProductScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.inventory.InventoryScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.inventory.ScanProductScreen

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
            ScanScreen(navController)
        }

        // Ruta para la pantalla de escanear producto
        composable(Screen.Activity.route) {
            ActivityScreen()
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
    }
}