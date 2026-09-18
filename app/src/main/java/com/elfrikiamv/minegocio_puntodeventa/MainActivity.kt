package com.elfrikiamv.minegocio_puntodeventa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.shell.AppShell
import com.elfrikiamv.minegocio_puntodeventa.ui.theme.AppThemeState
import com.elfrikiamv.minegocio_puntodeventa.ui.theme.SazmanForooshgahTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Splash Screen
        installSplashScreen()
        setContent {
            // پوستهٔ زنده — از تب «ظاهر» در تنظیمات تغییر می‌کند
            val theme by AppThemeState.current.collectAsState()
            SazmanForooshgahTheme(theme = theme) {
                // پوستهٔ اصلی «سازمان فروشگاه»
                AppShell()
            }
        }
    }
}
