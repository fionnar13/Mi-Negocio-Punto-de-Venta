package com.elfrikiamv.minegocio_puntodeventa.utils

// ReportExporter.kt — خروجی گزارش‌ها: CSV، Excel (POI) و چاپ

import android.content.ContentValues
import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.print.pdf.PrintedPdfDocument
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.ContextCompat
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * ابزار خروجی گرفتن از گزارش‌ها.
 *
 * - CSV: ذخیره در پوشهٔ Downloads (MediaStore، بدون نیاز به مجوز).
 * - Excel: Apache POI (xlsx) — برای اندروید با Aalto-XML تنظیم شده است.
 * - چاپ: چارچوب چاپ اندروید با PDF سادهٔ جدولی (RTL).
 */
object ReportExporter {

    init {
        // POI روی اندروید به پیاده‌سازی StAX نیاز دارد
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLOutputFactory",
            "com.fasterxml.aalto.stax.OutputFactoryImpl"
        )
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLInputFactory",
            "com.fasterxml.aalto.stax.InputFactoryImpl"
        )
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLEventFactory",
            "com.fasterxml.aalto.stax.EventFactoryImpl"
        )
    }

    /** ساخت نام فایل امن از نام گزارش و بازهٔ تاریخ. */
    private fun fileName(name: String, from: String, to: String, ext: String): String =
        "${name.replace(Regex("[\\\\/:*?\"<>|]"), "-")}_${from.replace('/', '-')}_${to.replace('/', '-')}.$ext"

    /** ذخیرهٔ بایت‌ها در Downloads/SazmanForooshgah (API 29+). */
    private fun saveToDownloads(context: Context, displayName: String, mime: String, bytes: ByteArray): Boolean {
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, displayName)
            put(MediaStore.Downloads.MIME_TYPE, mime)
            put(
                MediaStore.Downloads.RELATIVE_PATH,
                android.os.Environment.DIRECTORY_DOWNLOADS + "/SazmanForooshgah"
            )
        }
        val uri: Uri = context.contentResolver
            .insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return false
        context.contentResolver.openOutputStream(uri)?.use { out: OutputStream ->
            out.write(bytes)
        } ?: return false
        return true
    }

    // ---- CSV ----

    /** خروجی CSV (با BOM برای نمایش درست فارسی در اکسل). پیام نتیجه را برمی‌گرداند. */
    fun exportCsv(
        context: Context,
        reportName: String,
        from: String,
        to: String,
        headers: List<String>,
        rows: List<List<String>>
    ): String {
        val sb = StringBuilder("\uFEFF") // BOM
        sb.appendLine(headers.joinToString(",") { csvCell(it) })
        rows.forEach { row ->
            sb.appendLine(row.joinToString(",") { csvCell(it) })
        }
        val name = fileName(reportName, from, to, "csv")
        val ok = saveToDownloads(context, name, "text/csv", sb.toString().toByteArray(Charsets.UTF_8))
        return if (ok) "ذخیره شد: Downloads/SazmanForooshgah/$name" else "خطا در ذخیرهٔ فایل"
    }

    private fun csvCell(value: String): String =
        if (value.any { it == ',' || it == '"' || it == '\n' }) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else value

    // ---- Excel (Apache POI) ----

    /** خروجی Excel (xlsx) با POI. پیام نتیجه را برمی‌گرداند. */
    fun exportExcel(
        context: Context,
        reportName: String,
        from: String,
        to: String,
        headers: List<String>,
        rows: List<List<String>>
    ): String {
        return try {
            XSSFWorkbook().use { wb ->
                val sheet = wb.createSheet("گزارش")
                sheet.isRightToLeft = true // شیت راست‌به‌چپ

                val headerRow = sheet.createRow(0)
                headers.forEachIndexed { i, h ->
                    headerRow.createCell(i).setCellValue(h)
                }
                rows.forEachIndexed { r, row ->
                    val sheetRow = sheet.createRow(r + 1)
                    row.forEachIndexed { c, v ->
                        sheetRow.createCell(c).setCellValue(v)
                    }
                }
                // عرض ستون‌ها (برای متن فارسی)
                headers.indices.forEach { sheet.setColumnWidth(it, 22 * 256) }

                val bytes = ByteArrayOutputStream().use { out ->
                    wb.write(out)
                    out.toByteArray()
                }
                val name = fileName(reportName, from, to, "xlsx")
                val ok = saveToDownloads(
                    context, name,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    bytes
                )
                if (ok) "ذخیره شد: Downloads/SazmanForooshgah/$name" else "خطا در ذخیرهٔ فایل"
            }
        } catch (e: Exception) {
            "خطا در ساخت اکسل: ${e.message}"
        }
    }

    // ---- چاپ ----

    /** ارسال جدول گزارش به چارچوب چاپ اندروید. */
    fun print(
        context: Context,
        title: String,
        headers: List<String>,
        rows: List<List<String>>
    ) {
        val printManager = ContextCompat.getSystemService(context, PrintManager::class.java) ?: return
        printManager.print(
            title,
            TablePrintAdapter(context, title, headers, rows),
            PrintAttributes.Builder().build()
        )
    }
}

/**
 * آداپتور چاپ جدول: هر صفحه عنوان/سرجدول دارد و ردیف‌ها با StaticLayout
 * (شکل‌دهی درست فارسی) رسم می‌شوند.
 */
private class TablePrintAdapter(
    private val context: Context,
    private val title: String,
    private val headers: List<String>,
    private val rows: List<List<String>>
) : PrintDocumentAdapter() {

    private val margin = 36f
    private val rowHeight = 26f
    private val headerHeight = 30f

    private var attrs: PrintAttributes = PrintAttributes.Builder().build()
    private var pageCount = 1

    private val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 16f
        typeface = Typeface.DEFAULT_BOLD
    }
    private val headerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 11f
        typeface = Typeface.DEFAULT_BOLD
    }
    private val cellPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 10f }

    private var rowsPerPage: Int = 20

    override fun onLayout(
        attributes: PrintAttributes,
        oldAttributes: PrintAttributes?,
        cancellationSignal: CancellationSignal,
        callback: PrintDocumentAdapter.LayoutResultCallback,
        extras: Bundle?
    ) {
        attrs = attributes
        val pdf = PrintedPdfDocument(context, attrs)
        val pageHeight = pdf.getPageHeight().toFloat()
        val usable = pageHeight - 2 * margin - headerHeight - 60f // 60 = فضای عنوان صفحهٔ اول
        rowsPerPage = (usable / rowHeight).toInt().coerceAtLeast(1)
        pageCount = if (rows.isEmpty()) 1 else (rows.size + rowsPerPage - 1) / rowsPerPage

        val info = PrintDocumentInfo.Builder(title.replace(Regex("\\s+"), "_") + ".pdf")
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .setPageCount(pageCount)
            .build()
        callback.onLayoutFinished(info, true)
    }

    override fun onWrite(
        pages: Array<PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal,
        callback: PrintDocumentAdapter.WriteResultCallback
    ) {
        val pdf = PrintedPdfDocument(context, attrs)
        try {
            for (page in 0 until pageCount) {
                val pageInfo = pdf.startPage(page)
                val canvas = pageInfo.canvas
                val pageWidth = pdf.getPageWidth().toFloat()
                val usableWidth = pageWidth - 2 * margin
                val colWidth = usableWidth / headers.size

                // عنوان فقط صفحهٔ اول
                if (page == 0) {
                    val titleLayout = staticLayout(title, titlePaint, usableWidth.toInt())
                    canvas.save()
                    canvas.translate(margin, margin)
                    titleLayout.draw(canvas)
                    canvas.restore()
                }

                // سرجدول در همهٔ صفحات
                var y = margin + if (page == 0) 60f else 20f
                headers.forEachIndexed { i, h ->
                    val layout = staticLayout(h, headerPaint, (colWidth - 6).toInt())
                    canvas.save()
                    canvas.translate(margin + (headers.size - 1 - i) * colWidth + 4f, y)
                    // ستون‌ها راست‌به‌چپ چیده می‌شوند (ستون اول در راست)
                    layout.draw(canvas)
                    canvas.restore()
                }
                y += headerHeight

                // ردیف‌های این صفحه
                val from = page * rowsPerPage
                val to = minOf(from + rowsPerPage, rows.size)
                for (r in from until to) {
                    val row = rows[r]
                    headers.indices.forEach { i ->
                        val text = row.getOrNull(i) ?: ""
                        val layout = staticLayout(text, cellPaint, (colWidth - 6).toInt())
                        canvas.save()
                        canvas.translate(
                            margin + (headers.size - 1 - i) * colWidth + 4f,
                            y + (rowHeight - layout.height).coerceAtMost(rowHeight) / 2f
                        )
                        layout.draw(canvas)
                        canvas.restore()
                    }
                    y += rowHeight
                }

                pdf.finishPage(pageInfo)
            }
            FileOutputStream(destination.fileDescriptor).use { pdf.writeTo(it) }
            callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        } finally {
            pdf.close()
        }
    }

    override fun onFinish() {
        // هیچ منبع پایداری برای آزادسازی نداریم
    }

    private fun staticLayout(text: String, paint: TextPaint, width: Int): StaticLayout =
        StaticLayout.Builder
            .obtain(text, 0, text.length, paint, width.coerceAtLeast(1))
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1f)
            .setIncludePad(false)
            .build()
}
