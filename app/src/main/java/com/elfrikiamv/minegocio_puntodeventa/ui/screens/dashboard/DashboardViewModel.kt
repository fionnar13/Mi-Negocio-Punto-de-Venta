package com.elfrikiamv.minegocio_puntodeventa.ui.screens.dashboard

// DashboardViewModel.kt — منطق صفحهٔ داشبورد

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.elfrikiamv.minegocio_puntodeventa.data.entity.ProductEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.SaleEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.SessionEntity
import com.elfrikiamv.minegocio_puntodeventa.data.local.SazmanDatabase
import com.elfrikiamv.minegocio_puntodeventa.data.repository.CustomerRepository
import com.elfrikiamv.minegocio_puntodeventa.data.repository.LedgerEntryRepository
import com.elfrikiamv.minegocio_puntodeventa.data.repository.ProductRepository
import com.elfrikiamv.minegocio_puntodeventa.data.repository.SaleRepository
import com.elfrikiamv.minegocio_puntodeventa.data.repository.SessionRepository
import com.elfrikiamv.minegocio_puntodeventa.utils.JalaliDateUtils
import com.elfrikiamv.minegocio_puntodeventa.utils.NumberUtils
import com.elfrikiamv.minegocio_puntodeventa.utils.PersianFormat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import com.elfrikiamv.minegocio_puntodeventa.R

/** وضعیت داشبورد. */
data class DashboardUiState(
    val loading: Boolean = true,
    // شیفت
    val sessionActive: Boolean = false,
    val sessionLabel: String = "",
    val sessionStartCash: Double = 0.0,
    // KPIها
    val todaySales: Double = 0.0,
    val monthProfit: Double = 0.0,
    val inventoryValue: Double = 0.0,
    val receivables: Double = 0.0,
    val cash: Double = 0.0,
    val bank: Double = 0.0,
    // نمودارها
    val weekly: List<Pair<String, Double>> = emptyList(),
    val hourly: List<Pair<String, Double>> = emptyList(),
    val byCategory: List<Pair<String, Double>> = emptyList(),
    val bySubCategory: List<Pair<String, Double>> = emptyList(),
    val byBrand: List<Pair<String, Double>> = emptyList(),
    // فهرست‌ها
    val lowStock: List<ProductEntity> = emptyList(),
    val recentSales: List<SaleEntity> = emptyList(),
    val customerNames: Map<Long, String> = emptyMap(),
    val message: String? = null
)

/**
 * ViewModel داشبورد — همهٔ داده‌ها با refresh() بارگذاری می‌شوند.
 *
 * شیفت «فعال» یعنی آخرین نشست امروز شروع شده باشد (مدل نشست فیلد بستن ندارد).
 */
class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val database = SazmanDatabase.getDatabase(application)
    private val sessionRepo = SessionRepository(database.sessionDao())
    private val saleRepo = SaleRepository(database.saleDao())
    private val productRepo = ProductRepository(database.productDao())
    private val ledgerRepo = LedgerEntryRepository(database.ledgerEntryDao())
    private val customerRepo = CustomerRepository(database.customerDao())

    private val _state = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _state

    init {
        refresh()
    }

    fun onMessageShown() {
        _state.value = _state.value.copy(message = null)
    }

    /** بارگذاری/بروزرسانی همهٔ داده‌های داشبورد. */
    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)

            val today = PersianFormat.todayDateString()
            val p = JalaliDateUtils.now()
            val monthStart = PersianFormat.jalaliString(p.getShYear(), p.getShMonth(), 1)

            // ---- شیفت ----
            val latest: SessionEntity? = sessionRepo.getLatest()
            // شیفت فعال = آخرین نشست هنوز بسته نشده باشد (هماهنگ با تب دستگاه‌ها)
            val sessionActive = latest != null && latest.endTime == null
            val sessionLabel = if (sessionActive && latest != null) {
                val pd = JalaliDateUtils.fromTimestamp(latest.startTime)
                "شیفت فعال از ${PersianFormat.displayTime(
                    "%02d:%02d".format(pd.getHour(), pd.getMinute())
                )}"
            } else {
                getApplication<Application>().getString(R.string.str_286)
            }

            // ---- KPIها ----
            val todaySales = saleRepo.totalGrandBetween(today, today)
            val monthSales = saleRepo.getBetween(monthStart, today)
            val products = productRepo.getAllOnce()
            val productMap = products.associateBy { it.id }
            var monthProfit = 0.0
            monthSales.forEach { sale ->
                sale.items.forEach { item ->
                    val buy = productMap[item.productId]?.buyC ?: 0.0
                    monthProfit += (item.price - buy) * item.qty
                }
            }
            val inventoryValue = products.sumOf { it.stock * it.buyC }
            val receivables = ledgerRepo.totalReceivables().first()
            val cash = ledgerRepo.totalCashBalance().first()
            val bank = ledgerRepo.totalBankBalance().first()

            // ---- نمودار هفتگی (۷ روز اخیر) ----
            val cal = Calendar.getInstance()
            val weekDates = mutableListOf<String>()
            repeat(7) {
                val pd = JalaliDateUtils.fromTimestamp(cal.timeInMillis)
                weekDates += PersianFormat.jalaliString(
                    pd.getShYear(), pd.getShMonth(), pd.getShDay()
                )
                cal.add(Calendar.DAY_OF_YEAR, -1)
            }
            weekDates.reverse() // قدیمی → جدید
            val weekSalesMap = saleRepo
                .getBetween(weekDates.first(), today)
                .groupBy { it.date }
                .mapValues { (_, v) -> v.sumOf { it.grand } }
            val weekly = weekDates.map { d ->
                val pd = JalaliDateUtils.fromJalali(
                    d.substring(0, 4).toInt(), d.substring(5, 7).toInt(), d.substring(8, 10).toInt()
                )
                val label = "${NumberUtils.toPersian(pd.getShDay())} ${JalaliDateUtils.monthName(pd.getShMonth())}"
                label to (weekSalesMap[d] ?: 0.0)
            }

            // ---- نمودار ساعتی (امروز) ----
            val todaySalesList = saleRepo.getBetween(today, today)
            val hourly = (0 until 24).mapNotNull { h ->
                val total = todaySalesList
                    .filter { it.time.take(2).toIntOrNull() == h }
                    .sumOf { it.grand }
                if (total > 0.0) NumberUtils.toPersian(h) to total else null
            }

            // ---- نمودار دسته/زیردسته/برند (ماه جاری) ----
            fun groupBy(selector: (ProductEntity) -> String): List<Pair<String, Double>> =
                monthSales
                    .flatMap { it.items }
                    .groupBy { item -> productMap[item.productId]?.let(selector).orEmpty() }
                    .filterKeys { it.isNotBlank() }
                    .map { (k, v) -> k to v.sumOf { it.total } }
                    .sortedByDescending { it.second }
                    .take(8)

            val byCategory = groupBy { it.cat }
            val bySubCategory = groupBy { it.subCat }
            val byBrand = groupBy { it.brand }

            // ---- فهرست‌ها ----
            val lowStock = products.filter { it.stock <= it.min }.sortedBy { it.name }
            val recentSales = saleRepo.getAll().first().take(6)
            val customerNames = customerRepo.getAllOnce()
                .associate { it.id to it.name }

            _state.value = DashboardUiState(
                loading = false,
                sessionActive = sessionActive,
                sessionLabel = sessionLabel,
                sessionStartCash = latest?.startCash ?: 0.0,
                todaySales = todaySales,
                monthProfit = monthProfit,
                inventoryValue = inventoryValue,
                receivables = receivables,
                cash = cash,
                bank = bank,
                weekly = weekly,
                hourly = hourly,
                byCategory = byCategory,
                bySubCategory = bySubCategory,
                byBrand = byBrand,
                lowStock = lowStock,
                recentSales = recentSales,
                customerNames = customerNames
            )
        }
    }

    /** شروع شیفت جدید با موجودی اولیهٔ صندوق. */
    fun startShift(startCash: Double) {
        viewModelScope.launch {
            sessionRepo.insert(
                SessionEntity(
                    startTime = System.currentTimeMillis(),
                    startCash = startCash
                )
            )
            _state.value = _state.value.copy(message = getApplication<Application>().getString(R.string.str_320))
            refresh()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                DashboardViewModel(app)
            }
        }
    }
}
