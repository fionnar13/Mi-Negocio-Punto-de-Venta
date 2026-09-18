package com.elfrikiamv.minegocio_puntodeventa.ui.screens.calendar

// CalendarScreen.kt — تقویم شمسی با رویدادها

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elfrikiamv.minegocio_puntodeventa.R
import com.elfrikiamv.minegocio_puntodeventa.data.entity.EventEntity
import com.elfrikiamv.minegocio_puntodeventa.ui.components.TimePickerDialog
import com.elfrikiamv.minegocio_puntodeventa.utils.NumberUtils
import com.elfrikiamv.minegocio_puntodeventa.utils.PersianFormat
import androidx.compose.ui.res.stringResource

/** عناوین ستون‌های هفته (شنبه در راست). */
private val WEEK_HEADERS = listOf(
    R.string.wd_sat, R.string.wd_sun, R.string.wd_mon, R.string.wd_tue,
    R.string.wd_wed, R.string.wd_thu, R.string.wd_fri
)

/** پالت رنگ رویداد. */
private val EVENT_COLORS = listOf(
    0xFF6366F1.toInt(), 0xFF22D3EE.toInt(), 0xFFD4AF37.toInt(),
    0xFFF87171.toInt(), 0xFF34D399.toInt(), 0xFF818CF8.toInt()
)

/**
 * صفحهٔ تقویم: سرصفحهٔ ماه، شبکهٔ ۷ ستونه، نقطه‌های رویداد،
 * دیالوگ روز و بخش «پیش‌رو».
 */
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = viewModel(factory = CalendarViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsState()
    var selectedDate by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ---- سرصفحهٔ ماه ----
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    IconButton(onClick = viewModel::prevMonth) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_chevron_left_24),
                            contentDescription = stringResource(R.string.str_010)
                        )
                    }
                    Text(
                        text = state.monthTitle,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    IconButton(onClick = viewModel::nextMonth) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_chevron_right_24),
                            contentDescription = stringResource(R.string.str_011)
                        )
                    }
                }
            }
        }

        // ---- شبکهٔ تقویم ----
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    // سرستون‌های هفته
                    Row(modifier = Modifier.fillMaxWidth()) {
                        WEEK_HEADERS.forEach { h ->
                            Text(
                                text = stringResource(h),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    // ردیف‌های ۷تایی
                    val cells: List<Int?> =
                        List(state.firstWeekday) { null } + (1..state.daysInMonth).toList()
                    cells.chunked(7).forEach { week ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            (0 until 7).forEach { col ->
                                val day = week.getOrNull(col)
                                if (day == null) {
                                    Spacer(modifier = Modifier.weight(1f))
                                } else {
                                    DayCell(
                                        day = day,
                                        events = state.eventsByDate[
                                            PersianFormat.jalaliString(state.year, state.month, day)
                                        ].orEmpty(),
                                        isToday = PersianFormat.jalaliString(
                                            state.year, state.month, day
                                        ) == state.todayKey,
                                        onClick = {
                                            selectedDate = PersianFormat.jalaliString(
                                                state.year, state.month, day
                                            )
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ---- پیش‌رو ----
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(stringResource(R.string.str_067), style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    if (state.upcoming.isEmpty()) {
                        Text(
                            stringResource(R.string.str_068),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        state.upcoming.forEach { event ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(Color(event.color), CircleShape)
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(
                                    text = event.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = buildString {
                                        append(PersianFormat.displayDate(event.date))
                                        if (event.time.isNotBlank()) {
                                            append(" · ").append(PersianFormat.displayTime(event.time))
                                        }
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ---- دیالوگ روز انتخاب‌شده ----
    selectedDate?.let { date ->
        DayDialog(
            date = date,
            events = state.eventsByDate[date].orEmpty(),
            onDismiss = { selectedDate = null },
            onDelete = viewModel::deleteEvent,
            onAdd = { title, time, color, reminder ->
                viewModel.addEvent(title, date, time, color, reminder)
            }
        )
    }
}

/** یک خانهٔ روز تقویم. */
@Composable
private fun DayCell(
    day: Int,
    events: List<EventEntity>,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .then(
                if (isToday) {
                    Modifier.border(
                        BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        RoundedCornerShape(10.dp)
                    )
                } else Modifier
            )
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = NumberUtils.toPersian(day),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isToday) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        // نقطه‌های رویداد: حداکثر ۳ + «+N»
        val shown = events.take(3)
        val more = events.size - shown.size
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            shown.forEach { e ->
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Color(e.color), CircleShape)
                )
            }
            if (more > 0) {
                Text(
                    text = "+${NumberUtils.toPersian(more)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** دیالوگ روز: فهرست رویدادها + فرم افزودن. */
@Composable
private fun DayDialog(
    date: String,
    events: List<EventEntity>,
    onDismiss: () -> Unit,
    onDelete: (EventEntity) -> Unit,
    onAdd: (title: String, time: String, color: Int, reminder: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var colorIndex by remember { mutableStateOf(0) }
    var reminder by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(PersianFormat.displayDate(date)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // رویدادهای این روز
                if (events.isNotEmpty()) {
                    events.forEach { event ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color(event.color), CircleShape)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = buildString {
                                    append(event.title)
                                    if (event.time.isNotBlank()) {
                                        append(" · ").append(PersianFormat.displayTime(event.time))
                                    }
                                    if (event.reminder) append(" · 🔔")
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { onDelete(event) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_delete_24),
                                    contentDescription = stringResource(R.string.str_006),
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // فرم افزودن رویداد
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.str_069)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = if (time.isBlank()) "" else PersianFormat.displayTime(time),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.str_070)) },
                    placeholder = { Text("—") },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { showTimePicker = true }) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_access_time_24),
                                contentDescription = null
                            )
                        }
                    }
                )
                // انتخاب رنگ
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.str_071), style = MaterialTheme.typography.labelMedium)
                    EVENT_COLORS.forEachIndexed { i, c ->
                        Box(
                            modifier = Modifier
                                .size(if (i == colorIndex) 26.dp else 20.dp)
                                .background(Color(c), CircleShape)
                                .then(
                                    if (i == colorIndex) {
                                        Modifier.border(
                                            BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface),
                                            CircleShape
                                        )
                                    } else Modifier
                                )
                                .clickable { colorIndex = i }
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.str_072), style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = reminder, onCheckedChange = { reminder = it })
                }
            }
        },
        confirmButton = {
            TextButton(enabled = title.isNotBlank(), onClick = {
                onAdd(title.trim(), time, EVENT_COLORS[colorIndex], reminder)
                title = ""
                time = ""
                reminder = false
            }) { Text(stringResource(R.string.str_073)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.str_074)) } }
    )

    if (showTimePicker) {
        val parts = time.split(":").map { it.toIntOrNull() ?: 0 }
        TimePickerDialog(
            initialHour = parts.getOrElse(0) { 9 },
            initialMinute = parts.getOrElse(1) { 0 },
            onSelected = { h, m ->
                time = "%02d:%02d".format(h, m)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}
