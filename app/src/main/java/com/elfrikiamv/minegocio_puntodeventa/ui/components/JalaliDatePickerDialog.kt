package com.elfrikiamv.minegocio_puntodeventa.ui.components

// JalaliDatePickerDialog.kt — انتخابگر تاریخ شمسی

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elfrikiamv.minegocio_puntodeventa.utils.JalaliDateUtils
import com.elfrikiamv.minegocio_puntodeventa.utils.NumberUtils
import androidx.compose.ui.res.stringResource
import com.elfrikiamv.minegocio_puntodeventa.R

/**
 * دیالوگ انتخاب تاریخ شمسی.
 *
 * @param onDateSelected با انتخاب روز فراخوانی می‌شود: (سال، ماه، روز).
 */
@Composable
fun JalaliDatePickerDialog(
    initialYear: Int,
    initialMonth: Int,
    initialDay: Int,
    onDateSelected: (year: Int, month: Int, day: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var year by remember { mutableStateOf(initialYear) }
    var month by remember { mutableStateOf(initialMonth) }

    val today = remember { JalaliDateUtils.now() }
    val todayY = today.getShYear()
    val todayM = today.getShMonth()
    val todayD = today.getShDay()

    val monthDays = remember(year, month) {
        JalaliDateUtils.fromJalali(year, month, 1).getMonthDays()
    }
    // روز هفتهٔ روز اول ماه (0 = شنبه)
    val firstDayOfWeek = remember(year, month) {
        JalaliDateUtils.fromJalali(year, month, 1).dayOfWeek()
    }

    fun prevMonth() {
        if (month == 1) { month = 12; year -= 1 } else month -= 1
    }

    fun nextMonth() {
        if (month == 12) { month = 1; year += 1 } else month += 1
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.str_009)) },
        text = {
            Column {
                // ناوبری ماه و سال
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = { prevMonth() }) { Text(stringResource(R.string.str_010)) }
                    Text(
                        text = "${JalaliDateUtils.monthName(month)} ${NumberUtils.toPersian(year)}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    TextButton(onClick = { nextMonth() }) { Text(stringResource(R.string.str_011)) }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = { year -= 1 }) { Text(stringResource(R.string.str_012)) }
                    Spacer(modifier = Modifier.size(8.dp))
                    TextButton(onClick = { year += 1 }) { Text(stringResource(R.string.str_013)) }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // سرستون روزهای هفته (شنبه … جمعه)
                Row(modifier = Modifier.fillMaxWidth()) {
                    JalaliDateUtils.DAY_NAMES.forEach { name ->
                        Text(
                            text = name.take(1), // حرف اول: ش ی د س چ پ ج
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // شبکهٔ روزها
                val cells: List<Int?> = List(firstDayOfWeek) { null } + (1..monthDays).toList()
                cells.chunked(7).forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        week.forEach { day ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                if (day != null) {
                                    val isToday = year == todayY && month == todayM && day == todayD
                                    val isInitial = year == initialYear &&
                                        month == initialMonth && day == initialDay
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1f)
                                            .background(
                                                when {
                                                    isInitial -> MaterialTheme.colorScheme.primary
                                                    isToday -> MaterialTheme.colorScheme.primaryContainer
                                                    else -> androidx.compose.ui.graphics.Color.Transparent
                                                },
                                                CircleShape
                                            )
                                            .border(
                                                width = if (isToday) 1.dp else 0.dp,
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                onDateSelected(year, month, day)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = NumberUtils.toPersian(day),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = when {
                                                isInitial -> MaterialTheme.colorScheme.onPrimary
                                                else -> MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        // تکمیل هفتهٔ آخر با سلول‌های خالی
                        repeat(7 - week.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.str_014)) }
        }
    )
}
