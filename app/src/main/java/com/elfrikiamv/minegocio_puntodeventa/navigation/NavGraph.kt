package com.elfrikiamv.minegocio_puntodeventa.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.AddProductScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.InventoryScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.LoginScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.MainScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.RegisterScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = "login") {
        // Ruta para la pantalla de login
        composable("login") {
            LoginScreen(navController)
        }
        // Ruta para la pantalla de registro
        composable("register") {
            RegisterScreen(navController)
        }
        // Ruta para la pantalla principal
        composable("main") {
            MainScreen(navController)
        }

        // Ruta para la pantalla de inventario
        composable("inventory") {
            InventoryScreen(navController)
        }

        // Ruta para la pantalla de agregar producto
        composable("addProduct") {
            AddProductScreen(navController)
        }
    }
}