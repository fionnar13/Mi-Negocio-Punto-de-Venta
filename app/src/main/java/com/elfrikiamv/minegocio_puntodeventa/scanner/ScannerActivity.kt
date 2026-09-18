package com.elfrikiamv.minegocio_puntodeventa.scanner

// ScannerActivity.kt

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Bundle
import android.os.SystemClock
import android.widget.ImageButton
import android.widget.Toast
import com.elfrikiamv.minegocio_puntodeventa.R
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.journeyapps.barcodescanner.camera.CameraSettings

/**
 * Escáner de códigos de barras basado en ZXing-Android-Embedded.
 *
 * Utiliza un [CaptureManager] personalizado (permiso de cámara, ciclo de vida,
 * orientación y errores) junto con decodeContinuous() para el escaneo continuo.
 *
 * Cada código detectado se entrega a la pantalla que invocó al escáner a través
 * de [BarcodeScannerCallbackHost] y, al cerrarse en modo normal, también como
 * resultado de la Activity.
 */
class ScannerActivity : Activity() {

    companion object {
        /** Modo continuo: el escáner permanece abierto y entrega cada código leído. */
        const val EXTRA_CONTINUOUS = "scanner.continuous"

        /** Cámara inicial (0 = trasera, 1 = frontal). */
        const val EXTRA_CAMERA_ID = "scanner.camera_id"

        /** Código escaneado (resultado de la Activity). */
        const val EXTRA_BARCODE = "scanner.barcode"

        /** Formato del código escaneado (resultado de la Activity). */
        const val EXTRA_BARCODE_FORMAT = "scanner.barcode_format"

        const val CAMERA_BACK = 0
        const val CAMERA_FRONT = 1

        private const val STATE_TORCH = "scanner.state.torch"
        private const val STATE_CAMERA = "scanner.state.camera"

        /** Ventana mínima para ignorar lecturas duplicadas del mismo código. */
        private const val DUPLICATE_SCAN_INTERVAL_MS = 1500L

        /** Formatos de código de barras soportados. */
        private val SUPPORTED_FORMATS: List<BarcodeFormat> = listOf(
            BarcodeFormat.EAN_13,
            BarcodeFormat.EAN_8,
            BarcodeFormat.UPC_A,
            BarcodeFormat.UPC_E,
            BarcodeFormat.CODE_128,
            BarcodeFormat.CODE_39,
            BarcodeFormat.CODE_93,
            BarcodeFormat.ITF,
            BarcodeFormat.QR_CODE,
            BarcodeFormat.DATA_MATRIX
        )

        /** Crea el intent para lanzar el escáner. */
        fun createIntent(
            context: Context,
            continuous: Boolean = false,
            cameraId: Int = CAMERA_BACK
        ): Intent {
            return Intent(context, ScannerActivity::class.java)
                .putExtra(EXTRA_CONTINUOUS, continuous)
                .putExtra(EXTRA_CAMERA_ID, cameraId)
        }
    }

    private lateinit var captureManager: CaptureManager
    private lateinit var barcodeView: DecoratedBarcodeView
    private lateinit var beepManager: BeepManager
    private lateinit var btnClose: ImageButton
    private lateinit var btnTorch: ImageButton
    private lateinit var btnSwitchCamera: ImageButton

    private var continuousMode = false
    private var currentCameraId = CAMERA_BACK
    private var torchOn = false
    private var lastBarcode: String? = null
    private var lastBarcodeAt = 0L

    /** Callback de ZXing: se invoca por cada código decodificado (hilo principal). */
    private val decodeCallback: BarcodeCallback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult) {
            onBarcodeDecoded(result)
        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>?) {
            // No se utilizan los puntos de interés del escaneo
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        barcodeView = findViewById(R.id.zxing_barcode_scanner)
        btnClose = findViewById(R.id.btn_close_scanner)
        btnTorch = findViewById(R.id.btn_toggle_torch)
        btnSwitchCamera = findViewById(R.id.btn_switch_camera)

        continuousMode = intent.getBooleanExtra(EXTRA_CONTINUOUS, false)
        currentCameraId = savedInstanceState?.getInt(STATE_CAMERA)
            ?: intent.getIntExtra(EXTRA_CAMERA_ID, CAMERA_BACK)
        torchOn = savedInstanceState?.getBoolean(STATE_TORCH) ?: false

        // CaptureManager: permiso de cámara, ciclo de vida, orientación y errores
        captureManager = CaptureManager(this, barcodeView)
        captureManager.initializeFromIntent(intent, savedInstanceState)

        // Formatos soportados por el decodificador
        barcodeView.setDecoderFactory(DefaultDecoderFactory(SUPPORTED_FORMATS))

        // Auto-enfoque (continuo) y cámara seleccionada
        applyCameraSettings()

        barcodeView.setStatusText(
            if (continuousMode) {
                getString(R.string.scanner_status_continuous)
            } else {
                getString(R.string.scanner_status_normal)
            }
        )

        // Beep + vibración al detectar un código
        beepManager = BeepManager(this)
        beepManager.setVibrateEnabled(true)

        // Escaneo continuo con ZXing
        barcodeView.decodeContinuous(decodeCallback)

        btnClose.setOnClickListener { closeScanner() }
        btnTorch.setOnClickListener { toggleTorch() }
        btnSwitchCamera.setOnClickListener { switchCamera() }
        updateTorchIcon()
    }

    override fun onResume() {
        super.onResume()
        captureManager.onResume()
        // Reaplicar la linterna si estaba activa
        if (torchOn) {
            barcodeView.setTorchOn()
        }
    }

    override fun onPause() {
        super.onPause()
        captureManager.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        captureManager.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_CAMERA, currentCameraId)
        outState.putBoolean(STATE_TORCH, torchOn)
        captureManager.onSaveInstanceState(outState)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        // پیام فارسی در صورت رد شدن دسترسی دوربین (سپس رفتار پیش‌فرض کتابخانه اجرا می‌شود)
        val cameraIdx = permissions.indexOfFirst { it == android.Manifest.permission.CAMERA }
        if (cameraIdx >= 0 &&
            grantResults.getOrNull(cameraIdx) == PackageManager.PERMISSION_DENIED
        ) {
            Toast.makeText(
                this,
                getString(R.string.scanner_permission_denied),
                Toast.LENGTH_LONG
            ).show()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        captureManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        BarcodeScannerCallbackHost.callback?.onScannerClosed()
        super.onBackPressed()
    }

    /**
     * Entrega el código escaneado a la pantalla que invocó al escáner mediante
     * la interfaz de callback. En modo normal cierra el escáner y devuelve el
     * resultado; en modo continuo sigue escaneando.
     */
    private fun onBarcodeDecoded(result: BarcodeResult) {
        val barcode = result.text?.trim().orEmpty()
        if (barcode.isEmpty()) return

        // Ignorar lecturas duplicadas inmediatas del mismo código
        val now = SystemClock.elapsedRealtime()
        if (barcode == lastBarcode && now - lastBarcodeAt < DUPLICATE_SCAN_INTERVAL_MS) {
            return
        }
        lastBarcode = barcode
        lastBarcodeAt = now

        val format = result.barcodeFormat?.toString().orEmpty()
        beepManager.playBeepSoundAndVibrate()

        // Interfaz de callback: la pantalla que invocó recibe cada código escaneado
        BarcodeScannerCallbackHost.callback?.onBarcodeScanned(
            BarcodeScanResult(barcode = barcode, format = format)
        )

        if (continuousMode) {
            // Modo continuo: se sigue escaneando y se muestra el último código leído
            barcodeView.setStatusText(getString(R.string.scanner_last_code, barcode, format))
        } else {
            // Modo normal: se devuelve el resultado y se cierra el escáner
            val data = Intent()
                .putExtra(EXTRA_BARCODE, barcode)
                .putExtra(EXTRA_BARCODE_FORMAT, format)
            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }

    /** Activar/desactivar la linterna (torch). */
    private fun toggleTorch() {
        torchOn = !torchOn
        if (torchOn) {
            barcodeView.setTorchOn()
        } else {
            barcodeView.setTorchOff()
        }
        updateTorchIcon()
    }

    private fun updateTorchIcon() {
        btnTorch.setImageResource(
            if (torchOn) R.drawable.baseline_flashlight_on_24
            else R.drawable.baseline_flashlight_off_24
        )
        btnTorch.contentDescription =
            if (torchOn) getString(R.string.scanner_torch_off)
            else getString(R.string.scanner_torch_on)
    }

    /** Alternar entre cámara trasera y frontal. */
    private fun switchCamera() {
        val newCameraId = if (currentCameraId == CAMERA_BACK) CAMERA_FRONT else CAMERA_BACK
        if (newCameraId == CAMERA_FRONT && !hasFrontCamera()) {
            Toast.makeText(
                this,
                getString(R.string.scanner_no_front_camera),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        currentCameraId = newCameraId
        torchOn = false
        updateTorchIcon()

        // Reinicia la cámara con la nueva configuración; el escaneo continuo
        // se reanuda automáticamente al iniciar la vista previa.
        barcodeView.pause()
        applyCameraSettings()
        barcodeView.resume()
    }

    @Suppress("DEPRECATION")
    private fun hasFrontCamera(): Boolean {
        val cameraInfo = Camera.CameraInfo()
        for (i in 0 until Camera.getNumberOfCameras()) {
            Camera.getCameraInfo(i, cameraInfo)
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                return true
            }
        }
        return false
    }

    /** Configura la cámara seleccionada con auto-enfoque continuo. */
    private fun applyCameraSettings() {
        val settings = CameraSettings()
        settings.setRequestedCameraId(currentCameraId)
        settings.setAutoFocusEnabled(true)       // Auto-enfoque habilitado
        settings.setContinuousFocusEnabled(true) // Enfoque continuo para mejor rendimiento
        barcodeView.setCameraSettings(settings)
    }

    private fun closeScanner() {
        BarcodeScannerCallbackHost.callback?.onScannerClosed()
        finish()
    }
}
