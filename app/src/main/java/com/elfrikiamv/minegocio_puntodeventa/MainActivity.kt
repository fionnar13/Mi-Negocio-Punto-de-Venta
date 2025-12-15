package com.elfrikiamv.minegocio_puntodeventa

import android.Manifest
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.elfrikiamv.minegocio_puntodeventa.navigation.NavGraph
import com.elfrikiamv.minegocio_puntodeventa.ui.theme.MiNegocioPuntodeVentaTheme
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //  Splash Screen
        installSplashScreen()

        // firebase messaging
        firebaseMessagingToken()

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

    private fun firebaseMessagingToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("firebaseMessagingToken", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result.toString()
            //val msg = (token)
            Log.d("firebaseMessagingToken", token)

        })
    }
}