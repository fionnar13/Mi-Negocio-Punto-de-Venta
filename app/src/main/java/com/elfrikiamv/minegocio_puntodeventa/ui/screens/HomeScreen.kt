package com.elfrikiamv.minegocio_puntodeventa.ui.screens

// HomeScreen.kt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {

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
                        MyReport()
                    }
                }
            }
        }
    )
}

@Preview
@Composable
fun MyReport() {

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
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(text = "Has\ngastado:")
                    Text(text = "#")
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
        // Tarjeta de mi lista de faltantes
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {

                /*Text(
                    text = "Total en mi inventario:",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )*/
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
                        Text(text = "#", modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Valor:",
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Text(text = "#", modifier = Modifier.align(Alignment.CenterHorizontally))
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
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(text = "Mi lista\nde faltantes:")
                    Text(text = "#")
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            // Tarjeta de productos bajos en inventario
            ElevatedCard(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                    //.fillMaxWidth()
                ) {
                    Text(text = "Productos con\nbajo inventario:")
                    Text(text = "#")
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
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(text = "Productos\nagotados:")
                    Text(text = "#")
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            // Tarjeta de productos mas vendidos en inventario
            ElevatedCard(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(text = "Productos\nmás vendidos:")
                    Text(text = "#")
                }
            }
        }
    }
}