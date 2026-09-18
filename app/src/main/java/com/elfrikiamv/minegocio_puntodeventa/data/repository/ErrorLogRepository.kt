package com.elfrikiamv.minegocio_puntodeventa.data.repository

// ErrorLogRepository.kt — مخزن لاگ خطاها

import com.elfrikiamv.minegocio_puntodeventa.data.dao.ErrorLogDao
import com.elfrikiamv.minegocio_puntodeventa.data.dao.LevelCount
import com.elfrikiamv.minegocio_puntodeventa.data.entity.ErrorLogEntity
import kotlinx.coroutines.flow.Flow

/**
 * مخزن لاگ خطاها (تب دیباگر).
 */
class ErrorLogRepository(private val dao: ErrorLogDao) {

    suspend fun insert(entry: ErrorLogEntity): Long = dao.insert(entry)

    /** آخرین لاگ‌ها برای نمایش. */
    fun observeRecent(): Flow<List<ErrorLogEntity>> = dao.observeRecent()

    /** شمارش هر سطح. */
    fun observeCounts(): Flow<List<LevelCount>> = dao.observeCounts()

    suspend fun deleteAll() = dao.deleteAll()
}
