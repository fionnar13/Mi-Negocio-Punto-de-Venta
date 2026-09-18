package com.elfrikiamv.minegocio_puntodeventa.ui.screens.purchase

// PurchaseViewModel.kt — منطق صفحهٔ خرید از تامین‌کننده

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.room.withTransaction
import com.elfrikiamv.minegocio_puntodeventa.data.entity.LedgerEntryEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.ProductEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.PurchaseEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.PurchaseItem
import com.elfrikiamv.minegocio_puntodeventa.data.entity.SettingsEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.SupplierEntity
import com.elfrikiamv.minegocio_puntodeventa.data.local.SazmanDatabase
import com.elfrikiamv.minegocio_puntodeventa.data.repository.CounterRepository
import com.elfrikiamv.minegocio_puntodeventa.data.repository.LedgerEntryRepository
import com.elfrikiamv.minegocio_puntodeventa.data.repository.ProductRepository
import com.elfrikiamv.minegocio_puntodeventa.data.repository.PurchaseRepository
import com.elfrikiamv.minegocio_puntodeventa.data.repository.ScanLogRepository
import com.elfrikiamv.minegocio_puntodeventa.data.repository.SettingsRepository
import com.elfrikiamv.minegocio_puntodeventa.data.repository.SupplierRepository
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
 * وضعیت صفحهٔ خرید.
 *
 * @param updatePrices اگر true باشد، قیمت خرید کالاها با قیمت فاکتور بروزرسانی می‌شود.
 */
data class PurchaseUiState(
    val suppliers: List<SupplierEntity> = emptyList(),
    val selectedSupplierId: Long? = null,
    val date: String = PersianFormat.todayDateString(),
    val cart: List<CartItem> = emptyList(),
    val query: String = "",
    val searchResults: List<ProductEntity> = emptyList(),
    val discountInput: String = "",
    val paidInput: String = "",
    val updatePrices: Boolean = false,
    val settings: SettingsEntity = SettingsEntity(),
    /** آخرین فاکتورهای خرید (چاپ/اشتراک‌گذاری). */
    val recent: List<PurchaseEntity> = emptyList(),
    val message: String? = null
) {
    val total: Double get() = cart.sumOf { it.total }
    val discount: Double get() = PersianFormat.parse(discountInput).coerceIn(0.0, total)
    val grand: Double get() = total - discount
    val paid: Double get() = PersianFormat.parse(paidInput).coerceIn(0.0, grand)
    val debt: Double get() = grand - paid
}

/**
 * ViewModel خرید — ثبت فاکتور خرید به‌صورت تراکنشی:
 * Purchase + LedgerEntry + افزایش موجودی (+ بروزرسانی قیمت خرید اختیاری)
 * + شمارندهٔ فاکتور خرید.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PurchaseViewModel(application: Application) : AndroidViewModel(application) {

    private val database = SazmanDatabase.getDatabase(application)
    private val productRepo = ProductRepository(database.productDao())
    private val supplierRepo = SupplierRepository(database.supplierDao())
    private val purchaseRepo = PurchaseRepository(database.purchaseDao())
    private val ledgerRepo = LedgerEntryRepository(database.ledgerEntryDao())
    private val counterRepo = CounterRepository(database.counterDao())
    private val settingsRepo = SettingsRepository(database.settingsDao())
    private val scanLogRepo = ScanLogRepository(database.scanLogDao())

    private val _state = MutableStateFlow(PurchaseUiState())
    val uiState: StateFlow<PurchaseUiState> = combine(
        _state,
        supplierRepo.getAll(),
        searchResultsFlow()
    ) { s, suppliers, results ->
        s.copy(suppliers = suppliers, searchResults = results)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PurchaseUiState()
    )

    init {
        refreshRecent()
    }

    /** تنظیمات و آخرین فاکتورهای خرید (چاپ/اشتراک‌گذاری). */
    fun refreshRecent() {
        viewModelScope.launch {
            val settings = settingsRepo.getOrDefaults()
            val recent = purchaseRepo.getRecentOnce(10)
            update { it.copy(settings = settings, recent = recent) }
        }
    }

    private fun searchResultsFlow() = _state
        .map { it.query }
        .distinctUntilChanged()
        .flatMapLatest { q ->
            if (q.isBlank()) flowOf(emptyList())
            else productRepo.search(q).map { list -> list.take(6) }
        }

    private fun update(transform: (PurchaseUiState) -> PurchaseUiState) {
        _state.value = transform(_state.value)
    }

    private fun message(text: String) = update { it.copy(message = text) }

    fun onMessageShown() = update { it.copy(message = null) }

    // ---- تامین‌کننده / تاریخ ----

    fun onSupplierChange(id: Long?) = update { it.copy(selectedSupplierId = id) }

    fun onDateChange(date: String) = update { it.copy(date = date) }

    /** افزودن سریع تامین‌کننده از دیالوگ «+». */
    fun addSupplier(name: String, phone: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val id = supplierRepo.insert(
                SupplierEntity(name = name.trim(), phone = phone.trim())
            )
            update { it.copy(selectedSupplierId = id, message = getApplication<Application>().getString(R.string.str_280)) }
        }
    }

    // ---- جستجو و اسکن ----

    fun onQueryChange(query: String) = update { it.copy(query = query) }

    /** انتخاب کالا از نتایج جستجو (با قیمت خرید). */
    fun addFromSearch(product: ProductEntity) {
        val unit = product.buyU.ifBlank { product.baseUnit }.ifBlank { "u" }
        addItem(product.id, product.name, unit, 1.0, product.buyC)
        update { it.copy(query = "") }
    }

    /** اسکن بارکد — پشتیبانی از بارکد وزنی (پیشوند 20/28). */
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
                addItem(product.id, product.name, unit, weight.weightKg, product.buyC, isWeight = true)
            } else {
                val unit = product.buyU.ifBlank { product.baseUnit }.ifBlank { "u" }
                addItem(product.id, product.name, unit, 1.0, product.buyC)
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

    fun onIncrement(item: CartItem) = changeQty(item, +if (item.isWeight) 0.1 else 1.0)

    fun onDecrement(item: CartItem) = changeQty(item, -(if (item.isWeight) 0.1 else 1.0))

    private fun changeQty(item: CartItem, delta: Double) = update { s ->
        val newQty = ((item.qty + delta * 100).toLong() / 100.0)
        val newCart = when {
            newQty <= 0.0 -> s.cart.filterNot { it.productId == item.productId && it.unit == item.unit }
            else -> s.cart.map {
                if (it.productId == item.productId && it.unit == item.unit) it.copy(qty = newQty)
                else it
            }
        }
        s.copy(cart = newCart)
    }

    fun onRemove(item: CartItem) = update { s ->
        s.copy(cart = s.cart.filterNot { it.productId == item.productId && it.unit == item.unit })
    }

    fun onDiscountChange(input: String) = update { it.copy(discountInput = input) }

    fun onPaidChange(input: String) = update { it.copy(paidInput = input) }

    fun onUpdatePricesChange(enabled: Boolean) = update { it.copy(updatePrices = enabled) }

    // ---- ثبت خرید ----

    /**
     * ثبت فاکتور خرید — تراکنشی:
     * ۱) شماره از شمارندهٔ خرید  ۲) ذخیرهٔ Purchase  ۳) افزایش موجودی
     * ۴) بروزرسانی قیمت خرید (در صورت فعال بودن چک‌باکس)
     * ۵) سند دفتر کل (بستانکار = مبلغ فاکتور، بدهکار = پرداخت‌شده).
     */
    fun submitPurchase() {
        val s = _state.value
        if (s.cart.isEmpty()) {
            message(getApplication<Application>().getString(R.string.str_282))
            return
        }

        viewModelScope.launch {
            var purchNo = ""
            database.withTransaction {
                purchNo = counterRepo.nextPurchNo().toString()

                val purchaseId = purchaseRepo.insert(
                    PurchaseEntity(
                        no = purchNo,
                        date = s.date,
                        supplierId = s.selectedSupplierId,
                        items = s.cart.map {
                            PurchaseItem(
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
                        paid = s.paid
                    )
                )

                s.cart.forEach { item ->
                    val product = productRepo.getById(item.productId) ?: return@forEach
                    val factor = if (item.unit == product.baseUnit) {
                        1.0
                    } else {
                        product.conv[item.unit] ?: 1.0
                    }
                    val baseQty = item.qty * factor

                    // افزایش موجودی (برعکس فروش)
                    productRepo.updateStock(item.productId, +baseQty)

                    // بروزرسانی قیمت خرید در صورت فعال بودن گزینه
                    if (s.updatePrices && item.price > 0.0) {
                        productRepo.update(
                            product.copy(buyC = item.price, buyU = item.unit)
                        )
                    }
                }

                // سند دفتر کل: بستانکار = بدهی به تامین‌کننده، بدهکار = پرداخت نقدی
                ledgerRepo.insert(
                    LedgerEntryEntity(
                        t = LedgerEntryEntity.TYPE_PURCHASE,
                        refId = purchaseId,
                        supplierId = s.selectedSupplierId,
                        debit = s.paid,
                        credit = s.grand,
                        cashOut = s.paid,
                        date = s.date,
                        note = "فاکتور خرید شماره $purchNo"
                    )
                )
            }

            val freshSettings = settingsRepo.getOrDefaults()
            val freshRecent = purchaseRepo.getRecentOnce(10)
            update { st ->
                st.copy(
                    message = "فاکتور خرید $purchNo ثبت شد",
                    cart = emptyList(),
                    discountInput = "",
                    paidInput = "",
                    query = "",
                    settings = freshSettings,
                    recent = freshRecent
                    // تامین‌کننده و تاریخ حفظ می‌شوند
                )
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                PurchaseViewModel(app)
            }
        }
    }
}
