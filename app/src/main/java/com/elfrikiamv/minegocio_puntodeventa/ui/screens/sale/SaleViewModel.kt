package com.elfrikiamv.minegocio_puntodeventa.ui.screens.sale

// SaleViewModel.kt — منطق صفحهٔ فروش (POS)

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.room.withTransaction
import com.elfrikiamv.minegocio_puntodeventa.data.entity.CustomerEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.LedgerEntryEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.Payment
import com.elfrikiamv.minegocio_puntodeventa.data.entity.ProductEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.SaleEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.SaleItem
import com.elfrikiamv.minegocio_puntodeventa.data.entity.SettingsEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.VisitorEntity
import com.elfrikiamv.minegocio_puntodeventa.data.local.SazmanDatabase
import com.elfrikiamv.minegocio_puntodeventa.data.repository.CounterRepository
import com.elfrikiamv.minegocio_puntodeventa.data.repository.CustomerRepository
import com.elfrikiamv.minegocio_puntodeventa.data.repository.LedgerEntryRepository
import com.elfrikiamv.minegocio_puntodeventa.data.repository.ProductRepository
import com.elfrikiamv.minegocio_puntodeventa.data.repository.SaleRepository
import com.elfrikiamv.minegocio_puntodeventa.data.repository.ScanLogRepository
import com.elfrikiamv.minegocio_puntodeventa.data.repository.SessionRepository
import com.elfrikiamv.minegocio_puntodeventa.data.repository.SettingsRepository
import com.elfrikiamv.minegocio_puntodeventa.data.repository.VisitorRepository
import com.elfrikiamv.minegocio_puntodeventa.ui.components.CartItem
import com.elfrikiamv.minegocio_puntodeventa.utils.Barcodes
import com.elfrikiamv.minegocio_puntodeventa.utils.PersianFormat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.elfrikiamv.minegocio_puntodeventa.R

/**
 * وضعیت صفحهٔ فروش.
 *
 * `date` و `time` به شکل ذخیره‌ای لاتین صفرپرشده‌اند
 * ("1405/06/27" و "14:30") تا در دیتابیس مرتب‌سازی‌پذیر باشند.
 */
data class SaleUiState(
    val customers: List<CustomerEntity> = emptyList(),
    val visitors: List<VisitorEntity> = emptyList(),
    val selectedCustomerId: Long? = null,
    val selectedVisitorId: Long? = null,
    val date: String = PersianFormat.todayDateString(),
    val time: String = PersianFormat.nowTimeString(),
    val cart: List<CartItem> = emptyList(),
    val query: String = "",
    val searchResults: List<ProductEntity> = emptyList(),
    val discountInput: String = "",
    val payments: List<Payment> = emptyList(),
    val settings: SettingsEntity = SettingsEntity(),
    /** آخرین فاکتورها (چاپ/اشتراک‌گذاری). */
    val recent: List<SaleEntity> = emptyList(),
    val message: String? = null
) {
    val total: Double get() = cart.sumOf { it.total }
    val discount: Double get() = PersianFormat.parse(discountInput).coerceIn(0.0, total)
    val grand: Double get() = total - discount
    val paid: Double get() = payments.sumOf { it.amount }
    val remaining: Double get() = grand - paid
}

/**
 * ViewModel فروش (POS) — ثبت فاکتور به‌صورت تراکنشی:
 * Sale + LedgerEntry + کاهش موجودی + شمارندهٔ فاکتور، همه در یک تراکنش Room.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SaleViewModel(application: Application) : AndroidViewModel(application) {

    private val database = SazmanDatabase.getDatabase(application)
    private val productRepo = ProductRepository(database.productDao())
    private val customerRepo = CustomerRepository(database.customerDao())
    private val visitorRepo = VisitorRepository(database.visitorDao())
    private val saleRepo = SaleRepository(database.saleDao())
    private val ledgerRepo = LedgerEntryRepository(database.ledgerEntryDao())
    private val counterRepo = CounterRepository(database.counterDao())
    private val sessionRepo = SessionRepository(database.sessionDao())
    private val settingsRepo = SettingsRepository(database.settingsDao())
    private val scanLogRepo = ScanLogRepository(database.scanLogDao())

    private val _state = MutableStateFlow(SaleUiState())
    val uiState: StateFlow<SaleUiState> = combine(
        _state,
        customerRepo.getAll(),
        visitorRepo.getAll(),
        searchResultsFlow()
    ) { s, customers, visitors, results ->
        s.copy(
            customers = customers,
            visitors = visitors,
            searchResults = results
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SaleUiState()
    )

    init {
        // تنظیمات و آخرین فاکتورها برای چاپ/اشتراک‌گذاری
        refreshRecent()
    }

    /** نتایج جستجوی کالا (۶ مورد اول). */
    private fun searchResultsFlow() = _state
        .map { it.query }
        .distinctUntilChanged()
        .flatMapLatest { q ->
            if (q.isBlank()) flowOf(emptyList())
            else productRepo.search(q).map { list -> list.take(6) }
        }

    private fun update(transform: (SaleUiState) -> SaleUiState) {
        _state.value = transform(_state.value)
    }

    private fun message(text: String) = update { it.copy(message = text) }

    /** بارگذاری تنظیمات و آخرین فاکتورها (چاپ/اشتراک‌گذاری). */
    fun refreshRecent() {
        viewModelScope.launch {
            val settings = settingsRepo.getOrDefaults()
            val recent = saleRepo.getRecentOnce(10)
            update { it.copy(settings = settings, recent = recent) }
        }
    }

    fun onMessageShown() = update { it.copy(message = null) }

    // ---- انتخاب مشتری / ویزیتور / تاریخ / ساعت ----

    fun onCustomerChange(id: Long?) = update { it.copy(selectedCustomerId = id) }

    fun onVisitorChange(id: Long?) = update { it.copy(selectedVisitorId = id) }

    fun onDateChange(date: String) = update { it.copy(date = date) }

    fun onTimeChange(time: String) = update { it.copy(time = time) }

    /** افزودن مشتری سریع از دیالوگ «+». */
    fun addCustomer(name: String, phone: String, kind: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val id = customerRepo.insert(
                CustomerEntity(name = name.trim(), phone = phone.trim(), kind = kind)
            )
            update { it.copy(selectedCustomerId = id, message = getApplication<Application>().getString(R.string.str_268)) }
        }
    }

    // ---- جستجو و اسکن ----

    fun onQueryChange(query: String) = update { it.copy(query = query) }

    /** انتخاب کالا از نتایج جستجو. */
    fun addFromSearch(product: ProductEntity) {
        val unit = product.sellU.ifBlank { product.baseUnit }.ifBlank { "u" }
        val price = product.sellUnits[unit]?.takeIf { it > 0 } ?: product.sellC
        addItem(
            productId = product.id,
            name = product.name,
            unit = unit,
            qty = 1.0,
            price = price
        )
        update { it.copy(query = "") }
    }

    /**
     * اسکن بارکد: کالا به سبد اضافه یا تعدادش زیاد می‌شود.
     * بارکدهای وزنی (پیشوند 20/28) با کد ۵ رقمی و وزن تعبیه‌شده خوانده می‌شوند.
     */
    fun onBarcodeScanned(barcode: String) {
        viewModelScope.launch {
            val weight = Barcodes.parseWeightBarcode(barcode)
            val product = productRepo.findByBarcode(barcode)
                ?: weight?.let { productRepo.findByCode(it.code) }

            // ثبت در لاگ اسکن (گزارش‌های اسکنر)
            scanLogRepo.log(android.os.Build.MODEL, barcode, product != null)

            if (product == null) {
                message(getApplication<Application>().getString(R.string.str_270))
                return@launch
            }

            if (weight != null) {
                val unit = if (product.baseUnit == "kg") "kg"
                else product.baseUnit.ifBlank { "kg" }
                val price = product.sellUnits[unit]?.takeIf { it > 0 }
                    ?: product.sellUnits["kg"]?.takeIf { it > 0 }
                    ?: product.sellC
                addItem(
                    productId = product.id,
                    name = product.name,
                    unit = unit,
                    qty = weight.weightKg,
                    price = price,
                    isWeight = true
                )
            } else {
                val unit = product.sellU.ifBlank { product.baseUnit }.ifBlank { "u" }
                val price = product.sellUnits[unit]?.takeIf { it > 0 } ?: product.sellC
                addItem(
                    productId = product.id,
                    name = product.name,
                    unit = unit,
                    qty = 1.0,
                    price = price
                )
            }
        }
    }

    private fun addItem(
        productId: Long,
        name: String,
        unit: String,
        qty: Double,
        price: Double,
        isWeight: Boolean = false
    ) = update { s ->
        val index = s.cart.indexOfFirst { it.productId == productId && it.unit == unit }
        val newCart = if (index >= 0) {
            s.cart.toMutableList().apply {
                val current = get(index)
                set(index, current.copy(qty = current.qty + qty, price = price))
            }
        } else {
            s.cart + CartItem(
                productId = productId,
                name = name,
                unit = unit,
                qty = qty,
                price = price,
                isWeight = isWeight
            )
        }
        s.copy(cart = newCart)
    }

    // ---- سبد ----

    fun onIncrement(item: CartItem) = changeQty(item, +step(item))

    fun onDecrement(item: CartItem) = changeQty(item, -step(item))

    private fun step(item: CartItem) = if (item.isWeight) 0.1 else 1.0

    private fun changeQty(item: CartItem, delta: Double) = update { s ->
        val newQty = ((item.qty + delta * 100).toLong() / 100.0) // حذف خطای اعشار
        val newCart = when {
            newQty <= 0.0 -> s.cart.filterNot { it.productId == item.productId && it.unit == item.unit }
            else -> s.cart.map {
                if (it.productId == item.productId && it.unit == item.unit) {
                    it.copy(qty = newQty)
                } else it
            }
        }
        s.copy(cart = newCart)
    }

    fun onRemove(item: CartItem) = update { s ->
        s.copy(cart = s.cart.filterNot { it.productId == item.productId && it.unit == item.unit })
    }

    fun onDiscountChange(input: String) = update { it.copy(discountInput = input) }

    // ---- پرداخت‌ها ----

    /** افزودن پرداخت (نقد/پوز/کارت/چک). */
    fun addPayment(method: String, amountInput: String) {
        val amount = PersianFormat.parse(amountInput)
        when {
            amount <= 0.0 -> message(getApplication<Application>().getString(R.string.str_272))
            _state.value.paid + amount > _state.value.grand + 0.01 ->
                message(getApplication<Application>().getString(R.string.str_274))
            else -> update { it.copy(payments = it.payments + Payment(method = method, amount = amount)) }
        }
    }

    fun removePayment(index: Int) = update { s ->
        if (index in s.payments.indices) s.copy(payments = s.payments.filterIndexed { i, _ -> i != index })
        else s
    }

    // ---- ثبت فاکتور ----

    /**
     * ثبت فاکتور فروش — تراکنشی:
     * ۱) شماره از شمارنده  ۲) ذخیرهٔ Sale  ۳) کاهش موجودی و افزایش soldCount
     * ۴) سند دفتر کل  ۵) پاک‌سازی سبد (مشتری حفظ می‌شود).
     */
    fun submitSale() {
        val s = _state.value
        if (s.cart.isEmpty()) {
            message(getApplication<Application>().getString(R.string.str_276))
            return
        }
        if (s.paid > s.grand + 0.01) {
            message(getApplication<Application>().getString(R.string.str_274))
            return
        }
        if (s.paid < s.grand - 0.01) {
            val customer = s.customers.find { it.id == s.selectedCustomerId }
            // مشتری متفرقه یا نقدی: باید کل مبلغ پرداخت شود
            if (customer == null || customer.kind == CustomerEntity.KIND_CASH) {
                message(getApplication<Application>().getString(R.string.str_278))
                return
            }
        }

        viewModelScope.launch {
            val sessionId = sessionRepo.getLatest()?.id
            val visitor = s.visitors.find { it.id == s.selectedVisitorId }

            // پورسانت ویزیتور بر اساس نوع او
            val commission = visitor?.let { v ->
                when (v.type) {
                    VisitorEntity.TYPE_PERCENT -> s.grand * v.value / 100.0
                    VisitorEntity.TYPE_CARTON ->
                        s.cart.filter { it.unit == "c" }.sumOf { it.qty } * v.value
                    VisitorEntity.TYPE_FIXED -> v.value
                    else -> 0.0
                }
            } ?: 0.0

            var saleNo = ""
            database.withTransaction {
                saleNo = counterRepo.nextSaleNo().toString()

                val saleId = saleRepo.insert(
                    SaleEntity(
                        no = saleNo,
                        date = s.date,
                        time = s.time,
                        customerId = s.selectedCustomerId,
                        visitorId = s.selectedVisitorId,
                        sessionId = sessionId,
                        items = s.cart.map {
                            SaleItem(
                                productId = it.productId,
                                name = it.name,
                                unit = it.unit,
                                qty = it.qty,
                                price = it.price,
                                total = it.total
                            )
                        },
                        total = s.total,
                        discount = s.discount,
                        grand = s.grand,
                        paid = s.paid,
                        pays = s.payments,
                        commission = commission
                    )
                )

                // کاهش موجودی و افزایش شمارندهٔ فروش (بر حسب واحد پایه)
                s.cart.forEach { item ->
                    val product = productRepo.getById(item.productId) ?: return@forEach
                    val factor = if (item.unit == product.baseUnit) {
                        1.0
                    } else {
                        product.conv[item.unit] ?: 1.0
                    }
                    val baseQty = item.qty * factor
                    productRepo.updateStock(item.productId, -baseQty)
                    productRepo.increaseSoldCount(item.productId, baseQty)
                }

                // سند دفتر کل: بدهکار = مبلغ فاکتور، بستانکار = پرداخت‌شده
                val cashPaid = s.payments
                    .filter { it.method == Payment.METHOD_CASH }
                    .sumOf { it.amount }
                val bankPaid = s.payments
                    .filter { it.method != Payment.METHOD_CASH }
                    .sumOf { it.amount }
                ledgerRepo.insert(
                    LedgerEntryEntity(
                        t = LedgerEntryEntity.TYPE_SALE,
                        refId = saleId,
                        customerId = s.selectedCustomerId,
                        sessionId = sessionId,
                        debit = s.grand,
                        credit = s.paid,
                        cashIn = cashPaid,
                        bankIn = bankPaid,
                        date = s.date,
                        note = "فاکتور فروش شماره $saleNo"
                    )
                )
            }

            // پیام + چاپ در صورت فعال بودن
            val settings = settingsRepo.getOrDefaults()
            val printNote = if (settings.printFormat.isNotBlank()) {
                " · ارسال برای چاپ (${settings.printFormat})"
            } else ""
            val recent = saleRepo.getRecentOnce(10)
            update { st ->
                st.copy(
                    message = "فاکتور فروش $saleNo ثبت شد$printNote",
                    cart = emptyList(),
                    payments = emptyList(),
                    discountInput = "",
                    query = "",
                    settings = settings,
                    recent = recent
                    // مشتری و ویزیتور و تاریخ حفظ می‌شوند
                )
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                SaleViewModel(app)
            }
        }
    }
}
