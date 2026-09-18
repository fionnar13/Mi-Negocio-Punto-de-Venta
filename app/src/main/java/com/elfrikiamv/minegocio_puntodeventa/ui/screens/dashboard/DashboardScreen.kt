package com.elfrikiamv.minegocio_puntodeventa.ui.screens.dashboard

// DashboardScreen.kt — داشبورد «سازمان فروشگاه»

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elfrikiamv.minegocio_puntodeventa.R
import com.elfrikiamv.minegocio_puntodeventa.ui.components.CategoryPieChart
import com.elfrikiamv.minegocio_puntodeventa.ui.components.SalesBarChart
import com.elfrikiamv.minegocio_puntodeventa.ui.components.SalesLineChart
import com.elfrikiamv.minegocio_puntodeventa.utils.NumberUtils
import com.elfrikiamv.minegocio_puntodeventa.utils.PersianFormat
import androidx.compose.ui.res.stringResource

/**
 * داشبورد: وضعیت شیفت، ۶ کارت KPI، ۵ نمودار،
 * کالاهای کم‌موجودی و آخرین فروش‌ها.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showStartShift by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.refresh() }

    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.onMessageShown()
        }
    }

    if (state.loading) {
        CircularProgressIndicator(modifier = Modifier.fillMaxSize().wrapContentSize())
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ---- کارت شیفت ----
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_point_of_sale_24),
                        contentDescription = null,
                        tint = if (state.sessionActive) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.sessionLabel,
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (state.sessionActive) {
                            Text(
                                text = stringResource(R.string.str_085, PersianFormat.amount(state.sessionStartCash)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (!state.sessionActive) {
                        Button(onClick = { showStartShift = true }) {
                            Text(stringResource(R.string.str_086))
                        }
                    }
                }
            }
        }

        // ---- ۶ کارت KPI (۲ ستون) ----
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KpiCard(stringResource(R.string.str_087), state.todaySales, Modifier.weight(1f))
                KpiCard(stringResource(R.string.str_088), state.monthProfit, Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KpiCard(stringResource(R.string.str_089), state.inventoryValue, Modifier.weight(1f))
                KpiCard(stringResource(R.string.str_019), state.receivables, Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KpiCard(stringResource(R.string.str_017), state.cash, Modifier.weight(1f))
                KpiCard(stringResource(R.string.str_018), state.bank, Modifier.weight(1f))
            }
        }

        // ---- نمودارها ----
        item { SalesLineChart(title = stringResource(R.string.str_090), points = state.weekly) }
        item { SalesBarChart(title = stringResource(R.string.str_091), points = state.hourly) }
        item { CategoryPieChart(title = stringResource(R.string.str_092), points = state.byCategory) }
        item { SalesBarChart(title = stringResource(R.string.str_093), points = state.bySubCategory) }
        item { CategoryPieChart(title = stringResource(R.string.str_094), points = state.byBrand) }

        // ---- کالاهای کم‌موجودی ----
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = stringResource(R.string.str_095, NumberUtils.toPersian(state.lowStock.size)),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (state.lowStock.isEmpty()) {
                        Text(
                            stringResource(R.string.dashboard_stock_ok),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            state.lowStock.take(12).forEach { product ->
                                AssistChip(
                                    onClick = { },
                                    label = {
                                        Text(
                                            "${product.name} (${PersianFormat.qty(product.stock)})",
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // ---- آخرین فروش‌ها ----
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(stringResource(R.string.str_096), style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableHeader(stringResource(R.string.str_097), 1f)
                        TableHeader(stringResource(R.string.str_098), 0.8f)
                        TableHeader(stringResource(R.string.str_044), 1.6f)
                        TableHeader(stringResource(R.string.str_099), 1.2f)
                    }
                    if (state.recentSales.isEmpty()) {
                        Text(
                            stringResource(R.string.dashboard_no_sales),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    state.recentSales.forEach { sale ->
                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                NumberUtils.toPersian(sale.no),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                PersianFormat.displayTime(sale.time),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(0.8f)
                            )
                            Text(
                                state.customerNames[sale.customerId] ?: stringResource(R.string.str_100),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1.6f)
                            )
                            Text(
                                PersianFormat.amount(sale.grand),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1.2f)
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    // ---- دیالوگ شروع شیفت ----
    if (showStartShift) {
        var cash by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showStartShift = false },
            title = { Text(stringResource(R.string.str_101)) },
            text = {
                OutlinedTextField(
                    value = cash,
                    onValueChange = { cash = it },
                    label = { Text(stringResource(R.string.str_102)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.startShift(PersianFormat.parse(cash))
                    showStartShift = false
                }) { Text(stringResource(R.string.str_103)) }
            },
            dismissButton = {
                TextButton(onClick = { showStartShift = false }) { Text(stringResource(R.string.str_014)) }
            }
        )
    }
}

/** کارت KPI. */
@Composable
private fun KpiCard(title: String, value: Double, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = PersianFormat.amount(value),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.str_104),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
