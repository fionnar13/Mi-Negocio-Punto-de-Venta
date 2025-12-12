package com.elfrikiamv.minegocio_puntodeventa.navigation

// Screen.kt

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Main : Screen("main")
    object Inventory : Screen("inventory")
    object EditProduct : Screen("editProduct")
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
    object ExpensesListScreen : Screen("expensesListScreen")
    object DetailsExpense : Screen("detailsExpense")
    object AddExpense : Screen("addExpense")
    object AddMissing : Screen("addMissing")
    object AccountScreen : Screen("accountScreen")


    // Si alguna pantalla necesita parámetros, puedes agregar métodos helper.
    /*object ProductDetail : Screen("productDetail/{productId}") {
        fun createRoute(productId: Int) = "productDetail/$productId"
    }*/
}