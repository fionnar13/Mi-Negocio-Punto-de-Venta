package com.elfrikiamv.minegocio_puntodeventa.ui.screens.visitors

// VisitorsScreen.kt — صفحهٔ ویزیتورها (پورسانت درصدی/کارتنی/ثابت)

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elfrikiamv.minegocio_puntodeventa.R
import com.elfrikiamv.minegocio_puntodeventa.data.entity.VisitorEntity
import com.elfrikiamv.minegocio_puntodeventa.utils.NumberUtils
import com.elfrikiamv.minegocio_puntodeventa.utils.PersianFormat
import androidx.compose.ui.res.stringResource

/** برچسب فارسی نوع پورسانت. */
private fun commissionTypeLabel(type: String): String = when (type) {
    VisitorEntity.TYPE_PERCENT -> "درصدی"
    VisitorEntity.TYPE_CARTON -> "کارتنی"
    VisitorEntity.TYPE_FIXED -> "ثابت"
    else -> type
}

/**
 * صفحهٔ ویزیتورها: نام، جمع پورسانت کسب‌شده و مانده.
 */
@Composable
fun VisitorsScreen(
    viewModel: VisitorsViewModel = viewModel(factory = VisitorsViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsState()

    var editing by remember { mutableStateOf<VisitorEntity?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<VisitorEntity?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.visitors.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_user_check),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(stringResource(R.string.str_257), style = MaterialTheme.typography.titleMedium)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.visitors, key = { it.id }) { visitor ->
                    VisitorRow(
                        visitor = visitor,
                        commission = state.commissions[visitor.id] ?: 0.0,
                        onEdit = { editing = visitor; showDialog = true },
                        onDelete = { deleting = visitor }
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
            text = { Text(stringResource(R.string.str_258)) },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        )
    }

    if (showDialog) {
        VisitorDialog(
            visitor = editing ?: VisitorEntity(),
            onDismiss = { showDialog = false },
            onSave = { viewModel.saveVisitor(it); showDialog = false }
        )
    }

    deleting?.let { visitor ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text(stringResource(R.string.str_259)) },
            text = { Text(stringResource(R.string.str_055, visitor.name)) },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteVisitor(visitor); deleting = null }) {
                    Text(stringResource(R.string.str_006), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text(stringResource(R.string.str_014)) } }
        )
    }
}

/** ردیف ویزیتور: نام + نوع پورسانت + جمع پورسانت + مانده. */
@Composable
private fun VisitorRow(
    visitor: VisitorEntity,
    commission: Double,
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
                        text = visitor.name,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = stringResource(R.string.str_260, commissionTypeLabel(visitor.type)) +
                            " (${NumberUtils.toPersian(PersianFormat.plain(visitor.value))})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                // مانده فعلاً برابر جمع پورسانت است (پرداخت پورسانت هنوز پیاده نشده)
                Text(
                    text = stringResource(R.string.str_261, PersianFormat.amount(commission)) +
                        stringResource(R.string.str_079, PersianFormat.amount(commission)),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (commission > 0.0) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
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

/** دیالوگ افزودن/ویرایش ویزیتور: نام، نوع پورسانت، مقدار. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VisitorDialog(
    visitor: VisitorEntity,
    onDismiss: () -> Unit,
    onSave: (VisitorEntity) -> Unit
) {
    var name by remember { mutableStateOf(visitor.name) }
    var type by remember { mutableStateOf(visitor.type) }
    var valueInput by remember { mutableStateOf(PersianFormat.plain(visitor.value)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (visitor.id == 0L) stringResource(R.string.str_258) else stringResource(R.string.str_262)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.str_082)) },
                    singleLine = true
                )

                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = commissionTypeLabel(type),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.str_263)) },
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
                        listOf(
                            VisitorEntity.TYPE_PERCENT,
                            VisitorEntity.TYPE_CARTON,
                            VisitorEntity.TYPE_FIXED
                        ).forEach { option ->
                            DropdownMenuItem(
                                text = { Text(commissionTypeLabel(option)) },
                                onClick = { type = option; expanded = false }
                            )
                        }
                    }
                }

                val valueLabel = when (type) {
                    VisitorEntity.TYPE_PERCENT -> stringResource(R.string.str_264)
                    VisitorEntity.TYPE_CARTON -> stringResource(R.string.str_265)
                    else -> stringResource(R.string.str_266)
                }
                OutlinedTextField(
                    value = valueInput,
                    onValueChange = { valueInput = it },
                    label = { Text(valueLabel) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            TextButton(enabled = name.isNotBlank(), onClick = {
                onSave(
                    visitor.copy(
                        name = name.trim(),
                        type = type,
                        value = PersianFormat.parse(valueInput)
                    )
                )
            }) { Text(stringResource(R.string.str_066)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.str_014)) } }
    )
}
