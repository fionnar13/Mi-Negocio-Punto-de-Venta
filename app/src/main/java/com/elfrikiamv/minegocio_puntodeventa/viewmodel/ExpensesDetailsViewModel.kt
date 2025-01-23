package com.elfrikiamv.minegocio_puntodeventa.viewmodel

// ExpensesDetailsViewModel.kt

import androidx.lifecycle.ViewModel
import com.elfrikiamv.minegocio_puntodeventa.model.TicketFirebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ExpensesDetailsViewModel : ViewModel() {

    // MutableStateFlow para almacenar la lista de gastos
    private val _expensesDetailsList = MutableStateFlow<List<TicketFirebase>>(emptyList())
    val expensesDetailsList: StateFlow<List<TicketFirebase>> = _expensesDetailsList // StateFlow expuesto a la vista

    // Nuevo campo para el estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
}