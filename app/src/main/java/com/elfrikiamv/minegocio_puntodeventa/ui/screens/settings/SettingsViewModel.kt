package com.elfrikiamv.minegocio_puntodeventa.ui.screens.settings

// SettingsViewModel.kt — منطق صفحهٔ تنظیمات (۹ تب)

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.elfrikiamv.minegocio_puntodeventa.data.dao.ScannerDevice
import com.elfrikiamv.minegocio_puntodeventa.data.entity.SettingsEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.SessionEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.UserEntity
import com.elfrikiamv.minegocio_puntodeventa.data.local.SazmanDatabase
import com.elfrikiamv.minegocio_puntodeventa.data.repository.SaleRepository
import com.elfrikiamv.minegocio_puntodeventa.data.repository.ScanLogRepository
import com.elfrikiamv.minegocio_puntodeventa.data.repository.SessionRepository
import com.elfrikiamv.minegocio_puntodeventa.data.repository.SettingsRepository
import com.elfrikiamv.minegocio_puntodeventa.data.repository.UserRepository
import com.elfrikiamv.minegocio_puntodeventa.ui.theme.AppThemeState
import com.elfrikiamv.minegocio_puntodeventa.utils.AppLog
import com.elfrikiamv.minegocio_puntodeventa.utils.DataManager
import com.elfrikiamv.minegocio_puntodeventa.utils.JalaliDateUtils
import com.elfrikiamv.minegocio_puntodeventa.data.entity.ErrorLogEntity
import com.elfrikiamv.minegocio_puntodeventa.utils.PersianFormat
import com.elfrikiamv.minegocio_puntodeventa.utils.ReportExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import com.elfrikiamv.minegocio_puntodeventa.R

/** وضعیت صفحهٔ تنظیمات. */
data class SettingsUiState(
    val settings: SettingsEntity = SettingsEntity(),
    val loaded: Boolean = false,
    val tab: Int = 0,
    val message: String? = null,
    // کاربران
    val users: List<UserEntity> = emptyList(),
    // دستگاه‌ها
    val scanners: List<ScannerDevice> = emptyList(),
    val sessionActive: Boolean = false,
    val sessionLabel: String = "",
    // داده‌ها
    val storage: String = "",
    // سرور
    val wsStatus: String = "قطع",
    val wsConnected: Boolean = false,
    // دیباگر
    val logs: List<ErrorLogEntity> = emptyList(),
    val logQuery: String = "",
    val logLevel: String = "",
    /** شمارش هر سطح لاگ. */
    val logStats: Map<String, Int> = emptyMap()
)

/**
 * ViewModel تنظیمات — همهٔ تب‌ها از این ViewModel استفاده می‌کنند.
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = SazmanDatabase.getDatabase(application)
    private val settingsRepo = SettingsRepository(database.settingsDao())
    private val userRepo = UserRepository(database.userDao())
    private val sessionRepo = SessionRepository(database.sessionDao())
    private val saleRepo = SaleRepository(database.saleDao())
    private val scanLogRepo = ScanLogRepository(database.scanLogDao())
    private val errorLogRepo = com.elfrikiamv.minegocio_puntodeventa.data.repository.ErrorLogRepository(database.errorLogDao())
    private val dataManager = DataManager(application)

    private val _state = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _state

    private fun update(transform: (SettingsUiState) -> SettingsUiState) {
        _state.value = transform(_state.value)
    }

    init {
        load()
        // لاگ‌ها به‌صورت زنده از پایگاه داده (با فیلتر جاری)
        viewModelScope.launch {
            combine(
                errorLogRepo.observeRecent(),
                _state.map { it.logLevel to it.logQuery }
            ) { entries, (level, query) ->
                entries.filter { e ->
                    (level.isBlank() || e.level == level) && (
                        query.isBlank() || e.message.contains(query, true) ||
                            e.scope.contains(query, true) || e.technical.contains(query, true)
                        )
                }
            }.collect { filtered -> update { it.copy(logs = filtered) } }
        }
        viewModelScope.launch {
            errorLogRepo.observeCounts().collect { counts ->
                update { it.copy(logStats = counts.associate { c -> c.level to c.cnt }) } }
        }
    }

    /** بارگذاری تنظیمات، کاربران، اسکنرها و وضعیت شیفت. */
    fun load() {
        viewModelScope.launch {
            val settings = settingsRepo.getOrDefaults()
            val users = userRepo.getAll().first()
            val scanners = scanLogRepo.devices()
            val latest = sessionRepo.getLatest()
            val active = latest != null && latest.endTime == null
            update {
                it.copy(
                    settings = settings,
                    loaded = true,
                    users = users,
                    scanners = scanners,
                    sessionActive = active,
                    sessionLabel = if (active && latest != null) {
                        val pd = JalaliDateUtils.fromTimestamp(latest.startTime)
                        "شیفت فعال از ${PersianFormat.displayTime(
                            "%02d:%02d".format(pd.getHour(), pd.getMinute())
                        )}"
                    } else getApplication<Application>().getString(R.string.str_286),
                    storage = dataManager.storageUsage()
                )
            }
        }
    }

    fun onMessageShown() = update { it.copy(message = null) }

    fun setTab(tab: Int) = update { it.copy(tab = tab) }

    /** تغییر نسخهٔ کاری تنظیمات (بدون ذخیره). */
    fun updateSettings(transform: (SettingsEntity) -> SettingsEntity) =
        update { it.copy(settings = transform(it.settings)) }

    /** ذخیرهٔ تنظیمات + اعمال فوری پوسته. */
    fun saveSettings() {
        viewModelScope.launch {
            val s = _state.value.settings
            settingsRepo.save(s)
            AppThemeState.set(AppThemeState.fromSettings(s.theme))
            update { it.copy(message = getApplication<Application>().getString(R.string.str_288)) }
        }
    }

    // ---- تب کاربران ----

    fun saveUser(user: UserEntity) {
        viewModelScope.launch {
            val withToken = if (user.token.isBlank()) {
                user.copy(token = UUID.randomUUID().toString())
            } else user
            if (withToken.id == 0L) userRepo.insert(withToken)
            else userRepo.update(withToken)
            val users = userRepo.getAll().first()
            update { it.copy(users = users, message = getApplication<Application>().getString(R.string.str_290)) }
        }
    }

    fun deleteUser(user: UserEntity) {
        viewModelScope.launch {
            userRepo.delete(user)
            val users = userRepo.getAll().first()
            update { it.copy(users = users, message = getApplication<Application>().getString(R.string.str_292)) }
        }
    }

    // ---- تب دستگاه‌ها ----

    /** شروع شیفت با موجودی اولیه. */
    fun startShift(startCash: Double) {
        viewModelScope.launch {
            sessionRepo.insert(
                SessionEntity(startTime = System.currentTimeMillis(), startCash = startCash)
            )
            update { it.copy(message = getApplication<Application>().getString(R.string.str_294)) }
            load()
        }
    }

    /** پایان شیفت + چاپ گزارش شیفت. */
    fun endShift() {
        viewModelScope.launch {
            val latest = sessionRepo.getLatest() ?: return@launch
            sessionRepo.closeSession()

            // گزارش چاپی شیفت
            val sales = saleRepo.getBySessionOnce(latest.id)
            val cash = sales.sumOf { s -> s.pays.filter { it.method == com.elfrikiamv.minegocio_puntodeventa.data.entity.Payment.METHOD_CASH }.sumOf { it.amount } }
            val nonCash = sales.sumOf { it.grand } - cash
            ReportExporter.print(
                getApplication(),
                getApplication<Application>().getString(R.string.str_296),
                listOf(getApplication<Application>().getString(R.string.str_050), getApplication<Application>().getString(R.string.str_298)),
                listOf(
                    listOf(getApplication<Application>().getString(R.string.str_300), com.elfrikiamv.minegocio_puntodeventa.utils.NumberUtils.toPersian(sales.size)),
                    listOf(getApplication<Application>().getString(R.string.str_302), PersianFormat.amount(sales.sumOf { it.grand })),
                    listOf(getApplication<Application>().getString(R.string.str_304), PersianFormat.amount(cash)),
                    listOf(getApplication<Application>().getString(R.string.str_306), PersianFormat.amount(nonCash))
                )
            )
            update { it.copy(message = getApplication<Application>().getString(R.string.str_308)) }
            load()
        }
    }

    // ---- تب داده‌ها ----

    fun backup() {
        viewModelScope.launch {
            val msg = dataManager.backupJson()
            update { it.copy(message = msg) }
        }
    }

    fun restore(uri: android.net.Uri) {
        viewModelScope.launch {
            val msg = dataManager.restoreJson(uri)
            update { it.copy(message = msg) }
            load()
        }
    }

    fun resetData() {
        viewModelScope.launch {
            val msg = dataManager.reset()
            update { it.copy(message = msg) }
            load()
        }
    }

    fun sampleData() {
        viewModelScope.launch {
            val msg = dataManager.sampleData()
            update { it.copy(message = msg) }
            load()
        }
    }

    fun exportProductsExcel() {
        viewModelScope.launch {
            val msg = dataManager.exportProductsExcel()
            update { it.copy(message = msg) }
        }
    }

    fun exportCustomersExcel() {
        viewModelScope.launch {
            val msg = dataManager.exportCustomersExcel()
            update { it.copy(message = msg) }
        }
    }

    fun importProducts(uri: android.net.Uri) {
        viewModelScope.launch {
            val msg = dataManager.importProductsExcel(uri)
            update { it.copy(message = msg) }
        }
    }

    fun importCustomers(uri: android.net.Uri) {
        viewModelScope.launch {
            val msg = dataManager.importCustomersExcel(uri)
            update { it.copy(message = msg) }
        }
    }

    fun refreshStorage() = update { it.copy(storage = dataManager.storageUsage()) }

    // ---- تب سرور (WebSocket) ----

    private var ws: okhttp3.WebSocket? = null
    private val wsClient by lazy { okhttp3.OkHttpClient() }

    /** اتصال به سرور WebSocket. */
    fun wsConnect() {
        val raw = _state.value.settings.wsUrl.trim()
        if (raw.isBlank()) {
            update { it.copy(message = getApplication<Application>().getString(R.string.str_310)) }
            return
        }
        val url = if (raw.startsWith("ws://") || raw.startsWith("wss://") ||
            raw.startsWith("http://") || raw.startsWith("https://")
        ) raw else "ws://$raw"

        wsDisconnect()
        update { it.copy(wsStatus = getApplication<Application>().getString(R.string.str_312)) }
        val request = okhttp3.Request.Builder().url(url).build()
        ws = wsClient.newWebSocket(request, object : okhttp3.WebSocketListener() {
            override fun onOpen(webSocket: okhttp3.WebSocket, response: okhttp3.Response) {
                AppLog.i("WS", "متصل شد: $url")
                update { it.copy(wsStatus = getApplication<Application>().getString(R.string.str_314), wsConnected = true) }
            }

            override fun onFailure(webSocket: okhttp3.WebSocket, t: Throwable, response: okhttp3.Response?) {
                AppLog.e("WS", getApplication<Application>().getString(R.string.str_316), t)
                update { it.copy(wsStatus = "خطا: ${t.message}", wsConnected = false) }
            }

            override fun onClosed(webSocket: okhttp3.WebSocket, code: Int, reason: String) {
                update { it.copy(wsStatus = getApplication<Application>().getString(R.string.str_318), wsConnected = false) }
            }

            override fun onMessage(webSocket: okhttp3.WebSocket, text: String) {
                AppLog.i("WS", text.take(300))
            }
        })
    }

    fun wsDisconnect() {
        ws?.close(1000, "bye")
        ws = null
        update { it.copy(wsStatus = getApplication<Application>().getString(R.string.str_318), wsConnected = false) }
    }

    override fun onCleared() {
        ws?.cancel()
        super.onCleared()
    }

    // ---- تب دیباگر ----

    fun refreshLogs() = load()

    fun onLogQueryChange(query: String) {
        update { it.copy(logQuery = query) }
        refreshLogs()
    }

    fun onLogLevelChange(level: String) {
        update { it.copy(logLevel = level) }
        refreshLogs()
    }

    fun clearLogs() {
        viewModelScope.launch {
            errorLogRepo.deleteAll()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                SettingsViewModel(app)
            }
        }
    }
}
