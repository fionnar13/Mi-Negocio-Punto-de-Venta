package com.elfrikiamv.minegocio_puntodeventa.utils

// DataManager.kt — پشتیبان‌گیری/بازیابی، ورود/خروجی اکسل، دادهٔ نمونه و بازنشانی

import android.content.Context
import android.net.Uri
import com.elfrikiamv.minegocio_puntodeventa.data.entity.BankEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.CustomerEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.EventEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.LedgerEntryEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.ProductEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.PurchaseEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.SaleEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.SessionEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.SettingsEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.SupplierEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.TerminalEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.UserEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.VisitorEntity
import com.elfrikiamv.minegocio_puntodeventa.data.local.SazmanDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream

/**
 * ابزار مدیریت داده‌ها (تب «داده‌ها» در تنظیمات).
 */
class DataManager(private val context: Context) {

    private val db = SazmanDatabase.getDatabase(context)
    private val gson = Gson()

    // ---- پشتیبان‌گیری / بازیابی JSON ----

    /** پشتیبان کامل JSON همهٔ جدول‌ها در Downloads. */
    suspend fun backupJson(): String {
        val data = mapOf(
            "products" to db.productDao().getAll().first(),
            "customers" to db.customerDao().getAll().first(),
            "suppliers" to db.supplierDao().getAll().first(),
            "visitors" to db.visitorDao().getAll().first(),
            "banks" to db.bankDao().getAll().first(),
            "users" to db.userDao().getAll().first(),
            "terminals" to db.terminalDao().getAll().first(),
            "sales" to db.saleDao().getAll().first(),
            "purchases" to db.purchaseDao().getAll().first(),
            "ledger" to db.ledgerEntryDao().getAll().first(),
            "events" to db.eventDao().getAll().first(),
            "sessions" to db.sessionDao().getAll().first(),
            "settings" to listOfNotNull(db.settingsDao().get())
        )
        val bytes = gson.toJson(data).toByteArray(Charsets.UTF_8)
        val name = "sazman-backup-${PersianFormat.todayDateString().replace('/', '-')}.json"
        val ok = saveToDownloads(name, "application/json", bytes)
        return if (ok) "پشتیبان ذخیره شد: Downloads/SazmanForooshgah/$name" else "خطا در ذخیرهٔ پشتیبان"
    }

    /** بازیابی پشتیبان از URI انتخاب‌شده. */
    suspend fun restoreJson(uri: Uri): String {
        return try {
            val text = context.contentResolver.openInputStream(uri)
                ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                ?: return "فایل خوانده نشد"
            val data: Map<String, List<Any>> =
                gson.fromJson(text, object : TypeToken<Map<String, List<Any>>>() {}.type)

            fun <T> list(key: String, type: java.lang.reflect.Type): List<T> =
                gson.fromJson(gson.toJson(data[key] ?: emptyList<Any>()), type)

            db.productDao().deleteAll()
            db.productDao().insertAll(
                list("products", TypeToken.getParameterized(List::class.java, ProductEntity::class.java).type)
            )
            db.customerDao().deleteAll()
            db.customerDao().insertAll(
                list("customers", TypeToken.getParameterized(List::class.java, CustomerEntity::class.java).type)
            )
            db.supplierDao().deleteAll()
            db.supplierDao().insertAll(
                list("suppliers", TypeToken.getParameterized(List::class.java, SupplierEntity::class.java).type)
            )
            db.visitorDao().deleteAll()
            db.visitorDao().insertAll(
                list("visitors", TypeToken.getParameterized(List::class.java, VisitorEntity::class.java).type)
            )
            db.bankDao().deleteAll()
            db.bankDao().insertAll(
                list("banks", TypeToken.getParameterized(List::class.java, BankEntity::class.java).type)
            )
            db.userDao().deleteAll()
            db.userDao().insertAll(
                list("users", TypeToken.getParameterized(List::class.java, UserEntity::class.java).type)
            )
            db.terminalDao().deleteAll()
            db.terminalDao().insertAll(
                list("terminals", TypeToken.getParameterized(List::class.java, TerminalEntity::class.java).type)
            )
            db.saleDao().deleteAll()
            db.saleDao().insertAll(
                list("sales", TypeToken.getParameterized(List::class.java, SaleEntity::class.java).type)
            )
            db.purchaseDao().deleteAll()
            db.purchaseDao().insertAll(
                list("purchases", TypeToken.getParameterized(List::class.java, PurchaseEntity::class.java).type)
            )
            db.ledgerEntryDao().deleteAll()
            db.ledgerEntryDao().insertAll(
                list("ledger", TypeToken.getParameterized(List::class.java, LedgerEntryEntity::class.java).type)
            )
            db.eventDao().deleteAll()
            db.eventDao().insertAll(
                list("events", TypeToken.getParameterized(List::class.java, EventEntity::class.java).type)
            )
            db.sessionDao().deleteAll()
            db.sessionDao().insertAll(
                list("sessions", TypeToken.getParameterized(List::class.java, SessionEntity::class.java).type)
            )
            "بازیابی کامل شد"
        } catch (e: Exception) {
            AppLog.e("DataManager", "restoreJson", e)
            "خطا در بازیابی: ${e.message}"
        }
    }

    // ---- بازنشانی ----

    /** حذف همهٔ داده‌های کسب‌وکار (تنظیمات حفظ می‌شود). */
    suspend fun reset(): String {
        db.saleDao().deleteAll()
        db.purchaseDao().deleteAll()
        db.ledgerEntryDao().deleteAll()
        db.productDao().deleteAll()
        db.customerDao().deleteAll()
        db.supplierDao().deleteAll()
        db.visitorDao().deleteAll()
        db.bankDao().deleteAll()
        db.userDao().deleteAll()
        db.terminalDao().deleteAll()
        db.eventDao().deleteAll()
        db.sessionDao().deleteAll()
        db.scanLogDao().deleteAll()
        return "همهٔ داده‌ها بازنشانی شد"
    }

    // ---- دادهٔ نمونه ----

    /** درج دادهٔ نمونه برای شروع سریع. */
    suspend fun sampleData(): String {
        if (db.productDao().getAll().first().isNotEmpty()) {
            return "داده‌ها خالی نیست؛ ابتدا بازنشانی کنید"
        }
        db.productDao().insertAll(
            listOf(
                ProductEntity(
                    name = "شیر پرچرب پگاه ۱ لیتری", barcode = "6260000100014", code = "1001",
                    cat = "لبنیات", subCat = "شیر", brand = "پگاه",
                    baseUnit = "u", buyU = "u", buyC = 28000.0, sellU = "u", sellC = 32000.0,
                    stock = 48.0, min = 12.0
                ),
                ProductEntity(
                    name = "ماست چکیده ۹۰۰ گرمی", barcode = "6260000100021", code = "1002",
                    cat = "لبنیات", subCat = "ماست", brand = "چکیده",
                    baseUnit = "u", buyU = "c", buyC = 240000.0, sellU = "u", sellC = 31000.0,
                    conv = mapOf("c" to 10.0), stock = 35.0, min = 8.0
                ),
                ProductEntity(
                    name = "برنج هاشمی درجه یک", barcode = "6260000100038", code = "1003",
                    cat = "خواربار", subCat = "برنج", brand = "هاشمی",
                    baseUnit = "kg", buyU = "kg", buyC = 145000.0, sellU = "kg", sellC = 168000.0,
                    stock = 240.0, min = 50.0
                ),
                ProductEntity(
                    name = "روغن سرخ‌کردنی ۱.۶ لیتری", barcode = "6260000100045", code = "1004",
                    cat = "خواربار", subCat = "روغن", brand = "لادن",
                    baseUnit = "u", buyU = "u", buyC = 178000.0, sellU = "u", sellC = 198000.0,
                    stock = 22.0, min = 6.0
                ),
                ProductEntity(
                    name = "چای کیسه‌ای ۱۰۰ عددی", barcode = "6260000100052", code = "1005",
                    cat = "نوشیدنی", subCat = "چای", brand = "احمد",
                    baseUnit = "u", buyU = "u", buyC = 96000.0, sellU = "u", sellC = 115000.0,
                    stock = 4.0, min = 10.0
                ),
                ProductEntity(
                    name = "تخم مرغ بسته ۹ عددی", barcode = "6260000100069", code = "1006",
                    cat = "لبنیات", subCat = "تخم مرغ", brand = "محلی",
                    baseUnit = "u", buyU = "u", buyC = 52000.0, sellU = "u", sellC = 61000.0,
                    stock = 30.0, min = 10.0
                ),
                ProductEntity(
                    name = "پنیر لیقوان ۴۰۰ گرمی", barcode = "6260000100076", code = "1007",
                    cat = "لبنیات", subCat = "پنیر", brand = "لیقوان",
                    baseUnit = "u", buyU = "u", buyC = 118000.0, sellU = "u", sellC = 138000.0,
                    stock = 15.0, min = 5.0
                ),
                ProductEntity(
                    name = "رب گوجه فرنگی ۸۰۰ گرمی", barcode = "6260000100083", code = "1008",
                    cat = "خواربار", subCat = "رب و سس", brand = "چین‌چین",
                    baseUnit = "u", buyU = "u", buyC = 88000.0, sellU = "u", sellC = 104000.0,
                    stock = 26.0, min = 8.0
                )
            )
        )
        db.customerDao().insertAll(
            listOf(
                CustomerEntity(name = "سوپرمارکت آفتاب", phone = "09121110022", kind = CustomerEntity.KIND_CREDIT),
                CustomerEntity(name = "رستوران گلسرخ", phone = "09123330044", kind = CustomerEntity.KIND_CREDIT),
                CustomerEntity(name = "خرید حضوری", phone = "", kind = CustomerEntity.KIND_CASH)
            )
        )
        db.supplierDao().insertAll(
            listOf(
                SupplierEntity(name = "پخش لبنیات پگاه", phone = "02155667788"),
                SupplierEntity(name = "پخش برنج شمال", phone = "02144332211")
            )
        )
        db.visitorDao().insertAll(
            listOf(
                VisitorEntity(name = "علی محمدی", type = VisitorEntity.TYPE_PERCENT, value = 2.0),
                VisitorEntity(name = "رضا کریمی", type = VisitorEntity.TYPE_CARTON, value = 15000.0)
            )
        )
        db.bankDao().insertAll(
            listOf(
                BankEntity(bank = "بانک ملت", card = "6104337812345678", account = "1234567890", sheba = "IR120120000000001234567890"),
                BankEntity(bank = "بانک سامان", card = "6219861012345678", account = "9876543210", sheba = "IR050560000000009876543210")
            )
        )
        db.userDao().insertAll(
            listOf(
                UserEntity(name = "مدیر فروشگاه", role = "مدیر"),
                UserEntity(name = "صندوقدار", role = "صندوقدار")
            )
        )
        db.eventDao().insertAll(
            listOf(
                EventEntity(
                    title = "پرداخت به پخش پگاه", date = nextDays(3),
                    color = 0xFFF87171.toInt(), reminder = true
                ),
                EventEntity(
                    title = "فیزیک انبار پایان ماه", date = nextDays(10),
                    color = 0xFF6366F1.toInt(), reminder = false
                )
            )
        )
        return "دادهٔ نمونه درج شد"
    }

    // ---- اکسل ----

    /** خروجی اکسل کالاها. */
    suspend fun exportProductsExcel(): String {
        val products = db.productDao().getAll().first()
        val headers = listOf("نام", "بارکد", "کد", "دسته", "زیردسته", "برند", "واحد", "خرید", "فروش", "موجودی", "حداقل")
        val rows = products.map {
            listOf(
                it.name, it.barcode, it.code, it.cat, it.subCat, it.brand, it.baseUnit,
                PersianFormat.plain(it.buyC), PersianFormat.plain(it.sellC),
                PersianFormat.plain(it.stock), PersianFormat.plain(it.min)
            )
        }
        return exportExcelFile("کالاها", headers, rows)
    }

    /** خروجی اکسل مشتریان. */
    suspend fun exportCustomersExcel(): String {
        val customers = db.customerDao().getAll().first()
        val headers = listOf("نام", "تلفن", "نوع")
        val rows = customers.map { listOf(it.name, it.phone, it.kind) }
        return exportExcelFile("مشتریان", headers, rows)
    }

    /** ورود کالاها از اکسل؛ تعداد ردیف‌های افزوده‌شده را برمی‌گرداند. */
    suspend fun importProductsExcel(uri: Uri): String {
        return try {
            val book = context.contentResolver.openInputStream(uri)?.use { XSSFWorkbook(it) }
                ?: return "فایل خوانده نشد"
            val sheet = book.getSheetAt(0)
            val fmt = DataFormatter()
            val headerRow = sheet.getRow(0) ?: return "فایل خالی است"
            val header = (0 until headerRow.lastCellNum).associate { fmt.formatCellValue(headerRow.getCell(it)) to it }

            fun cell(row: org.apache.poi.ss.usermodel.Row, name: String): String =
                header[name]?.let { fmt.formatCellValue(row.getCell(it)) }?.trim().orEmpty()

            val products = mutableListOf<ProductEntity>()
            for (i in 1..sheet.lastRowNum) {
                val row = sheet.getRow(i) ?: continue
                val name = cell(row, "نام")
                if (name.isBlank()) continue
                products += ProductEntity(
                    name = name,
                    barcode = cell(row, "بارکد"),
                    code = cell(row, "کد"),
                    cat = cell(row, "دسته"),
                    subCat = cell(row, "زیردسته"),
                    brand = cell(row, "برند"),
                    baseUnit = cell(row, "واحد").ifBlank { "u" },
                    buyU = cell(row, "واحد").ifBlank { "u" },
                    buyC = cell(row, "خرید").toDoubleOrNull() ?: 0.0,
                    sellU = cell(row, "واحد").ifBlank { "u" },
                    sellC = cell(row, "فروش").toDoubleOrNull() ?: 0.0,
                    stock = cell(row, "موجودی").toDoubleOrNull() ?: 0.0,
                    min = cell(row, "حداقل").toDoubleOrNull() ?: 0.0
                )
            }
            if (products.isEmpty()) return "کالایی برای درج یافت نشد"
            db.productDao().insertAll(products)
            "${PersianFormat.plain(products.size.toLong())} کالا درج شد"
        } catch (e: Exception) {
            AppLog.e("DataManager", "importProductsExcel", e)
            "خطا در خواندن اکسل: ${e.message}"
        }
    }

    /** ورود مشتریان از اکسل. */
    suspend fun importCustomersExcel(uri: Uri): String {
        return try {
            val book = context.contentResolver.openInputStream(uri)?.use { XSSFWorkbook(it) }
                ?: return "فایل خوانده نشد"
            val sheet = book.getSheetAt(0)
            val fmt = DataFormatter()
            val headerRow = sheet.getRow(0) ?: return "فایل خالی است"
            val header = (0 until headerRow.lastCellNum).associate { fmt.formatCellValue(headerRow.getCell(it)) to it }

            fun cell(row: org.apache.poi.ss.usermodel.Row, name: String): String =
                header[name]?.let { fmt.formatCellValue(row.getCell(it)) }?.trim().orEmpty()

            val customers = mutableListOf<CustomerEntity>()
            for (i in 1..sheet.lastRowNum) {
                val row = sheet.getRow(i) ?: continue
                val name = cell(row, "نام")
                if (name.isBlank()) continue
                customers += CustomerEntity(
                    name = name,
                    phone = cell(row, "تلفن"),
                    kind = cell(row, "نوع").ifBlank { CustomerEntity.KIND_CASH }
                )
            }
            if (customers.isEmpty()) return "مشتری‌ای برای درج یافت نشد"
            db.customerDao().insertAll(customers)
            "${PersianFormat.plain(customers.size.toLong())} مشتری درج شد"
        } catch (e: Exception) {
            AppLog.e("DataManager", "importCustomersExcel", e)
            "خطا در خواندن اکسل: ${e.message}"
        }
    }

    // ---- فضای ذخیره‌سازی ----

    /** گزارش فضای مصرفی برنامه. */
    fun storageUsage(): String {
        fun dirSize(dir: java.io.File): Long =
            dir.walkBottomUp().filter { it.isFile }.sumOf { it.length() }

        val dbSize = context.getDatabasePath("sazman_forooshgah.db")?.length() ?: 0L
        val filesSize = dirSize(context.filesDir)
        return "پایگاه داده: ${PersianFormat.size(dbSize)} · فایل‌ها: ${PersianFormat.size(filesSize)}"
    }

    // ---- کمکی ----

    private fun exportExcelFile(name: String, headers: List<String>, rows: List<List<String>>): String {
        return try {
            XSSFWorkbook().use { wb ->
                val sheet = wb.createSheet(name)
                sheet.isRightToLeft = true
                val headerRow = sheet.createRow(0)
                headers.forEachIndexed { i, h -> headerRow.createCell(i).setCellValue(h) }
                rows.forEachIndexed { r, row ->
                    val sheetRow = sheet.createRow(r + 1)
                    row.forEachIndexed { c, v -> sheetRow.createCell(c).setCellValue(v) }
                }
                headers.indices.forEach { sheet.setColumnWidth(it, 20 * 256) }
                val bytes = ByteArrayOutputStream().use { out ->
                    wb.write(out); out.toByteArray()
                }
                val fileName = "$name-${PersianFormat.todayDateString().replace('/', '-')}.xlsx"
                val ok = saveToDownloads(
                    fileName,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    bytes
                )
                if (ok) "ذخیره شد: Downloads/SazmanForooshgah/$fileName" else "خطا در ذخیرهٔ فایل"
            }
        } catch (e: Exception) {
            AppLog.e("DataManager", "exportExcelFile", e)
            "خطا در ساخت اکسل: ${e.message}"
        }
    }

    private fun saveToDownloads(displayName: String, mime: String, bytes: ByteArray): Boolean {
        val values = android.content.ContentValues().apply {
            put(android.provider.MediaStore.Downloads.DISPLAY_NAME, displayName)
            put(android.provider.MediaStore.Downloads.MIME_TYPE, mime)
            put(
                android.provider.MediaStore.Downloads.RELATIVE_PATH,
                android.os.Environment.DIRECTORY_DOWNLOADS + "/SazmanForooshgah"
            )
        }
        val uri = context.contentResolver
            .insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return false
        context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) } ?: return false
        return true
    }

    private fun nextDays(days: Int): String {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, days)
        val pd = JalaliDateUtils.fromTimestamp(cal.timeInMillis)
        return PersianFormat.jalaliString(pd.getShYear(), pd.getShMonth(), pd.getShDay())
    }
}
