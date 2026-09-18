package com.elfrikiamv.minegocio_puntodeventa.ui.screens.products

// ProductThumb.kt — بندانگشتی تصویر کالا (بارگذاری غیرهمگام، بدون وابستگی جدید)

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.elfrikiamv.minegocio_puntodeventa.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * بندانگشتی تصویر کالا. اگر تصویری نباشد (یا خطا بخورد) جای‌نگه‌دار
 * با آیکون بسته نمایش داده می‌شود.
 *
 * @param img نشانی تصویر (URI انتخاب‌شده یا مسیر فایل) یا null.
 */
@Composable
fun ProductThumb(img: String?, modifier: Modifier = Modifier, size: Dp = 56.dp) {
    val context = LocalContext.current
    var bitmap by remember(img) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(img) {
        bitmap = if (img.isNullOrBlank()) null else decodeImage(context, img, 256)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center
    ) {
        val bmp = bitmap
        if (bmp != null) {
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size)
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_package),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(size / 2)
            )
        }
    }
}

/** رمزگشایی تصویر با نمونه‌برداری (inSampleSize) روی نخ IO. */
private suspend fun decodeImage(context: Context, source: String, target: Int): Bitmap? =
    withContext(Dispatchers.IO) {
        runCatching {
            val uri = Uri.parse(source)
            if (uri.scheme == null) {
                // مسیر فایل خام
                BitmapFactory.decodeFile(source)
            } else {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, bounds)
                } ?: return@runCatching null

                var sample = 1
                while (
                    bounds.outWidth / (sample * 2) >= target &&
                    bounds.outHeight / (sample * 2) >= target
                ) {
                    sample *= 2
                }
                val options = BitmapFactory.Options().apply { inSampleSize = sample }
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, options)
                }
            }
        }.getOrNull()
    }
