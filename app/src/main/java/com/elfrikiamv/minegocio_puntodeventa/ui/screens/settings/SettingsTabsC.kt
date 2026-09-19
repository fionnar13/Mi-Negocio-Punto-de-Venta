package com.elfrikiamv.minegocio_puntodeventa.ui.screens.settings

// SettingsTabsC.kt — تب‌های داده‌ها، سرور و دیباگر

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.elfrikiamv.minegocio_puntodeventa.data.entity.ErrorLogEntity
import com.elfrikiamv.minegocio_puntodeventa.utils.JalaliDateUtils
import com.elfrikiamv.minegocio_puntodeventa.utils.NumberUtils
import com.elfrikiamv.minegocio_puntodeventa.utils.PersianFormat
import com.elfrikiamv.minegocio_puntodeventa.utils.ShareUtils
import androidx.compose.ui.res.stringResource
import com.elfrikiamv.minegocio_puntodeventa.R

private const val XLSX_MIME =
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

// ---------------------------------------------------------------------------
// تب ۷: داده‌ها
// ---------------------------------------------------------------------------

@Composable
internal fun DataTab(state: SettingsUiState, viewModel: SettingsViewModel) {
    val context = LocalContext.current
    var confirmReset by remember { mutableStateOf(false) }

    val importProducts = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? -> uri?.let(viewModel::importProducts) }

    val importCustomers = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? -> uri?.let(viewModel::importCustomers) }

    val restorePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? -> uri?.let(viewModel::restore) }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionCard(stringResource(R.string.str_216)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = viewModel::exportProductsExcel,
                    modifier = Modifier.weight(1f)
                ) { Text(stringResource(R.string.str_217), maxLines = 1) }
                OutlinedButton(
                    onClick = { importProducts.launch(arrayOf(XLSX_MIME)) },
                    modifier = Modifier.weight(1f)
                ) { Text(stringResource(R.string.str_218), maxLines = 1) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = viewModel::exportCustomersExcel,
                    modifier = Modifier.weight(1f)
                ) { Text(stringResource(R.string.str_219), maxLines = 1) }
                OutlinedButton(
                    onClick = { importCustomers.launch(arrayOf(XLSX_MIME)) },
                    modifier = Modifier.weight(1f)
                ) { Text(stringResource(R.string.str_220), maxLines = 1) }
            }
            Text(
                stringResource(R.string.str_221) +
                    stringResource(R.string.str_222),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        SectionCard(stringResource(R.string.str_223)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = viewModel::backup,
                    modifier = Modifier.weight(1f)
                ) { Text(stringResource(R.string.str_224), maxLines = 1) }
                OutlinedButton(
                    onClick = { restorePicker.launch(arrayOf("application/json")) },
                    modifier = Modifier.weight(1f)
                ) { Text(stringResource(R.string.str_225), maxLines = 1) }
            }
        }

        SectionCard(stringResource(R.string.str_226)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { confirmReset = true },
                    modifier = Modifier.weight(1f)
                ) { Text(stringResource(R.string.str_227), maxLines = 1, color = MaterialTheme.colorScheme.error) }
                OutlinedButton(
                    onClick = viewModel::sampleData,
                    modifier = Modifier.weight(1f)
                ) { Text(stringResource(R.string.str_228), maxLines = 1) }
            }
            Text(
                stringResource(R.string.str_229),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        SectionCard(stringResource(R.string.str_230)) {
            Text(state.storage, style = MaterialTheme.typography.bodyMedium)
            OutlinedButton(
                onClick = viewModel::refreshStorage,
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.str_231)) }
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text(stringResource(R.string.str_227)) },
            text = { Text(stringResource(R.string.str_232)) },
            confirmButton = {
                TextButton(onClick = { viewModel.resetData(); confirmReset = false }) {
                    Text(stringResource(R.string.str_233), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text(stringResource(R.string.str_014)) } }
        )
    }
}

// ---------------------------------------------------------------------------
// تب ۸: سرور
// ---------------------------------------------------------------------------

@Composable
internal fun ServerTab(state: SettingsUiState, viewModel: SettingsViewModel) {
    val s = state.settings
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionCard(stringResource(R.string.str_234)) {
            OutlinedTextField(
                value = s.wsUrl,
                onValueChange = { v -> viewModel.updateSettings { it.copy(wsUrl = v) } },
                label = { Text(stringResource(R.string.str_235)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            if (state.wsConnected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error,
                            CircleShape
                        )
                )
                Text(
                    text = state.wsStatus,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = viewModel::wsConnect,
                    enabled = !state.wsConnected,
                    modifier = Modifier.weight(1f)
                ) { Text(stringResource(R.string.str_236)) }
                OutlinedButton(
                    onClick = viewModel::wsDisconnect,
                    enabled = state.wsConnected,
                    modifier = Modifier.weight(1f)
                ) { Text(stringResource(R.string.str_237)) }
            }
            OutlinedButton(
                onClick = viewModel::saveSettings,
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.str_238)) }
        }
    }
}

// ---------------------------------------------------------------------------
// تب ۹: دیباگر
// ---------------------------------------------------------------------------

/** قالب‌بندی زمان رکورد لاگ (شمسی + ساعت). */
private fun formatLogTs(ts: Long): String {
    val pd = com.elfrikiamv.minegocio_puntodeventa.utils.JalaliDateUtils.fromTimestamp(ts)
    val date = com.elfrikiamv.minegocio_puntodeventa.utils.PersianFormat.jalaliString(
        pd.getShYear(), pd.getShMonth(), pd.getShDay()
    )
    return com.elfrikiamv.minegocio_puntodeventa.utils.PersianFormat.displayDate(date) +
        " " + com.elfrikiamv.minegocio_puntodeventa.utils.PersianFormat.displayTime(
        "%02d:%02d".format(pd.getHour(), pd.getMinute())
    )
}

/** رنگ نشان سطح لاگ. */
@Composable
private fun levelColor(level: String) = when (level) {
    "ERROR" -> MaterialTheme.colorScheme.error
    "WARN" -> androidx.compose.ui.graphics.Color(0xFFD4AF37)
    "INFO" -> MaterialTheme.colorScheme.primary
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DebuggerTab(state: SettingsUiState, viewModel: SettingsViewModel) {
    val context = LocalContext.current
    var detail by remember {
        mutableStateOf<ErrorLogEntity?>(null)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // ---- کارت‌های آمار ----
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(stringResource(R.string.str_239), state.logStats["ERROR"] ?: 0, "ERROR", Modifier.weight(1f))
            StatCard(stringResource(R.string.str_194), state.logStats["WARN"] ?: 0, "WARN", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(stringResource(R.string.str_240), state.logStats["INFO"] ?: 0, "INFO", Modifier.weight(1f))
            StatCard(stringResource(R.string.str_241), state.logStats["DEBUG"] ?: 0, "DEBUG", Modifier.weight(1f))
        }

        // ---- فیلترها ----
        OutlinedTextField(
            value = state.logQuery,
            onValueChange = viewModel::onLogQueryChange,
            label = { Text(stringResource(R.string.str_242)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        LevelDropdown(
            selected = state.logLevel,
            onSelect = viewModel::onLogLevelChange
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedButton(
                onClick = viewModel::refreshLogs,
                modifier = Modifier.weight(1f)
            ) { Text(stringResource(R.string.str_231), maxLines = 1) }
            OutlinedButton(
                onClick = viewModel::clearLogs,
                modifier = Modifier.weight(1f)
            ) { Text(stringResource(R.string.str_126), maxLines = 1) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedButton(
                onClick = {
                    val text = state.logs.joinToString("\n\n") { e ->
                        "${e.level} [${e.scope}] ${formatLogTs(e.ts)}\n${e.message}\n${e.technical}"
                    }.ifBlank { "لاگی ثبت نشده است" }
                    val msg = com.elfrikiamv.minegocio_puntodeventa.utils.ShareUtils.saveText(
                        context,
                        "sazman-log-${com.elfrikiamv.minegocio_puntodeventa.utils.PersianFormat.todayDateString().replace('/', '-')}.txt",
                        text
                    )
                    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                },
                modifier = Modifier.weight(1f)
            ) { Text(stringResource(R.string.str_243), maxLines = 1) }
            OutlinedButton(
                onClick = {
                    val body = state.logs.joinToString("\n") { e ->
                        "[${e.level}] ${formatLogTs(e.ts)} · ${e.scope} · ${e.message}"
                    }.ifBlank { "لاگی ثبت نشده است" }
                    com.elfrikiamv.minegocio_puntodeventa.utils.ShareUtils.email(
                        context, "لاگ سازمان فروشگاه", body
                    )
                },
                modifier = Modifier.weight(1f)
            ) { Text(stringResource(R.string.str_175), maxLines = 1) }
        }

        Text(
            stringResource(R.string.str_244, NumberUtils.toPersian(state.logs.size.toLong())),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // ---- فهرست خطاها ----
        if (state.logs.isEmpty()) {
            Text(
                stringResource(R.string.debugger_no_logs),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }
        LazyColumn(modifier = Modifier.height(300.dp)) {
            items(state.logs) { entry ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    onClick = { detail = entry }
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // نشان سطح
                            Text(
                                text = entry.level,
                                style = MaterialTheme.typography.labelSmall,
                                color = androidx.compose.ui.graphics.Color.White,
                                modifier = Modifier
                                    .background(levelColor(entry.level), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = entry.scope,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = formatLogTs(entry.ts),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = entry.message,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (entry.technical.isNotBlank()) {
                            Text(
                                text = entry.technical.lineSequence().firstOrNull() ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }

    // ---- دیالوگ جزئیات ----
    detail?.let { e ->
        AlertDialog(
            onDismissRequest = { detail = null },
            title = { Text(stringResource(R.string.str_245, NumberUtils.toPersian(e.id))) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    DetailRow(stringResource(R.string.str_246), e.level)
                    DetailRow(stringResource(R.string.str_247), e.scope)
                    DetailRow(stringResource(R.string.str_248), formatLogTs(e.ts))
                    DetailRow(stringResource(R.string.str_249), e.message)
                    if (e.technical.isNotBlank()) DetailRow(stringResource(R.string.str_250), e.technical)
                    if (e.context.isNotBlank()) DetailRow(stringResource(R.string.str_251), e.context)
                    if (e.userAgent.isNotBlank()) DetailRow(stringResource(R.string.str_252), e.userAgent)
                    if (e.url.isNotBlank()) DetailRow(stringResource(R.string.str_253), e.url)
                }
            },
            confirmButton = { TextButton(onClick = { detail = null }) { Text(stringResource(R.string.str_074)) } }
        )
    }
}

/** کارت آمار سطح لاگ. */
@Composable
private fun StatCard(title: String, count: Int, level: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(levelColor(level), CircleShape)
            )
            Spacer(modifier = Modifier.size(10.dp))
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    NumberUtils.toPersian(count.toLong()),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

/** دراپ‌داون سطح لاگ. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LevelDropdown(selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(
        "" to stringResource(R.string.str_036),
        "ERROR" to stringResource(R.string.str_239),
        "WARN" to stringResource(R.string.str_194),
        "INFO" to stringResource(R.string.str_240),
        "DEBUG" to stringResource(R.string.str_241)
    )
    val label = options.find { it.first == selected }?.second ?: stringResource(R.string.str_036)
    androidx.compose.material3.ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.str_246)) },
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
            options.forEach { (value, text) ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = { onSelect(value); expanded = false }
                )
            }
        }
    }
}

/** ردیف «برچسب: مقدار» در دیالوگ جزئیات. */
@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}
