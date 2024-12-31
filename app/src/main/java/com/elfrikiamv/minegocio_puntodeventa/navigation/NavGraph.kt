package com.elfrikiamv.minegocio_puntodeventa.navigation

// NavGraph.kt

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.AddProductScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.InventoryScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.LoginScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.MainScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.RegisterScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.ScanProductScreen

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
            MainScreen(navController)
        }

        // Ruta para la pantalla de Inventario
        composable(Screen.Inventory.route) {
            InventoryScreen(navController)
        }

        // Ruta para la pantalla de Agregar Producto
        composable(Screen.AddProduct.route) {
            AddProductScreen(navController)
        }

        // Ruta para la pantalla de escanear producto
        composable(Screen.ScanProduct.route) {
            ScanProductScreen(navController)
        }
    }
}