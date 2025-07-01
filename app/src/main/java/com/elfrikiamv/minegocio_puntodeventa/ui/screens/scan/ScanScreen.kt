package com.elfrikiamv.minegocio_puntodeventa.ui.screens.scan

// ScanScreen.kt

import android.Manifest
import android.util.Log
import android.util.Size
import android.view.MotionEvent
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elfrikiamv.minegocio_puntodeventa.model.inventory.ProductFirebase
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.inventory.InventoryViewModel
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.scan.ScanViewModel
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.shopping.ShoppingViewModel
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

@Composable
fun ScanScreen() {
    val viewModelInventory: InventoryViewModel = viewModel()
    val viewModelShopping: ShoppingViewModel = viewModel()
    val scanViewModel: ScanViewModel = viewModel()
    //val screenColor by scanViewModel.screenColor.collectAsState()
    val cameraError by scanViewModel.cameraError.collectAsState()
    val quantityDialogVisible by scanViewModel.quantityDialogVisible.collectAsState()
    val scannedProduct by scanViewModel.scannedProduct.collectAsState()
    var showFocusIndicator by remember { mutableStateOf(false) }
    var focusPoint by remember { mutableStateOf(Offset.Zero) }

    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            scanViewModel.resetScreen()
        }
    }

    LaunchedEffect(Unit) {
        // Solicitar permisos de cámara al iniciar la pantalla
        scanViewModel.checkCameraPermission(context) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(
        modifier = Modifier
            //.padding(paddingValues)
            .fillMaxSize()
        //.background(screenColor)
    ) {
        if (cameraError != null) {
            // Mostrar mensaje de error si ocurre un problema con la cámara
            Text(
                text = cameraError ?: "Error desconocido",
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.error
            )
        } else {
            CameraPreview(
                onBarcodeDetected = { barcode ->
                    // Manejar el código de barras detectado
                    scanViewModel.onBarcodeDetected(barcode, viewModelInventory)
                },
                onFocusTapped = { x, y ->
                    focusPoint = Offset(x, y)
                    showFocusIndicator = true
                }
            )
            // Mostrar indicador visual de enfoque
            if (showFocusIndicator) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color.Green,
                        radius = 50f,
                        center = focusPoint
                    )
                }
                LaunchedEffect(Unit) {
                    delay(500) // Ocultar el indicador después de 0.5 segundos
                    showFocusIndicator = false
                }
            }
        }
    }

    // Diálogo para agregar cantidad de producto
    if (quantityDialogVisible && scannedProduct != null) {
        QuantityDialog(
            product = scannedProduct!!,
            onConfirm = { quantity ->
                scanViewModel.confirmQuantity(quantity, viewModelShopping)
            },
            onDismiss = {
                scanViewModel.resetScreen()
            }
        )
    }
}

@Composable
fun QuantityDialog(
    product: ProductFirebase,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var quantityInput by remember { mutableStateOf("") }
    val isInputValid = quantityInput.toIntOrNull()?.let { it in 1..product.quantity } == true

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar ${product.name}") },
        text = {
            Column {
                Text("Cantidad disponible: ${product.quantity}")
                TextField(
                    value = quantityInput,
                    onValueChange = { input ->
                        quantityInput = input.filter { it.isDigit() }
                    },
                    label = { Text("Ingrese la cantidad") }
                )
                if (!isInputValid && quantityInput.isNotEmpty()) {
                    Text(
                        "Cantidad inválida. Debe estar entre 1 y ${product.quantity}.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isInputValid) onConfirm(quantityInput.toInt())
                },
                enabled = isInputValid // Deshabilita el botón si la entrada no es válida
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// Vista previa de la cámara optimizada
@Composable
private fun CameraPreview(
    onBarcodeDetected: (String) -> Unit,
    onFocusTapped: (Float, Float) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var cameraControl: CameraControl? by remember { mutableStateOf(null) }
    var cameraProvider: ProcessCameraProvider? by remember { mutableStateOf(null) }

    // Crear un solo hilo para procesamiento de imágenes
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            PreviewView(ctx).apply {
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = surfaceProvider
                    }

                    // Opciones para limitar los formatos de código de barras
                    val barcodeScannerOptions = BarcodeScannerOptions.Builder().build()

                    val barcodeAnalyzer = BarcodeAnalyzer(
                        barcodeScannerOptions, // Analizador para todos los formatos
                        onBarcodesDetected = { barcodes, _ ->
                            barcodes.firstOrNull()?.rawValue?.let(onBarcodeDetected) // Manejar el primer código
                        }
                    )

                    val analysisUseCase = ImageAnalysis.Builder()
                        .setTargetResolution(Size(1280, 720)) // Configurar resolución
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // Evitar acumulaciones
                        .build()
                        .also {
                            it.setAnalyzer(analysisExecutor, barcodeAnalyzer) // Asignar analizador
                        }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider?.unbindAll()
                        val camera = cameraProvider?.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            analysisUseCase
                        )
                        cameraControl = camera?.cameraControl
                    } catch (exc: Exception) {
                        Log.e("CameraPreview", "Error al iniciar la cámara: ${exc.message}")
                    }
                }, ContextCompat.getMainExecutor(context))

                // Manejo de toques para enfoque
                setOnTouchListener { v, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        val factory = this.meteringPointFactory
                        val point = factory.createPoint(event.x, event.y)
                        val action = FocusMeteringAction.Builder(point).build()
                        cameraControl?.startFocusAndMetering(action)
                        onFocusTapped(event.x, event.y)
                        v.performClick()
                    }
                    true
                }
            }
        },
        update = { /* No se requieren actualizaciones dinámicas */ }
    )

    DisposableEffect(Unit) {
        onDispose {
            cameraProvider?.unbindAll()
            cameraControl = null
            analysisExecutor.shutdown() // Liberar el ejecutor
        }
    }
}

// Clase para procesar códigos de barras
class BarcodeAnalyzer(
    options: BarcodeScannerOptions,
    private val onBarcodesDetected: (List<Barcode>, ImageProxy) -> Unit
) : ImageAnalysis.Analyzer {
    private val scanner = BarcodeScanning.getClient(options)

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return
        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                onBarcodesDetected(barcodes, imageProxy)
            }
            .addOnFailureListener { e ->
                Log.e("BarcodeAnalyzer", "Error al procesar la imagen: ${e.message}")
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}