package com.elfrikiamv.minegocio_puntodeventa.ui.screens.accounts

// AccountsViewModel.kt — منطق صفحهٔ حساب‌ها (دفتر کل)

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.elfrikiamv.minegocio_puntodeventa.data.entity.CustomerEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.LedgerEntryEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.SupplierEntity
import com.elfrikiamv.minegocio_puntodeventa.data.local.SazmanDatabase
import com.elfrikiamv.minegocio_puntodeventa.data.repository.CustomerRepository
import com.elfrikiamv.minegocio_puntodeventa.data.repository.LedgerEntryRepository
import com.elfrikiamv.minegocio_puntodeventa.data.repository.SupplierRepository
import com.elfrikiamv.minegocio_puntodeventa.utils.PersianFormat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.elfrikiamv.minegocio_puntodeventa.R

/** جمع‌های خلاصهٔ حساب‌ها. */
data class AccountsTotals(
    val cash: Double = 0.0,
    val bank: Double = 0.0,
    val receivables: Double = 0.0,
    val payables: Double = 0.0
)

/** وضعیت صفحهٔ حساب‌ها. */
data class AccountsUiState(
    val totals: AccountsTotals = AccountsTotals(),
    val entries: List<LedgerEntryEntity> = emptyList(),
    val typeFilter: String = "",
    val query: String = "",
    val from: String = "",
    val to: String = "",
    val customers: List<CustomerEntity> = emptyList(),
    val suppliers: List<SupplierEntity> = emptyList(),
    val message: String? = null
)

/**
 * ViewModel حساب‌ها: خلاصه‌ها (صندوق/بانک/مطالبات/بدهی تامین)،
 * فهرست فیلترشدنی اسناد و ثبت تراکنش‌های دریافت/پرداخت/هزینه/درآمد.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AccountsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = SazmanDatabase.getDatabase(application)
    private val ledgerRepo = LedgerEntryRepository(database.ledgerEntryDao())
    private val customerRepo = CustomerRepository(database.customerDao())
    private val supplierRepo = SupplierRepository(database.supplierDao())

    private val _state = MutableStateFlow(AccountsUiState())
    val uiState: StateFlow<AccountsUiState>

    private data class Filters(
        val type: String,
        val query: String,
        val from: String,
        val to: String
    )

    init {
        val filters = combine(
            _state.map { it.typeFilter },
            _state.map { it.query },
            _state.map { it.from },
            _state.map { it.to }
        ) { t, q, f, to -> Filters(t, q, f, to) }

        val totals = combine(
            ledgerRepo.totalCashBalance(),
            ledgerRepo.totalBankBalance(),
            ledgerRepo.totalReceivables(),
            ledgerRepo.totalPayables()
        ) { cash, bank, recv, pay -> AccountsTotals(cash, bank, recv, pay) }

        val parties = combine(
            customerRepo.getAll(),
            supplierRepo.getAll()
        ) { c, s -> c to s }

        val entries = filters.flatMapLatest { f ->
            ledgerRepo.filtered(f.type, f.query, f.from, f.to)
        }

        uiState = combine(
            _state, totals, parties, entries
        ) { s, t, (customers, suppliers), e ->
            s.copy(totals = t, customers = customers, suppliers = suppliers, entries = e)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AccountsUiState())
    }

    private fun update(transform: (AccountsUiState) -> AccountsUiState) {
        _state.value = transform(_state.value)
    }

    fun onMessageShown() = update { it.copy(message = null) }

    fun onTypeFilterChange(type: String) = update { it.copy(typeFilter = type) }

    fun onQueryChange(query: String) = update { it.copy(query = query) }

    fun onFromDateChange(date: String) = update { it.copy(from = date) }

    fun onToDateChange(date: String) = update { it.copy(to = date) }

    fun clearDateRange() = update { it.copy(from = "", to = "") }

    /**
     * ثبت تراکنش دستی (دریافت/پرداخت/هزینه/درآمد) در دفتر کل.
     *
     * دریافت: بستانکار مشتری + نقد/بانکی ورودی
     * پرداخت: بدهکار تامین‌کننده + نقد/بانکی خروجی
     * هزینه: بدهکار + خروجی · درآمد: بستانکار + ورودی
     */
    fun addTransaction(
        type: String,
        date: String,
        amountInput: String,
        partyId: Long?,
        isBank: Boolean,
        note: String
    ) {
        val amount = PersianFormat.parse(amountInput)
        if (amount <= 0.0) {
            update { it.copy(message = getApplication<Application>().getString(R.string.accounts_invalid_amount)) }
            return
        }

        val customerId: Long?
        val supplierId: Long?
        when (type) {
            LedgerEntryEntity.TYPE_RECEIPT -> {
                customerId = partyId
                supplierId = null
                if (customerId == null) {
                    update { it.copy(message = getApplication<Application>().getString(R.string.accounts_customer_required)) }
                    return
                }
            }
            LedgerEntryEntity.TYPE_PAY_SUP -> {
                customerId = null
                supplierId = partyId
                if (supplierId == null) {
                    update { it.copy(message = getApplication<Application>().getString(R.string.accounts_supplier_required)) }
                    return
                }
            }
            else -> {
                customerId = null
                supplierId = null
                if (note.isBlank()) {
                    update { it.copy(message = getApplication<Application>().getString(R.string.accounts_note_required)) }
                    return
                }
            }
        }

        val finalNote = when {
            note.isNotBlank() -> note.trim()
            type == LedgerEntryEntity.TYPE_RECEIPT ->
                "دریافت از ${_state.value.customers.find { it.id == customerId }?.name.orEmpty()}"
            type == LedgerEntryEntity.TYPE_PAY_SUP ->
                "پرداخت به ${_state.value.suppliers.find { it.id == supplierId }?.name.orEmpty()}"
            else -> ""
        }

        val entry = when (type) {
            LedgerEntryEntity.TYPE_RECEIPT -> LedgerEntryEntity(
                t = type, date = date, customerId = customerId,
                credit = amount,
                cashIn = if (isBank) 0.0 else amount,
                bankIn = if (isBank) amount else 0.0,
                note = finalNote
            )
            LedgerEntryEntity.TYPE_PAY_SUP -> LedgerEntryEntity(
                t = type, date = date, supplierId = supplierId,
                debit = amount,
                cashOut = if (isBank) 0.0 else amount,
                bankOut = if (isBank) amount else 0.0,
                note = finalNote
            )
            LedgerEntryEntity.TYPE_EXPENSE -> LedgerEntryEntity(
                t = type, date = date,
                debit = amount,
                cashOut = if (isBank) 0.0 else amount,
                bankOut = if (isBank) amount else 0.0,
                note = finalNote
            )
            else -> LedgerEntryEntity( // درآمد
                t = LedgerEntryEntity.TYPE_INCOME, date = date,
                credit = amount,
                cashIn = if (isBank) 0.0 else amount,
                bankIn = if (isBank) amount else 0.0,
                note = finalNote
            )
        }

        viewModelScope.launch {
            ledgerRepo.insert(entry)
            update { it.copy(message = getApplication<Application>().getString(R.string.str_284)) }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                AccountsViewModel(app)
            }
        }
    }
}
