package com.elfrikiamv.minegocio_puntodeventa.viewmodel

// ScanProductViewModel.kt

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ScanProductViewModel(application: Application) : AndroidViewModel(application) {
    private val _cameraError = MutableStateFlow<String?>(null)
    val cameraError: StateFlow<String?> = _cameraError
    private var lastScanTime = 0L // Control de tiempo entre escaneos
    private val vibrator = application.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

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

    // función para manejar la detección de código de barras y navegación
    fun onBarcodeDetected(barcode: String, navigateToAddProduct: (String) -> Unit) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastScanTime >= 2000) {
            lastScanTime = currentTime
            // Vibrar
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    200,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
            Log.d("ScanProductViewModel", "Código de barras detectado: $barcode")
            // Navegar a la pantalla de agregar producto con el código de barras
            navigateToAddProduct(barcode)
        }
    }
}