package com.elfrikiamv.minegocio_puntodeventa.ui.screens.accounts

// AccountsScreen.kt — صفحهٔ حساب‌ها (دفتر کل + تراکنش‌های دستی)

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elfrikiamv.minegocio_puntodeventa.R
import com.elfrikiamv.minegocio_puntodeventa.data.entity.CustomerEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.LedgerEntryEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.SupplierEntity
import com.elfrikiamv.minegocio_puntodeventa.ui.components.JalaliDatePickerDialog
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.customers.ledgerTypeLabel
import com.elfrikiamv.minegocio_puntodeventa.utils.PersianFormat
import androidx.compose.ui.res.stringResource

/**
 * صفحهٔ حساب‌ها: کارت‌های خلاصه (صندوق/بانک‌ها/مطالبات/بدهی تامین)،
 * فیلترها (نوع/متن/بازهٔ تاریخ)، دکمه‌های تراکنش و جدول اسناد.
 */
@Composable
fun AccountsScreen(
    viewModel: AccountsViewModel = viewModel(factory = AccountsViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    var txType by remember { mutableStateOf<String?>(null) }
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.onMessageShown()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ---- کارت‌های خلاصه ----
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryCard(
                    title = stringResource(R.string.str_017),
                    value = state.totals.cash,
                    iconRes = R.drawable.baseline_payments_24,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = stringResource(R.string.str_018),
                    value = state.totals.bank,
                    iconRes = R.drawable.ic_landmark,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryCard(
                    title = stringResource(R.string.str_019),
                    value = state.totals.receivables,
                    iconRes = R.drawable.ic_users,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = stringResource(R.string.str_020),
                    value = state.totals.payables,
                    iconRes = R.drawable.ic_factory,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ---- فیلترها ----
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TypeFilterDropdown(
                    selected = state.typeFilter,
                    onSelect = viewModel::onTypeFilterChange,
                    modifier = Modifier.weight(1.2f)
                )
                OutlinedTextField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    singleLine = true,
                    label = { Text(stringResource(R.string.str_021)) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.baseline_search_24),
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.weight(1.8f)
                )
            }
        }
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DateField(
                    label = stringResource(R.string.str_022),
                    date = state.from,
                    onClick = { showFromPicker = true },
                    modifier = Modifier.weight(1f)
                )
                DateField(
                    label = stringResource(R.string.str_023),
                    date = state.to,
                    onClick = { showToPicker = true },
                    modifier = Modifier.weight(1f)
                )
                if (state.from.isNotBlank() || state.to.isNotBlank()) {
                    TextButton(onClick = viewModel::clearDateRange) { Text(stringResource(R.string.str_024)) }
                }
            }
        }

        // ---- دکمه‌های تراکنش ----
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionButton(stringResource(R.string.str_025), Modifier.weight(1f)) {
                    txType = LedgerEntryEntity.TYPE_RECEIPT
                }
                ActionButton(stringResource(R.string.str_026), Modifier.weight(1f)) {
                    txType = LedgerEntryEntity.TYPE_PAY_SUP
                }
                ActionButton(stringResource(R.string.str_027), Modifier.weight(1f)) {
                    txType = LedgerEntryEntity.TYPE_EXPENSE
                }
                ActionButton(stringResource(R.string.str_028), Modifier.weight(1f)) {
                    txType = LedgerEntryEntity.TYPE_INCOME
                }
            }
        }

        // ---- جدول اسناد ----
        item {
            Row(
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableHeader(stringResource(R.string.str_029), 1f)
                TableHeader(stringResource(R.string.str_030), 0.9f)
                TableHeader(stringResource(R.string.str_031), 2.2f)
                TableHeader(stringResource(R.string.str_032), 1.1f)
                TableHeader(stringResource(R.string.str_033), 1.1f)
            }
        }

        if (state.entries.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.str_034),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            items(state.entries, key = { it.id }) { entry ->
                LedgerEntryRow(entry, state.customers, state.suppliers)
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // ---- دیالوگ‌ها ----

    txType?.let { type ->
        TransactionDialog(
            type = type,
            customers = state.customers,
            suppliers = state.suppliers,
            onDismiss = { txType = null },
            onConfirm = { date, amount, partyId, isBank, note ->
                viewModel.addTransaction(type, date, amount, partyId, isBank, note)
                txType = null
            }
        )
    }

    if (showFromPicker) {
        JalaliDatePickerDialog(
            initialYear = 1405, initialMonth = 1, initialDay = 1,
            onDateSelected = { y, m, d ->
                viewModel.onFromDateChange(PersianFormat.jalaliString(y, m, d))
                showFromPicker = false
            },
            onDismiss = { showFromPicker = false }
        )
    }

    if (showToPicker) {
        JalaliDatePickerDialog(
            initialYear = 1405, initialMonth = 1, initialDay = 1,
            onDateSelected = { y, m, d ->
                viewModel.onToDateChange(PersianFormat.jalaliString(y, m, d))
                showToPicker = false
            },
            onDismiss = { showToPicker = false }
        )
    }
}

// ---- اجزای صفحهٔ حساب‌ها ----

/** کارت خلاصهٔ حساب. */
@Composable
private fun SummaryCard(
    title: String,
    value: Double,
    iconRes: Int,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.size(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.str_035, PersianFormat.amount(value)),
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

/** فیلتر نوع سند. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypeFilterDropdown(
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(
        "" to stringResource(R.string.str_036),
        LedgerEntryEntity.TYPE_SALE to ledgerTypeLabel(LedgerEntryEntity.TYPE_SALE),
        LedgerEntryEntity.TYPE_PURCHASE to ledgerTypeLabel(LedgerEntryEntity.TYPE_PURCHASE),
        LedgerEntryEntity.TYPE_RECEIPT to ledgerTypeLabel(LedgerEntryEntity.TYPE_RECEIPT),
        LedgerEntryEntity.TYPE_PAY_SUP to ledgerTypeLabel(LedgerEntryEntity.TYPE_PAY_SUP),
        LedgerEntryEntity.TYPE_EXPENSE to ledgerTypeLabel(LedgerEntryEntity.TYPE_EXPENSE),
        LedgerEntryEntity.TYPE_INCOME to ledgerTypeLabel(LedgerEntryEntity.TYPE_INCOME)
    )
    val label = options.find { it.first == selected }?.second ?: stringResource(R.string.str_036)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.str_037)) },
            singleLine = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (value, text) ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = { onSelect(value); expanded = false }
                )
            }
        }
    }
}

/** دکمهٔ تراکنش. */
@Composable
private fun ActionButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = modifier) {
        Text(text = label, maxLines = 1)
    }
}

/** فیلد تاریخ فیلتر (کلیک → دیالوگ). */
@Composable
private fun DateField(
    label: String,
    date: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        OutlinedTextField(
            value = if (date.isBlank()) "" else PersianFormat.displayDate(date),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text("—") },
            singleLine = true,
            trailingIcon = {
                Icon(painter = painterResource(R.drawable.ic_calendar), contentDescription = null)
            },
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(onClick = onClick)
        )
    }
}

@Composable
private fun TableHeader(label: String, weight: Float) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.weight(weight)
    )
}

/** ردیف سند دفتر کل. */
@Composable
private fun LedgerEntryRow(
    entry: LedgerEntryEntity,
    customers: List<CustomerEntity>,
    suppliers: List<SupplierEntity>
) {
    val party = when {
        entry.customerId != null ->
            customers.find { it.id == entry.customerId }?.name ?: stringResource(R.string.str_038)
        entry.supplierId != null ->
            suppliers.find { it.id == entry.supplierId }?.name ?: stringResource(R.string.str_039)
        else -> null
    }

    Row(
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = PersianFormat.displayDate(entry.date),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = ledgerTypeLabel(entry.t),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(0.9f)
        )
        Column(modifier = Modifier.weight(2.2f)) {
            Text(
                text = party ?: entry.note.ifBlank { "—" },
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (party != null && entry.note.isNotBlank()) {
                Text(
                    text = entry.note,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Text(
            text = if (entry.debit > 0.0) PersianFormat.amount(entry.debit) else "—",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1.1f)
        )
        Text(
            text = if (entry.credit > 0.0) PersianFormat.amount(entry.credit) else "—",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1.1f)
        )
    }
}

/** دیالوگ ثبت تراکنش (دریافت/پرداخت/هزینه/درآمد). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionDialog(
    type: String,
    customers: List<CustomerEntity>,
    suppliers: List<SupplierEntity>,
    onDismiss: () -> Unit,
    onConfirm: (date: String, amount: String, partyId: Long?, isBank: Boolean, note: String) -> Unit
) {
    var date by remember { mutableStateOf(PersianFormat.todayDateString()) }
    var amount by remember { mutableStateOf("") }
    var partyId by remember { mutableStateOf<Long?>(null) }
    var isBank by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    val titleLabel = when (type) {
        LedgerEntryEntity.TYPE_RECEIPT -> stringResource(R.string.str_040)
        LedgerEntryEntity.TYPE_PAY_SUP -> stringResource(R.string.str_041)
        LedgerEntryEntity.TYPE_EXPENSE -> stringResource(R.string.str_042)
        else -> stringResource(R.string.str_043)
    }
    val needsParty = type == LedgerEntryEntity.TYPE_RECEIPT || type == LedgerEntryEntity.TYPE_PAY_SUP

    val isValid = amount.isNotBlank() &&
        (if (needsParty) partyId != null else true) &&
        (if (!needsParty) note.isNotBlank() else true)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titleLabel) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // تاریخ
                DateField(
                    label = stringResource(R.string.str_029),
                    date = date,
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                )

                // طرف حساب (مشتری/تامین‌کننده)
                if (needsParty) {
                    var expanded by remember { mutableStateOf(false) }
                    val list = if (type == LedgerEntryEntity.TYPE_RECEIPT) {
                        customers.map { it.id to it.name }
                    } else {
                        suppliers.map { it.id to it.name }
                    }
                    val selectedName = list.find { it.first == partyId }?.second ?: ""
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedName,
                            onValueChange = {},
                            readOnly = true,
                            label = {
                                Text(
                                    if (type == LedgerEntryEntity.TYPE_RECEIPT) stringResource(R.string.str_044)
                                    else stringResource(R.string.str_045)
                                )
                            },
                            singleLine = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            list.forEach { (id, name) ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = { partyId = id; expanded = false }
                                )
                            }
                        }
                    }
                }

                // مبلغ
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(stringResource(R.string.str_046)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                // نقد / بانک
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !isBank,
                        onClick = { isBank = false },
                        label = { Text(stringResource(R.string.str_047)) }
                    )
                    FilterChip(
                        selected = isBank,
                        onClick = { isBank = true },
                        label = { Text(stringResource(R.string.str_048)) }
                    )
                }

                // شرح
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(if (needsParty) stringResource(R.string.str_049) else stringResource(R.string.str_050)) },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(enabled = isValid, onClick = {
                onConfirm(date, amount, partyId, isBank, note)
            }) { Text(stringResource(R.string.str_051)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.str_014)) } }
    )

    if (showDatePicker) {
        val parts = date.split("/").map { it.toIntOrNull() ?: 0 }
        JalaliDatePickerDialog(
            initialYear = parts.getOrElse(0) { 1405 },
            initialMonth = parts.getOrElse(1) { 1 },
            initialDay = parts.getOrElse(2) { 1 },
            onDateSelected = { y, m, d ->
                date = PersianFormat.jalaliString(y, m, d)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}
