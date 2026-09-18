package com.elfrikiamv.minegocio_puntodeventa.data.dao

// SessionDao.kt

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.elfrikiamv.minegocio_puntodeventa.data.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: SessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sessions: List<SessionEntity>): List<Long>

    @Update
    suspend fun update(session: SessionEntity)

    @Delete
    suspend fun delete(session: SessionEntity)

    @Query("DELETE FROM sessions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM sessions")
    suspend fun deleteAll()

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getById(id: Long): SessionEntity?

    @Query("SELECT * FROM sessions ORDER BY id DESC")
    fun getAll(): Flow<List<SessionEntity>>

    /** آخرین نشست (نشست جاری صندوق). */
    @Query("SELECT * FROM sessions ORDER BY id DESC LIMIT 1")
    suspend fun getLatest(): SessionEntity?

    /** بستن شیفت. */
    @Query("UPDATE sessions SET endTime = :end WHERE id = :id")
    suspend fun close(id: Long, end: Long)

    @Query("SELECT * FROM sessions WHERE userId = :userId ORDER BY id DESC")
    fun getByUserId(userId: Long): Flow<List<SessionEntity>>

    /** فهرست کامل نشست‌ها (یک‌بار، برای گزارش‌ها). */
    @Query("SELECT * FROM sessions")
    suspend fun getAllOnce(): List<SessionEntity>
}
