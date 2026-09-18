package com.elfrikiamv.minegocio_puntodeventa.data.dao

// ErrorLogDao.kt — لاگ خطاها

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.elfrikiamv.minegocio_puntodeventa.data.entity.ErrorLogEntity
import kotlinx.coroutines.flow.Flow

/** شمارش هر سطح لاگ. */
data class LevelCount(
    val level: String,
    val cnt: Int
)

@Dao
interface ErrorLogDao {

    @Insert
    suspend fun insert(entry: ErrorLogEntity): Long

    /** آخرین لاگ‌ها (جدیدترین اول، حداکثر ۲۰۰ رکورد). */
    @Query("SELECT * FROM error_logs ORDER BY ts DESC, id DESC LIMIT 200")
    fun observeRecent(): Flow<List<ErrorLogEntity>>

    /** شمارش به تفکیک سطح. */
    @Query("SELECT level, COUNT(*) AS cnt FROM error_logs GROUP BY level")
    fun observeCounts(): Flow<List<LevelCount>>

    @Query("DELETE FROM error_logs")
    suspend fun deleteAll()
}
