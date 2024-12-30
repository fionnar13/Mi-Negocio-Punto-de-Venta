package com.elfrikiamv.minegocio_puntodeventa.navigation

// Screen.kt

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Main : Screen("main")
    object Inventory : Screen("inventory")
    object AddProduct : Screen("addProduct")

    // Si alguna pantalla necesita parámetros, puedes agregar métodos helper.
    object ProductDetail : Screen("productDetail/{productId}") {
        fun createRoute(productId: Int) = "productDetail/$productId"
    }
}
