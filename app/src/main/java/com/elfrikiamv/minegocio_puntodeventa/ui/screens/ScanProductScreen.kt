package com.elfrikiamv.minegocio_puntodeventa.ui.screens

// ScanProductScreen.kt

import android.Manifest
import android.util.Log
import android.util.Size
import android.view.MotionEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.navigation.NavHostController
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.ScanProductViewModel
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanProductScreen(
    //scanProductViewModel: ScanProductViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val scanProductViewModel: ScanProductViewModel = viewModel()

    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            scanProductViewModel.setCameraError("Permiso de cámara denegado")
        }
    }

    // Solicitar permiso de cámara
    LaunchedEffect(Unit) {
        scanProductViewModel.checkCameraPermission(context) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val cameraError by scanProductViewModel.cameraError.collectAsState()
    var showFocusIndicator by remember { mutableStateOf(false) }
    var focusPoint by remember { mutableStateOf(Offset.Zero) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escanear Producto") },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Regresar",
                        modifier = Modifier.clickable { navController.popBackStack() }
                    )
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
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
                        scanProductViewModel.onBarcodeDetected(barcode)
                    },
                    onFocusTapped = { x, y ->
                        focusPoint = Offset(x, y)
                        showFocusIndicator = true
                    }
                )
                if (showFocusIndicator) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color.Green,
                            radius = 50f,
                            center = focusPoint
                        )
                    }
                    LaunchedEffect(Unit) {
                        delay(500)
                        showFocusIndicator = false
                    }
                }
            }
        }
    }
}

@Composable
private fun CameraPreview(
    onBarcodeDetected: (String) -> Unit,
    onFocusTapped: (Float, Float) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var cameraControl: CameraControl? by remember { mutableStateOf(null) }
    var cameraProvider: ProcessCameraProvider? by remember { mutableStateOf(null) }
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

                    val barcodeScannerOptions = BarcodeScannerOptions.Builder().build()

                    val barcodeAnalyzer = BarcodeAnalyzer(barcodeScannerOptions) { barcodes, _ ->
                        barcodes.firstOrNull()?.rawValue?.let(onBarcodeDetected)
                    }

                    val analysisUseCase = ImageAnalysis.Builder()
                        .setTargetResolution(Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(analysisExecutor, barcodeAnalyzer)
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
        update = { }
    )

    DisposableEffect(Unit) {
        onDispose {
            cameraProvider?.unbindAll()
            cameraControl = null
            analysisExecutor.shutdown()
        }
    }
}