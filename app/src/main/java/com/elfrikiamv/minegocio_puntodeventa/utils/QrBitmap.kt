package com.elfrikiamv.minegocio_puntodeventa.utils

// QrBitmap.kt — تولید بیت‌مپ QR با ZXing core

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

/**
 * تولید تصویر QR از متن (برای نمایش کد ورود کاربر).
 */
object QrBitmap {

    /**
     * @param content متن (مثلاً JSON توکن کاربر).
     * @param size ضلع تصویر به پیکسل.
     */
    fun generate(content: String, size: Int = 512): Bitmap {
        val hints = mapOf(EncodeHintType.MARGIN to 1)
        val matrix = QRCodeWriter().encode(
            content, BarcodeFormat.QR_CODE, size, size, hints
        )
        val pixels = IntArray(size * size)
        for (y in 0 until size) {
            for (x in 0 until size) {
                pixels[y * size + x] =
                    if (matrix.get(x, y)) Color.BLACK else Color.WHITE
            }
        }
        return Bitmap.createBitmap(pixels, size, size, Bitmap.Config.RGB_565)
    }
}
