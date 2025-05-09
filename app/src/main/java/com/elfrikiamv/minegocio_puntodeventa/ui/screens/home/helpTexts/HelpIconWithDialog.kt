package com.elfrikiamv.minegocio_puntodeventa.ui.screens.home.helpTexts

// HelpIconWithDialog.kt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.elfrikiamv.minegocio_puntodeventa.R

@Composable
fun HelpIconWithDialog(
    title: String,
    helpText: AnnotatedString,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    var showDialog by remember { mutableStateOf(false) }

    IconButton(
        onClick = { showDialog = true },
        modifier = modifier
            .size(24.dp)
            .alpha(0.8f)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = "help icon"
            )
        } else {
            Icon(
                painter = painterResource(id = R.drawable.baseline_help_outline_24),
                contentDescription = "help icon"
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Entendido")
                }
            },
            title = {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_info_outline_24),
                        contentDescription = "info icon",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            text = {
                Text(
                    text = helpText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        )
    }
}