package com.elfrikiamv.minegocio_puntodeventa.ui.screens

// HomeScreen.kt

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.HomeViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val viewModel: HomeViewModel = viewModel()
    val totalQuantity by viewModel.totalQuantity.collectAsState()
    val totalSalePrice by viewModel.totalSalePrice.collectAsState()

    // Contenido de la pantalla de inicio
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inicio") }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {

                // Mostrar resumen de mi negocio
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    item {
                        MyReport(navController, totalQuantity, totalSalePrice)
                    }
                }
            }
        }
    )
}

@Composable
fun MyReport(navController: NavController, totalQuantity: Int, totalSalePrice: Double) {

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(text = "Resumen de mi negocio:")

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
                    Text(text = "Has\nvendido:")
                    Text(text = "#")
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
                        Text(text = "#")
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
                    Text(text = "Has\nganado:")
                    Text(text = "#")
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
                    Text(text = "Transacciones\ntotales:")
                    Text(text = "#")
                }
            }
        }

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
                        Text(text = "Mi lista\nde faltantes:")
                        Text(text = "#")
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
                        Text(text = "#")
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
                        Text(text = "#")
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
                        Text(text = "Productos\nmás vendidos:")
                        Text(text = "#")
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
    }
}

fun onExpenseDetails(navController: NavController) {
    // Acción para ver detalles de gastos
    navController.navigate(Screen.DetailsInventory.route)
}

fun onMissingList(navController: NavController) {
    // Acción para ver mi lista de faltantes
    navController.navigate(Screen.DetailsInventory.route)
}

fun onLowInventory(navController: NavController) {
    // Acción para ver productos bajos en inventario
    navController.navigate(Screen.DetailsInventory.route)
}

fun onOutOfStock(navController: NavController) {
    // Acción para ver productos agotados
    navController.navigate(Screen.DetailsInventory.route)
}

fun onBestSellers(navController: NavController) {
    // Acción para ver productos mas vendidos
    navController.navigate(Screen.DetailsInventory.route)
}