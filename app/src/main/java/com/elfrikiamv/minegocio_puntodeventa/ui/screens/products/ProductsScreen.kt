package com.elfrikiamv.minegocio_puntodeventa.ui.screens.products

// ProductsScreen.kt — صفحهٔ کالاها (جستجو، فیلتر، فهرست، افزودن/ویرایش/حذف)

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elfrikiamv.minegocio_puntodeventa.R
import com.elfrikiamv.minegocio_puntodeventa.data.entity.ProductEntity
import androidx.compose.ui.res.stringResource

/**
 * صفحهٔ مدیریت کالاها.
 *
 * - نوار جستجو (نام / بارکد / کد)
 * - ردیف فیلتر: دسته، زیردسته، برند
 * - فهرست کالاها (LazyColumn) با دکمهٔ ویرایش و حذف در هر ردیف
 * - دکمهٔ شناور «+ کالای جدید»
 */
@Composable
fun ProductsScreen(
    viewModel: ProductsViewModel = viewModel(factory = ProductsViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<ProductEntity?>(null) }
    var deleting by remember { mutableStateOf<ProductEntity?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            SearchBar(query = state.query, onQueryChange = viewModel::onQueryChange)

            FilterRow(
                state = state,
                onCatChange = viewModel::onCatChange,
                onSubCatChange = viewModel::onSubCatChange,
                onBrandChange = viewModel::onBrandChange
            )

            val hasFilters = state.query.isNotBlank() ||
                state.cat != null || state.subCat != null || state.brand != null

            if (state.products.isEmpty()) {
                EmptyState(hasFilters = hasFilters)
            } else {
                ProductList(
                    products = state.products,
                    onEdit = { product ->
                        editing = product
                        showDialog = true
                    },
                    onDelete = { product -> deleting = product }
                )
            }
        }

        // دکمهٔ شناور افزودن کالا (در RTL سمت راست پایین)
        ExtendedFloatingActionButton(
            onClick = {
                editing = null
                showDialog = true
            },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.baseline_add_24),
                    contentDescription = null
                )
            },
            text = { Text(stringResource(R.string.str_106)) },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        )
    }

    // دیالوگ افزودن/ویرایش کالا
    if (showDialog) {
        ProductDialog(
            product = editing ?: ProductEntity(),
            catOptions = state.cats,
            subCatOptions = state.subCats,
            brandOptions = state.brands,
            onDismiss = { showDialog = false },
            onSave = { product ->
                viewModel.saveProduct(product)
                showDialog = false
            }
        )
    }

    // دیالوگ تأیید حذف
    deleting?.let { product ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text(stringResource(R.string.str_120)) },
            text = { Text(stringResource(R.string.str_055, product.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteProduct(product)
                        deleting = null
                    }
                ) {
                    Text(stringResource(R.string.str_006), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleting = null }) { Text(stringResource(R.string.str_014)) }
            }
        )
    }
}

/** فهرست کالاها. */
@Composable
private fun ProductList(
    products: List<ProductEntity>,
    onEdit: (ProductEntity) -> Unit,
    onDelete: (ProductEntity) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(products, key = { it.id }) { product ->
            ProductRow(product = product, onEdit = onEdit, onDelete = onDelete)
        }
    }
}

/** ردیف کالا: تصویر، نام، کد/بارکد، قیمت فروش، موجودی + دکمه‌های ویرایش/حذف. */
@Composable
private fun ProductRow(
    product: ProductEntity,
    onEdit: (ProductEntity) -> Unit,
    onDelete: (ProductEntity) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            ProductThumb(img = product.img)
            Spacer(modifier = Modifier.size(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val codeLine = buildString {
                    if (product.code.isNotBlank()) append(stringResource(R.string.str_121, product.code))
                    if (product.barcode.isNotBlank()) {
                        if (isNotEmpty()) append(" · ")
                        append(stringResource(R.string.str_122, product.barcode))
                    }
                }
                if (codeLine.isNotEmpty()) {
                    Text(
                        text = codeLine,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.str_123, product.sellC.faFormat()),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                // موجودی: قرمز اگر به حداقل رسیده باشد
                val lowStock = product.stock <= product.min
                val unitLabel = ProductUnit.fromCode(product.baseUnit).faLabel
                Text(
                    text = stringResource(R.string.str_124, product.stock.faFormat(), unitLabel),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (lowStock) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                Row {
                    IconButton(onClick = { onEdit(product) }) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_edit_24),
                            contentDescription = stringResource(R.string.str_060),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { onDelete(product) }) {
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

/** نوار جستجو. */
@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text(stringResource(R.string.str_125)) },
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.baseline_search_24),
                contentDescription = null
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_close_24),
                        contentDescription = stringResource(R.string.str_126)
                    )
                }
            }
        },
        singleLine = true
    )
}

/** ردیف فیلترها: دسته، زیردسته، برند. */
@Composable
private fun FilterRow(
    state: ProductsUiState,
    onCatChange: (String?) -> Unit,
    onSubCatChange: (String?) -> Unit,
    onBrandChange: (String?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterDropdown(
            label = stringResource(R.string.str_127),
            selected = state.cat,
            options = state.cats,
            onSelect = onCatChange,
            modifier = Modifier.weight(1f)
        )
        FilterDropdown(
            label = stringResource(R.string.str_112),
            selected = state.subCat,
            options = state.subCats,
            onSelect = onSubCatChange,
            modifier = Modifier.weight(1f)
        )
        FilterDropdown(
            label = stringResource(R.string.str_113),
            selected = state.brand,
            options = state.brands,
            onSelect = onBrandChange,
            modifier = Modifier.weight(1f)
        )
    }
}

/** دراپ‌داون فیلتر (فقط-خواندنی) با گزینهٔ «همه». */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterDropdown(
    label: String,
    selected: String?,
    options: List<String>,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected ?: stringResource(R.string.str_036),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
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
            DropdownMenuItem(
                text = { Text(stringResource(R.string.str_036)) },
                onClick = {
                    onSelect(null)
                    expanded = false
                }
            )
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

/** حالت خالی: بدون کالا یا بدون نتیجه. */
@Composable
private fun EmptyState(hasFilters: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_package),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (hasFilters) stringResource(R.string.str_128) else stringResource(R.string.str_129),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (hasFilters) {
                stringResource(R.string.str_130)
            } else {
                stringResource(R.string.str_131)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
