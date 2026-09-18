package com.elfrikiamv.minegocio_puntodeventa.ui.theme

// Type.kt — Tipografía Vazirmatn

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.elfrikiamv.minegocio_puntodeventa.R

/**
 * Familia tipográfica Vazirmatn (Google Fonts), ideal para persa (farsi)
 * y con excelente legibilidad para números y textos latinos.
 */
val VazirmatnFontFamily = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_semibold, FontWeight.SemiBold),
    Font(R.font.vazirmatn_bold, FontWeight.Bold)
)

private val base = Typography()

/** Tipografía de la app: Vazirmatn como fontFamily por defecto en todos los estilos. */
val AppTypography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = VazirmatnFontFamily),
    displayMedium = base.displayMedium.copy(fontFamily = VazirmatnFontFamily),
    displaySmall = base.displaySmall.copy(fontFamily = VazirmatnFontFamily),
    headlineLarge = base.headlineLarge.copy(fontFamily = VazirmatnFontFamily),
    headlineMedium = base.headlineMedium.copy(fontFamily = VazirmatnFontFamily),
    headlineSmall = base.headlineSmall.copy(fontFamily = VazirmatnFontFamily),
    titleLarge = base.titleLarge.copy(fontFamily = VazirmatnFontFamily),
    titleMedium = base.titleMedium.copy(fontFamily = VazirmatnFontFamily),
    titleSmall = base.titleSmall.copy(fontFamily = VazirmatnFontFamily),
    bodyLarge = base.bodyLarge.copy(fontFamily = VazirmatnFontFamily),
    bodyMedium = base.bodyMedium.copy(fontFamily = VazirmatnFontFamily),
    bodySmall = base.bodySmall.copy(fontFamily = VazirmatnFontFamily),
    labelLarge = base.labelLarge.copy(fontFamily = VazirmatnFontFamily),
    labelMedium = base.labelMedium.copy(fontFamily = VazirmatnFontFamily),
    labelSmall = base.labelSmall.copy(fontFamily = VazirmatnFontFamily)
)
