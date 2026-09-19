package com.elfrikiamv.minegocio_puntodeventa.ui.components

// CartTable.kt — جدول اقلام فاکتور (مشترک فروش و خرید)

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.elfrikiamv.minegocio_puntodeventa.R
import com.elfrikiamv.minegocio_puntodeventa.utils.PersianFormat
import androidx.compose.ui.res.stringResource

/** قلم فاکتور در سبد. */
data class CartItem(
    val productId: Long,
    val name: String,
    val unit: String,
    val qty: Double,
    val price: Double,
    val isWeight: Boolean = false
) {
    /** جمع ردیف: تعداد × فی. */
    val total: Double get() = qty * price
}

/**
 * جدول اقلام فاکتور با ستون‌های
 * «کالا / واحد / تعداد / فی / جمع / حذف».
 *
 * تعداد با دکمه‌های + و − تغییر می‌کند (گام وزنی: 0/1).
 */
@Composable
fun CartTable(
    items: List<CartItem>,
    onIncrement: (CartItem) -> Unit,
    onDecrement: (CartItem) -> Unit,
    onRemove: (CartItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        item {
            // سرجدول
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeaderCell(stringResource(R.string.str_001), 2.6f)
                HeaderCell(stringResource(R.string.str_002), 0.8f)
                HeaderCell(stringResource(R.string.str_003), 1.7f)
                HeaderCell(stringResource(R.string.str_004), 1.2f)
                HeaderCell(stringResource(R.string.str_005), 1.2f)
                HeaderCell(stringResource(R.string.str_006), 0.5f)
            }
        }

        items(items, key = { "${it.productId}-${it.unit}" }) { item ->
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // کالا
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(2.6f)
                        .padding(end = 4.dp)
                )
                // واحد
                Text(
                    text = unitLabel(item.unit),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(0.8f)
                )
                // تعداد (+ −)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1.7f)
                ) {
                    IconButton(
                        onClick = { onDecrement(item) },
                        modifier = Modifier.padding(0.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_remove_24),
                            contentDescription = stringResource(R.string.str_007),
                            modifier = Modifier.padding(0.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = PersianFormat.qty(item.qty),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                    IconButton(
                        onClick = { onIncrement(item) },
                        modifier = Modifier.padding(0.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_add_24),
                            contentDescription = stringResource(R.string.str_008),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                // فی
                Text(
                    text = PersianFormat.amount(item.price),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1.2f)
                )
                // جمع
                Text(
                    text = PersianFormat.amount(item.total),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1.2f)
                )
                // حذف
                IconButton(
                    onClick = { onRemove(item) },
                    modifier = Modifier.weight(0.5f)
                ) {
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

@Composable
private fun RowScope.HeaderCell(label: String, weight: Float) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.weight(weight)
    )
}

/** برچسب فارسی واحد. */
fun unitLabel(unit: String): String = when (unit) {
    "u" -> "عدد"
    "c" -> "کارتن"
    "kg" -> "کیلو"
    "g" -> "گرم"
    else -> unit
}
