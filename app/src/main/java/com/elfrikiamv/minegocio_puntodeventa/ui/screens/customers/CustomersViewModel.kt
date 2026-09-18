package com.elfrikiamv.minegocio_puntodeventa.ui.screens.customers

// CustomersViewModel.kt — منطق صفحهٔ مشتریان

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.elfrikiamv.minegocio_puntodeventa.data.entity.CustomerEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.LedgerEntryEntity
import com.elfrikiamv.minegocio_puntodeventa.data.local.SazmanDatabase
import com.elfrikiamv.minegocio_puntodeventa.data.repository.CustomerRepository
import com.elfrikiamv.minegocio_puntodeventa.data.repository.LedgerEntryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** وضعیت صفحهٔ مشتریان. */
data class CustomersUiState(
    val customers: List<CustomerEntity> = emptyList(),
    /** مانده هر مشتری: بدهکار − بستانکار (مثبت = بدهی مشتری به ما). */
    val balances: Map<Long, Double> = emptyMap()
)

/** یک سند دفتر کل با ماندهٔ تجمعی (برای دیالوگ گردش حساب). */
data class LedgerRow(
    val entry: LedgerEntryEntity,
    val runningBalance: Double
)

/** گردش حساب یک مشتری. */
data class CustomerLedger(
    val customer: CustomerEntity,
    val rows: List<LedgerRow>
)

/**
 * ViewModel مشتریان — فهرست با مانده از دفتر کل + گردش حساب.
 */
class CustomersViewModel(application: Application) : AndroidViewModel(application) {

    private val database = SazmanDatabase.getDatabase(application)
    private val customerRepo = CustomerRepository(database.customerDao())
    private val ledgerRepo = LedgerEntryRepository(database.ledgerEntryDao())

    val uiState: StateFlow<CustomersUiState> = combine(
        customerRepo.getAll(),
        ledgerRepo.totalsByCustomer()
    ) { customers, totals ->
        CustomersUiState(
            customers = customers,
            balances = totals.associate {
                it.customerId to (it.totalDebit - it.totalCredit)
            }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CustomersUiState())

    private val _ledger = MutableStateFlow<CustomerLedger?>(null)
    val ledger: StateFlow<CustomerLedger?> = _ledger

    fun saveCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            if (customer.id == 0L) customerRepo.insert(customer)
            else customerRepo.update(customer)
        }
    }

    fun deleteCustomer(customer: CustomerEntity) {
        viewModelScope.launch { customerRepo.delete(customer) }
    }

    /**
     * بارگذاری گردش حساب: اسناد به ترتیب صعودی مرتب و ماندهٔ تجمعی
     * محاسبه می‌شود، سپس برای نمایش برعکس (جدیدترین اول) می‌شود.
     */
    fun loadLedger(customer: CustomerEntity) {
        viewModelScope.launch {
            val entries = ledgerRepo.getByCustomerId(customer.id).first()
            var running = 0.0
            val rows = entries
                .sortedWith(compareBy({ it.date }, { it.id }))
                .map { e ->
                    running += (e.debit - e.credit)
                    LedgerRow(e, running)
                }
                .asReversed()
            _ledger.value = CustomerLedger(customer, rows)
        }
    }

    fun closeLedger() {
        _ledger.value = null
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                CustomersViewModel(app)
            }
        }
    }
}
