package com.elfrikiamv.minegocio_puntodeventa.ui.screens.dialogues.helpDialogues

// HelpTexts.kt

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

object HelpTexts {

    val salesHelp: AnnotatedString
        get() = buildAnnotatedString {
            append("Aquí se muestra el total de dinero generado por todas las ventas realizadas.\n\n")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF00C853))) {
                append("Fórmula:\n")
            }
            append("Total vendido = suma de todos los ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF2962FF))) {
                append("precios de venta")
            }
            append(" de los productos registrados como vendidos.")
        }

    val earningsHelp: AnnotatedString
        get() = buildAnnotatedString {
            append("Representa el beneficio real obtenido por la diferencia entre el precio de venta y el precio de compra (proveedor) de cada producto vendido.\n\n")

            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF00C853))) {
                append("Fórmula:\n")
            }

            append("Ganancia neta = ∑ (")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF2962FF))) {
                append("precio de venta")
            }
            append(" - ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFFD50000))) {
                append("precio proveedor")
            }
            append(") × ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF2962FF))) {
                append("cantidad vendida")
            }
            append(")\n\n")

            append("Se calcula sumando la ganancia individual de cada producto vendido.")
        }


    val totalTransactionsHelp: AnnotatedString
        get() = buildAnnotatedString {
            append("Cantidad total de transacciones realizadas en el periodo.\n\n")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF00C853))) {
                append("Fórmula:\n")
            }
            append("Transacciones totales = cantidad de ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF2962FF))) {
                append("ventas registradas")
            }
            append(" en el periodo.")
        }


    val summaryInventoryHelp: AnnotatedString
        get() = buildAnnotatedString {
            append("Muestra un resumen rápido del inventario actual:\n\n")

            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF00C853))) {
                append("Incluye:\n")
            }

            append("- ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF2962FF))) {
                append("Cantidad total de productos")
            }
            append(": suma de todas las existencias de los productos en inventario.\n")

            append("- ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF2962FF))) {
                append("Valor total de productos")
            }
            append(": suma de los precios de venta de cada producto multiplicado por su stock actual.\n\n")

            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF00C853))) {
                append("Fórmula:\n")
            }

            append("Valor total = ∑ (")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF2962FF))) {
                append("precio de venta")
            }
            append(" × ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF2962FF))) {
                append("stock actual")
            }
            append(") por producto.")
        }

}
