package com.elfrikiamv.minegocio_puntodeventa.data.repository

// ScanLogRepository.kt — مخزن لاگ اسکن

import com.elfrikiamv.minegocio_puntodeventa.data.dao.DeviceScanStat
import com.elfrikiamv.minegocio_puntodeventa.data.dao.ScanLogDao
import com.elfrikiamv.minegocio_puntodeventa.data.entity.ScanLogEntity

/**
 * مخزن لاگ اسکن بارکد.
 */
class ScanLogRepository(private val dao: ScanLogDao) {

    /** ثبت یک اسکن (همهٔ فیلدهای زمانی همین‌جا پر می‌شوند). */
    suspend fun log(device: String, barcode: String, success: Boolean) {
        dao.insert(
            ScanLogEntity(
                date = com.elfrikiamv.minegocio_puntodeventa.utils.PersianFormat.todayDateString(),
                time = com.elfrikiamv.minegocio_puntodeventa.utils.PersianFormat.nowTimeString(),
                timestamp = System.currentTimeMillis(),
                device = device,
                barcode = barcode,
                success = success
            )
        )
    }

    suspend fun betweenDates(from: String, to: String): List<ScanLogEntity> =
        dao.betweenDates(from, to)

    suspend fun deviceStats(from: String, to: String): List<DeviceScanStat> =
        dao.deviceStats(from, to)

    /** فهرست دستگاه‌های اسکنر (تب دستگاه‌ها). */
    suspend fun devices(): List<com.elfrikiamv.minegocio_puntodeventa.data.dao.ScannerDevice> =
        dao.devices()
}
