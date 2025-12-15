package com.elfrikiamv.minegocio_puntodeventa

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.elfrikiamv.minegocio_puntodeventa.navigation.NavGraph
import com.elfrikiamv.minegocio_puntodeventa.ui.theme.MiNegocioPuntodeVentaTheme
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //  Splash Screen
        installSplashScreen()
        MobileAds.initialize(this) {
            Log.d("mainActivityLog", "onCreate: $it")
        }
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