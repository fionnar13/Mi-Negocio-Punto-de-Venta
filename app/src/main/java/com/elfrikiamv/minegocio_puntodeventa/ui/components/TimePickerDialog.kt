package com.elfrikiamv.minegocio_puntodeventa.ui.components

// TimePickerDialog.kt — انتخابگر ساعت (Material 3)

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.elfrikiamv.minegocio_puntodeventa.R

/**
 * دیالوگ انتخاب ساعت ۲۴ ساعته.
 *
 * @param onSelected با تأیید فراخوانی می‌شود: (ساعت، دقیقه).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onSelected: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.str_015)) },
        text = { TimePicker(state = state) },
        confirmButton = {
            TextButton(onClick = { onSelected(state.hour, state.minute) }) {
                Text(stringResource(R.string.str_016))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.str_014)) }
        }
    )
}
