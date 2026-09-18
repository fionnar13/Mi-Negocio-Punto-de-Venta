package com.elfrikiamv.minegocio_puntodeventa.ui.screens.sale

// SaleScreen.kt — صفحهٔ فروش (POS)

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elfrikiamv.minegocio_puntodeventa.R
import com.elfrikiamv.minegocio_puntodeventa.data.entity.CustomerEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.Payment
import com.elfrikiamv.minegocio_puntodeventa.scanner.BarcodeScanCallback
import com.elfrikiamv.minegocio_puntodeventa.scanner.BarcodeScanResult
import com.elfrikiamv.minegocio_puntodeventa.scanner.BarcodeScannerCallbackHost
import com.elfrikiamv.minegocio_puntodeventa.scanner.ScannerActivity
import com.elfrikiamv.minegocio_puntodeventa.ui.components.CartTable
import com.elfrikiamv.minegocio_puntodeventa.ui.components.JalaliDatePickerDialog
import com.elfrikiamv.minegocio_puntodeventa.ui.components.TimePickerDialog
import com.elfrikiamv.minegocio_puntodeventa.ui.theme.GoldAccent
import com.elfrikiamv.minegocio_puntodeventa.utils.PersianFormat
import androidx.compose.ui.res.stringResource

/**
 * صفحهٔ فروش (صندوق):
 * انتخاب مشتری/ویزیتور/تاریخ/ساعت → جستجو یا اسکن کالا → سبد →
 * تخفیف و پرداخت‌ها → ثبت تراکنشی فاکتور.
 */
@Composable
fun SaleScreen(
    viewModel: SaleViewModel = viewModel(factory = SaleViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var shareSale by remember { mutableStateOf<com.elfrikiamv.minegocio_puntodeventa.data.entity.SaleEntity?>(null) }

    var showCustomerDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var paymentMethod by remember { mutableStateOf<String?>(null) }

    // اسکنر پیوسته: هر بارکد بلافاصله به سبد اضافه می‌شود؛ بستن با دکمهٔ بستن
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

    // نمایش پیام‌ها (Toast)
    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.onMessageShown()
        }
    }

    // میان‌بر F2 برای فوکوس روی جستجو
    val searchFocus = remember { FocusRequester() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onPreviewKeyEvent { event: KeyEvent ->
                if (event.type == KeyEventType.KeyDown && event.key == Key.F2) {
                    searchFocus.requestFocus()
                    true
                } else {
                    false
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ---- ردیف ۱: مشتری + افزودن + ویزیتور ----
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val customerLabel = state.customers
                    .find { it.id == state.selectedCustomerId }
                    ?.let { "${it.name} (${it.kind})" }
                    ?: stringResource(R.string.str_153)
                DropdownField(
                    label = stringResource(R.string.str_044),
                    value = customerLabel,
                    modifier = Modifier.weight(1.4f)
                ) { close ->
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.str_153)) },
                        onClick = { viewModel.onCustomerChange(null); close() }
                    )
                    state.customers.forEach { customer ->
                        DropdownMenuItem(
                            text = { Text("${customer.name} (${customer.kind})") },
                            onClick = { viewModel.onCustomerChange(customer.id); close() }
                        )
                    }
                }
                IconButton(onClick = { showCustomerDialog = true }) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_add_24),
                        contentDescription = stringResource(R.string.str_076)
                    )
                }
                DropdownField(
                    label = stringResource(R.string.str_154),
                    value = state.visitors
                        .find { it.id == state.selectedVisitorId }?.name
                        ?: stringResource(R.string.str_155),
                    modifier = Modifier.weight(1f)
                ) { close ->
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.str_155)) },
                        onClick = { viewModel.onVisitorChange(null); close() }
                    )
                    state.visitors.forEach { visitor ->
                        DropdownMenuItem(
                            text = { Text(visitor.name) },
                            onClick = { viewModel.onVisitorChange(visitor.id); close() }
                        )
                    }
                }
            }

            // ---- ردیف ۲: تاریخ و ساعت ----
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DateField(
                    date = state.date,
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f)
                )
                TimeField(
                    time = state.time,
                    onClick = { showTimePicker = true },
                    modifier = Modifier.weight(1f)
                )
            }

            // ---- جستجوی کالا (F2) + دکمهٔ اسکن ----
            SearchWithResults(
                state = state,
                onQueryChange = viewModel::onQueryChange,
                onPick = viewModel::addFromSearch,
                onScan = {
                    scanLauncher.launch(
                        ScannerActivity.createIntent(context, continuous = true)
                    )
                },
                focusRequester = searchFocus
            )

            // ---- سبد خرید ----
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

            // ---- جمع‌ها ----
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
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal
                            ),
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
                    if (state.remaining > 0.009 && state.payments.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.str_079, PersianFormat.amount(state.remaining)),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // ---- پرداخت‌ها ----
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PaymentButton("💵", stringResource(R.string.str_156), Payment.METHOD_CASH, Modifier.weight(1f)) {
                    paymentMethod = Payment.METHOD_CASH
                }
                PaymentButton("💳", stringResource(R.string.str_157), Payment.METHOD_CARD, Modifier.weight(1f)) {
                    paymentMethod = Payment.METHOD_CARD
                }
                PaymentButton("🔄", stringResource(R.string.str_057), Payment.METHOD_TRANSFER, Modifier.weight(1f)) {
                    paymentMethod = Payment.METHOD_TRANSFER
                }
                PaymentButton("📄", stringResource(R.string.str_158), Payment.METHOD_CHEQUE, Modifier.weight(1f)) {
                    paymentMethod = Payment.METHOD_CHEQUE
                }
            }

            if (state.payments.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                        state.payments.forEachIndexed { index, payment ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${methodEmoji(payment.method)} ${payment.method}",
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = stringResource(R.string.str_035, PersianFormat.amount(payment.amount))
                                )
                                IconButton(onClick = { viewModel.removePayment(index) }) {
                                    Icon(
                                        painter = painterResource(R.drawable.baseline_close_24),
                                        contentDescription = stringResource(R.string.str_159),
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ---- دکمهٔ ثبت (طلایی) ----
            Button(
                onClick = viewModel::submitSale,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldAccent,
                    contentColor = androidx.compose.ui.graphics.Color(0xFF201A05)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(text = stringResource(R.string.str_160), style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ---- فاکتورهای اخیر (چاپ / اشتراک‌گذاری) ----
            if (state.recent.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(stringResource(R.string.str_161), style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        state.recent.forEach { sale ->
                            val customerName =
                                state.customers.find { it.id == sale.customerId }?.name ?: stringResource(R.string.str_100)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        stringResource(R.string.str_162, com.elfrikiamv.minegocio_puntodeventa.utils.NumberUtils.toPersian(sale.no)) +
                                            "${PersianFormat.displayDate(sale.date)} ${PersianFormat.displayTime(sale.time)}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        stringResource(R.string.str_143, customerName, PersianFormat.amount(sale.grand)),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(onClick = {
                                    com.elfrikiamv.minegocio_puntodeventa.utils.InvoicePrinter.printSale(
                                        context = context,
                                        sale = sale,
                                        customerName = customerName,
                                        visitorName = state.visitors.find { it.id == sale.visitorId }?.name.orEmpty(),
                                        settings = state.settings
                                    )
                                }) {
                                    Icon(
                                        painter = androidx.compose.ui.res.painterResource(R.drawable.baseline_print_24),
                                        contentDescription = stringResource(R.string.str_144),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { shareSale = sale }) {
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
    }

    // ---- دیالوگ‌ها ----

    // دیالوگ انتخاب مقصد اشتراک‌گذاری فاکتور فروش
    shareSale?.let { sale ->
        val customerName = state.customers.find { it.id == sale.customerId }?.name.orEmpty()
        val visitorName = state.visitors.find { it.id == sale.visitorId }?.name.orEmpty()
        AlertDialog(
            onDismissRequest = { shareSale = null },
            title = {
                Text(
                    stringResource(R.string.str_163) +
                        com.elfrikiamv.minegocio_puntodeventa.utils.NumberUtils.toPersian(sale.no)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    com.elfrikiamv.minegocio_puntodeventa.utils.ShareTarget.entries.forEach { target ->
                        OutlinedButton(
                            onClick = {
                                com.elfrikiamv.minegocio_puntodeventa.utils.ShareUtils.share(
                                    context,
                                    com.elfrikiamv.minegocio_puntodeventa.utils.ReceiptText.sale(
                                        sale, customerName, visitorName, state.settings
                                    ),
                                    target
                                )
                                shareSale = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("${target.icon} ${target.faTitle}") }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { shareSale = null }) { Text(stringResource(R.string.str_074)) }
            }
        )
    }

    if (showCustomerDialog) {
        QuickCustomerDialog(
            onDismiss = { showCustomerDialog = false },
            onSave = { name, phone, kind ->
                viewModel.addCustomer(name, phone, kind)
                showCustomerDialog = false
            }
        )
    }

    if (showDatePicker) {
        val parts = state.date.split("/").map { it.toIntOrNull() ?: 0 }
        JalaliDatePickerDialog(
            initialYear = parts.getOrElse(0) { 1405 },
            initialMonth = parts.getOrElse(1) { 1 },
            initialDay = parts.getOrElse(2) { 1 },
            onDateSelected = { y, m, d -> viewModel.onDateChange(PersianFormat.jalaliString(y, m, d)) },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showTimePicker) {
        val parts = state.time.split(":").map { it.toIntOrNull() ?: 0 }
        TimePickerDialog(
            initialHour = parts.getOrElse(0) { 14 },
            initialMinute = parts.getOrElse(1) { 30 },
            onSelected = { h, m -> viewModel.onTimeChange("%02d:%02d".format(h, m)) },
            onDismiss = { showTimePicker = false }
        )
    }

    paymentMethod?.let { method ->
        PaymentAmountDialog(
            method = method,
            remaining = state.remaining,
            onDismiss = { paymentMethod = null },
            onConfirm = { amount ->
                viewModel.addPayment(method, amount)
                paymentMethod = null
            }
        )
    }
}

// ---- اجزای کوچک صفحهٔ فروش ----

/** دراپ‌داون فقط-خواندنی با محتوای دلخواه. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    content: @Composable (close: () -> Unit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
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
            content { expanded = false }
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
        // لایهٔ کلیک روی کل فیلد
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(onClick = onClick)
        )
    }
}

/** فیلد ساعت (کلیک → دیالوگ). */
@Composable
private fun TimeField(time: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        OutlinedTextField(
            value = PersianFormat.displayTime(time),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.str_098)) },
            singleLine = true,
            trailingIcon = {
                Icon(
                    painter = painterResource(R.drawable.baseline_access_time_24),
                    contentDescription = null
                )
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

/** جستجوی کالا با پیشنهادها + دکمهٔ اسکن + نشانگر F2. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchWithResults(
    state: SaleUiState,
    onQueryChange: (String) -> Unit,
    onPick: (com.elfrikiamv.minegocio_puntodeventa.data.entity.ProductEntity) -> Unit,
    onScan: () -> Unit,
    focusRequester: FocusRequester
) {
    var expanded by remember { mutableStateOf(false) }

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
                // نشانگر میان‌بر F2
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Text(
                        text = "F2",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
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
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = androidx.compose.ui.text.input.ImeAction.Search
            ),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable)
                .focusRequester(focusRequester)
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
                                text = stringResource(R.string.str_123, PersianFormat.amount(product.sellC)),
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

/** دکمهٔ روش پرداخت. */
@Composable
private fun PaymentButton(
    emoji: String,
    label: String,
    method: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(onClick = onClick, modifier = modifier) {
        Text(text = "$emoji $label", maxLines = 1)
    }
}

/** دیالوگ مبلغ پرداخت. */
@Composable
private fun PaymentAmountDialog(
    method: String,
    remaining: Double,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var amount by remember {
        mutableStateOf(if (remaining > 0) PersianFormat.plain(remaining) else "")
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.str_164, methodEmoji(method), method)) },
        text = {
            Column {
                if (remaining > 0) {
                    Text(
                        text = stringResource(R.string.str_165, PersianFormat.amount(remaining)),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.str_046)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(amount) }) { Text(stringResource(R.string.str_073)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.str_014)) }
        }
    )
}

/** دیالوگ افزودن سریع مشتری. */
@Composable
private fun QuickCustomerDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, kind: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf(CustomerEntity.KIND_CASH) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.str_076)) },
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
            TextButton(
                enabled = name.isNotBlank(),
                onClick = { onSave(name, phone, kind) }
            ) { Text(stringResource(R.string.str_066)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.str_014)) }
        }
    )
}

/** ایموجی روش پرداخت. */
private fun methodEmoji(method: String): String = when (method) {
    Payment.METHOD_CASH -> "💵"
    Payment.METHOD_CARD -> "💳"
    Payment.METHOD_TRANSFER -> "🔄"
    Payment.METHOD_CHEQUE -> "📄"
    else -> "💵"
}
