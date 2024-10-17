package com.elfrikiamv.minegocio_puntodeventa.ui.screens

// ScanScreen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.InventoryViewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    viewModel: InventoryViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var scanResult by remember { mutableStateOf<String?>(null) }
    var productInfo by remember { mutableStateOf<String?>(null) }
    var isCooldownActive by remember { mutableStateOf(false) }

    val cameraPermission = Manifest.permission.CAMERA
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
            Log.e("ScanScreen", "Permiso de cámara denegado")
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, cameraPermission)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(cameraPermission)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Escanear Código de Barras") })
        }
    ) { paddingValues ->
        Box(modifier = modifier.padding(paddingValues)) {
            Text(
                text = productInfo ?: "Escanea un código de barras",
                modifier = Modifier.align(Alignment.Center)
            )

            CameraPreview { barcode ->
                if (!isCooldownActive) {
                    scanResult = barcode
                    viewModel.checkProductExists(barcode) { product ->
                        productInfo = product?.let {
                            "Producto: ${it.name}, Código: ${it.barcode}"
                        } ?: "Producto no existe en el inventario"

                        Toast.makeText(context, productInfo, Toast.LENGTH_SHORT).show()
                        Log.d("ScanScreen", productInfo ?: "Producto no encontrado")

                        val vibrator =
                            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        vibrator.vibrate(
                            VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
                        )

                        isCooldownActive = true
                    }
                }
            }

            if (isCooldownActive) {
                LaunchedEffect(Unit) {
                    delay(3000)
                    isCooldownActive = false
                }
            }
        }
    }
}

@Composable
fun CameraPreview(onBarcodeDetected: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var cameraControl: CameraControl? by remember { mutableStateOf(null) }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            PreviewView(ctx).apply {
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(surfaceProvider)
                    }
                    val barcodeAnalyzer = BarcodeAnalyzer { barcodes ->
                        barcodes.firstOrNull()?.rawValue?.let { barcode ->
                            onBarcodeDetected(barcode)
                        }
                    }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    val analysisUseCase = ImageAnalysis.Builder().build().also {
                        it.setAnalyzer(ContextCompat.getMainExecutor(context), barcodeAnalyzer)
                    }

                    try {
                        cameraProvider.unbindAll() // Desvincular cualquier uso anterior
                        val camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            analysisUseCase
                        )
                        cameraControl = camera.cameraControl
                    } catch (exc: Exception) {
                        Log.e("CameraPreview", "Error al iniciar la cámara: ${exc.message}")
                    }
                }, ContextCompat.getMainExecutor(context))

                setOnTouchListener { v, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        val factory = this.meteringPointFactory
                        val point = factory.createPoint(event.x, event.y)
                        val action = FocusMeteringAction.Builder(point).build()
                        cameraControl?.startFocusAndMetering(action)
                        v.performClick()
                    }
                    true
                }

            }
        },
        update = { /* Puedes manejar la actualización de la vista aquí si es necesario */ }
    )

    // Limpiar recursos cuando la composición se desmonte
    DisposableEffect(Unit) {
        onDispose {
            cameraControl = null
        }
    }
}

class BarcodeAnalyzer(private val onBarcodesDetected: (List<Barcode>) -> Unit) :
    ImageAnalysis.Analyzer {
    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return
        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        BarcodeScanning.getClient().process(inputImage)
            .addOnSuccessListener { barcodes ->
                onBarcodesDetected(barcodes)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
            .addOnFailureListener { e ->
                Log.e("BarcodeAnalyzer", "Error al procesar la imagen: ${e.message}")
            }
    }
}