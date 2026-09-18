package com.elfrikiamv.minegocio_puntodeventa.ui.screens.banks

// BanksViewModel.kt — منطق صفحهٔ بانک‌ها

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.elfrikiamv.minegocio_puntodeventa.data.entity.BankEntity
import com.elfrikiamv.minegocio_puntodeventa.data.local.SazmanDatabase
import com.elfrikiamv.minegocio_puntodeventa.data.repository.BankRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** ViewModel بانک‌ها — CRUD ساده. */
class BanksViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = BankRepository(SazmanDatabase.getDatabase(application).bankDao())

    val banks: StateFlow<List<BankEntity>> = repo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun saveBank(bank: BankEntity) {
        viewModelScope.launch {
            if (bank.id == 0L) repo.insert(bank) else repo.update(bank)
        }
    }

    fun deleteBank(bank: BankEntity) {
        viewModelScope.launch { repo.delete(bank) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                BanksViewModel(app)
            }
        }
    }
}
