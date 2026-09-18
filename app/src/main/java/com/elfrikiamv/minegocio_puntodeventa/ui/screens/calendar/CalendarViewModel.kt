package com.elfrikiamv.minegocio_puntodeventa.ui.screens.calendar

// CalendarViewModel.kt — منطق صفحهٔ تقویم و رویدادها

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.elfrikiamv.minegocio_puntodeventa.data.entity.EventEntity
import com.elfrikiamv.minegocio_puntodeventa.data.local.SazmanDatabase
import com.elfrikiamv.minegocio_puntodeventa.data.repository.EventRepository
import com.elfrikiamv.minegocio_puntodeventa.utils.JalaliDateUtils
import com.elfrikiamv.minegocio_puntodeventa.utils.NumberUtils
import com.elfrikiamv.minegocio_puntodeventa.utils.PersianFormat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** وضعیت صفحهٔ تقویم. */
data class CalendarUiState(
    val year: Int = 1405,
    val month: Int = 1,
    /** عنوان ماه + سال، مثل «مهر ۱۴۰۵». */
    val monthTitle: String = "",
    val daysInMonth: Int = 30,
    /** روز هفتهٔ روز اول ماه (۰ = شنبه … ۶ = جمعه). */
    val firstWeekday: Int = 0,
    /** کلید تاریخ امروز ("1405/06/27"). */
    val todayKey: String = "",
    /** رویدادها به تفکیک تاریخ. */
    val eventsByDate: Map<String, List<EventEntity>> = emptyMap(),
    /** ۸ رویداد پیش‌رو (از امروز به بعد). */
    val upcoming: List<EventEntity> = emptyList()
)

/**
 * ViewModel تقویم: ناوبری ماه، رویدادهای هر روز و رویدادهای پیش‌رو.
 */
class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val eventRepo = EventRepository(SazmanDatabase.getDatabase(application).eventDao())

    private val now = JalaliDateUtils.now()

    private val _yearMonth = MutableStateFlow(now.getShYear() to now.getShMonth())

    val uiState: StateFlow<CalendarUiState> = combine(
        _yearMonth,
        eventRepo.getAll()
    ) { (year, month), events ->
        val todayKey = PersianFormat.todayDateString()
        val daysInMonth = JalaliDateUtils.fromJalali(year, month, 1).getMonthDays()
        CalendarUiState(
            year = year,
            month = month,
            monthTitle = "${JalaliDateUtils.monthName(month)} ${NumberUtils.toPersian(year)}",
            daysInMonth = daysInMonth,
            firstWeekday = JalaliDateUtils.fromJalali(year, month, 1).dayOfWeek(),
            todayKey = todayKey,
            eventsByDate = events.groupBy { it.date },
            upcoming = events
                .filter { it.date >= todayKey }
                .sortedWith(compareBy({ it.date }, { it.time }))
                .take(8)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarUiState())

    /** ماه قبل. */
    fun prevMonth() {
        _yearMonth.value = _yearMonth.value.let { (y, m) ->
            if (m == 1) y - 1 to 12 else y to m - 1
        }
    }

    /** ماه بعد. */
    fun nextMonth() {
        _yearMonth.value = _yearMonth.value.let { (y, m) ->
            if (m == 12) y + 1 to 1 else y to m + 1
        }
    }

    /** افزودن رویداد. */
    fun addEvent(title: String, date: String, time: String, color: Int, reminder: Boolean) {
        viewModelScope.launch {
            eventRepo.insert(
                EventEntity(
                    title = title.trim(),
                    date = date,
                    time = time,
                    color = color,
                    reminder = reminder
                )
            )
        }
    }

    /** حذف رویداد. */
    fun deleteEvent(event: EventEntity) {
        viewModelScope.launch { eventRepo.delete(event) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                CalendarViewModel(app)
            }
        }
    }
}
