package com.elfrikiamv.minegocio_puntodeventa.ui.screens.settings

// SettingsTabsB.kt — تب‌های کاربران (با QR ورود) و دستگاه‌ها (شیفت)

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.elfrikiamv.minegocio_puntodeventa.R
import com.elfrikiamv.minegocio_puntodeventa.data.entity.UserEntity
import com.elfrikiamv.minegocio_puntodeventa.utils.JalaliDateUtils
import com.elfrikiamv.minegocio_puntodeventa.utils.NumberUtils
import com.elfrikiamv.minegocio_puntodeventa.utils.PersianFormat
import com.elfrikiamv.minegocio_puntodeventa.utils.QrBitmap
import com.google.gson.Gson
import androidx.compose.ui.res.stringResource

// ---------------------------------------------------------------------------
// تب ۵: کاربران
// ---------------------------------------------------------------------------

@Composable
internal fun UsersTab(state: SettingsUiState, viewModel: SettingsViewModel) {
    var editing by remember { mutableStateOf<UserEntity?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<UserEntity?>(null) }
    var qrUser by remember { mutableStateOf<UserEntity?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
            onClick = { editing = null; showDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.str_198)) }

        if (state.users.isEmpty()) {
            Text(
                stringResource(R.string.str_199),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            state.users.forEach { user ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(user.name, style = MaterialTheme.typography.titleSmall)
                            Text(
                                "${user.role.ifBlank { "—" }} · توکن: …${NumberUtils.toPersian(user.token.takeLast(6))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = { qrUser = user }) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_qr_code_scanner_24),
                                contentDescription = stringResource(R.string.str_201),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { editing = user; showDialog = true }) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_edit_24),
                                contentDescription = stringResource(R.string.str_060),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { deleting = user }) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_delete_24),
                                contentDescription = stringResource(R.string.str_006),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        UserDialog(
            user = editing ?: UserEntity(),
            onDismiss = { showDialog = false },
            onSave = { viewModel.saveUser(it); showDialog = false }
        )
    }

    deleting?.let { user ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text(stringResource(R.string.str_202)) },
            text = { Text(stringResource(R.string.str_055, user.name)) },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteUser(user); deleting = null }) {
                    Text(stringResource(R.string.str_006), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text(stringResource(R.string.str_014)) } }
        )
    }

    qrUser?.let { user ->
        AlertDialog(
            onDismissRequest = { qrUser = null },
            title = { Text(stringResource(R.string.str_203, user.name)) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val qr = remember(user.id) {
                        QrBitmap.generate(
                            Gson().toJson(
                                mapOf("id" to user.id, "name" to user.name, "token" to user.token)
                            )
                        )
                    }
                    Image(
                        bitmap = qr.asImageBitmap(),
                        contentDescription = stringResource(R.string.str_201),
                        modifier = Modifier.size(240.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.str_204),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { qrUser = null }) { Text(stringResource(R.string.str_074)) }
            }
        )
    }
}

/** دیالوگ افزودن/ویرایش کاربر. */
@Composable
private fun UserDialog(
    user: UserEntity,
    onDismiss: () -> Unit,
    onSave: (UserEntity) -> Unit
) {
    var name by remember { mutableStateOf(user.name) }
    var role by remember { mutableStateOf(user.role) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (user.id == 0L) stringResource(R.string.str_205) else stringResource(R.string.str_206)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text(stringResource(R.string.str_082)) }, singleLine = true
                )
                OutlinedTextField(
                    value = role, onValueChange = { role = it },
                    label = { Text(stringResource(R.string.str_207)) }, singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(enabled = name.isNotBlank(), onClick = {
                onSave(user.copy(name = name.trim(), role = role.trim()))
            }) { Text(stringResource(R.string.str_066)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.str_014)) } }
    )
}

// ---------------------------------------------------------------------------
// تب ۶: دستگاه‌ها
// ---------------------------------------------------------------------------

@Composable
internal fun DevicesTab(state: SettingsUiState, viewModel: SettingsViewModel) {
    var showStart by remember { mutableStateOf(false) }
    var confirmEnd by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(bottom = 24.dp)
    ) {
        // وضعیت شیفت
        SectionCard(stringResource(R.string.str_208)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.baseline_point_of_sale_24),
                    contentDescription = null,
                    tint = if (state.sessionActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    state.sessionLabel,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.sessionActive) {
                    Button(onClick = { confirmEnd = true }, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.str_209))
                    }
                } else {
                    Button(onClick = { showStart = true }, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.str_086))
                    }
                }
            }
        }

        // اسکنرهای دیده‌شده
        SectionCard(stringResource(R.string.str_210)) {
            if (state.scanners.isEmpty()) {
                Text(
                    stringResource(R.string.str_211),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                state.scanners.forEach { d ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(d.device, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                stringResource(R.string.str_212, NumberUtils.toPersian(d.cnt.toLong())),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        val pd = JalaliDateUtils.fromTimestamp(d.lastSeen)
                        val dateStr = PersianFormat.jalaliString(
                            pd.getShYear(), pd.getShMonth(), pd.getShDay()
                        )
                        Text(
                            "${PersianFormat.displayDate(dateStr)} ${PersianFormat.displayTime(
                                "%02d:%02d".format(pd.getHour(), pd.getMinute())
                            )}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showStart) {
        var cash by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showStart = false },
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
                    showStart = false
                }) { Text(stringResource(R.string.str_103)) }
            },
            dismissButton = {
                TextButton(onClick = { showStart = false }) { Text(stringResource(R.string.str_014)) }
            }
        )
    }

    if (confirmEnd) {
        AlertDialog(
            onDismissRequest = { confirmEnd = false },
            title = { Text(stringResource(R.string.str_213)) },
            text = { Text(stringResource(R.string.str_214)) },
            confirmButton = {
                TextButton(onClick = { viewModel.endShift(); confirmEnd = false }) {
                    Text(stringResource(R.string.str_215))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmEnd = false }) { Text(stringResource(R.string.str_014)) }
            }
        )
    }
}
