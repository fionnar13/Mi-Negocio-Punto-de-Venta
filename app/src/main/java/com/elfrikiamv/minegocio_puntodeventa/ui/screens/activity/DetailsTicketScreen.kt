package com.elfrikiamv.minegocio_puntodeventa.ui.screens.activity

// DetailsTicketScreen.kt

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.elfrikiamv.minegocio_puntodeventa.R
import com.elfrikiamv.minegocio_puntodeventa.model.TicketFirebase
import com.elfrikiamv.minegocio_puntodeventa.navigation.Screen
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.ActivityViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsTicketScreen(navController: NavController, ticketId: String?) {
    val viewModel: ActivityViewModel = viewModel()
    var ticketDetails by remember { mutableStateOf<TicketFirebase?>(null) }

    // Estado para carga de datos
    var isLoading by remember { mutableStateOf(true) }

    // Cargar los detalles del ticket
    LaunchedEffect(ticketId) {
        ticketId?.let {
            viewModel.checkTicketExists(it) { fetchedTicket ->
                ticketDetails = fetchedTicket
                isLoading = false
            }
        } ?: run { isLoading = false } // Si no hay ID, terminamos la carga
    }

    Scaffold(
        topBar = {
            TopAppBar(
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
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (isLoading) {
                    // Indicador de carga
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else if (ticketDetails == null) {
                    // Mensaje de error si no hay datos
                    Text(
                        "No se encontraron detalles del ticket.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    // Mostrar detalles del ticket
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        item {
                            TicketDetailsCard(ticketDetails!!)
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun TicketDetailsCard(ticketDetails: TicketFirebase) {
    val totalPrice = String.format(Locale.getDefault(), "%.2f", ticketDetails.totalPrice)
    val amountReceived = String.format(Locale.getDefault(), "%.2f", ticketDetails.amountReceived)
    val change = String.format(Locale.getDefault(), "%.2f", ticketDetails.change)

    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Encabezado
            Text(
                text = "Ticket ID: ${ticketDetails.ticketId}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Fecha: ${ticketDetails.date}"
            )
            Text(
                text = "Hora: ${ticketDetails.time}"
            )

            HorizontalDivider(
                modifier = Modifier.padding(bottom = 8.dp, top = 8.dp),
                thickness = 1.dp
            )

            // Lista de productos
            Text(
                text = "Productos:"
            )
            ticketDetails.products.forEachIndexed { index, product ->
                ProductDetails(product, index)
            }

            HorizontalDivider(
                modifier = Modifier.padding(bottom = 8.dp, top = 8.dp),
                thickness = 1.dp
            )

            // Totales
            Text(
                text = "Total de productos: ${ticketDetails.totalProducts}"
            )
            Text(
                text = "Precio total: $$totalPrice"
            )

            HorizontalDivider(
                modifier = Modifier.padding(bottom = 8.dp, top = 8.dp),
                thickness = 1.dp
            )

            Text(
                text = "Pago recibido: $$amountReceived"
            )
            Text(
                text = "Cambio: $$change"
            )

            Button(
                onClick = {
                    // Lógica para enviar el ticket
                    showDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text(text = "Enviar ticket")
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Enviar por correo") },
                    text = {
                        Column {
                            Text("Introduce un correo electrónico:")
                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                keyboardActions = KeyboardActions.Default,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            showDialog = false
                            scope.launch {
                                val pdfFile = generatePDF(context, ticketDetails)
                                sendEmail(context, emailInput, pdfFile)
                            }
                        }) {
                            Text("Enviar")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

        }
    }
}

fun generatePDF(context: Context, ticket: TicketFirebase): File {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas

    // Escribir el contenido del ticket en el PDF
    canvas.drawText("Ticket ID: ${ticket.ticketId}", 10f, 50f, Paint().apply { textSize = 12f })
    canvas.drawText("Fecha: ${ticket.date}", 10f, 70f, Paint().apply { textSize = 12f })
    canvas.drawText("Hora: ${ticket.time}", 10f, 90f, Paint().apply { textSize = 12f })

    ticket.products.forEachIndexed { index, product ->
        val yPosition = 110 + (index * 20)
        canvas.drawText(
            "${index + 1}. ${product["name"]} - ${product["quantity"]} x $${product["price"]}",
            10f, yPosition.toFloat(), Paint().apply { textSize = 12f }
        )
    }

    canvas.drawText("Total: $${ticket.totalPrice}", 10f, 200f, Paint().apply { textSize = 14f })
    pdfDocument.finishPage(page)

    // Guardar el archivo en el almacenamiento local
    val file = File(context.cacheDir, "ticket_${ticket.ticketId}.pdf")
    pdfDocument.writeTo(FileOutputStream(file))
    pdfDocument.close()

    return file
}

fun sendEmail(context: Context, email: String, file: File) {
    val fileUri = FileProvider.getUriForFile(
        context,
        "com.elfrikiamv.minegocio_puntodeventa.fileprovider",
        file
    )

    val emailIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        putExtra(Intent.EXTRA_SUBJECT, "Detalles del Ticket")
        putExtra(Intent.EXTRA_TEXT, "Adjunto encontrarás los detalles del ticket.")
        putExtra(Intent.EXTRA_STREAM, fileUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(emailIntent, "Compartir archivo"))
}

@Composable
fun ProductDetails(product: Map<String, Any>, index: Int) {
    val price = (product["price"] as? Number)?.toDouble() ?: 0.0
    val quantity = (product["quantity"] as? Number)?.toInt() ?: 0
    val subTotal = price * quantity
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = "${index + 1}. ${product["name"] ?: "Producto sin nombre"}"
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 17.dp)
        ) {
            Text(
                text = "Código de barras: ${product["barcode"] ?: "N/A"}"
            )
            Text(
                text = "Cantidad: ${product["quantity"] ?: "N/A"}"
            )
            Text(
                text = "Precio: $${String.format(Locale.getDefault(), "%.2f", price)}"
            )
            Text(
                text = "SubTotal: $${String.format(Locale.getDefault(), "%.2f", subTotal)}"
            )
        }
    }
}