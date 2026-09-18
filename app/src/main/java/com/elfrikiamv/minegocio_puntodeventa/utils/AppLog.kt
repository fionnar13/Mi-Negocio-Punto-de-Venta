package com.elfrikiamv.minegocio_puntodeventa.utils

// AppLog.kt — لاگ برنامه با ذخیره‌سازی در Room (تب دیباگر)

import android.content.Context
import android.os.Build
import com.elfrikiamv.minegocio_puntodeventa.data.entity.ErrorLogEntity
import com.elfrikiamv.minegocio_puntodeventa.data.local.SazmanDatabase
import com.elfrikiamv.minegocio_puntodeventa.data.repository.ErrorLogRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

/**
 * لاگر سراسری برنامه؛ رکوردها در جدول error_logs ذخیره می‌شوند.
 *
 * پیش از [init] فقط در بافر حافظه نگه‌داری می‌شود. خطاهای مهلک
 * (UncaughtExceptionHandler) با [logCrashBlocking] همگام نوشته می‌شوند.
 */
object AppLog {

    private var repo: ErrorLogRepository? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** بافر پیش از init (یا در صورت خطای پایگاه داده). */
    private val buffer = ArrayDeque<ErrorLogEntity>()
    private const val BUFFER_MAX = 200

    /** مشخصات دستگاه/برنامه — در همهٔ رکوردها ذخیره می‌شود. */
    @Volatile
    var userAgent: String = ""
        private set

    /** نشانی اخیر (مثل نشانی سرور) — اختیاری. */
    @Volatile
    var lastUrl: String = ""

    /** راه‌اندازی لاگر (از کلاس Application). */
    fun init(context: Context) {
        if (repo != null) return
        repo = ErrorLogRepository(SazmanDatabase.getDatabase(context).errorLogDao())
        userAgent = runCatching {
            val pm = context.packageManager.getPackageInfo(context.packageName, 0)
            "${Build.MANUFACTURER} ${Build.MODEL} · Android ${Build.VERSION.RELEASE} · ${context.packageName} ${pm.versionName}"
        }.getOrElse { "${Build.MANUFACTURER} ${Build.MODEL} · Android ${Build.VERSION.RELEASE}" }
        // تخلیهٔ بافر اولیه
        while (buffer.isNotEmpty()) {
            val e = buffer.removeFirst()
            scope.launch { runCatching { repo?.insert(e) } }
        }
    }

    fun e(scopeName: String, message: String, error: Throwable? = null) {
        log(
            ErrorLogEntity.LEVEL_ERROR, scopeName, message,
            technical = error?.let { android.util.Log.getStackTraceString(it) } ?: ""
        )
    }

    fun w(scopeName: String, message: String) =
        log(ErrorLogEntity.LEVEL_WARN, scopeName, message, "")

    fun i(scopeName: String, message: String) =
        log(ErrorLogEntity.LEVEL_INFO, scopeName, message, "")

    fun d(scopeName: String, message: String) =
        log(ErrorLogEntity.LEVEL_DEBUG, scopeName, message, "")

    /** ثبت رکورد (آsync، بهترین‌تلاش). */
    fun log(
        level: String,
        scopeName: String,
        message: String,
        technical: String,
        contextName: String = ""
    ) {
        val entry = ErrorLogEntity(
            ts = System.currentTimeMillis(),
            level = level,
            scope = scopeName,
            message = message,
            technical = technical,
            context = contextName,
            userAgent = userAgent,
            url = lastUrl
        )
        val r = repo
        if (r == null) {
            synchronized(buffer) {
                buffer.addLast(entry)
                if (buffer.size > BUFFER_MAX) buffer.removeFirst()
            }
        } else {
            scope.launch { runCatching { r.insert(entry) } }
        }
    }

    /**
     * ثبت همگام برای خطاهای مهلک — پیش از سپردن به هندلر پیش‌فرض
     * (با مهلت ۲.۵ ثانیه تا فرآیند معلق نماند).
     */
    fun logCrashBlocking(thread: Thread, error: Throwable) {
        val entry = ErrorLogEntity(
            ts = System.currentTimeMillis(),
            level = ErrorLogEntity.LEVEL_ERROR,
            scope = "CRASH",
            message = "${error.javaClass.simpleName}: ${error.message ?: "بدون پیام"}",
            technical = android.util.Log.getStackTraceString(error),
            context = "thread=${thread.name}",
            userAgent = userAgent,
            url = lastUrl
        )
        val r = repo
        if (r != null) {
            runCatching {
                runBlocking {
                    withTimeout(2_500) { r.insert(entry) }
                }
            }
        }
    }
}
