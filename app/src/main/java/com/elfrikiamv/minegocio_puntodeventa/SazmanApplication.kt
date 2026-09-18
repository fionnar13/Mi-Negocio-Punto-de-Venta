package com.elfrikiamv.minegocio_puntodeventa

// SazmanApplication.kt — کلاس Application (راه‌اندازی لاگر و هندلر خطاهای مهلک)

import android.app.Application
import android.content.Context
import com.elfrikiamv.minegocio_puntodeventa.utils.AppLog
import org.osmdroid.config.Configuration

/**
 * کلاس Application «سازمان فروشگاه».
 *
 * - لاگر Room را راه‌اندازی می‌کند.
 * - خطاهای مهلک نخ‌ها را در جدول error_logs ثبت کرده و سپس
 *   به هندلر پیش‌فرض سیستم سپرده می‌شود (کرش‌دیلوگ همچنان نمایش داده می‌شود).
 */
class SazmanApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // لاگر (نوشتن در جدول error_logs)
        AppLog.init(this)

        // شناسهٔ کاربر osmdroid برای بارگیری کاشی‌های نقشه
        runCatching {
            Configuration.getInstance().userAgentValue = packageName
        }

        // هندلر سراسری خطاهای مهلک
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            AppLog.logCrashBlocking(thread, throwable)
            previous?.uncaughtException(thread, throwable)
        }

        AppLog.i("APP", "برنامه راه‌اندازی شد")
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }
}
