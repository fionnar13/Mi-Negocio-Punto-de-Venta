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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.elfrikiamv.minegocio_puntodeventa.R
import com.elfrikiamv.minegocio_puntodeventa.model.inventory.ProductFirebase
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.shopping.ShoppingViewModel
import java.util.Locale

@Composable
fun ShoppingScreen(viewModel: ShoppingViewModel) {

    // Observamos el estado del carrito
    val products by viewModel.cartProducts.collectAsState()

    val context = LocalContext.current

    var email by remember { mutableStateOf("") }

    // Calcular el total de productos y el total de la compra
    val totalProducts = products.sumOf { it.quantity }

    val totalPurchase by viewModel.totalPurchase.collectAsState()

    val showEmailDialog by viewModel.showEmailDialog

    // Observar el estado de isLoading
    val isLoading by viewModel.isLoading.collectAsState()

    val change by viewModel.change.collectAsState()

    val shoppingAmountGivenInCents by viewModel.shoppingAmountGivenInCents.collectAsState()

    // Función para formatear el precio en centavos a un string con formato 0.00
    fun formatPrice(cents: Long): String {
        return String.format(Locale.US, "%.2f", cents / 100.00)
    }

    val shoppingAmountGivenTextFieldValue = TextFieldValue(
        text = formatPrice(shoppingAmountGivenInCents),
        selection = TextRange(formatPrice(shoppingAmountGivenInCents).length)
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        if (isLoading) {

            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        } else if (products.isEmpty()) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Text(
                        "No hay productos en el carrito, por favor escanee un producto.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        } else {

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    // Tarjeta de resumen del ticket
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {

                            Text(
                                text = "Resumen del ticket",
                                fontStyle = FontStyle.Italic,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            Row {
                                Text(
                                    text = "Productos totales: ",
                                    fontStyle = FontStyle.Italic,
                                    modifier = Modifier
                                        .alignByBaseline()
                                        .alpha(0.8f) // Aplica opacidad del 80%
                                )
                                Text(
                                    text = "$totalProducts",
                                    modifier = Modifier.alignByBaseline()
                                )
                            }
                            Row {
                                Text(
                                    text = "Total de la compra: ",
                                    fontStyle = FontStyle.Italic,
                                    modifier = Modifier
                                        .alignByBaseline()
                                        .alpha(0.8f) // Aplica opacidad del 80%
                                )
                                Text(
                                    text = "$${"%.2f".format(totalPurchase)}",
                                    modifier = Modifier.alignByBaseline()
                                )
                            }
                            Row {
                                Text(
                                    text = "Cambio de la compra: ",
                                    fontStyle = FontStyle.Italic,
                                    modifier = Modifier
                                        .alignByBaseline()
                                        .alpha(0.8f) // Aplica opacidad del 80%
                                )
                                Text(
                                    text = "$${"%.2f".format(change)}",
                                    modifier = Modifier.alignByBaseline()
                                )
                            }
                            /*Text(
                                text = "Productos totales: $totalProducts"
                            )
                            Text(
                                text = "Total de la compra: $${"%.2f".format(totalPurchase)}"
                            )
                            Text(
                                text = "Cambio de la compra: $${"%.2f".format(change)}"
                            )*/
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = shoppingAmountGivenTextFieldValue,
                                onValueChange = { newValue ->
                                    viewModel.onAmountGivenChange(newValue.text)
                                },
                                label = { Text("¿Cantidad de pago recibida?") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                singleLine = true,
                                isError = shoppingAmountGivenInCents == 0L,
                                supportingText = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Text("Máximo: $99,999.99")
                                    }
                                }
                            )
                        }
                    }
                }

                item {
                    HorizontalDivider(
                        //modifier = Modifier.padding(vertical = 16.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                items(products) { product ->
                    ProductTicket(
                        product = product,
                        onIncreaseQuantity = { viewModel.increaseCartProductQuantity(product.barcode) },
                        onDecreaseQuantity = { viewModel.decreaseCartProductQuantity(product.barcode) },
                        onDelete = { viewModel.deleteCartProduct(product.barcode) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }
    }

    // Diálogo para ingresar correo electrónico
    if (showEmailDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showEmailDialog.value = false },
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
                            val amountReceivedValue = shoppingAmountGivenInCents / 100.0
                            viewModel.confirmTicket(
                                amountReceived = amountReceivedValue,
                                context = context,
                                email = email,
                                onSuccess = { successMessage ->
                                    viewModel.showEmailDialog.value = false
                                    viewModel.onAmountGivenChange("0")
                                    Toast.makeText(context, successMessage, Toast.LENGTH_SHORT)
                                        .show()
                                },
                                onError = { errorMessage ->
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                }
                            )
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
                Button(onClick = { viewModel.showEmailDialog.value = false }) {
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

                    Row {
                        Text(
                            text = "Producto: ",
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier
                                .alignByBaseline()
                                .alpha(0.8f) // Aplica opacidad del 80%
                        )
                        Text(
                            text = product.name,
                            modifier = Modifier.alignByBaseline()
                        )
                    }
                    Row {
                        Text(
                            text = "Cantidad: ",
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier
                                .alignByBaseline()
                                .alpha(0.8f) // Aplica opacidad del 80%
                        )
                        Text(
                            text = "${product.quantity}",
                            modifier = Modifier.alignByBaseline()
                        )
                    }
                    Row {
                        Text(
                            text = "Precio Unitario: ",
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier
                                .alignByBaseline()
                                .alpha(0.8f) // Aplica opacidad del 80%
                        )
                        Text(
                            //text = "$${product.salePrice}",
                            text = "$${"%.2f".format(product.salePrice)}",
                            modifier = Modifier.alignByBaseline()
                        )
                    }
                    Row {
                        Text(
                            text = "SubTotal: ",
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier
                                .alignByBaseline()
                                .alpha(0.8f) // Aplica opacidad del 80%
                        )
                        Text(
                            //text = "$${subTotal}",
                            text = "$${"%.2f".format(subTotal)}",
                            modifier = Modifier.alignByBaseline()
                        )
                    }
                    //Text("Producto: ${product.name}")
                    /*Text("Cantidad: ${product.quantity}")
                    Text("Precio Unitario: $${product.salePrice}")
                    Text("SubTotal: $${subTotal}")*/
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