package com.elfrikiamv.minegocio_puntodeventa.ui.screens

//MainScreen.kt

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.home.expensesList.DetailsExpenseScreen
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
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.inventory.EditProductScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.inventory.InventoryScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.inventory.ScanProductScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.scan.ScanScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.shopping.ShoppingScreen
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.MainViewModel
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.home.expenses.ExpensesViewModel
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.home.missing.MissingProductsViewModel
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.inventory.AddProductViewModel
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.inventory.CheckProductExistsViewModel
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.inventory.EditProductViewModel
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.inventory.InventoryViewModel
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.shopping.ShoppingViewModel
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val expensesViewModel: ExpensesViewModel = viewModel()
    val inventoryViewModel: InventoryViewModel = viewModel()
    val editProductViewModel: EditProductViewModel = viewModel()
    val addProductViewModel: AddProductViewModel = viewModel()
    val checkProductExistsViewModel: CheckProductExistsViewModel = viewModel()
    val missingProductsViewModel: MissingProductsViewModel = viewModel()
    val shoppingViewModel: ShoppingViewModel = viewModel()
    val mainViewModel: MainViewModel = viewModel()
    val context = LocalContext.current
    val bannerID by mainViewModel.bannerID.collectAsState()

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
            painterResource(R.drawable.outline_barcode_scanner_24)
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

                Screen.DetailsExpense.route + "?expenseId={expenseId}" -> TopAppBar(
                    title = { Text("Detalles del gasto") },
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

                Screen.EditProduct.route + "?barcode={barcode}" -> TopAppBar(
                    title = { Text("Editar Producto") },
                    navigationIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_arrow_back_ios_new_24),
                            contentDescription = "Regresar",
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .clickable {
                                    navController.navigate(Screen.DetailsProduct.route + "?barcode=${inventoryViewModel.productDetails.value!!.barcode}") {
                                        popUpTo(Screen.DetailsProduct.route + "?barcode=${inventoryViewModel.productDetails.value!!.barcode}") {
                                            inclusive = true
                                        }
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
            Column() {
                BannerAds(bannerID = bannerID)
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

                Screen.DetailsExpense.route + "?expenseId={expenseId}" -> {
                    ExtendedFloatingActionButton(
                        onClick = {
                            // Lógica para borrar un gasto
                            Toast.makeText(
                                context,
                                "Borrado con éxito.",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_delete_24),
                                contentDescription = "Borrar gasto"
                            )
                        },
                        text = { Text("Borrar gasto") }
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

                Screen.AddExpense.route -> {
                    //val viewModel: ExpensesViewModel = viewModel()
                    val isLoading by expensesViewModel.isLoading.collectAsState()

                    ExtendedFloatingActionButton(
                        onClick = {
                            if (expensesViewModel.concept.value.isBlank() ||
                                expensesViewModel.paymentMethod.value.isBlank() ||
                                expensesViewModel.description.value.isBlank() ||
                                expensesViewModel.expensePriceInCents.longValue <= 0 ||
                                expensesViewModel.expenseAmountGivenInCents.longValue <= 0
                            ) {
                                expensesViewModel.errorMessage.value =
                                    "Todos los campos son obligatorios."
                            } else if (!isLoading) {
                                expensesViewModel.confirmExpense(
                                    paymentConcept = expensesViewModel.concept.value,
                                    paymentMethod = expensesViewModel.paymentMethod.value,
                                    description = expensesViewModel.description.value,
                                    amountExpense = expensesViewModel.expensePriceInCents.longValue / 100.0,
                                    amountGiven = expensesViewModel.expenseAmountGivenInCents.longValue / 100.0
                                )
                                // limpiar campos
                                expensesViewModel.clearExpenseFields()
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_save_24),
                                contentDescription = "Guardar gasto"
                            )
                        },
                        text = { Text("Guardar Gasto") },
                        /*containerColor = if (!isLoading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (!isLoading) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant*/
                    )
                }

                Screen.Inventory.route -> {
                    ExtendedFloatingActionButton(
                        onClick = { inventoryViewModel.showBottomSheet.value = true },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_add_24),
                                contentDescription = "Añadir Producto"
                            )
                        },
                        text = { Text("Añadir Producto") }
                        //contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        //containerColor = MaterialTheme.colorScheme.primaryContainer

                        /*containerColor = if (!isLoading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (!isLoading) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant*/

                    )
                }

                Screen.Shopping.route -> {
                    val isLoading by shoppingViewModel.isLoading.collectAsState()
                    val cartProducts by shoppingViewModel.cartProducts.collectAsState()
                    ExtendedFloatingActionButton(
                        onClick = {
                            shoppingViewModel.onConfirmPurchaseClicked(
                                onSuccess = { message ->
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                },
                                onError = { errorMessage ->
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                },
                                onEmptyCart = { emptyCart ->
                                    Toast.makeText(context, emptyCart, Toast.LENGTH_LONG).show()
                                    navController.navigate(Screen.ScanProduct.route) {
                                        popUpTo(Screen.ScanProduct.route) { inclusive = true }
                                    }
                                },
                            )
                        },
                        icon = {
                            Icon(
                                painter = if (isLoading)
                                    painterResource(id = R.drawable.baseline_access_time_24)
                                else if (cartProducts.isEmpty())
                                    painterResource(id = R.drawable.outline_barcode_scanner_24)
                                else
                                    painterResource(id = R.drawable.outline_check_24),

                                contentDescription = "Confirmar compra"

                            )
                        },
                        text = {
                            Text(
                                text = if (isLoading)
                                    "Procesando..."
                                else if (cartProducts.isEmpty())
                                    "Escanear código de barras"
                                else
                                    "Confirmar compra"
                            )
                        },
                        // Cambia el color para deshabilitar visualmente el botón mientras carga
                        containerColor = if (isLoading) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (isLoading) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                /*Screen.Shopping.route -> {

                    val isLoading by shoppingViewModel.isLoading.collectAsState()
                    ExtendedFloatingActionButton(
                        onClick = {
                            if (isLoading) {
                                Toast.makeText(context, "Ya se está cargando", Toast.LENGTH_SHORT)
                                    .show()
                                return@ExtendedFloatingActionButton
                            } else if (shoppingViewModel.amountReceived.value.toFloatOrNull()
                                    ?.let { it >= shoppingViewModel.totalPurchase.value } == true
                            ) {

                                shoppingViewModel.showEmailDialog.value = true
                                Toast.makeText(context, "Ticket guardado", Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                Toast.makeText(
                                    context,
                                    "El pago recibido debe ser mayor o igual al total de la compra.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.outline_check_24),
                                contentDescription = "confirmar compra"
                            )
                        },
                        text = { Text("confirmar compra") }
                    )
                }*/

                Screen.AddMissing.route -> {

                    val isLoading by missingProductsViewModel.isLoading.collectAsState()
                    ExtendedFloatingActionButton(
                        onClick = {
                            if (missingProductsViewModel.name.value.isBlank() ||
                                (missingProductsViewModel.quantity.value <= 0.toString() || missingProductsViewModel.quantity.value.isBlank()) ||
                                missingProductsViewModel.description.value.isBlank() ||
                                missingProductsViewModel.productPriceInCents.longValue <= 0 ||
                                missingProductsViewModel.totalPrice.value <= 0.toString()
                            ) {
                                missingProductsViewModel.errorMessage.value =
                                    "Todos los campos son obligatorios."
                            } else if (!isLoading) {
                                // Guardar gasto en Room y subir a Firebase
                                missingProductsViewModel.confirmMissing(
                                    name = missingProductsViewModel.name.value,
                                    quantity = missingProductsViewModel.quantity.value.toInt(),
                                    providerPrice = missingProductsViewModel.productPriceInCents.longValue / 100.0,
                                    totalPrice = missingProductsViewModel.totalPrice.value.toDouble(),
                                    description = missingProductsViewModel.description.value
                                )
                                // Limpiar campos después de guardar
                                missingProductsViewModel.clearMissingFields()
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_save_24),
                                contentDescription = "Guardar faltante"
                            )
                        },
                        text = { Text("Guardar faltante") },
                    )
                }

                Screen.DetailsProduct.route + "?barcode={barcode}" -> {

                    val isLoading by inventoryViewModel.isLoading.collectAsState()
                    ExtendedFloatingActionButton(
                        onClick = {
                            if (inventoryViewModel.productDetails.value == null) {
                                inventoryViewModel.errorMessage.value =
                                    "Hubo un error al cargar los datos del producto."
                            } else if (!isLoading) {
                                navController.navigate(Screen.EditProduct.route + "?barcode=${inventoryViewModel.productDetails.value!!.barcode}")
                                //navController.navigate(Screen.DetailsProduct.route + "?barcode=${editProductViewModel.barcode.value}")
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_edit_24),
                                contentDescription = "Editar Producto"
                            )
                        },
                        text = { Text("Editar Producto") }
                    )
                }

                Screen.AddProduct.route + "?barcode={barcode}" -> {

                    val isLoading by addProductViewModel.isLoading.collectAsState()

                    ExtendedFloatingActionButton(
                        onClick = {
                            if (addProductViewModel.barcode.value.isBlank() ||
                                addProductViewModel.name.value.isBlank() ||
                                (addProductViewModel.quantity.value <= 0.toString() || addProductViewModel.quantity.value.isBlank()) ||
                                addProductViewModel.providerPriceInCents.longValue <= 0 ||
                                addProductViewModel.salePriceInCents.longValue <= 0 ||
                                addProductViewModel.description.value.isBlank()
                            ) {
                                addProductViewModel.errorMessage.value =
                                    "Todos los campos son obligatorios."
                            } else if (!isLoading) {

                                addProductViewModel.errorMessage.value = ""

                                if (!addProductViewModel.isProductFound.value) {
                                    Toast.makeText(
                                        context,
                                        "El producto ya existe.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    // Lógica para guardar el producto
                                    val quantityValue =
                                        addProductViewModel.quantity.value.toIntOrNull() ?: 0

                                    addProductViewModel.addOrUpdateProduct(
                                        id = addProductViewModel.id.value,
                                        name = addProductViewModel.name.value,
                                        quantity = quantityValue,
                                        barcode = addProductViewModel.barcode.value,
                                        providerPrice = addProductViewModel.providerPriceInCents.longValue / 100.0,
                                        salePrice = addProductViewModel.salePriceInCents.longValue / 100.0,
                                        description = addProductViewModel.description.value
                                    )
                                    // Limpiar campos después de guardar
                                    addProductViewModel.clearAddProductSaveFields()

                                    // Mostrar mensaje de éxito
                                    Toast.makeText(
                                        context,
                                        "Producto guardado.",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_save_24),
                                contentDescription = "Guardar Producto"
                            )
                        },
                        text = { Text("Guardar Producto") },
                    )
                }

                Screen.EditProduct.route + "?barcode={barcode}" -> {

                    val isLoading by editProductViewModel.isLoading.collectAsState()

                    ExtendedFloatingActionButton(
                        onClick = {
                            if (editProductViewModel.barcode.value.isBlank() ||
                                editProductViewModel.name.value.isBlank() ||
                                (editProductViewModel.quantity.value <= 0.toString() || editProductViewModel.quantity.value.isBlank()) ||
                                editProductViewModel.providerPriceInCents.longValue <= 0 ||
                                editProductViewModel.salePriceInCents.longValue <= 0 ||
                                editProductViewModel.description.value.isBlank()
                            ) {
                                editProductViewModel.errorMessage.value =
                                    "Todos los campos son obligatorios."
                            } else if (!isLoading) {
                                // Lógica para guardar el producto
                                //val providerPriceValue = providerPrice.toDoubleOrNull() ?: 0.0
                                //val salePriceValue = salePrice.toDoubleOrNull() ?: 0.0
                                val quantityValue =
                                    editProductViewModel.quantity.value.toIntOrNull() ?: 0

                                editProductViewModel.addOrUpdateProduct(
                                    id = editProductViewModel.id.value,
                                    name = editProductViewModel.name.value,
                                    quantity = quantityValue,
                                    barcode = editProductViewModel.barcode.value,
                                    //providerPrice = providerPriceValue,
                                    //salePrice = salePriceValue,
                                    providerPrice = editProductViewModel.providerPriceInCents.longValue / 100.0,
                                    salePrice = editProductViewModel.salePriceInCents.longValue / 100.0,
                                    description = editProductViewModel.description.value
                                )
                                // Limpiar campos después de guardar
                                editProductViewModel.clearEditProductSaveFields()

                                // Mostrar mensaje de éxito
                                Toast.makeText(context, "Producto guardado", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_save_24),
                                contentDescription = "Guardar Producto"
                            )
                        },
                        text = { Text("Guardar Producto") },
                    )
                }

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
                composable(Screen.ScanProduct.route) {
                    ScanScreen(
                        navController = navController,
                        checkProductExistsViewModel = checkProductExistsViewModel
                    )
                }
                composable(Screen.Shopping.route) { ShoppingScreen(shoppingViewModel) }
                composable(Screen.Inventory.route) {
                    InventoryScreen(
                        navController,
                        inventoryViewModel
                    )
                }
                composable(
                    route = Screen.AddProduct.route + "?barcode={barcode}",
                    arguments = listOf(navArgument("barcode") { type = NavType.StringType })
                ) { backStackEntry ->

                    val barcode = backStackEntry.arguments?.getString("barcode")
                    AddProductScreen(
                        barcodeRoute = barcode,
                        navController,
                        addProductViewModel,
                        checkProductExistsViewModel
                    )
                }
                composable(
                    route = Screen.EditProduct.route + "?barcode={barcode}",
                    arguments = listOf(navArgument("barcode") { type = NavType.StringType })
                ) { backStackEntry ->

                    val barcode = backStackEntry.arguments?.getString("barcode")
                    EditProductScreen(
                        barcodeDetails = barcode,
                        editProductViewModel,
                        checkProductExistsViewModel
                    )
                }
                composable(Screen.ScanAddProduct.route) { ScanProductScreen(navController = navController) }
                composable(
                    route = Screen.DetailsProduct.route + "?barcode={barcode}",
                    arguments = listOf(navArgument("barcode") { type = NavType.StringType })
                ) { backStackEntry ->

                    val barcode = backStackEntry.arguments?.getString("barcode")
                    DetailsProductScreen(
                        barcode = barcode,
                        inventoryViewModel,
                        checkProductExistsViewModel
                    )
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
                composable(Screen.ExpensesListScreen.route) { ExpensesListScreen(navController) }
                composable(
                    route = Screen.DetailsExpense.route + "?expenseId={expenseId}",
                    arguments = listOf(navArgument("expenseId") { type = NavType.StringType })
                ) { backStackEntry ->

                    val expenseId = backStackEntry.arguments?.getString("expenseId")
                    DetailsExpenseScreen(expenseId = expenseId, expensesViewModel)
                }
                composable(Screen.AddExpense.route) { AddExpenseScreen(expensesViewModel) }
                composable(Screen.AddMissing.route) { AddMissingScreen(missingProductsViewModel) }
            }
        }
    )
}

@Composable
fun BannerAds(bannerID: String) {
    Box(
        modifier = Modifier.fillMaxWidth()
        //.background(color = androidx.compose.ui.graphics.Color.DarkGray),
        //Modifier.background(color = androidx.compose.ui.graphics.Color.Magenta),
        //contentAlignment = Alignment.BottomCenter,
    ) {
        //Text("test")

        AndroidView(modifier = Modifier.fillMaxWidth(), factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = bannerID
                loadAd(AdRequest.Builder().build())
                this.adListener = object : AdListener() {
                    override fun onAdClicked() {
                        // Code to be executed when the user clicks on an ad.
                    }

                    override fun onAdClosed() {
                        // Code to be executed when the user is about to return
                        // to the app after tapping on an ad.
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.d("AdView", "onAdFailedToLoad $adError")
                        // Code to be executed when an ad request fails.
                    }

                    override fun onAdImpression() {
                        // Code to be executed when an impression is recorded
                        // for an ad.
                    }

                    override fun onAdLoaded() {
                        // Code to be executed when an ad finishes loading.
                    }

                    override fun onAdOpened() {
                        // Code to be executed when an ad opens an overlay that
                        // covers the screen.
                    }
                }

            }
        })
    }
}

// Clase de datos para los elementos de navegación
data class NavigationItem(
    val label: String,
    val route: String,
    val icon: Painter
)