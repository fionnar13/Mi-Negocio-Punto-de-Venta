package com.elfrikiamv.minegocio_puntodeventa.viewmodel

// ScanViewModel.kt

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import com.elfrikiamv.minegocio_puntodeventa.model.ProductFirebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ScanViewModel(application: Application) : AndroidViewModel(application) {
    private val vibrator =
        application.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    // Estados observables
    /*private val _screenColor = MutableStateFlow(Color.White)
    val screenColor: StateFlow<Color> = _screenColor*/

    private val _cameraError = MutableStateFlow<String?>(null)
    val cameraError: StateFlow<String?> = _cameraError

    private val _quantityDialogVisible = MutableStateFlow(false)
    val quantityDialogVisible: StateFlow<Boolean> = _quantityDialogVisible

    private val _scannedProduct = MutableStateFlow<ProductFirebase?>(null)
    val scannedProduct: StateFlow<ProductFirebase?> = _scannedProduct

    private var lastScanTime = 0L // Control de tiempo entre escaneos

    // Verificar permisos de cámara
    fun checkCameraPermission(context: Context, onPermissionDenied: () -> Unit) {
        val cameraPermission = Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(context, cameraPermission)
            != PackageManager.PERMISSION_GRANTED
        ) {
            onPermissionDenied()
        }
    }

    // Manejar detección de código de barras
    fun onBarcodeDetected(
        barcode: String,
        inventoryViewModel: InventoryViewModel
    ) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastScanTime >= 2000) {
            lastScanTime = currentTime

            inventoryViewModel.checkProductExists(barcode) { product ->
                if (product != null) {
                    _scannedProduct.value = product
                    //_screenColor.value = Color.Green
                    _quantityDialogVisible.value = true

                    // Vibrar
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(
                            200,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )
                } else {
                    //_cameraError.value = "Producto no encontrado"
                    //resetScreen()
                    Toast.makeText(
                        getApplication(),
                        "Producto no encontrado en el inventario.",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }
    }

    // Confirmar cantidad del producto
    fun confirmQuantity(quantity: Int, shoppingViewModel: ShoppingViewModel) {
        _scannedProduct.value?.let { product ->
            if (quantity in 1..product.quantity) { // Validación final de cantidad
                shoppingViewModel.addToCart(product.copy(quantity = quantity))
            } else {
                Toast.makeText(
                    getApplication(),
                    "Cantidad inválida. No se agregó al carrito.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            resetScreen()
        }
    }

    // Restablecer estados de la pantalla
    fun resetScreen() {
        _quantityDialogVisible.value = false
    }
}