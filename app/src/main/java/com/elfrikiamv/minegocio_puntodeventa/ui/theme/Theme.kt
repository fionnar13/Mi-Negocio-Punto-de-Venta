package com.elfrikiamv.minegocio_puntodeventa.ui.theme

// Theme.kt — Tema de «سازمان فروشگاه»

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

/** Esquemas de color disponibles de la marca. */
enum class AppTheme {
    /** Oscuro (predeterminado): fondo #080B12, superficie #0E1220, acento #6366F1. */
    DARK,

    /** Claro: fondo #F4F5F0, superficie #FFFFFF, acento #0F766E. */
    LIGHT,

    /** Dorado: fondo #000000, superficie translúcida blanca, acento #D4AF37. */
    GOLD
}

/**
 * Esquema Oscuro (predeterminado).
 * Azul noche profundo con acento índigo, pensado para uso prolongado en punto de venta.
 */
private val DarkColorScheme = darkColorScheme(
    primary = DarkAccent,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF312E81),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFF818CF8),
    onSecondary = Color(0xFF1E1B4B),
    secondaryContainer = Color(0xFF3730A3),
    onSecondaryContainer = Color(0xFFE0E7FF),
    tertiary = Color(0xFF22D3EE),
    onTertiary = Color(0xFF083344),
    tertiaryContainer = Color(0xFF155E75),
    onTertiaryContainer = Color(0xFFCFFAFE),
    background = DarkBackground,
    onBackground = Color(0xFFE2E8F0),
    surface = DarkSurface,
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF1B2234),
    onSurfaceVariant = Color(0xFF98A2B8),
    surfaceDim = Color(0xFF0A0E18),
    surfaceBright = Color(0xFF1A2136),
    surfaceContainerLowest = Color(0xFF090D17),
    surfaceContainerLow = Color(0xFF101525),
    surfaceContainer = Color(0xFF141A2C),
    surfaceContainerHigh = Color(0xFF1A2136),
    surfaceContainerHighest = Color(0xFF202842),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFECACA),
    outline = Color(0xFF374151),
    outlineVariant = Color(0xFF232A3D),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFE2E8F0),
    inverseOnSurface = Color(0xFF0B0F19),
    inversePrimary = Color(0xFF4F46E5)
)

/**
 * Esquema Claro.
 * Blanco cálido con acento verde azulado para entientes con mucha luz ambiental.
 */
private val LightColorScheme = lightColorScheme(
    primary = LightAccent,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF99F6E4),
    onPrimaryContainer = Color(0xFF042F2E),
    secondary = Color(0xFF115E59),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF042F2E),
    tertiary = Color(0xFFB45309),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFEF3C7),
    onTertiaryContainer = Color(0xFF78350F),
    background = LightBackground,
    onBackground = Color(0xFF1C1917),
    surface = LightSurface,
    onSurface = Color(0xFF1C1917),
    surfaceVariant = Color(0xFFE2E4DA),
    onSurfaceVariant = Color(0xFF55534E),
    surfaceDim = Color(0xFFD9DAD3),
    surfaceBright = Color(0xFFFAFAF6),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF0F1EB),
    surfaceContainer = Color(0xFFEAECE4),
    surfaceContainerHigh = Color(0xFFE4E6DE),
    surfaceContainerHighest = Color(0xFFDEE1D7),
    error = Color(0xFFDC2626),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),
    outline = Color(0xFFA8A29E),
    outlineVariant = Color(0xFFD6D3D1),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF2E2C29),
    inverseOnSurface = Color(0xFFF1EFEB),
    inversePrimary = Color(0xFF5EEAD4)
)

/**
 * Esquema Dorado.
 * Negro absoluto con superficies blancas translúcidas y acento dorado de lujo.
 */
private val GoldColorScheme = darkColorScheme(
    primary = GoldAccent,
    onPrimary = Color(0xFF201A05),
    primaryContainer = Color(0xFF3D340D),
    onPrimaryContainer = Color(0xFFF5EAC0),
    secondary = Color(0xFFD9C06A),
    onSecondary = Color(0xFF1F1B0E),
    secondaryContainer = Color(0xFF2A2410),
    onSecondaryContainer = Color(0xFFEDE0A8),
    tertiary = Color(0xFFB8C0C8),
    onTertiary = Color(0xFF1B2228),
    tertiaryContainer = Color(0xFF2C343B),
    onTertiaryContainer = Color(0xFFD9E2E8),
    background = GoldBackground,
    onBackground = Color(0xFFF3EFE2),
    surface = GoldSurface, // blanco 12 % de opacidad
    onSurface = Color(0xFFF3EFE2),
    surfaceVariant = Color(0x29FFFFFF),
    onSurfaceVariant = Color(0xFFCFC7A8),
    surfaceDim = Color(0xFF050505),
    surfaceBright = Color(0xFF26241B),
    surfaceContainerLowest = Color(0x0DFFFFFF),
    surfaceContainerLow = Color(0x14FFFFFF),
    surfaceContainer = Color(0x1FFFFFFF),
    surfaceContainerHigh = Color(0x29FFFFFF),
    surfaceContainerHighest = Color(0x33FFFFFF),
    error = Color(0xFFE5484D),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFF5C1A1D),
    onErrorContainer = Color(0xFFFFDAD5),
    outline = Color(0xFF7A7050),
    outlineVariant = Color(0xFF3A3626),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFF3EFE2),
    inverseOnSurface = Color(0xFF14120A),
    inversePrimary = Color(0xFFB08D26)
)

/**
 * Tema de la aplicación «سازمان فروشگاه».
 *
 * - Aplica uno de los tres esquemas de color de marca (oscuro por defecto).
 * - Establece la tipografía Vazirmatn como fuente predeterminada.
 * - Fuerza la dirección de diseño RTL para todo el contenido Compose.
 *
 * @param theme esquema de color de marca a aplicar.
 */
@Composable
fun SazmanForooshgahTheme(
    theme: AppTheme = AppTheme.DARK,
    content: @Composable () -> Unit
) {
    val colorScheme = when (theme) {
        AppTheme.DARK -> DarkColorScheme
        AppTheme.LIGHT -> LightColorScheme
        AppTheme.GOLD -> GoldColorScheme
    }

    // Soporte RTL completo: todo el contenido Compose se dibuja de derecha a izquierda
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
