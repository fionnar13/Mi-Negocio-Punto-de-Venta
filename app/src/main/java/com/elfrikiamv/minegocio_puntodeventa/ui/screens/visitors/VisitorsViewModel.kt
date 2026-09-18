package com.elfrikiamv.minegocio_puntodeventa.ui.screens.visitors

// VisitorsViewModel.kt — منطق صفحهٔ ویزیتورها

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.elfrikiamv.minegocio_puntodeventa.data.entity.VisitorEntity
import com.elfrikiamv.minegocio_puntodeventa.data.local.SazmanDatabase
import com.elfrikiamv.minegocio_puntodeventa.data.repository.SaleRepository
import com.elfrikiamv.minegocio_puntodeventa.data.repository.VisitorRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** وضعیت صفحهٔ ویزیتورها. */
data class VisitorsUiState(
    val visitors: List<VisitorEntity> = emptyList(),
    /** جمع پورسانت کسب‌شدهٔ هر ویزیتور از فاکتورهای فروش. */
    val commissions: Map<Long, Double> = emptyMap()
)

/**
 * ViewModel ویزیتورها.
 *
 * مانده payable فعلاً برابر جمع پورسانت است؛ پس از افزودن
 * قابلیت پرداخت پورسانت، مانده = پورسانت − پرداخت‌ها خواهد شد.
 */
class VisitorsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = SazmanDatabase.getDatabase(application)
    private val visitorRepo = VisitorRepository(database.visitorDao())
    private val saleRepo = SaleRepository(database.saleDao())

    val uiState: StateFlow<VisitorsUiState> = combine(
        visitorRepo.getAll(),
        saleRepo.commissionsByVisitor()
    ) { visitors, commissions ->
        VisitorsUiState(
            visitors = visitors,
            commissions = commissions.associate { it.visitorId to it.totalCommission }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), VisitorsUiState())

    fun saveVisitor(visitor: VisitorEntity) {
        viewModelScope.launch {
            if (visitor.id == 0L) visitorRepo.insert(visitor)
            else visitorRepo.update(visitor)
        }
    }

    fun deleteVisitor(visitor: VisitorEntity) {
        viewModelScope.launch { visitorRepo.delete(visitor) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                VisitorsViewModel(app)
            }
        }
    }
}
