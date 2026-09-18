package com.elfrikiamv.minegocio_puntodeventa.scanner

// BarcodeScanCallback.kt

/**
 * Resultado de un escaneo exitoso realizado por [ScannerActivity].
 */
data class BarcodeScanResult(
    val barcode: String,
    val format: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Interfaz de callback para que la pantalla que invoca al escáner
 * reciba cada código de barras escaneado.
 */
interface BarcodeScanCallback {

    /** Se llama por cada código detectado por el escáner. */
    fun onBarcodeScanned(result: BarcodeScanResult)

    /** Se llama cuando el usuario cierra el escáner sin escanear. */
    fun onScannerClosed() {}

    /** Se llama cuando ocurre un error con la cámara o el decodificador. */
    fun onScannerError(message: String) {}
}

/**
 * Puente a nivel de proceso entre [ScannerActivity] y la pantalla que la invocó.
 *
 * La pantalla registra su callback antes de lanzar el escáner (bind) y lo
 * desregistra al salir de la composición (unbind).
 */
object BarcodeScannerCallbackHost {

    @Volatile
    var callback: BarcodeScanCallback? = null

    fun bind(callback: BarcodeScanCallback) {
        this.callback = callback
    }

    fun unbind(callback: BarcodeScanCallback?) {
        if (this.callback === callback) {
            this.callback = null
        }
    }
}
