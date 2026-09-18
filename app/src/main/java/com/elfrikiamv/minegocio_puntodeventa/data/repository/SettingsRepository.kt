package com.elfrikiamv.minegocio_puntodeventa.data.repository

// SettingsRepository.kt

import com.elfrikiamv.minegocio_puntodeventa.data.dao.SettingsDao
import com.elfrikiamv.minegocio_puntodeventa.data.entity.SettingsEntity

/**
 * مخزن تنظیمات فروشگاه (جدول تک‌ردیفی).
 */
class SettingsRepository(private val dao: SettingsDao) {

    /** تنظیمات ذخیره‌شده یا null اگر هنوز ذخیره نشده است. */
    suspend fun get(): SettingsEntity? = dao.get()

    /** تنظیمات ذخیره‌شده یا مقادیر پیش‌فرض. */
    suspend fun getOrDefaults(): SettingsEntity = dao.get() ?: SettingsEntity()

    /** ذخیرهٔ تنظیمات (همیشه روی ردیف ۱). */
    suspend fun save(settings: SettingsEntity) = dao.upsert(settings.copy(id = 1))

    /** بازنشانی به پیش‌فرض‌ها. */
    suspend fun reset() {
        dao.deleteAll()
        dao.upsert(SettingsEntity())
    }
}
