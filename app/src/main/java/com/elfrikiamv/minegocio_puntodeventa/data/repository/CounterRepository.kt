package com.elfrikiamv.minegocio_puntodeventa.data.repository

// CounterRepository.kt

import com.elfrikiamv.minegocio_puntodeventa.data.dao.CounterDao
import com.elfrikiamv.minegocio_puntodeventa.data.entity.CounterEntity

/**
 * مخزن شمارنده‌های شماره فاکتور (جدول تک‌ردیفی).
 *
 * افزایش شماره اتمیک در سطح SQL انجام می‌شود؛ چون برنامه تک‌فرایندی است
 * خواندن مقدار بعد از افزایش، به‌قدر کافی امن است.
 */
class CounterRepository(private val dao: CounterDao) {

    suspend fun get(): CounterEntity? = dao.get()

    /** شمارهٔ فاکتور فروش بعدی را می‌گیرد، شمارنده را یک واحد افزایش می‌دهد
     *  و شمارهٔ جدید را برمی‌گرداند. */
    suspend fun nextSaleNo(): Long {
        ensureInitialized()
        dao.incrementSaleNo()
        return dao.get()?.saleNo ?: 1L
    }

    /** شمارهٔ فاکتور خرید بعدی را می‌گیرد، شمارنده را یک واحد افزایش می‌دهد
     *  و شمارهٔ جدید را برمی‌گرداند. */
    suspend fun nextPurchNo(): Long {
        ensureInitialized()
        dao.incrementPurchNo()
        return dao.get()?.purchNo ?: 1L
    }

    /** بازنشانی شمارنده‌ها. */
    suspend fun reset() {
        dao.deleteAll()
        dao.upsert(CounterEntity())
    }

    private suspend fun ensureInitialized() {
        if (dao.get() == null) {
            dao.upsert(CounterEntity())
        }
    }
}
