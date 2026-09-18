package com.elfrikiamv.minegocio_puntodeventa.utils

// ShareUtils.kt — اشتراک‌گذاری متن فاکتور (تلگرام/واتساپ/بله/عمومی)

import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.content.ContentValues
import java.io.OutputStream
import com.elfrikiamv.minegocio_puntodeventa.R

/** مقصدهای اشتراک‌گذاری. */
enum class ShareTarget(val faTitle: String, val icon: String) {
    TELEGRAM("تلگرام", "✈️"),
    WHATSAPP("واتساپ", "🟢"),
    BALE("بله", "🟣"),
    OTHER("اشتراک‌گذاری…", "📤")
}

/**
 * ابزار اشتراک‌گذاری متن.
 */
object ShareUtils {

    /** بسته‌های کاندید برای هر مقصد (به ترتیب اولویت). */
    private val PACKAGES = mapOf(
        ShareTarget.TELEGRAM to listOf("org.telegram.messenger", "org.telegram.messenger.web"),
        ShareTarget.WHATSAPP to listOf("com.whatsapp", "com.whatsapp.w4b"),
        ShareTarget.BALE to listOf("ir.nasim", "im.bale.messenger")
    )

    /**
     * اشتراک‌گذاری متن با مقصد دلخواه؛ اگر بسته نصب نباشد،
     * انتخابگر عمومی باز می‌شود.
     */
    fun share(context: Context, text: String, target: ShareTarget) {
        val base = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val intent: Intent = PACKAGES[target]?.firstOrNull { pkg ->
            runCatching { context.packageManager.getPackageInfo(pkg, 0) }.isSuccess
        }?.let { pkg ->
            base.apply { setPackage(pkg) }
        } ?: run {
            if (target != ShareTarget.OTHER) {
                // بستهٔ اختصاصی نصب نیست → انتخابگر عمومی
                base
            } else base
        }
        runCatching {
            context.startActivity(
                Intent.createChooser(intent, context.getString(R.string.share_dialog_title))
            )
        }.onFailure {
            android.widget.Toast.makeText(
                context, context.getString(R.string.share_target_missing), android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    /** ذخیرهٔ متن در Downloads (برای «دانلود txt» لاگ‌ها). */
    fun saveText(context: Context, displayName: String, content: String): String {
        return try {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, displayName)
                put(MediaStore.Downloads.MIME_TYPE, "text/plain")
                put(
                    MediaStore.Downloads.RELATIVE_PATH,
                    android.os.Environment.DIRECTORY_DOWNLOADS + "/SazmanForooshgah"
                )
            }
            val uri = context.contentResolver
                .insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: return "خطا در ذخیرهٔ فایل"
            context.contentResolver.openOutputStream(uri)?.use { out: OutputStream ->
                out.write(content.toByteArray(Charsets.UTF_8))
            } ?: return "خطا در ذخیرهٔ فایل"
            "ذخیره شد: Downloads/SazmanForooshgah/$displayName"
        } catch (e: Exception) {
            AppLog.e("ShareUtils", "saveText", e)
            "خطا در ذخیرهٔ فایل: ${e.message}"
        }
    }

    /** ایمیل متن (ACTION_SENDTO). */
    fun email(context: Context, subject: String, body: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = android.net.Uri.parse("mailto:")
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        runCatching {
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_email_title)))
        }
    }
}
