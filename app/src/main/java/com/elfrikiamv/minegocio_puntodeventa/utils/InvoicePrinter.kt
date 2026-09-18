package com.elfrikiamv.minegocio_puntodeventa.utils

// InvoicePrinter.kt — چاپ فاکتور فروش/خرید (A4 / 80mm / 58mm)

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.elfrikiamv.minegocio_puntodeventa.data.entity.PurchaseEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.SaleEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.SettingsEntity
import com.elfrikiamv.minegocio_puntodeventa.ui.components.unitLabel

/**
 * چاپ فاکتور با PrintManager — قالب از [SettingsEntity.printFormat]:
 * A4 (برگ کامل)، 80mm و 58mm (رسید باریک).
 */
object InvoicePrinter {

    /** اندازه/فونت هر قالب. */
    private class FormatSpec(
        val pageWidth: Int,     // پیکسل @72dpi
        val pageHeight: Int,
        val titleSize: Float,
        val bodySize: Float,
        val smallSize: Float,
        val margin: Float,
        val rowHeight: Float
    )

    private val A4 = FormatSpec(595, 842, 16f, 10f, 8.5f, 36f, 22f)
    private val MM80 = FormatSpec(227, 842, 12f, 8.5f, 7.5f, 12f, 18f)
    private val MM58 = FormatSpec(165, 842, 10f, 7.5f, 7f, 8f, 16f)

    private fun spec(format: String): FormatSpec = when (format) {
        "80mm" -> MM80
        "58mm" -> MM58
        else -> A4
    }

    // ---- API ----

    /** چاپ فاکتور فروش. */
    fun printSale(
        context: Context,
        sale: SaleEntity,
        customerName: String,
        visitorName: String,
        settings: SettingsEntity
    ) {
        val spec = spec(settings.printFormat)
        val logo = loadLogo(settings.storeLogo)
        print(context, "sale-${sale.no}", buildSaleDoc(spec, sale, customerName, visitorName, settings), logo, spec)
    }

    /** چاپ فاکتور خرید. */
    fun printPurchase(
        context: Context,
        purchase: PurchaseEntity,
        supplierName: String,
        settings: SettingsEntity
    ) {
        val spec = spec(settings.printFormat)
        val logo = loadLogo(settings.storeLogo)
        print(
            context, "purchase-${purchase.no}",
            buildPurchaseDoc(spec, purchase, supplierName, settings), logo, spec
        )
    }

    /** محتوای فاکتور فروش. */
    private fun buildSaleDoc(
        spec: FormatSpec,
        sale: SaleEntity,
        customerName: String,
        visitorName: String,
        settings: SettingsEntity
    ): BuiltDoc = buildInvoice(
        spec = spec,
        title = "فاکتور فروش",
        store = settings,
        metaLines = listOfNotNull(
            "شماره فاکتور: ${NumberUtils.toPersian(sale.no)}",
            "تاریخ: ${PersianFormat.displayDate(sale.date)} · ساعت: ${PersianFormat.displayTime(sale.time)}",
            "مشتری: ${customerName.ifBlank { "متفرقه" }}",
            "ویزیتور: ${visitorName.ifBlank { "—" }}".takeIf { visitorName.isNotBlank() }
        ),
        headers = listOf("ردیف", "کالا", "تعداد", "فی", "جمع"),
        rows = sale.items.mapIndexed { i, item ->
            listOf(
                NumberUtils.toPersian(i + 1),
                item.name,
                "${PersianFormat.qty(item.qty)} ${unitLabel(item.unit)}",
                PersianFormat.amount(item.price),
                PersianFormat.amount(item.total)
            )
        },
        totals = buildList {
            add("جمع" to PersianFormat.amount(sale.total))
            if (sale.discount > 0.0) add("تخفیف" to PersianFormat.amount(sale.discount))
            add("قابل پرداخت" to PersianFormat.amount(sale.grand))
            add("پرداخت‌شده" to PersianFormat.amount(sale.paid))
            if (sale.grand - sale.paid > 0.009) {
                add("مانده" to PersianFormat.amount(sale.grand - sale.paid))
            }
            sale.pays.forEach { p -> add(p.method to PersianFormat.amount(p.amount)) }
        },
        footer = "با تشکر از خرید شما 🙏"
    )

    /** محتوای فاکتور خرید. */
    private fun buildPurchaseDoc(
        spec: FormatSpec,
        purchase: PurchaseEntity,
        supplierName: String,
        settings: SettingsEntity
    ): BuiltDoc = buildInvoice(
        spec = spec,
        title = "فاکتور خرید",
        store = settings,
        metaLines = listOfNotNull(
            "شماره فاکتور: ${NumberUtils.toPersian(purchase.no)}",
            "تاریخ: ${PersianFormat.displayDate(purchase.date)}",
            "تامین‌کننده: ${supplierName.ifBlank { "—" }}".takeIf { supplierName.isNotBlank() }
        ),
        headers = listOf("ردیف", "کالا", "تعداد", "فی", "جمع"),
        rows = purchase.items.mapIndexed { i, item ->
            listOf(
                NumberUtils.toPersian(i + 1),
                item.name,
                "${PersianFormat.qty(item.qty)} ${unitLabel(item.unit)}",
                PersianFormat.amount(item.price),
                PersianFormat.amount(item.total)
            )
        },
        totals = buildList {
            add("جمع" to PersianFormat.amount(purchase.total))
            if (purchase.discount > 0.0) add("تخفیف" to PersianFormat.amount(purchase.discount))
            add("قابل پرداخت" to PersianFormat.amount(purchase.grand))
            add("پرداخت‌شده" to PersianFormat.amount(purchase.paid))
            if (purchase.grand - purchase.paid > 0.009) {
                add("بدهی" to PersianFormat.amount(purchase.grand - purchase.paid))
            }
        },
        footer = "ثبت شد در «سازمان فروشگاه»"
    )

    // ---- ساخت سند ----

    /** یک سند مدل‌شده از خطوط متن + جدول + جمع‌ها. */
    private class BuiltDoc(
        val pages: List<CanvasPage>
    ) {
        class CanvasPage(
            val headerLines: List<String>,
            val metaLines: List<String>,
            val tableHeader: List<String>,
            val tableRows: List<List<String>>,
            val totals: List<Pair<String, String>>,
            val footer: String
        )
    }

    private fun print(context: Context, name: String, doc: BuiltDoc, logo: Bitmap?, spec: FormatSpec) {
        val manager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        manager.print(
            name.replace(Regex("\\s+"), "_"),
            InvoicePrintAdapter(name, doc, logo, spec),
            PrintAttributes.Builder().build()
        )
    }

    /** ساخت صفحات (شکست صفحه بر اساس ارتفاع). */
    private fun buildInvoice(
        spec: FormatSpec,
        title: String,
        store: SettingsEntity,
        metaLines: List<String>,
        headers: List<String>,
        rows: List<List<String>>,
        totals: List<Pair<String, String>>,
        footer: String
    ): BuiltDoc {
        // سرصفحهٔ فروشگاه
        val header = buildList {
            add(store.storeName.ifBlank { "سازمان فروشگاه" })
            if (store.storePhone.isNotBlank()) add("تلفن: ${PersianFormat.displayPhone(store.storePhone)}")
            if (store.storeAddress.isNotBlank()) add(store.storeAddress)
            if (store.licenseNo.isNotBlank()) add("جواز کسب: ${store.licenseNo}")
            add(title)
        }

        // برآورد ارتفاع هر بخش برای شکست صفحه
        val usable = spec.pageHeight - 2 * spec.margin
        val headerH = header.size * (spec.titleSize + 6) + 30
        val metaH = metaLines.size * (spec.bodySize + 6) + 10
        val tableHeaderH = spec.rowHeight + 8
        val totalsH = totals.size * (spec.bodySize + 8) + 14
        val footerH = 40

        val pages = mutableListOf<BuiltDoc.CanvasPage>()
        var current = mutableListOf<List<String>>()
        var used = headerH + metaH + tableHeaderH + totalsH + footerH
        rows.forEach { row ->
            if (used + spec.rowHeight > usable && current.isNotEmpty()) {
                pages += BuiltDoc.CanvasPage(header, metaLines, headers, current.toList(), emptyList(), "")
                current = mutableListOf()
                used = tableHeaderH + totalsH + footerH
            }
            current += row
            used += spec.rowHeight
        }
        // صفحهٔ پایانی با جمع‌ها و پانویس
        pages += BuiltDoc.CanvasPage(header, metaLines, headers, current.toList(), totals, footer)
        return BuiltDoc(pages)
    }

    /** آداپتور چاپ. */
    private class InvoicePrintAdapter(
        private val name: String,
        private val doc: BuiltDoc,
        private val logo: Bitmap?,
        private val spec: FormatSpec
    ) : PrintDocumentAdapter() {

        override fun onLayout(
            attributes: PrintAttributes,
            oldAttributes: PrintAttributes?,
            callback: PrintDocumentCallback,
            extras: Bundle?
        ) {
            val info = PrintDocumentInfo.Builder("$name.pdf")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(doc.pages.size)
                .build()
            callback.onLayoutFinished(info, true)
        }

        override fun onWrite(
            pages: Array<out PageRange>?,
            destination: ParcelFileDescriptor,
            callback: PrintDocumentCallback,
            extras: Bundle?
        ) {
            val pdf = PdfDocument()
            try {
                doc.pages.forEachIndexed { index, page ->
                    val pageInfo = PdfDocument.PageInfo.Builder(spec.pageWidth, spec.pageHeight, index + 1)
                        .create()
                    val p = pdf.startPage(pageInfo)
                    drawPage(p.canvas, page, spec, logo)
                    pdf.finishPage(p)
                }
                java.io.FileOutputStream(destination.fileDescriptor).use { pdf.writeTo(it) }
                callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
            } finally {
                pdf.close()
            }
        }
    }

    // ---- رسم ----

    private fun drawPage(canvas: Canvas, page: BuiltDoc.CanvasPage, spec: FormatSpec, logo: Bitmap?) {
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = spec.bodySize }
        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = spec.titleSize
            typeface = Typeface.DEFAULT_BOLD
        }
        val smallPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = spec.smallSize }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            strokeWidth = 0.8f
        }

        val contentWidth = (spec.pageWidth - 2 * spec.margin).toInt()
        var y = spec.margin

        // لوگو (اگر موجود) — بالا-راست، متن سرصفحه پایین آن
        if (logo != null) {
            val logoH = 48f
            val scaled = Bitmap.createScaledBitmap(
                logo,
                (logoH * logo.width / logo.height).toInt().coerceAtMost(contentWidth),
                logoH.toInt(),
                true
            )
            canvas.save()
            canvas.translate(spec.pageWidth - spec.margin - scaled.width, y)
            canvas.drawBitmap(scaled, 0f, 0f, null)
            canvas.restore()
            y += logoH + 8f
        }

        // سرصفحه (راست‌چین)
        page.headerLines.forEachIndexed { i, line ->
            val paint = if (i == 0 || i == page.headerLines.size - 1) titlePaint else smallPaint
            val layout = staticLayout(line, paint, contentWidth)
            canvas.save()
            canvas.translate(spec.margin, y)
            layout.draw(canvas)
            canvas.restore()
            y += paint.textSize + 8
        }
        y += 6
        drawHr(canvas, y, spec, linePaint)
        y += 10

        // متا
        page.metaLines.forEach { line ->
            val layout = staticLayout(line, textPaint, contentWidth)
            canvas.save()
            canvas.translate(spec.margin, y)
            layout.draw(canvas)
            canvas.restore()
            y += textPaint.textSize + 6
        }
        y += 6

        // جدول — ستون‌ها راست‌به‌چپ: [کوچک، بزرگ، متوسط، متوسط، متوسط]
        if (page.tableRows.isNotEmpty() || page.tableHeader.isNotEmpty()) {
            val weights = listOf(0.5f, 2.6f, 1.1f, 1.2f, 1.3f)
            val widths = weights.map { it * contentWidth }
            // سرجدول
            drawRow(canvas, page.tableHeader, widths, y, smallPaint, spec, bold = true)
            y += spec.rowHeight * 0.8f
            drawHr(canvas, y, spec, linePaint)
            y += 6
            // ردیف‌ها
            page.tableRows.forEach { row ->
                drawRow(canvas, row, widths, y, smallPaint, spec, bold = false)
                y += spec.rowHeight
            }
            y += 6
            drawHr(canvas, y, spec, linePaint)
            y += 10
        }

        // جمع‌ها (برچسب راست، مقدار چپ)
        page.totals.forEach { (label, value) ->
            val labelLayout = staticLayout(label, textPaint, contentWidth / 2)
            val valueLayout = staticLayout(value, textPaint, contentWidth / 2)
            canvas.save()
            canvas.translate(spec.margin + contentWidth / 2f, y)
            labelLayout.draw(canvas)
            canvas.restore()
            canvas.save()
            canvas.translate(spec.margin, y)
            valueLayout.draw(canvas)
            canvas.restore()
            y += textPaint.textSize + 8
        }

        // پانویس
        if (page.footer.isNotBlank()) {
            y += 8
            val layout = staticLayout(page.footer, smallPaint, contentWidth)
            canvas.save()
            canvas.translate(spec.margin, y)
            layout.draw(canvas)
            canvas.restore()
        }
    }

    private fun drawHr(canvas: Canvas, y: Float, spec: FormatSpec, paint: Paint) {
        canvas.drawLine(spec.margin, y, spec.pageWidth - spec.margin, y, paint)
    }

    /** رسم یک ردیف جدول با ترتیب راست‌به‌چپ ستون‌ها. */
    private fun drawRow(
        canvas: Canvas,
        cells: List<String>,
        widths: List<Float>,
        y: Float,
        paint: TextPaint,
        spec: FormatSpec,
        bold: Boolean
    ) {
        val p = if (bold) TextPaint(paint).apply { typeface = Typeface.DEFAULT_BOLD } else paint
        var xRight = spec.pageWidth - spec.margin
        cells.forEachIndexed { i, cell ->
            val w = widths.getOrElse(i) { 40f }.toInt().coerceAtLeast(12)
            val layout = staticLayout(cell, p, w - 4)
            val cx = xRight - w + 2f
            canvas.save()
            canvas.translate(cx, y)
            layout.draw(canvas)
            canvas.restore()
            xRight -= w
        }
    }

    private fun staticLayout(text: String, paint: TextPaint, width: Int): StaticLayout =
        StaticLayout.Builder
            .obtain(text, 0, text.length, paint, width.coerceAtLeast(4))
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1f)
            .setIncludePad(false)
            .build()

    /** بارگذاری لوگو (null اگر موجود نباشد). */
    private fun loadLogo(path: String?): Bitmap? =
        path?.takeIf { it.isNotBlank() }?.let {
            runCatching { BitmapFactory.decodeFile(it) }.getOrNull()
        }
}
