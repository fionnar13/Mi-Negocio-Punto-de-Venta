package com.elfrikiamv.minegocio_puntodeventa.viewmodel

// ScanProductViewModel.kt

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ScanProductViewModel(application: Application) : AndroidViewModel(application) {
    private val _cameraError = MutableStateFlow<String?>(null)
    val cameraError: StateFlow<String?> = _cameraError

    fun checkCameraPermission(context: Context, onPermissionDenied: () -> Unit) {
        val cameraPermission = Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(context, cameraPermission)
            != PackageManager.PERMISSION_GRANTED
        ) {
            onPermissionDenied()
        }
    }

    fun setCameraError(message: String) {
        _cameraError.value = message
    }

    fun onBarcodeDetected(barcode: String) {
        Log.d("ScanProductViewModel", "Código de barras detectado: $barcode")
        // Aquí puedes añadir la lógica para manejar el código de barras escaneado
    }
}