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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
    var cameraError by remember { mutableStateOf<String?>(null) }
    var showFocusIndicator by remember { mutableStateOf(false) }
    var focusPoint by remember { mutableStateOf(Offset.Zero) }

    val cameraPermission = Manifest.permission.CAMERA
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
            cameraError = "Permiso de cámara denegado"
        } else {
            cameraError = null
        }
    }

    // Solicitar permiso de cámara si no está concedido
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
            if (cameraError != null) {
                Text(
                    text = cameraError ?: "Error desconocido",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                CameraPreview(
                    onBarcodeDetected = { barcode ->
                        if (!isCooldownActive) {
                            scanResult = barcode
                            viewModel.checkProductExists(barcode) { product ->
                                productInfo = product?.let {
                                    "Producto: ${it.name}, Código: ${it.barcode}"
                                } ?: "Producto no existe en el inventario"

                                Toast.makeText(context, productInfo, Toast.LENGTH_SHORT).show()
                                Log.d("ScanScreen", productInfo ?: "Producto no encontrado")

                                // Vibrar al detectar código de barras
                                val vibrator =
                                    context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                                vibrator.vibrate(
                                    VibrationEffect.createOneShot(
                                        200,
                                        VibrationEffect.DEFAULT_AMPLITUDE
                                    )
                                )

                                // Activar cooldown para evitar múltiples lecturas
                                isCooldownActive = true
                            }
                        }
                    },
                    onFocusTapped = { x, y ->
                        // Mostrar el indicador de enfoque en la pantalla
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

                    // Ocultar el indicador después de 0.5 segundos
                    LaunchedEffect(Unit) {
                        delay(500)
                        showFocusIndicator = false
                    }
                }

                // Mostrar CircularProgressIndicator si está en cooldown
                if (isCooldownActive) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(top = 16.dp)
                    )

                    // Manejar cooldown de 3 segundos después de un escaneo
                    LaunchedEffect(isCooldownActive) {
                        delay(3000)
                        isCooldownActive = false
                    }
                }
            }
        }
    }
}

@Composable
fun CameraPreview(
    onBarcodeDetected: (String) -> Unit,
    onFocusTapped: (Float, Float) -> Unit // Añadido para manejar los toques en la pantalla
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var cameraControl: CameraControl? by remember { mutableStateOf(null) }
    var cameraProvider: ProcessCameraProvider? by remember { mutableStateOf(null) }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            PreviewView(ctx).apply {
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(surfaceProvider)
                    }

                    val barcodeAnalyzer = BarcodeAnalyzer { barcodes ->
                        barcodes.firstOrNull()?.rawValue?.let { barcode ->
                            onBarcodeDetected(barcode)
                        }
                    }

                    val analysisUseCase = ImageAnalysis.Builder().build().also {
                        it.setAnalyzer(ContextCompat.getMainExecutor(context), barcodeAnalyzer)
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

                // Manejar toques en la pantalla para enfocar
                setOnTouchListener { v, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        val factory = this.meteringPointFactory
                        val point = factory.createPoint(event.x, event.y)
                        val action = FocusMeteringAction.Builder(point).build()
                        cameraControl?.startFocusAndMetering(action)
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
            cameraProvider?.unbindAll()
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