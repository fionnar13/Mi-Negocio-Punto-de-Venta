package com.elfrikiamv.minegocio_puntodeventa.navigation

// Screen.kt

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Main : Screen("main")
    object Inventory : Screen("inventory")
    object AddProduct : Screen("addProduct")
    object DetailsProduct : Screen("detailsProduct")
    object DetailsInventory : Screen("detailsInventory")
    object ScanProduct : Screen("scanProduct")
    object ScanAddProduct : Screen("scanAddProduct")
    object Activity : Screen("activity")
    object DetailsTicket : Screen("detailsTicket")
    object Shopping : Screen("shopping")
    object BestSellersProducts : Screen("bestSellersProducts")
    object LowInventoryProducts : Screen("lowInventoryProducts")
    object OutOfStockProducts : Screen("outOfStockProducts")
    object MissingListProducts : Screen("missingListProducts")
    object ExpensesDetails : Screen("expensesDetails")
    object AddExpense : Screen("addExpense")


    // Si alguna pantalla necesita parámetros, puedes agregar métodos helper.
    /*object ProductDetail : Screen("productDetail/{productId}") {
        fun createRoute(productId: Int) = "productDetail/$productId"
    }*/
}