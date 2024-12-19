package com.elfrikiamv.minegocio_puntodeventa.ui.screens

// ScanScreen.kt

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.navigation.NavController
import com.elfrikiamv.minegocio_puntodeventa.model.Product
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.InventoryViewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    viewModel: InventoryViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var cameraError by remember { mutableStateOf<String?>(null) }
    var showFocusIndicator by remember { mutableStateOf(false) }
    var focusPoint by remember { mutableStateOf(Offset.Zero) }
    var lastScanTime by remember { mutableLongStateOf(0L) }
    var scannedProduct by remember { mutableStateOf<Product?>(null) }
    var quantityDialogVisible by remember { mutableStateOf(false) }
    var screenColor by remember { mutableStateOf(Color.White) }

    val context = LocalContext.current
    val cameraPermission = Manifest.permission.CAMERA
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            cameraError = "Permiso de cámara denegado"
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
        Box(
            modifier = modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(screenColor)
        ) {
            if (cameraError != null) {
                Text(
                    text = cameraError ?: "Error desconocido",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                CameraPreview(
                    onBarcodeDetected = { barcode ->
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastScanTime >= 2000) {
                            lastScanTime = currentTime
                            viewModel.checkProductExists(barcode) { product ->
                                if (product != null) {
                                    scannedProduct = product
                                    screenColor = Color.Green
                                    quantityDialogVisible = true
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Producto no encontrado",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    },
                    onFocusTapped = { x, y ->
                        focusPoint = Offset(x, y)
                        showFocusIndicator = true
                    }
                )
            }
        }
    }

    if (quantityDialogVisible && scannedProduct != null) {
        QuantityDialog(
            product = scannedProduct!!,
            onConfirm = { quantity ->
                viewModel.addToCart(
                    scannedProduct!!.copy(
                        quantity = quantity
                    )
                )
                screenColor = Color.White
                quantityDialogVisible = false
            },
            onDismiss = {
                screenColor = Color.White
                quantityDialogVisible = false
            }
        )
    }
}

@Composable
fun QuantityDialog(product: Product, onConfirm: (Int) -> Unit, onDismiss: () -> Unit) {
    var quantity by remember { mutableStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar ${product.name}") },
        text = {
            Column {
                Text("Ingrese la cantidad:")
                TextField(
                    value = quantity.toString(),
                    onValueChange = { quantity = it.toIntOrNull() ?: 1 }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(quantity) }) {
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

// Procesar el código de barras detectado
/*private fun processBarcode(
    barcode: String,
    viewModel: InventoryViewModel,
    context: Context
) {
    // Verificar si el producto existe en el inventario
    viewModel.checkProductExists(barcode) { product ->
        val productInfo = product?.let {
            "Producto: ${it.name}, Código: ${it.barcode}"
        } ?: "Producto no existe en el inventario"

        // Mostrar un mensaje con el resultado
        Toast.makeText(context, productInfo, Toast.LENGTH_SHORT).show()
        Log.d("ScanScreen", productInfo)

        // Vibrar al detectar código de barras
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(
            VibrationEffect.createOneShot(
                200,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
        )
    }
}*/

// Componente para la vista previa de la cámara
@Composable
fun CameraPreview(
    onBarcodeDetected: (String) -> Unit, // Solo se pasa el código de barras
    onFocusTapped: (Float, Float) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var cameraControl: CameraControl? by remember { mutableStateOf(null) }
    var cameraProvider: ProcessCameraProvider? by remember { mutableStateOf(null) }

    // Vista previa de la cámara usando AndroidView
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            PreviewView(ctx).apply {
                // Inicializar el proveedor de cámara
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(surfaceProvider)
                    }

                    // Crear el analizador de códigos de barras
                    val barcodeAnalyzer = BarcodeAnalyzer { barcodes, _ ->
                        // Llamar a la función cuando se detecta un código de barras
                        barcodes.firstOrNull()?.rawValue?.let { barcode ->
                            onBarcodeDetected(barcode) // Pasar el código de barras directamente
                        }
                    }

                    // Crear la instancia de análisis de imágenes
                    val analysisUseCase = ImageAnalysis.Builder().build().also {
                        it.setAnalyzer(ContextCompat.getMainExecutor(context), barcodeAnalyzer)
                    }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider?.unbindAll() // Desvincular cámaras anteriores
                        val camera = cameraProvider?.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            analysisUseCase
                        )
                        cameraControl = camera?.cameraControl // Control de la cámara
                    } catch (exc: Exception) {
                        Log.e("CameraPreview", "Error al iniciar la cámara: ${exc.message}")
                    }
                }, ContextCompat.getMainExecutor(context))

                // Manejar toques en la pantalla para enfocar
                setOnTouchListener { v, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        val factory = this.meteringPointFactory
                        val point = factory.createPoint(event.x, event.y)
                        val action = FocusMeteringAction.Builder(point).build()
                        cameraControl?.startFocusAndMetering(action) // Iniciar enfoque
                        onFocusTapped(event.x, event.y) // Llamar para mostrar el indicador
                        v.performClick()
                    }
                    true
                }
            }
        },
        update = { /* Manejar actualizaciones si es necesario */ }
    )

    // Limpiar recursos cuando la composición se desmonte
    DisposableEffect(Unit) {
        onDispose {
            cameraProvider?.unbindAll() // Desvincular cámara
            cameraControl = null // Limpiar control de cámara
        }
    }
}

// Clase analizador de códigos de barras
class BarcodeAnalyzer(private val onBarcodesDetected: (List<Barcode>, ImageProxy) -> Unit) :
    ImageAnalysis.Analyzer {
    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return
        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        // Procesar la imagen para detectar códigos de barras
        BarcodeScanning.getClient().process(inputImage)
            .addOnSuccessListener { barcodes ->
                onBarcodesDetected(
                    barcodes,
                    imageProxy
                ) // Llamar cuando se detectan códigos de barras
            }
            .addOnCompleteListener {
                imageProxy.close() // Cerrar el proxy de imagen después de procesar
            }
            .addOnFailureListener { e ->
                Log.e("BarcodeAnalyzer", "Error al procesar la imagen: ${e.message}")
            }
    }
}

