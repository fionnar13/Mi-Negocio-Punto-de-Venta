package com.elfrikiamv.minegocio_puntodeventa.ui.screens.customers

// CustomersScreen.kt — صفحهٔ مشتریان (فهرست + گردش حساب + افزودن/ویرایش/حذف)

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
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
import com.elfrikiamv.minegocio_puntodeventa.data.entity.CustomerEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.LedgerEntryEntity
import com.elfrikiamv.minegocio_puntodeventa.utils.PersianFormat
import androidx.compose.ui.res.stringResource

/**
 * صفحهٔ مشتریان: نام، نوع (دفتری قرمز)، مانده حساب؛
 * اقدامات هر ردیف: گردش حساب، ویرایش، حذف.
 */
@Composable
fun CustomersScreen(
    viewModel: CustomersViewModel = viewModel(factory = CustomersViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsState()
    val ledger by viewModel.ledger.collectAsState()

    var editing by remember { mutableStateOf<CustomerEntity?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<CustomerEntity?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.customers.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_users),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(stringResource(R.string.str_075), style = MaterialTheme.typography.titleMedium)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.customers, key = { it.id }) { customer ->
                    CustomerRow(
                        customer = customer,
                        balance = state.balances[customer.id] ?: 0.0,
                        onLedger = { viewModel.loadLedger(customer) },
                        onEdit = { editing = customer; showDialog = true },
                        onDelete = { deleting = customer }
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
            text = { Text(stringResource(R.string.str_076)) },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        )
    }

    if (showDialog) {
        CustomerDialog(
            customer = editing ?: CustomerEntity(),
            onDismiss = { showDialog = false },
            onSave = { viewModel.saveCustomer(it); showDialog = false }
        )
    }

    deleting?.let { customer ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text(stringResource(R.string.str_077)) },
            text = { Text(stringResource(R.string.str_055, customer.name)) },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteCustomer(customer); deleting = null }) {
                    Text(stringResource(R.string.str_006), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text(stringResource(R.string.str_014)) } }
        )
    }

    ledger?.let { detail ->
        LedgerHistoryDialog(
            title = stringResource(R.string.str_078, detail.customer.name),
            rows = detail.rows,
            onDismiss = viewModel::closeLedger
        )
    }
}

/** ردیف مشتری. */
@Composable
private fun CustomerRow(
    customer: CustomerEntity,
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = customer.name,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    // نوع مشتری: دفتری قرمز نمایش داده می‌شود
                    Text(
                        text = customer.kind,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (customer.kind == CustomerEntity.KIND_CREDIT) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                if (customer.phone.isNotBlank()) {
                    Text(
                        text = PersianFormat.displayPhone(customer.phone),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.str_079, PersianFormat.amount(balance)),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (balance > 0.009) MaterialTheme.colorScheme.primary
                    else if (balance < -0.009) MaterialTheme.colorScheme.error
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

/** دیالوگ افزودن/ویرایش مشتری. */
@Composable
private fun CustomerDialog(
    customer: CustomerEntity,
    onDismiss: () -> Unit,
    onSave: (CustomerEntity) -> Unit
) {
    var name by remember { mutableStateOf(customer.name) }
    var phone by remember { mutableStateOf(customer.phone) }
    var kind by remember { mutableStateOf(customer.kind) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (customer.id == 0L) stringResource(R.string.str_076) else stringResource(R.string.str_081)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.str_082)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(stringResource(R.string.str_083)) },
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = kind == CustomerEntity.KIND_CASH,
                        onClick = { kind = CustomerEntity.KIND_CASH },
                        label = { Text(CustomerEntity.KIND_CASH) }
                    )
                    FilterChip(
                        selected = kind == CustomerEntity.KIND_CREDIT,
                        onClick = { kind = CustomerEntity.KIND_CREDIT },
                        label = { Text(CustomerEntity.KIND_CREDIT) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(enabled = name.isNotBlank(), onClick = {
                onSave(customer.copy(name = name.trim(), phone = phone.trim(), kind = kind))
            }) { Text(stringResource(R.string.str_066)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.str_014)) } }
    )
}

/** برچسب فارسی نوع سند دفتر کل. */
fun ledgerTypeLabel(t: String): String = when (t) {
    LedgerEntryEntity.TYPE_SALE -> "فروش"
    LedgerEntryEntity.TYPE_PURCHASE -> "خرید"
    LedgerEntryEntity.TYPE_RECEIPT -> "دریافت"
    LedgerEntryEntity.TYPE_PAY_SUP -> "پرداخت"
    LedgerEntryEntity.TYPE_EXPENSE -> "هزینه"
    LedgerEntryEntity.TYPE_INCOME -> "درآمد"
    else -> t
}

/**
 * دیالوگ گردش حساب: اسناد به ترتیب تاریخ نزولی با ماندهٔ تجمعی.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerHistoryDialog(
    title: String,
    rows: List<LedgerRow>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                // سرجدول
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.str_029), style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1.1f))
                    Text(stringResource(R.string.str_050), style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1.5f))
                    Text(stringResource(R.string.str_032), style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f))
                    Text(stringResource(R.string.str_033), style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f))
                    Text(stringResource(R.string.str_084), style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1.1f))
                }
                LazyColumn(
                    modifier = Modifier.height(360.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(rows) { row ->
                        val e = row.entry
                        Row(
                            modifier = Modifier.padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                PersianFormat.displayDate(e.date),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1.1f)
                            )
                            Text(
                                e.note.ifBlank { ledgerTypeLabel(e.t) },
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1.5f)
                            )
                            Text(
                                if (e.debit > 0.0) PersianFormat.amount(e.debit) else "—",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                if (e.credit > 0.0) PersianFormat.amount(e.credit) else "—",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                PersianFormat.amount(row.runningBalance),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (row.runningBalance >= 0) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                },
                                modifier = Modifier.weight(1.1f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.str_074)) }
        }
    )
}
