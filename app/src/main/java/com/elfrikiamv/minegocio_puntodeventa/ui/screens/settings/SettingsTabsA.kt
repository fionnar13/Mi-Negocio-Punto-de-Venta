package com.elfrikiamv.minegocio_puntodeventa.ui.screens.settings

// SettingsTabsA.kt — تب‌های عمومی، پروفایل فروشگاه، ظاهر و بارکد

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.elfrikiamv.minegocio_puntodeventa.data.entity.SettingsEntity
import com.elfrikiamv.minegocio_puntodeventa.ui.components.unitLabel
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.io.File
import androidx.compose.ui.res.stringResource
import com.elfrikiamv.minegocio_puntodeventa.R

/** گزینه‌های واحد پیش‌فرض فروش. */
private val UNIT_OPTIONS = listOf("u", "c", "kg", "g")

/** کارت بخش با عنوان. */
@Composable
internal fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            content()
        }
    }
}

// ---------------------------------------------------------------------------
// تب ۱: عمومی
// ---------------------------------------------------------------------------

@Composable
internal fun GeneralTab(state: SettingsUiState, viewModel: SettingsViewModel) {
    val s = state.settings
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionCard(stringResource(R.string.str_166)) {
            OutlinedTextField(
                value = s.storeName,
                onValueChange = { v -> viewModel.updateSettings { it.copy(storeName = v) } },
                label = { Text(stringResource(R.string.str_167)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = s.currency,
                onValueChange = { v -> viewModel.updateSettings { it.copy(currency = v) } },
                label = { Text(stringResource(R.string.str_168)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        SectionCard(stringResource(R.string.str_169)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = s.dateSystem == SettingsEntity.DATE_JALALI,
                    onClick = {
                        viewModel.updateSettings { it.copy(dateSystem = SettingsEntity.DATE_JALALI) }
                    },
                    label = { Text(stringResource(R.string.str_170)) }
                )
                FilterChip(
                    selected = s.dateSystem == SettingsEntity.DATE_GREGORIAN,
                    onClick = {
                        viewModel.updateSettings { it.copy(dateSystem = SettingsEntity.DATE_GREGORIAN) }
                    },
                    label = { Text(stringResource(R.string.str_171)) }
                )
            }
        }

        SectionCard(stringResource(R.string.str_172)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                UNIT_OPTIONS.forEach { u ->
                    FilterChip(
                        selected = s.defaultSellUnit == u,
                        onClick = { viewModel.updateSettings { it.copy(defaultSellUnit = u) } },
                        label = { Text(unitLabel(u)) }
                    )
                }
            }
        }

        Button(onClick = viewModel::saveSettings, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.str_066))
        }
    }
}

// ---------------------------------------------------------------------------
// تب ۲: پروفایل فروشگاه
// ---------------------------------------------------------------------------

@Composable
internal fun ProfileTab(state: SettingsUiState, viewModel: SettingsViewModel) {
    val s = state.settings
    val context = LocalContext.current

    // انتخاب لوگو و کپی در حافظهٔ داخلی
    val logoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            runCatching {
                val ext = (context.contentResolver.getType(it) ?: "image/png")
                    .substringAfterLast('/').ifBlank { "png" }
                val file = File(context.filesDir, "logo.$ext")
                context.contentResolver.openInputStream(it)?.use { input ->
                    file.outputStream().use { input.copyTo(it) }
                }
                viewModel.updateSettings { st -> st.copy(storeLogo = file.absolutePath) }
            }.onFailure {
                com.elfrikiamv.minegocio_puntodeventa.utils.AppLog.e("Settings", "logo copy", it)
            }
        }
    }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionCard(stringResource(R.string.str_173)) {
            OutlinedButton(
                onClick = { logoPicker.launch(arrayOf("image/*")) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("انتخاب لوگو${if (s.storeLogo != null) " ✓" else ""}") }
            if (!s.storeLogo.isNullOrBlank()) {
                Text(
                    text = "لوگوی ذخیره‌شده: ${s.storeLogo?.substringAfterLast('/') ?: ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedTextField(
                value = s.licenseNo,
                onValueChange = { v -> viewModel.updateSettings { it.copy(licenseNo = v) } },
                label = { Text(stringResource(R.string.str_174)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = s.storePhone,
                onValueChange = { v -> viewModel.updateSettings { it.copy(storePhone = v) } },
                label = { Text(stringResource(R.string.str_083)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = s.storeEmail,
                onValueChange = { v -> viewModel.updateSettings { it.copy(storeEmail = v) } },
                label = { Text(stringResource(R.string.str_175)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = s.storeAddress,
                onValueChange = { v -> viewModel.updateSettings { it.copy(storeAddress = v) } },
                label = { Text(stringResource(R.string.str_176)) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        SectionCard(stringResource(R.string.str_177)) {
            Text(
                stringResource(R.string.str_178),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            StoreMapView(state, viewModel)
        }

        SectionCard(stringResource(R.string.str_179)) {
            OutlinedTextField(
                value = s.oneDrive,
                onValueChange = { v -> viewModel.updateSettings { it.copy(oneDrive = v) } },
                label = { Text(stringResource(R.string.str_180)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = s.gDrive,
                onValueChange = { v -> viewModel.updateSettings { it.copy(gDrive = v) } },
                label = { Text(stringResource(R.string.str_181)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = s.gSheet,
                onValueChange = { v -> viewModel.updateSettings { it.copy(gSheet = v) } },
                label = { Text(stringResource(R.string.str_182)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Button(onClick = viewModel::saveSettings, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.str_066))
        }
    }
}

/** نقشهٔ osmdroid با نشانگر فروشگاه. */
@Composable
private fun StoreMapView(state: SettingsUiState, viewModel: SettingsViewModel) {
    val context = LocalContext.current
    var marker by remember { mutableStateOf<Marker?>(null) }

    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(15.5)
                overlays.add(
                    MapEventsOverlay(object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                            p ?: return false
                            marker?.let { overlays.remove(it) }
                            val m = Marker(this@apply).apply {
                                position = p
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            }
                            overlays.add(m)
                            marker = m
                            viewModel.updateSettings {
                                it.copy(storeLat = p.latitude, storeLng = p.longitude)
                            }
                            invalidate()
                            return true
                        }

                        override fun longPressHelper(p: GeoPoint?): Boolean = false
                    })
                )
            }
        },
        update = { map ->
            val hasPos = state.settings.storeLat != 0.0 || state.settings.storeLng != 0.0
            val lat = if (state.settings.storeLat != 0.0) state.settings.storeLat else 35.6892
            val lng = if (state.settings.storeLng != 0.0) state.settings.storeLng else 51.3890
            if (marker == null && hasPos) {
                val m = Marker(map).apply {
                    position = GeoPoint(lat, lng)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                map.overlays.add(m)
                marker = m
            }
            map.controller.setCenter(GeoPoint(lat, lng))
        }
    )
}

// ---------------------------------------------------------------------------
// تب ۳: ظاهر
// ---------------------------------------------------------------------------

@Composable
internal fun AppearanceTab(state: SettingsUiState, viewModel: SettingsViewModel) {
    val s = state.settings
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionCard(stringResource(R.string.str_183)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("A4", "80mm", "58mm").forEach { f ->
                    FilterChip(
                        selected = s.printFormat == f,
                        onClick = { viewModel.updateSettings { it.copy(printFormat = f) } },
                        label = { Text(f) }
                    )
                }
            }
        }

        SectionCard(stringResource(R.string.str_184)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    SettingsEntity.THEME_DARK to stringResource(R.string.str_185),
                    SettingsEntity.THEME_LIGHT to stringResource(R.string.str_186),
                    SettingsEntity.THEME_GOLD to stringResource(R.string.str_187)
                ).forEach { (value, label) ->
                    FilterChip(
                        selected = s.theme == value,
                        onClick = {
                            viewModel.updateSettings { it.copy(theme = value) }
                            // اعمال و ذخیرهٔ فوری پوسته
                            viewModel.saveSettings()
                        },
                        label = { Text(label) }
                    )
                }
            }
        }

        SectionCard(stringResource(R.string.str_188)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    SettingsEntity.LAYOUT_RTL to stringResource(R.string.str_189),
                    SettingsEntity.LAYOUT_LTR to stringResource(R.string.str_190)
                ).forEach { (value, label) ->
                    FilterChip(
                        selected = s.layout == value,
                        onClick = { viewModel.updateSettings { it.copy(layout = value) } },
                        label = { Text(label) }
                    )
                }
            }
        }

        Button(onClick = viewModel::saveSettings, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.str_066))
        }
    }
}

// ---------------------------------------------------------------------------
// تب ۴: بارکد
// ---------------------------------------------------------------------------

@Composable
internal fun BarcodeTab(state: SettingsUiState, viewModel: SettingsViewModel) {
    val s = state.settings
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionCard(stringResource(R.string.str_191)) {
            Text(
                stringResource(R.string.str_192),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = s.dupBarcode == SettingsEntity.DUP_BLOCK,
                    onClick = {
                        viewModel.updateSettings { it.copy(dupBarcode = SettingsEntity.DUP_BLOCK) }
                    },
                    label = { Text(stringResource(R.string.str_193)) }
                )
                FilterChip(
                    selected = s.dupBarcode == SettingsEntity.DUP_WARN,
                    onClick = {
                        viewModel.updateSettings { it.copy(dupBarcode = SettingsEntity.DUP_WARN) }
                    },
                    label = { Text(stringResource(R.string.str_194)) }
                )
            }
        }

        SectionCard(stringResource(R.string.str_195)) {
            Text(
                stringResource(R.string.str_196),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = s.invalidBarcode == SettingsEntity.INVALID_WARN,
                    onClick = {
                        viewModel.updateSettings { it.copy(invalidBarcode = SettingsEntity.INVALID_WARN) }
                    },
                    label = { Text(stringResource(R.string.str_194)) }
                )
                FilterChip(
                    selected = s.invalidBarcode == SettingsEntity.INVALID_IGNORE,
                    onClick = {
                        viewModel.updateSettings { it.copy(invalidBarcode = SettingsEntity.INVALID_IGNORE) }
                    },
                    label = { Text(stringResource(R.string.str_197)) }
                )
            }
        }

        Button(onClick = viewModel::saveSettings, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.str_066))
        }
    }
}
