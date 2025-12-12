package com.elfrikiamv.minegocio_puntodeventa.ui.screens.home

// HomeScreen.kt

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.elfrikiamv.minegocio_puntodeventa.R
import com.elfrikiamv.minegocio_puntodeventa.navigation.Screen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.dialogues.helpDialogues.HelpIconWithDialog
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.dialogues.helpDialogues.HelpTexts
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.home.HomeViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val viewModel: HomeViewModel = viewModel()

    // Observamos los datos necesarios
    val totalQuantity by viewModel.totalQuantity.collectAsState()
    val totalSalePrice by viewModel.totalSalePrice.collectAsState()
    val lowStockList by viewModel.lowStockProducts.collectAsState()
    val outStockList by viewModel.outStockProducts.collectAsState()
    val totalTicketsSold by viewModel.totalTicketsSold.collectAsState()
    val totalTransactions by viewModel.totalTransactions.collectAsState()
    val totalExpenses by viewModel.totalExpenses.collectAsState()
    val totalProfitEarned by viewModel.totalProfitEarned.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()

    // Estado de carga: verificar si algún dato aún no está disponible
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            //.padding(paddingValues)
            .fillMaxSize()
    ) {
        // Mostrar indicador de carga mientras se cargan todos los datos
        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        } else {
            // Mostrar contenido principal cuando los datos estén listos
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(end = 16.dp, start = 16.dp, top = 16.dp)
            ) {
                item {
                    MyReport(
                        navController,
                        totalQuantity,
                        totalSalePrice,
                        lowStockList,
                        outStockList,
                        totalTicketsSold,
                        totalTransactions,
                        totalExpenses,
                        totalProfitEarned,
                        currentMonth
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun MyReport(
    navController: NavController,
    totalQuantity: Int,
    totalSalePrice: Double,
    lowStockList: List<Map<String, Any>>,
    outStockList: List<Map<String, Any>>,
    totalTicketsSold: Double,
    totalTransactions: Int,
    totalExpenses: Double,
    totalProfitEarned: Double,
    currentMonth: String
) {

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Resumen de mi negocio ($currentMonth):"
            //style = MaterialTheme.typography.titleMedium
        )


        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {

            // Tarjeta de has vendido
            ElevatedCard(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Row {
                        Text(
                            text = "Has\nvendido:",
                            maxLines = 2,
                            minLines = 2
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        //HelpIconWithDialog(helpText = "Aquí se muestra la suma total de dinero generado por las ventas registradas.")
                        //helpText = HelpTexts.hasVendido
                        HelpIconWithDialog(
                            title = "Has vendido",
                            helpText = HelpTexts.salesHelp
                        )
                    }

                    Row {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_attach_money_24),
                            contentDescription = "money icon",
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.CenterVertically)
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "%.2f", totalTicketsSold),
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            // Tarjeta de has gastado
            ElevatedCard(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
                    .clickable { onExpenseDetails(navController = navController) }
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Has\ngastado:")
                        Row {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_attach_money_24),
                                contentDescription = "money icon",
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.CenterVertically)
                            )
                            Text(
                                text = String.format(Locale.getDefault(), "%.2f", totalExpenses),
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }
                    }
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_arrow_forward_ios_24),
                        contentDescription = "forward icon",
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.CenterVertically)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            // Tarjeta de has ganado
            ElevatedCard(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Row {
                        Text(text = "Has\nganado:")
                        Spacer(modifier = Modifier.weight(1f))
                        HelpIconWithDialog(
                            title = "Has ganado",
                            helpText = HelpTexts.earningsHelp
                        )
                    }
                    Row {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_attach_money_24),
                            contentDescription = "money icon",
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.CenterVertically)
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "%.2f", totalProfitEarned),
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            // Tarjeta de numero de transacciones
            ElevatedCard(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Row {
                        Text(text = "Transacciones\ntotales:")
                        Spacer(modifier = Modifier.weight(1f))
                        HelpIconWithDialog(
                            title = "Transacciones totales",
                            helpText = HelpTexts.totalTransactionsHelp
                        )
                    }
                    Row {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_point_of_sale_24),
                            contentDescription = "point of sale icon",
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.CenterVertically)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "$totalTransactions",
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(top = 3.dp),
            thickness = 1.dp
        )

        Text(text = "Resumen de mi inventario:")
        // Tarjeta de resumen de mi inventario
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {

                Row(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Productos:",
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Row(
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {

                            Icon(
                                painter = painterResource(id = R.drawable.baseline_shopping_bag_24),
                                contentDescription = "shopping icon",
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.CenterVertically)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "$totalQuantity",
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Valor:",
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Row(
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_attach_money_24),
                                contentDescription = "money icon",
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.CenterVertically)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = String.format(Locale.getDefault(), "%.2f", totalSalePrice),
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(3.dp))
                    Column {
                        Row {
                            HelpIconWithDialog(
                                title = "Resumen de mi inventario",
                                helpText = HelpTexts.summaryInventoryHelp
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            // Tarjeta de total de mi inventario
            ElevatedCard(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
                    .clickable { onMissingList(navController = navController) }
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_format_list_bulleted_24),
                            contentDescription = "list icon",
                            modifier = Modifier.size(24.dp)
                        )
                        Text(text = "Mi lista\nde faltantes:")
                    }
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_arrow_forward_ios_24),
                        contentDescription = "forward icon",
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.CenterVertically)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            // Tarjeta de productos bajos en inventario
            ElevatedCard(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
                    .clickable { onLowInventory(navController = navController) }
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Productos con\nbajo inventario:")
                        Text(text = "${lowStockList.size}")
                    }
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_arrow_forward_ios_24),
                        contentDescription = "forward icon",
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.CenterVertically)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {

            // Tarjeta de productos agotados en inventario
            ElevatedCard(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
                    .clickable { onOutOfStock(navController = navController) }
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Productos\nagotados:")
                        Text(text = "${outStockList.size}")
                    }
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_arrow_forward_ios_24),
                        contentDescription = "forward icon",
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.CenterVertically)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Tarjeta de productos mas vendidos en inventario
            ElevatedCard(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
                    .clickable { onBestSellers(navController = navController) }
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_sell_24),
                            contentDescription = "sell icon",
                            modifier = Modifier.size(24.dp)
                        )
                        Text(text = "Productos\nmás vendidos:")
                    }
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_arrow_forward_ios_24),
                        contentDescription = "forward icon",
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.CenterVertically)
                    )
                }
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = 3.dp),
            thickness = 1.dp
        )
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onMyAccount(navController = navController) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_account_circle_24),
                        contentDescription = "account icon",
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Mi perfil",
                        modifier = Modifier.alignByBaseline()
                    )
                }
            }
        }
    }
}

fun onExpenseDetails(navController: NavController) {
    // Acción para ver detalles de gastos
    navController.navigate(Screen.ExpensesListScreen.route)
}

fun onMissingList(navController: NavController) {
    // Acción para ver mi lista de faltantes
    navController.navigate(Screen.MissingListProducts.route)
}

fun onLowInventory(navController: NavController) {
    // Acción para ver productos bajos en inventario
    navController.navigate(Screen.LowInventoryProducts.route)
}

fun onOutOfStock(navController: NavController) {
    // Acción para ver productos agotados
    navController.navigate(Screen.OutOfStockProducts.route)
}

fun onBestSellers(navController: NavController) {
    // Acción para ver productos mas vendidos
    navController.navigate(Screen.BestSellersProducts.route)
}

fun onMyAccount(navController: NavController) {
    // Acción para ver la cuenta de ususario
    navController.navigate(Screen.AccountScreen.route)
}
