package com.elfrikiamv.minegocio_puntodeventa.ui.screens.purchase

// PurchaseScreen.kt — صفحهٔ خرید از تامین‌کننده (قرینهٔ فروش)

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elfrikiamv.minegocio_puntodeventa.R
import com.elfrikiamv.minegocio_puntodeventa.data.entity.ProductEntity
import com.elfrikiamv.minegocio_puntodeventa.scanner.BarcodeScanCallback
import com.elfrikiamv.minegocio_puntodeventa.scanner.BarcodeScanResult
import com.elfrikiamv.minegocio_puntodeventa.scanner.BarcodeScannerCallbackHost
import com.elfrikiamv.minegocio_puntodeventa.scanner.ScannerActivity
import com.elfrikiamv.minegocio_puntodeventa.ui.components.CartTable
import com.elfrikiamv.minegocio_puntodeventa.ui.components.JalaliDatePickerDialog
import com.elfrikiamv.minegocio_puntodeventa.utils.PersianFormat
import androidx.compose.ui.res.stringResource

/**
 * صفحهٔ خرید: انتخاب تامین‌کننده و تاریخ → جستجو/اسکن کالا → سبد →
 * تخفیف و مبلغ پرداخت‌شده و چک‌باکس «بروزرسانی قیمت» → ثبت تراکنشی خرید.
 */
@Composable
fun PurchaseScreen(
    viewModel: PurchaseViewModel = viewModel(factory = PurchaseViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var sharePurchase by remember {
        mutableStateOf<com.elfrikiamv.minegocio_puntodeventa.data.entity.PurchaseEntity?>(null)
    }

    var showSupplierDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    // اسکنر پیوسته
    val scanLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }
    val scanCallback = remember {
        object : BarcodeScanCallback {
            override fun onBarcodeScanned(result: BarcodeScanResult) {
                viewModel.onBarcodeScanned(result.barcode)
            }
        }
    }
    DisposableEffect(Unit) {
        BarcodeScannerCallbackHost.bind(scanCallback)
        onDispose { BarcodeScannerCallbackHost.unbind(scanCallback) }
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.onMessageShown()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ---- تامین‌کننده + افزودن ----
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SupplierDropdown(
                state = state,
                onSupplierChange = viewModel::onSupplierChange,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showSupplierDialog = true }) {
                Icon(
                    painter = painterResource(R.drawable.baseline_add_24),
                    contentDescription = stringResource(R.string.str_132)
                )
            }
        }

        // ---- تاریخ ----
        DateField(
            date = state.date,
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth()
        )

        // ---- جستجو + اسکن ----
        SearchWithResults(
            state = state,
            onQueryChange = viewModel::onQueryChange,
            onPick = viewModel::addFromSearch,
            onScan = {
                scanLauncher.launch(
                    ScannerActivity.createIntent(context, continuous = true)
                )
            }
        )

        // ---- سبد ----
        if (state.cart.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.str_133),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                )
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                CartTable(
                    items = state.cart,
                    onIncrement = viewModel::onIncrement,
                    onDecrement = viewModel::onDecrement,
                    onRemove = viewModel::onRemove,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
            }
        }

        // ---- جمع‌ها + پرداخت ----
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.str_134), modifier = Modifier.weight(1f))
                    Text(
                        text = stringResource(R.string.str_035, PersianFormat.amount(state.total)),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.str_135), modifier = Modifier.weight(1f))
                    OutlinedTextField(
                        value = state.discountInput,
                        onValueChange = viewModel::onDiscountChange,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.width(160.dp)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.str_136), modifier = Modifier.weight(1f))
                    Text(
                        text = stringResource(R.string.str_035, PersianFormat.amount(state.grand)),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.str_137), modifier = Modifier.weight(1f))
                    OutlinedTextField(
                        value = state.paidInput,
                        onValueChange = viewModel::onPaidChange,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.width(160.dp)
                    )
                }
                if (state.debt > 0.009) {
                    Text(
                        text = stringResource(R.string.str_138, PersianFormat.amount(state.debt)),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // چک‌باکس بروزرسانی قیمت خرید
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = state.updatePrices,
                        onCheckedChange = viewModel::onUpdatePricesChange
                    )
                    Text(stringResource(R.string.str_139))
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // ---- ثبت خرید ----
        Button(
            onClick = viewModel::submitPurchase,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(text = stringResource(R.string.str_140), style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ---- خریدهای اخیر (چاپ / اشتراک‌گذاری) ----
        if (state.recent.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(stringResource(R.string.str_141), style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    state.recent.forEach { purchase ->
                        val supplierName =
                            state.suppliers.find { it.id == purchase.supplierId }?.name ?: "—"
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    stringResource(R.string.str_142, com.elfrikiamv.minegocio_puntodeventa.utils.NumberUtils.toPersian(purchase.no)) +
                                        PersianFormat.displayDate(purchase.date),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    stringResource(R.string.str_143, supplierName, PersianFormat.amount(purchase.grand)),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = {
                                com.elfrikiamv.minegocio_puntodeventa.utils.InvoicePrinter.printPurchase(
                                    context = context,
                                    purchase = purchase,
                                    supplierName = supplierName,
                                    settings = state.settings
                                )
                            }) {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(R.drawable.baseline_print_24),
                                    contentDescription = stringResource(R.string.str_144),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { sharePurchase = purchase }) {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(R.drawable.baseline_share_24),
                                    contentDescription = stringResource(R.string.str_145),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // ---- دیالوگ‌ها ----

    // دیالوگ انتخاب مقصد اشتراک‌گذاری فاکتور خرید
    sharePurchase?.let { purchase ->
        val supplierName = state.suppliers.find { it.id == purchase.supplierId }?.name.orEmpty()
        AlertDialog(
            onDismissRequest = { sharePurchase = null },
            title = {
                Text(
                    stringResource(R.string.str_146) +
                        com.elfrikiamv.minegocio_puntodeventa.utils.NumberUtils.toPersian(purchase.no)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    com.elfrikiamv.minegocio_puntodeventa.utils.ShareTarget.entries.forEach { target ->
                        OutlinedButton(
                            onClick = {
                                com.elfrikiamv.minegocio_puntodeventa.utils.ShareUtils.share(
                                    context,
                                    com.elfrikiamv.minegocio_puntodeventa.utils.ReceiptText.purchase(
                                        purchase, supplierName, state.settings
                                    ),
                                    target
                                )
                                sharePurchase = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("${target.icon} ${target.faTitle}") }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { sharePurchase = null }) { Text(stringResource(R.string.str_074)) }
            }
        )
    }

    if (showSupplierDialog) {
        QuickSupplierDialog(
            onDismiss = { showSupplierDialog = false },
            onSave = { name, phone ->
                viewModel.addSupplier(name, phone)
                showSupplierDialog = false
            }
        )
    }

    if (showDatePicker) {
        val parts = state.date.split("/").map { it.toIntOrNull() ?: 0 }
        JalaliDatePickerDialog(
            initialYear = parts.getOrElse(0) { 1405 },
            initialMonth = parts.getOrElse(1) { 1 },
            initialDay = parts.getOrElse(2) { 1 },
            onDateSelected = { y, m, d ->
                viewModel.onDateChange(PersianFormat.jalaliString(y, m, d))
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

// ---- اجزای صفحهٔ خرید ----

/** دراپ‌داون تامین‌کننده. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SupplierDropdown(
    state: PurchaseUiState,
    onSupplierChange: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = state.suppliers
                .find { it.id == state.selectedSupplierId }?.name
                ?: stringResource(R.string.str_147),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.str_045)) },
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
            DropdownMenuItem(
                text = { Text(stringResource(R.string.str_147)) },
                onClick = { onSupplierChange(null); expanded = false }
            )
            state.suppliers.forEach { supplier ->
                DropdownMenuItem(
                    text = { Text(supplier.name) },
                    onClick = { onSupplierChange(supplier.id); expanded = false }
                )
            }
        }
    }
}

/** فیلد تاریخ شمسی (کلیک → دیالوگ). */
@Composable
private fun DateField(date: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        OutlinedTextField(
            value = PersianFormat.displayDate(date),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.str_029)) },
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

/** جستجوی کالا (با قیمت خرید) + دکمهٔ اسکن. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchWithResults(
    state: PurchaseUiState,
    onQueryChange: (String) -> Unit,
    onPick: (ProductEntity) -> Unit,
    onScan: () -> Unit
) {
    var expanded by remember { mutableStateOf(true) }

    ExposedDropdownMenuBox(
        expanded = expanded && state.searchResults.isNotEmpty(),
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChange,
            singleLine = true,
            label = { Text(stringResource(R.string.str_148)) },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.baseline_search_24),
                    contentDescription = null
                )
            },
            trailingIcon = {
                Row {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_close_24),
                                contentDescription = stringResource(R.string.str_126)
                            )
                        }
                    }
                    IconButton(onClick = onScan) {
                        Text(text = "📷", style = MaterialTheme.typography.titleMedium)
                    }
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded && state.searchResults.isNotEmpty(),
            onDismissRequest = { expanded = false }
        ) {
            state.searchResults.forEach { product ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(product.name)
                            Text(
                                text = stringResource(R.string.str_149, PersianFormat.amount(product.buyC)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    onClick = {
                        onPick(product)
                        expanded = false
                    }
                )
            }
        }
    }
}

/** دیالوگ افزودن سریع تامین‌کننده. */
@Composable
private fun QuickSupplierDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.str_132)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.str_082)) }
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.str_083)) }
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = { onSave(name, phone) }
            ) { Text(stringResource(R.string.str_066)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.str_014)) }
        }
    )
}
