package com.elfrikiamv.minegocio_puntodeventa.viewmodel

// MainViewModel.kt

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {

    // ID del banner admob
    private val _bannerID = MutableStateFlow("ca-app-pub-5315728410718973/5674832229")
    val bannerID = _bannerID.asStateFlow()

}