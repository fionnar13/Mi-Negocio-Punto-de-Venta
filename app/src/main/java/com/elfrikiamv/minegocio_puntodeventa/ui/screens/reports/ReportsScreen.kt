package com.elfrikiamv.minegocio_puntodeventa.ui.screens.reports

// ReportsScreen.kt — صفحهٔ گزارش‌ها (انتخاب نوع، بازهٔ تاریخ، جدول، خروجی)

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elfrikiamv.minegocio_puntodeventa.R
import com.elfrikiamv.minegocio_puntodeventa.ui.components.JalaliDatePickerDialog
import com.elfrikiamv.minegocio_puntodeventa.utils.PersianFormat
import com.elfrikiamv.minegocio_puntodeventa.utils.ReportExporter
import androidx.compose.ui.res.stringResource

/**
 * صفحهٔ گزارش‌ها: دراپ‌داون نوع گزارش، بازهٔ تاریخ شمسی، جدول داده
 * و دکمه‌های CSV / Excel / چاپ.
 */
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = viewModel(factory = ReportsViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    // بارگذاری مجدد با تغییر نوع یا بازه
    LaunchedEffect(state.type, state.from, state.to) {
        viewModel.load()
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.onMessageShown()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // ---- انتخاب نوع گزارش ----
        TypeDropdown(
            selected = state.type,
            onSelect = viewModel::onTypeChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // ---- بازهٔ تاریخ ----
        if (state.type.dateRanged) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
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
            }
        }

        // ---- جدول گزارش ----
        val report = state.report
        Box(modifier = Modifier.weight(1f)) {
            when {
                state.loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
                report == null || report.rows.isEmpty() -> Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_chart_line),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        stringResource(R.string.str_150),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${report.title} (${PersianFormat.displayDate(state.from)} — ${PersianFormat.displayDate(state.to)})",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                report.headers.forEach { h ->
                                    Text(
                                        text = h,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                        items(report.rows) { row ->
                            Row(
                                modifier = Modifier.padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                row.forEach { cell ->
                                    Text(
                                        text = cell,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ---- دکمه‌های خروجی ----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    report?.let {
                        val msg = ReportExporter.exportCsv(
                            context, it.title, state.from, state.to, it.headers, it.rows
                        )
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    }
                },
                enabled = report != null && report.rows.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) { Text("CSV", maxLines = 1) }

            Button(
                onClick = {
                    report?.let {
                        val msg = ReportExporter.exportExcel(
                            context, it.title, state.from, state.to, it.headers, it.rows
                        )
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    }
                },
                enabled = report != null && report.rows.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) { Text("Excel", maxLines = 1) }

            OutlinedButton(
                onClick = {
                    report?.let {
                        ReportExporter.print(context, it.title, it.headers, it.rows)
                    }
                },
                enabled = report != null && report.rows.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) { Text(stringResource(R.string.str_151), maxLines = 1) }
        }
    }

    // ---- دیالوگ‌های تاریخ ----

    if (showFromPicker) {
        val parts = state.from.split("/").map { it.toIntOrNull() ?: 0 }
        JalaliDatePickerDialog(
            initialYear = parts.getOrElse(0) { 1405 },
            initialMonth = parts.getOrElse(1) { 1 },
            initialDay = parts.getOrElse(2) { 1 },
            onDateSelected = { y, m, d ->
                viewModel.onFromChange(PersianFormat.jalaliString(y, m, d))
                showFromPicker = false
            },
            onDismiss = { showFromPicker = false }
        )
    }

    if (showToPicker) {
        val parts = state.to.split("/").map { it.toIntOrNull() ?: 0 }
        JalaliDatePickerDialog(
            initialYear = parts.getOrElse(0) { 1405 },
            initialMonth = parts.getOrElse(1) { 1 },
            initialDay = parts.getOrElse(2) { 1 },
            onDateSelected = { y, m, d ->
                viewModel.onToChange(PersianFormat.jalaliString(y, m, d))
                showToPicker = false
            },
            onDismiss = { showToPicker = false }
        )
    }
}

// ---- اجزای صفحهٔ گزارش‌ها ----

/** دراپ‌داون نوع گزارش. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypeDropdown(
    selected: ReportType,
    onSelect: (ReportType) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected.faTitle,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.str_152)) },
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
            ReportType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.faTitle) },
                    onClick = { onSelect(type); expanded = false }
                )
            }
        }
    }
}

/** فیلد تاریخ (کلیک → دیالوگ شمسی). */
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
            singleLine = true,
            trailingIcon = {
                Icon(painter = painterResource(R.drawable.ic_calendar), contentDescription = null)
            },
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick)
        )
    }
}
