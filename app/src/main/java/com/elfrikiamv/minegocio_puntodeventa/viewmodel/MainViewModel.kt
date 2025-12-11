package com.elfrikiamv.minegocio_puntodeventa.viewmodel

// MainViewModel.kt

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {

    // ID del banner admob
    private val _bannerID = MutableStateFlow("ca-app-pub-3940256099942544/9214589741")
    val bannerID = _bannerID.asStateFlow()

}