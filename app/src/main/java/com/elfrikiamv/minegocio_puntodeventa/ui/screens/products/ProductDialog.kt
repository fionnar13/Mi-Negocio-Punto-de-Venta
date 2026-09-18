package com.elfrikiamv.minegocio_puntodeventa.ui.screens.products

// ProductDialog.kt — دیالوگ افزودن/ویرایش کالا

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.elfrikiamv.minegocio_puntodeventa.R
import com.elfrikiamv.minegocio_puntodeventa.data.entity.ProductEntity
import com.elfrikiamv.minegocio_puntodeventa.scanner.BarcodeScanCallback
import com.elfrikiamv.minegocio_puntodeventa.scanner.BarcodeScanResult
import com.elfrikiamv.minegocio_puntodeventa.scanner.BarcodeScannerCallbackHost
import com.elfrikiamv.minegocio_puntodeventa.scanner.ScannerActivity
import com.elfrikiamv.minegocio_puntodeventa.utils.NumberUtils
import androidx.compose.ui.res.stringResource

/** واحدهای کالا (u/c/kg/g) با برچسب فارسی. */
enum class ProductUnit(val code: String, val faLabel: String) {
    PIECE("u", "عدد"),
    CARTON("c", "کارتن"),
    KILOGRAM("kg", "کیلوگرم"),
    GRAM("g", "گرم");

    companion object {
        fun fromCode(code: String): ProductUnit =
            entries.find { it.code == code } ?: PIECE
    }
}

/**
 * دیالوگ افزودن/ویرایش کالا با همهٔ فیلدها:
 * نام، بارکد (با دکمهٔ اسکن)، دسته‌بندی/زیردسته/برند (اتوکامپلیت)،
 * قیمت خرید/فروش، موجودی، حداقل موجودی، واحد و انتخاب تصویر.
 *
 * @param product کالای در حال ویرایش (id == 0 برای کالای جدید).
 */
@Composable
fun ProductDialog(
    product: ProductEntity,
    catOptions: List<String>,
    subCatOptions: List<String>,
    brandOptions: List<String>,
    onDismiss: () -> Unit,
    onSave: (ProductEntity) -> Unit
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf(product.name) }
    var barcode by remember { mutableStateOf(product.barcode) }
    var cat by remember { mutableStateOf(product.cat) }
    var subCat by remember { mutableStateOf(product.subCat) }
    var brand by remember { mutableStateOf(product.brand) }
    var buyC by remember { mutableStateOf(product.buyC.asInput()) }
    var sellC by remember { mutableStateOf(product.sellC.asInput()) }
    var stock by remember { mutableStateOf(product.stock.asInput()) }
    var min by remember { mutableStateOf(product.min.asInput()) }
    var unit by remember { mutableStateOf(ProductUnit.fromCode(product.baseUnit)) }
    var img by remember { mutableStateOf(product.img) }

    // انتخاب تصویر از گالری (سند پایدار تا بعد از ری‌استارت قابل خواندن باشد)
    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            img = uri.toString()
        }
    }

    // اسکن بارکد با ScannerActivity (ZXing)
    val scanLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /* نتیجه از طریق BarcodeScanCallback دریافت می‌شود */ }

    val scanCallback = remember {
        object : BarcodeScanCallback {
            override fun onBarcodeScanned(result: BarcodeScanResult) {
                barcode = result.barcode
            }
        }
    }
    DisposableEffect(Unit) {
        BarcodeScannerCallbackHost.bind(scanCallback)
        onDispose { BarcodeScannerCallbackHost.unbind(scanCallback) }
    }

    val isValid = name.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product.id == 0L) stringResource(R.string.str_106) else stringResource(R.string.str_107)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.str_108)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // بارکد + دکمهٔ اسکن
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = barcode,
                        onValueChange = { barcode = it },
                        label = { Text(stringResource(R.string.str_109)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        scanLauncher.launch(ScannerActivity.createIntent(context))
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.outline_barcode_scanner_24),
                            contentDescription = stringResource(R.string.str_110)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AutocompleteField(
                        label = stringResource(R.string.str_111),
                        value = cat,
                        options = catOptions,
                        onChange = { cat = it },
                        modifier = Modifier.weight(1f)
                    )
                    AutocompleteField(
                        label = stringResource(R.string.str_112),
                        value = subCat,
                        options = subCatOptions,
                        onChange = { subCat = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AutocompleteField(
                        label = stringResource(R.string.str_113),
                        value = brand,
                        options = brandOptions,
                        onChange = { brand = it },
                        modifier = Modifier.weight(1f)
                    )
                    UnitDropdown(
                        selected = unit,
                        onSelect = { unit = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField(
                        label = stringResource(R.string.str_114),
                        value = buyC,
                        onValueChange = { buyC = it },
                        modifier = Modifier.weight(1f)
                    )
                    NumberField(
                        label = stringResource(R.string.str_115),
                        value = sellC,
                        onValueChange = { sellC = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField(
                        label = stringResource(R.string.str_116),
                        value = stock,
                        onValueChange = { stock = it },
                        modifier = Modifier.weight(1f)
                    )
                    NumberField(
                        label = stringResource(R.string.str_117),
                        value = min,
                        onValueChange = { min = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                // انتخاب تصویر
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProductThumb(img = img, size = 56.dp)
                    OutlinedButton(onClick = { imagePicker.launch(arrayOf("image/*")) }) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_camera_alt_24),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.str_118))
                    }
                    if (img != null) {
                        IconButton(onClick = { img = null }) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_close_24),
                                contentDescription = stringResource(R.string.str_119)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = isValid,
                onClick = {
                    onSave(
                        product.copy(
                            name = name.trim(),
                            barcode = barcode.trim(),
                            cat = cat.trim(),
                            subCat = subCat.trim(),
                            brand = brand.trim(),
                            buyC = buyC.parseDoubleOrZero(),
                            sellC = sellC.parseDoubleOrZero(),
                            stock = stock.parseDoubleOrZero(),
                            min = min.parseDoubleOrZero(),
                            baseUnit = unit.code,
                            buyU = unit.code,
                            sellU = unit.code,
                            img = img
                        )
                    )
                }
            ) { Text(stringResource(R.string.str_066)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.str_014)) }
        }
    )
}

/** فیلد متنی با اتوکامپلیت (دسته‌بندی/زیردسته/برند) — مقدار آزاد هم پذیرفته می‌شود. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AutocompleteField(
    label: String,
    value: String,
    options: List<String>,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            label = { Text(label) },
            singleLine = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            val matches = options.filter {
                value.isBlank() || it.contains(value, ignoreCase = true)
            }
            matches.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

/** انتخاب واحد از میان گزینه‌های ثابت. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitDropdown(
    selected: ProductUnit,
    onSelect: (ProductUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected.faLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.str_002)) },
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
            ProductUnit.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.faLabel) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

/** فیلد عددی (اعشار مجاز؛ ارقام فارسی/لاتین و ممیز فارسی پذیرفته می‌شود). */
@Composable
private fun NumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            if (input.matches(Regex("\\d*[.,٫]?\\d*"))) onValueChange(input)
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier
    )
}

// ---- کمک‌تابع‌های عددی (مشترک با ProductsScreen) ----

/** نمایش ورودی عددی: صفر → خالی، بدون اعشار اضافی. */
internal fun Double.asInput(): String = when {
    this == 0.0 -> ""
    this % 1.0 == 0.0 -> toLong().toString()
    else -> toString()
}

/** تبدیل ورودی کاربر به عدد؛ ارقام فارسی و ممیز فارسی پشتیبانی می‌شوند. */
internal fun String.parseDoubleOrZero(): Double =
    trim().let { NumberUtils.toLatin(it) }
        .replace(',', '.')
        .replace('٫', '.')
        .toDoubleOrNull() ?: 0.0

/** قالب‌بندی عدد برای نمایش با ارقام فارسی. */
internal fun Double.faFormat(): String {
    val plain = if (this % 1.0 == 0.0) {
        toLong().toString()
    } else {
        java.math.BigDecimal.valueOf(this).stripTrailingZeros().toPlainString()
    }
    return NumberUtils.toPersian(plain)
}
