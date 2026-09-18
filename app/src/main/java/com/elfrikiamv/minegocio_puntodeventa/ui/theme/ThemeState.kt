package com.elfrikiamv.minegocio_puntodeventa.ui.theme

// ThemeState.kt — وضعیت سراسری پوستهٔ رنگی (برای تغییر زنده از تنظیمات)

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * نگه‌دارندهٔ سراسری پوستهٔ جاری؛ تب «ظاهر» در تنظیمات این مقدار را
 * تغییر می‌دهد و MainActivity بلافاصله پوستهٔ جدید را اعمال می‌کند.
 */
object AppThemeState {

    private val _current = MutableStateFlow(AppTheme.DARK)

    /** پوستهٔ فعال برنامه. */
    val current: StateFlow<AppTheme> = _current

    /** مقدار متناظر رشتهٔ ذخیره‌شده در تنظیمات. */
    fun fromSettings(value: String): AppTheme = when (value) {
        "light" -> AppTheme.LIGHT
        "gold" -> AppTheme.GOLD
        else -> AppTheme.DARK
    }

    fun set(theme: AppTheme) {
        _current.value = theme
    }
}
