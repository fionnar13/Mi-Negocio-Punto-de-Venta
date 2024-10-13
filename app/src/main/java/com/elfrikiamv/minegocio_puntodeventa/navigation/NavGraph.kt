package com.elfrikiamv.minegocio_puntodeventa.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.HomeScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.LoginScreen
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
        composable("home") {
            HomeScreen()
        }
    }
}