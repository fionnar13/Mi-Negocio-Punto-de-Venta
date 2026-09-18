package com.elfrikiamv.minegocio_puntodeventa.ui.screens.suppliers

// SuppliersScreen.kt — صفحهٔ تامین‌کنندگان (نام + مانده + گردش حساب)

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
import com.elfrikiamv.minegocio_puntodeventa.data.entity.SupplierEntity
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.customers.LedgerHistoryDialog
import com.elfrikiamv.minegocio_puntodeventa.utils.PersianFormat
import androidx.compose.ui.res.stringResource

/**
 * صفحهٔ تامین‌کنندگان: نام و مانده حساب؛
 * اقدامات: گردش حساب، ویرایش، حذف.
 */
@Composable
fun SuppliersScreen(
    viewModel: SuppliersViewModel = viewModel(factory = SuppliersViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsState()
    val ledger by viewModel.ledger.collectAsState()

    var editing by remember { mutableStateOf<SupplierEntity?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<SupplierEntity?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.suppliers.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_factory),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(stringResource(R.string.str_254), style = MaterialTheme.typography.titleMedium)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.suppliers, key = { it.id }) { supplier ->
                    SupplierRow(
                        supplier = supplier,
                        balance = state.balances[supplier.id] ?: 0.0,
                        onLedger = { viewModel.loadLedger(supplier) },
                        onEdit = { editing = supplier; showDialog = true },
                        onDelete = { deleting = supplier }
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
            text = { Text(stringResource(R.string.str_132)) },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        )
    }

    if (showDialog) {
        SupplierDialog(
            supplier = editing ?: SupplierEntity(),
            onDismiss = { showDialog = false },
            onSave = { viewModel.saveSupplier(it); showDialog = false }
        )
    }

    deleting?.let { supplier ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text(stringResource(R.string.str_255)) },
            text = { Text(stringResource(R.string.str_055, supplier.name)) },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteSupplier(supplier); deleting = null }) {
                    Text(stringResource(R.string.str_006), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text(stringResource(R.string.str_014)) } }
        )
    }

    ledger?.let { detail ->
        LedgerHistoryDialog(
            title = stringResource(R.string.str_078, detail.supplier.name),
            rows = detail.rows,
            onDismiss = viewModel::closeLedger
        )
    }
}

/** ردیف تامین‌کننده. */
@Composable
private fun SupplierRow(
    supplier: SupplierEntity,
    balance: Double,
    onLedger: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = supplier.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (supplier.phone.isNotBlank()) {
                    Text(
                        text = PersianFormat.displayPhone(supplier.phone),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.str_079, PersianFormat.amount(balance)),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (balance > 0.009) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onLedger) {
                Icon(
                    painter = painterResource(R.drawable.baseline_format_list_bulleted_24),
                    contentDescription = stringResource(R.string.str_080),
                    tint = MaterialTheme.colorScheme.primary
                )
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

/** دیالوگ افزودن/ویرایش تامین‌کننده. */
@Composable
private fun SupplierDialog(
    supplier: SupplierEntity,
    onDismiss: () -> Unit,
    onSave: (SupplierEntity) -> Unit
) {
    var name by remember { mutableStateOf(supplier.name) }
    var phone by remember { mutableStateOf(supplier.phone) }
    var card by remember { mutableStateOf(supplier.card) }
    var account by remember { mutableStateOf(supplier.account) }
    var address by remember { mutableStateOf(supplier.address) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (supplier.id == 0L) stringResource(R.string.str_132) else stringResource(R.string.str_256)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text(stringResource(R.string.str_082)) }, singleLine = true
                )
                OutlinedTextField(
                    value = phone, onValueChange = { phone = it },
                    label = { Text(stringResource(R.string.str_083)) }, singleLine = true
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
                    value = address, onValueChange = { address = it },
                    label = { Text(stringResource(R.string.str_176)) }
                )
            }
        },
        confirmButton = {
            TextButton(enabled = name.isNotBlank(), onClick = {
                onSave(
                    supplier.copy(
                        name = name.trim(), phone = phone.trim(),
                        card = card.trim(), account = account.trim(),
                        address = address.trim()
                    )
                )
            }) { Text(stringResource(R.string.str_066)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.str_014)) } }
    )
}
