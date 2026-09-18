package com.elfrikiamv.minegocio_puntodeventa.ui.screens.suppliers

// SuppliersViewModel.kt — منطق صفحهٔ تامین‌کنندگان

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.elfrikiamv.minegocio_puntodeventa.data.entity.SupplierEntity
import com.elfrikiamv.minegocio_puntodeventa.data.local.SazmanDatabase
import com.elfrikiamv.minegocio_puntodeventa.data.repository.LedgerEntryRepository
import com.elfrikiamv.minegocio_puntodeventa.data.repository.SupplierRepository
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.customers.LedgerRow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** وضعیت صفحهٔ تامین‌کنندگان. */
data class SuppliersUiState(
    val suppliers: List<SupplierEntity> = emptyList(),
    /** مانده هر تامین‌کننده: بستانکار − بدهکار (مثبت = بدهی ما). */
    val balances: Map<Long, Double> = emptyMap()
)

/** گردش حساب یک تامین‌کننده. */
data class SupplierLedger(
    val supplier: SupplierEntity,
    val rows: List<LedgerRow>
)

/**
 * ViewModel تامین‌کنندگان.
 */
class SuppliersViewModel(application: Application) : AndroidViewModel(application) {

    private val database = SazmanDatabase.getDatabase(application)
    private val supplierRepo = SupplierRepository(database.supplierDao())
    private val ledgerRepo = LedgerEntryRepository(database.ledgerEntryDao())

    val uiState: StateFlow<SuppliersUiState> = combine(
        supplierRepo.getAll(),
        ledgerRepo.totalsBySupplier()
    ) { suppliers, totals ->
        SuppliersUiState(
            suppliers = suppliers,
            balances = totals.associate {
                it.supplierId to (it.totalCredit - it.totalDebit)
            }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SuppliersUiState())

    private val _ledger = MutableStateFlow<SupplierLedger?>(null)
    val ledger: StateFlow<SupplierLedger?> = _ledger

    fun saveSupplier(supplier: SupplierEntity) {
        viewModelScope.launch {
            if (supplier.id == 0L) supplierRepo.insert(supplier)
            else supplierRepo.update(supplier)
        }
    }

    fun deleteSupplier(supplier: SupplierEntity) {
        viewModelScope.launch { supplierRepo.delete(supplier) }
    }

    /** گردش حساب (ماندهٔ تجمعی از ابتدا، نمایش نزولی). */
    fun loadLedger(supplier: SupplierEntity) {
        viewModelScope.launch {
            val entries = ledgerRepo.getBySupplierId(supplier.id).first()
            var running = 0.0
            val rows = entries
                .sortedWith(compareBy({ it.date }, { it.id }))
                .map { e ->
                    running += (e.credit - e.debit) // بدهی ما به تامین‌کننده
                    LedgerRow(e, running)
                }
                .asReversed()
            _ledger.value = SupplierLedger(supplier, rows)
        }
    }

    fun closeLedger() {
        _ledger.value = null
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                SuppliersViewModel(app)
            }
        }
    }
}
