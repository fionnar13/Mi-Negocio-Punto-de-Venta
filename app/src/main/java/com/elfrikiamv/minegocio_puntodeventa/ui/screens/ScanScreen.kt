package com.elfrikiamv.minegocio_puntodeventa.ui.screens

// Importaciones necesarias

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
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
import androidx.navigation.NavController
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.InventoryViewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

@OptIn(ExperimentalMaterial3Api::class) // Habilitamos el uso de APIs experimentales de Material 3
@Composable
fun ScanScreen( // Composable que representa la pantalla de escaneo de códigos de barras
    viewModel: InventoryViewModel, // El ViewModel que gestiona la lógica del inventario
    navController: NavController,  // Controlador de navegación para gestionar la navegación entre pantallas
    modifier: Modifier = Modifier   // Modificador opcional para personalizar el diseño del Composable
) {
    var scanResult by remember { mutableStateOf<String?>(null) } // Estado para almacenar el resultado del escaneo
    var productInfo by remember { mutableStateOf<String?>(null) } // Estado para almacenar la información del producto

    // Solicitar permisos de cámara
    val cameraPermission =
        Manifest.permission.CAMERA // Definimos el permiso que queremos solicitar: el de cámara
    val context = LocalContext.current // Obtenemos el contexto actual de la aplicación
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> // Verificamos si el usuario ha concedido o no el permiso de cámara
        if (!isGranted) {
            // Si el permiso es denegado, mostramos un mensaje Toast y un log en la consola
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
            Log.e("ScanScreen", "Permiso de cámara denegado")
        }
    }

    // Efecto lanzado al iniciar la pantalla, para solicitar el permiso de cámara si no ha sido otorgado
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                cameraPermission
            ) != PackageManager.PERMISSION_GRANTED // Comprobamos si el permiso ya ha sido concedido
        ) {
            // Si no se ha concedido, lanzamos la solicitud de permiso
            permissionLauncher.launch(cameraPermission)
        }
    }

    // Scaffold define la estructura básica de la pantalla con una barra superior y un cuerpo
    Scaffold(
        topBar = {
            // Barra superior que muestra el título de la pantalla
            TopAppBar(title = { Text("Escanear Código de Barras") })
        }
    ) { paddingValues ->
        // Caja que contiene el contenido de la pantalla, incluyendo el texto y la vista de la cámara
        Box(modifier = modifier.padding(paddingValues)) {
            // Mostramos la información del producto o un mensaje por defecto en el centro de la pantalla
            Text(
                text = productInfo ?: "Escanea un código de barras",
                modifier = Modifier.align(Alignment.Center)
            )
            // Vista de la cámara, que inicia el escaneo de código de barras
            CameraPreview { barcode ->
                // Cuando se detecta un código de barras, almacenamos el resultado en scanResult
                scanResult = barcode
                // Verificamos si el producto existe en el inventario
                viewModel.checkProductExists(barcode) { product ->
                    // Si el producto existe, mostramos su nombre y código de barras; si no, un mensaje de que no existe
                    productInfo = product?.let {
                        "Producto: ${it.name}, Código: ${it.barcode}"
                    } ?: "Producto no existe en el inventario"

                    // Mostramos el resultado en un Toast y lo registramos en el log
                    Toast.makeText(context, productInfo, Toast.LENGTH_SHORT).show()
                    Log.d("ScanScreen", productInfo ?: "Producto no encontrado")
                }
            }
        }
    }
}

@Composable
fun CameraPreview(onBarcodeDetected: (String) -> Unit) {
    // Composable que muestra la vista previa de la cámara y analiza los códigos de barras

    val context = LocalContext.current // Obtenemos el contexto de la aplicación
    val lifecycleOwner =
        androidx.lifecycle.compose.LocalLifecycleOwner.current // Obtenemos el dueño del ciclo de vida para gestionar la cámara

    // AndroidView permite integrar vistas nativas de Android en Composables
    AndroidView(
        modifier = Modifier.fillMaxSize(), // Ocupamos todo el tamaño disponible con la vista de la cámara
        factory = { ctx ->
            PreviewView(ctx).apply {
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

                cameraProviderFuture.addListener(
                    {
                        // Una vez que la cámara está lista, obtenemos el proveedor de cámara
                        val cameraProvider = cameraProviderFuture.get()

                        // Creamos una vista previa de la cámara
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(surfaceProvider) // Asignamos el proveedor de superficie para mostrar la imagen
                        }

                        // Creamos un analizador de códigos de barras
                        val barcodeAnalyzer = BarcodeAnalyzer { barcodes ->
                            // Si se detecta un código de barras, pasamos el valor a la función onBarcodeDetected
                            barcodes.firstOrNull()?.rawValue?.let { barcode ->
                                onBarcodeDetected(barcode)
                            }
                        }

                        // Seleccionamos la cámara trasera
                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        // Creamos un caso de uso para el análisis de imágenes
                        val analysisUseCase = ImageAnalysis.Builder().build().also {
                            it.setAnalyzer(
                                ContextCompat.getMainExecutor(context),
                                barcodeAnalyzer
                            ) // Asignamos el analizador a la cámara
                        }

                        try {
                            // Desvinculamos cualquier cámara que esté en uso y vinculamos la cámara nueva al ciclo de vida
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner, // Vinculamos la cámara al ciclo de vida actual
                                cameraSelector, // Usamos la cámara seleccionada (trasera)
                                preview, // Vista previa de la cámara
                                analysisUseCase // Caso de uso para el análisis de imágenes
                            )
                        } catch (exc: Exception) {
                            // En caso de error, lo registramos en el log
                            Log.e("CameraPreview", "Error al iniciar la cámara: ${exc.message}")
                        }
                    },
                    ContextCompat.getMainExecutor(context)
                ) // Ejecutamos el código en el hilo principal
            }
        }
    )
}

// Clase que analiza la imagen de la cámara en busca de códigos de barras
class BarcodeAnalyzer(private val onBarcodesDetected: (List<Barcode>) -> Unit) :
    ImageAnalysis.Analyzer {
    @androidx.annotation.OptIn(ExperimentalGetImage::class) // Habilitamos el uso de APIs experimentales
    override fun analyze(imageProxy: ImageProxy) {
        // Obtenemos la imagen de la cámara
        val mediaImage = imageProxy.image ?: return
        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        // Procesamos la imagen en busca de códigos de barras
        BarcodeScanning.getClient().process(inputImage)
            .addOnSuccessListener { barcodes ->
                // Si se detectan códigos de barras, los pasamos a la función onBarcodesDetected
                onBarcodesDetected(barcodes)
            }
            .addOnCompleteListener {
                // Cerramos la imagen procesada para liberar recursos
                imageProxy.close()
            }
            .addOnFailureListener { e ->
                // Si ocurre un error, lo registramos en el log
                Log.e("BarcodeAnalyzer", "Error al procesar la imagen: ${e.message}")
            }
    }
}
