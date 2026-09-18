package com.elfrikiamv.minegocio_puntodeventa.ui.screens.banks

// BanksScreen.kt — صفحهٔ بانک‌ها (CRUD ساده)

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elfrikiamv.minegocio_puntodeventa.R
import com.elfrikiamv.minegocio_puntodeventa.data.entity.BankEntity
import com.elfrikiamv.minegocio_puntodeventa.utils.PersianFormat
import androidx.compose.ui.res.stringResource

/**
 * صفحهٔ بانک‌ها: نام بانک، شماره کارت، حساب و شبا.
 */
@Composable
fun BanksScreen(
    viewModel: BanksViewModel = viewModel(factory = BanksViewModel.Factory)
) {
    val banks by viewModel.banks.collectAsState()

    var editing by remember { mutableStateOf<BankEntity?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<BankEntity?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (banks.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_landmark),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(stringResource(R.string.str_052), style = MaterialTheme.typography.titleMedium)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(banks, key = { it.id }) { bank ->
                    BankRow(
                        bank = bank,
                        onEdit = { editing = bank; showDialog = true },
                        onDelete = { deleting = bank }
                    )
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { editing = null; showDialog = true },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.baseline_add_24),
                    contentDescription = null
                )
            },
            text = { Text(stringResource(R.string.str_053)) },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        )
    }

    if (showDialog) {
        BankDialog(
            bank = editing ?: BankEntity(),
            onDismiss = { showDialog = false },
            onSave = { viewModel.saveBank(it); showDialog = false }
        )
    }

    deleting?.let { bank ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text(stringResource(R.string.str_054)) },
            text = { Text(stringResource(R.string.str_055, bank.bank)) },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteBank(bank); deleting = null }) {
                    Text(stringResource(R.string.str_006), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text(stringResource(R.string.str_014)) } }
        )
    }
}

/** ردیف بانک. */
@Composable
private fun BankRow(bank: BankEntity, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bank.bank.ifBlank { stringResource(R.string.str_056) },
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val details = listOf(
                    stringResource(R.string.str_057) to bank.card,
                    stringResource(R.string.str_058) to bank.account,
                    stringResource(R.string.str_059) to bank.sheba
                ).filter { it.second.isNotBlank() }
                if (details.isNotEmpty()) {
                    Text(
                        text = details.joinToString(" · ") {
                            "${it.first}: ${PersianFormat.displayPhone(it.second)}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(onClick = onEdit) {
                Icon(
                    painter = painterResource(R.drawable.baseline_edit_24),
                    contentDescription = stringResource(R.string.str_060),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    painter = painterResource(R.drawable.baseline_delete_24),
                    contentDescription = stringResource(R.string.str_006),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/** دیالوگ افزودن/ویرایش بانک. */
@Composable
private fun BankDialog(
    bank: BankEntity,
    onDismiss: () -> Unit,
    onSave: (BankEntity) -> Unit
) {
    var bankName by remember { mutableStateOf(bank.bank) }
    var card by remember { mutableStateOf(bank.card) }
    var account by remember { mutableStateOf(bank.account) }
    var sheba by remember { mutableStateOf(bank.sheba) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (bank.id == 0L) stringResource(R.string.str_053) else stringResource(R.string.str_061)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = bankName, onValueChange = { bankName = it },
                    label = { Text(stringResource(R.string.str_062)) }, singleLine = true
                )
                OutlinedTextField(
                    value = card, onValueChange = { card = it },
                    label = { Text(stringResource(R.string.str_063)) }, singleLine = true
                )
                OutlinedTextField(
                    value = account, onValueChange = { account = it },
                    label = { Text(stringResource(R.string.str_064)) }, singleLine = true
                )
                OutlinedTextField(
                    value = sheba, onValueChange = { sheba = it },
                    label = { Text(stringResource(R.string.str_065)) }, singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(enabled = bankName.isNotBlank(), onClick = {
                onSave(
                    bank.copy(
                        bank = bankName.trim(),
                        card = card.trim(),
                        account = account.trim(),
                        sheba = sheba.trim()
                    )
                )
            }) { Text(stringResource(R.string.str_066)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.str_014)) } }
    )
}
