package com.elfrikiamv.minegocio_puntodeventa.ui.screens.shopping

// ShoppingScreen.kt

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elfrikiamv.minegocio_puntodeventa.R
import com.elfrikiamv.minegocio_puntodeventa.model.inventory.ProductFirebase
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.shopping.ShoppingViewModel

@Composable
fun ShoppingScreen() {
    val viewModel: ShoppingViewModel = viewModel() // Obtenemos el ViewModel
    val products by viewModel.cartProducts.collectAsState() // Observamos el estado del carrito

    var amountReceived by remember { mutableStateOf("") } // Cantidad de pago recibida
    var isLoading by remember { mutableStateOf(false) } // Estado de carga para el botón
    val context = LocalContext.current

    var showEmailDialog by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }

    // Calcular el total de productos y el total de la compra
    val totalProducts = products.sumOf { it.quantity }
    val totalPurchase = products.sumOf { it.quantity * it.salePrice }

    // Calcular el cambio (si el amountReceived es válido y mayor o igual al total de la compra)
    val change = amountReceived.toFloatOrNull()?.let { received ->
        if (received >= totalPurchase) received - totalPurchase else 0f
    } ?: 0f

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        if (isLoading) {

            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        } else if (products.isEmpty()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "No hay productos en el carrito, por favor escanee un producto.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        } else {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                //.padding(end = 16.dp, start = 16.dp, top = 16.dp)
            ) {

                // Tarjeta de resumen del ticket
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Resumen del ticket"
                        )
                        Text(
                            text = "Productos totales: $totalProducts"
                        )
                        Text(
                            text = "Total de la compra: $${"%.2f".format(totalPurchase)}"
                        )
                        Text(
                            text = "Cambio de la compra: $${"%.2f".format(change)}"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = amountReceived,
                            onValueChange = { amountReceived = it },
                            label = { Text("Cantidad de pago recibida:") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = amountReceived.isBlank() || amountReceived.toFloatOrNull()
                                ?.let { it < totalPurchase } == true
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    //contentPadding = PaddingValues(vertical = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    items(products) { product ->
                        ProductTicket(
                            product = product,
                            onIncreaseQuantity = { viewModel.increaseCartProductQuantity(product.barcode) },
                            onDecreaseQuantity = { viewModel.decreaseCartProductQuantity(product.barcode) },
                            onDelete = { viewModel.deleteCartProduct(product.barcode) }
                        )
                    }
                }

                // Botón para confirmar el ticket
                Button(
                    //onClick = { showEmailDialog = true },
                    onClick = {
                        if (amountReceived.toFloatOrNull()
                                ?.let { it >= totalPurchase } == true
                        ) {
                            isLoading = true // Activar animación de carga

                            // Llamar al AlertDialog para ingresar el correo electrónico
                            showEmailDialog = true

                            //amountReceived = "" // Limpiar el campo de cantidad de pago

                            isLoading = false // Desactivar animación de carga

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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.inversePrimary
                        )
                    } else {
                        Text("Confirmar Ticket")
                    }
                }

            }
        }
    }

    // Diálogo para ingresar correo electrónico
    if (showEmailDialog) {
        AlertDialog(
            onDismissRequest = { showEmailDialog = false },
            title = { Text("Ingrese correo electrónico") },
            text = {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo electrónico") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email)
                                .matches()
                        ) {
                            showEmailDialog = false
                            isLoading = true

                            viewModel.confirmTicket(
                                totalProducts,
                                totalPurchase,
                                amountReceived.toDoubleOrNull() ?: 0.0,
                                context = context,
                                email = email
                            )

                            amountReceived = "" // Limpiar el campo de cantidad de pago
                            isLoading = false

                            Toast.makeText(context, "Ticket guardado", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Ingrese un correo válido", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                Button(onClick = { showEmailDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun ProductTicket(
    product: ProductFirebase,
    onIncreaseQuantity: () -> Unit, // Callback para incrementar cantidad
    onDecreaseQuantity: () -> Unit, // Callback para disminuir cantidad
    onDelete: () -> Unit // Callback para eliminar producto
) {
    val subTotal = product.quantity * product.salePrice // Total del producto

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // Campo Código de Barras con botón de cámara
            Row(
                modifier = Modifier.fillMaxWidth(),
                //horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Column(
                    modifier = Modifier
                        .weight(1f) // Ajusta el ancho para que ocupe el espacio restante
                        .fillMaxWidth()
                        .align(Alignment.CenterVertically)
                ) {
                    Text("Producto: ${product.name}")
                    Text("Cantidad: ${product.quantity}")
                    Text("Precio Unitario: $${product.salePrice}")
                    Text("SubTotal: $${subTotal}")
                }
                Column(
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    // Botón para disminuir cantidad
                    /*IconButton(
                        onClick = onDecreaseQuantity,
                        //modifier = Modifier.weight(1f),
                        enabled = false
                        //contentPadding = PaddingValues(12.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_remove_24), // Usa un ícono de cámara
                            contentDescription = "Disminuir Cantidad",
                            modifier = Modifier.size(24.dp) // Tamaño del ícono
                        )
                    }*/
                    //Spacer(modifier = Modifier.width(8.dp))
                    // Botón para incrementar cantidad
                    /*IconButton(
                        onClick = onIncreaseQuantity,
                        //modifier = Modifier.weight(1f),
                        enabled = false
                        //contentPadding = PaddingValues(12.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_add_24), // Usa un ícono de cámara
                            contentDescription = "Incrementar Cantidad",
                            modifier = Modifier.size(24.dp) // Tamaño del ícono
                        )
                    }*/
                    //Spacer(modifier = Modifier.width(8.dp))
                    // Botón para eliminar producto
                    IconButton(
                        onClick = onDelete,
                        //modifier = Modifier.weight(1f),
                        //contentPadding = PaddingValues(12.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_delete_24),
                            contentDescription = "Eliminar",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}