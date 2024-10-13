package com.elfrikiamv.minegocio_puntodeventa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.elfrikiamv.minegocio_puntodeventa.navigation.NavGraph
import com.elfrikiamv.minegocio_puntodeventa.ui.theme.MiNegocioPuntodeVentaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        //Splash Screen
        installSplashScreen()
        setContent {
            MiNegocioPuntodeVentaTheme {
                // Recordar el controlador de navegación
                val navController = rememberNavController()
                // Configurar el gráfico de navegación
                NavGraph(navController = navController)
            }
        }
    }
}