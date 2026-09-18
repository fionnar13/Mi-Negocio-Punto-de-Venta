package com.elfrikiamv.minegocio_puntodeventa.ui.screens.reports

// ReportsViewModel.kt — منطق صفحهٔ گزارش‌ها (۱۱ گزارش)

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.elfrikiamv.minegocio_puntodeventa.data.entity.Payment
import com.elfrikiamv.minegocio_puntodeventa.data.entity.ScanLogEntity
import com.elfrikiamv.minegocio_puntodeventa.data.local.SazmanDatabase
import com.elfrikiamv.minegocio_puntodeventa.data.repository.CustomerRepository
import com.elfrikiamv.minegocio_puntodeventa.data.repository.ProductRepository
import com.elfrikiamv.minegocio_puntodeventa.data.repository.SaleRepository
import com.elfrikiamv.minegocio_puntodeventa.data.repository.ScanLogRepository
import com.elfrikiamv.minegocio_puntodeventa.data.repository.SessionRepository
import com.elfrikiamv.minegocio_puntodeventa.data.repository.UserRepository
import com.elfrikiamv.minegocio_puntodeventa.data.repository.VisitorRepository
import com.elfrikiamv.minegocio_puntodeventa.ui.components.unitLabel
import com.elfrikiamv.minegocio_puntodeventa.utils.JalaliDateUtils
import com.elfrikiamv.minegocio_puntodeventa.utils.NumberUtils
import com.elfrikiamv.minegocio_puntodeventa.utils.PersianFormat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.elfrikiamv.minegocio_puntodeventa.R

/** انواع گزارش. */
enum class ReportType(val faTitle: String, val dateRanged: Boolean = true) {
    DAILY("روزانه"),
    TOP_SELLING("پرفروش"),
    PROFIT("سود کل"),
    CUSTOMER("مشتری"),
    VISITOR("ویزیتور"),
    INVENTORY("موجودی", dateRanged = false),
    PAYMENT("پرداخت"),
    SCAN_LOG("لاگ اسکن"),
    SCANNER_PERF("عملکرد اسکنرها"),
    SESSION_SALES("فروش شیفت‌ها"),
    USER_SALES("فروش کاربران")
}

/** نتیجهٔ یک گزارش: عنوان، سرستون‌ها و ردیف‌ها (متن آمادهٔ نمایش/خروجی). */
data class ReportResult(
    val title: String,
    val headers: List<String>,
    val rows: List<List<String>>
)

/** وضعیت صفحهٔ گزارش‌ها. */
data class ReportsUiState(
    val type: ReportType = ReportType.DAILY,
    val from: String = "",
    val to: String = "",
    val report: ReportResult? = null,
    val loading: Boolean = false,
    val message: String? = null
)

/**
 * ViewModel گزارش‌ها — بازهٔ پیش‌فرض: ابتدای ماه شمسیِ گذشته تا امروز.
 */
class ReportsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = SazmanDatabase.getDatabase(application)
    private val saleRepo = SaleRepository(database.saleDao())
    private val productRepo = ProductRepository(database.productDao())
    private val customerRepo = CustomerRepository(database.customerDao())
    private val visitorRepo = VisitorRepository(database.visitorDao())
    private val sessionRepo = SessionRepository(database.sessionDao())
    private val userRepo = UserRepository(database.userDao())
    private val scanLogRepo = ScanLogRepository(database.scanLogDao())

    private val _state = MutableStateFlow(ReportsUiState(from = prevMonthStart(), to = today()))
    val uiState: StateFlow<ReportsUiState> = _state

    private fun update(transform: (ReportsUiState) -> ReportsUiState) {
        _state.value = transform(_state.value)
    }

    fun onMessageShown() = update { it.copy(message = null) }

    fun onTypeChange(type: ReportType) = update { it.copy(type = type) }

    fun onFromChange(date: String) = update { it.copy(from = date) }

    fun onToChange(date: String) = update { it.copy(to = date) }

    /** بارگذاری گزارش جاری (با فیلترهای انتخابی). */
    fun load() {
        val s = _state.value
        viewModelScope.launch {
            update { it.copy(loading = true) }
            val report = runCatching { buildReport(s.type, s.from, s.to) }
                .getOrElse { e ->
                    update { st -> st.copy(message = "خطا در ساخت گزارش: ${e.message}") }
                    null
                }
            update { st ->
                st.copy(loading = false, report = report ?: st.report)
            }
        }
    }

    /** ساخت گزارش متناظر با نوع. */
    private suspend fun buildReport(type: ReportType, from: String, to: String): ReportResult =
        when (type) {
            ReportType.DAILY -> dailyReport(from, to)
            ReportType.TOP_SELLING -> topSellingReport(from, to)
            ReportType.PROFIT -> profitReport(from, to)
            ReportType.CUSTOMER -> customerReport(from, to)
            ReportType.VISITOR -> visitorReport(from, to)
            ReportType.INVENTORY -> inventoryReport()
            ReportType.PAYMENT -> paymentReport(from, to)
            ReportType.SCAN_LOG -> scanLogReport(from, to)
            ReportType.SCANNER_PERF -> scannerPerfReport(from, to)
            ReportType.SESSION_SALES -> sessionReport(from, to)
            ReportType.USER_SALES -> userReport(from, to)
        }

    // ---- گزارش‌ها ----

    private suspend fun dailyReport(from: String, to: String): ReportResult {
        val rows = saleRepo.dailySales(from, to).map {
            listOf(PersianFormat.displayDate(it.date), NumberUtils.toPersian(it.cnt), PersianFormat.amount(it.total))
        }
        return ReportResult(getApplication<Application>().getString(R.string.str_322), listOf(getApplication<Application>().getString(R.string.str_029), getApplication<Application>().getString(R.string.str_300), getApplication<Application>().getString(R.string.str_302)), rows)
    }

    private suspend fun topSellingReport(from: String, to: String): ReportResult {
        val products = productRepo.getAllOnce().associateBy { it.id }
        val agg = mutableMapOf<Long, Double>()  // productId -> qty
        val aggTotal = mutableMapOf<Long, Double>()
        saleRepo.getBetween(from, to).forEach { sale ->
            sale.items.forEach { item ->
                agg[item.productId] = (agg[item.productId] ?: 0.0) + item.qty
                aggTotal[item.productId] = (aggTotal[item.productId] ?: 0.0) + item.total
            }
        }
        val rows = agg.entries
            .sortedByDescending { it.value }
            .take(50)
            .map { (id, qty) ->
                val p = products[id]
                listOf(
                    p?.name ?: getApplication<Application>().getString(R.string.str_324),
                    PersianFormat.qty(qty),
                    unitLabel(p?.baseUnit ?: ""),
                    PersianFormat.amount(aggTotal[id] ?: 0.0)
                )
            }
        return ReportResult(getApplication<Application>().getString(R.string.str_326), listOf(getApplication<Application>().getString(R.string.str_001), getApplication<Application>().getString(R.string.str_003), getApplication<Application>().getString(R.string.str_002), getApplication<Application>().getString(R.string.str_302)), rows)
    }

    private suspend fun profitReport(from: String, to: String): ReportResult {
        val sales = saleRepo.getBetween(from, to)
        val products = productRepo.getAllOnce().associateBy { it.id }
        val totalSales = sales.sumOf { it.grand }
        // سود ناخالص: (فی فروش − قیمت خرید جاری) × تعداد — تقریب با قیمت خرید فعلی
        var grossProfit = 0.0
        sales.forEach { sale ->
            sale.items.forEach { item ->
                val buy = products[item.productId]?.buyC ?: 0.0
                grossProfit += (item.price - buy) * item.qty
            }
        }
        return ReportResult(
            getApplication<Application>().getString(R.string.str_328),
            listOf(getApplication<Application>().getString(R.string.str_330), getApplication<Application>().getString(R.string.str_332)),
            listOf(
                listOf(PersianFormat.amount(totalSales), PersianFormat.amount(grossProfit))
            )
        )
    }

    private suspend fun customerReport(from: String, to: String): ReportResult {
        val names = customerRepo.getAllOnce().associate { it.id to it.name }
        val rows = saleRepo.salesByCustomer(from, to).map {
            listOf(
                names[it.customerId] ?: getApplication<Application>().getString(R.string.str_038),
                NumberUtils.toPersian(it.cnt),
                PersianFormat.amount(it.total)
            )
        }
        return ReportResult(getApplication<Application>().getString(R.string.str_334), listOf(getApplication<Application>().getString(R.string.str_044), getApplication<Application>().getString(R.string.str_300), getApplication<Application>().getString(R.string.str_336)), rows)
    }

    private suspend fun visitorReport(from: String, to: String): ReportResult {
        val names = visitorRepo.getAllOnce().associate { it.id to it.name }
        val rows = saleRepo.salesByVisitor(from, to).map {
            listOf(
                names[it.visitorId] ?: getApplication<Application>().getString(R.string.str_338),
                NumberUtils.toPersian(it.cnt),
                PersianFormat.amount(it.total),
                PersianFormat.amount(it.commission)
            )
        }
        return ReportResult(getApplication<Application>().getString(R.string.str_340), listOf(getApplication<Application>().getString(R.string.str_154), getApplication<Application>().getString(R.string.str_300), getApplication<Application>().getString(R.string.str_302), getApplication<Application>().getString(R.string.str_342)), rows)
    }

    private suspend fun inventoryReport(): ReportResult {
        val rows = productRepo.getAllOnce()
            .sortedBy { it.name }
            .map { p ->
                val low = p.stock <= p.min
                listOf(
                    p.name,
                    p.barcode.ifBlank { "—" },
                    PersianFormat.qty(p.stock),
                    PersianFormat.qty(p.min),
                    unitLabel(p.baseUnit),
                    PersianFormat.amount(p.stock * p.buyC),
                    PersianFormat.amount(p.stock * p.sellC),
                    if (low) getApplication<Application>().getString(R.string.str_344) else getApplication<Application>().getString(R.string.str_346)
                )
            }
        return ReportResult(
            getApplication<Application>().getString(R.string.str_348),
            listOf(getApplication<Application>().getString(R.string.str_001), getApplication<Application>().getString(R.string.str_109), getApplication<Application>().getString(R.string.str_116), getApplication<Application>().getString(R.string.str_350), getApplication<Application>().getString(R.string.str_002), getApplication<Application>().getString(R.string.str_352), getApplication<Application>().getString(R.string.str_354), getApplication<Application>().getString(R.string.str_356)),
            rows
        )
    }

    private suspend fun paymentReport(from: String, to: String): ReportResult {
        val totals = linkedMapOf(
            Payment.METHOD_CASH to 0.0,
            Payment.METHOD_CARD to 0.0,
            Payment.METHOD_TRANSFER to 0.0,
            Payment.METHOD_CHEQUE to 0.0
        )
        saleRepo.getBetween(from, to).forEach { sale ->
            sale.pays.forEach { pay -> totals[pay.method] = (totals[pay.method] ?: 0.0) + pay.amount }
        }
        val rows = totals.map { listOf(it.key, PersianFormat.amount(it.value)) }
        return ReportResult(getApplication<Application>().getString(R.string.str_358), listOf(getApplication<Application>().getString(R.string.str_360), getApplication<Application>().getString(R.string.str_005)), rows)
    }

    private suspend fun scanLogReport(from: String, to: String): ReportResult {
        val logs: List<ScanLogEntity> = scanLogRepo.betweenDates(from, to)
        val rows = logs.take(500).map {
            listOf(
                PersianFormat.displayDate(it.date) + " " + PersianFormat.displayTime(it.time),
                it.device,
                it.barcode,
                ScanLogEntity.resultLabel(it.success)
            )
        }
        return ReportResult(getApplication<Application>().getString(R.string.str_362), listOf(getApplication<Application>().getString(R.string.str_248), getApplication<Application>().getString(R.string.str_252), getApplication<Application>().getString(R.string.str_109), getApplication<Application>().getString(R.string.str_364)), rows)
    }

    private suspend fun scannerPerfReport(from: String, to: String): ReportResult {
        val rows = scanLogRepo.deviceStats(from, to).map {
            listOf(
                it.device,
                NumberUtils.toPersian(it.cnt),
                NumberUtils.toPersian(it.successCnt)
            )
        }
        return ReportResult(getApplication<Application>().getString(R.string.str_366), listOf(getApplication<Application>().getString(R.string.str_252), getApplication<Application>().getString(R.string.str_368), getApplication<Application>().getString(R.string.str_370)), rows)
    }

    private suspend fun sessionReport(from: String, to: String): ReportResult {
        val rows = saleRepo.salesBySession(from, to).map {
            listOf(
                "شیفت ${NumberUtils.toPersian(it.sessionId)}",
                NumberUtils.toPersian(it.cnt),
                PersianFormat.amount(it.total)
            )
        }
        return ReportResult(getApplication<Application>().getString(R.string.str_372), listOf(getApplication<Application>().getString(R.string.str_374), getApplication<Application>().getString(R.string.str_300), getApplication<Application>().getString(R.string.str_302)), rows)
    }

    private suspend fun userReport(from: String, to: String): ReportResult {
        val names = userRepo.getAllOnce().associate { it.id to it.name }
        val rows = saleRepo.salesByUser(from, to).map {
            listOf(
                names[it.userId] ?: "کاربر ${NumberUtils.toPersian(it.userId)}",
                NumberUtils.toPersian(it.cnt),
                PersianFormat.amount(it.total)
            )
        }
        return ReportResult(getApplication<Application>().getString(R.string.str_376), listOf(getApplication<Application>().getString(R.string.str_378), getApplication<Application>().getString(R.string.str_300), getApplication<Application>().getString(R.string.str_302)), rows)
    }

    companion object {
        private fun today(): String = PersianFormat.todayDateString()

        /** ابتدای ماه شمسیِ گذشته (بازهٔ پیش‌فرض). */
        private fun prevMonthStart(): String {
            val p = JalaliDateUtils.now()
            val (y, m) = if (p.getShMonth() == 1) {
                p.getShYear() - 1 to 12
            } else {
                p.getShYear() to p.getShMonth() - 1
            }
            return PersianFormat.jalaliString(y, m, 1)
        }

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                ReportsViewModel(app)
            }
        }
    }
}
