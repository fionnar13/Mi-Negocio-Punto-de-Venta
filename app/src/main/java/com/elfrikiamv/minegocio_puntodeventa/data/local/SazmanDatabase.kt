package com.elfrikiamv.minegocio_puntodeventa.data.local

// SazmanDatabase.kt — پایگاه‌دادهٔ Room «سازمان فروشگاه»

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.elfrikiamv.minegocio_puntodeventa.data.dao.BankDao
import com.elfrikiamv.minegocio_puntodeventa.data.dao.CounterDao
import com.elfrikiamv.minegocio_puntodeventa.data.dao.CustomerDao
import com.elfrikiamv.minegocio_puntodeventa.data.dao.ErrorLogDao
import com.elfrikiamv.minegocio_puntodeventa.data.dao.EventDao
import com.elfrikiamv.minegocio_puntodeventa.data.dao.LedgerEntryDao
import com.elfrikiamv.minegocio_puntodeventa.data.dao.ProductDao
import com.elfrikiamv.minegocio_puntodeventa.data.dao.PurchaseDao
import com.elfrikiamv.minegocio_puntodeventa.data.dao.SaleDao
import com.elfrikiamv.minegocio_puntodeventa.data.dao.ScanLogDao
import com.elfrikiamv.minegocio_puntodeventa.data.dao.SessionDao
import com.elfrikiamv.minegocio_puntodeventa.data.dao.SettingsDao
import com.elfrikiamv.minegocio_puntodeventa.data.dao.SupplierDao
import com.elfrikiamv.minegocio_puntodeventa.data.dao.TerminalDao
import com.elfrikiamv.minegocio_puntodeventa.data.dao.UserDao
import com.elfrikiamv.minegocio_puntodeventa.data.dao.VisitorDao
import com.elfrikiamv.minegocio_puntodeventa.data.entity.BankEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.CounterEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.CustomerEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.ErrorLogEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.EventEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.LedgerEntryEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.ProductEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.PurchaseEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.SaleEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.ScanLogEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.SessionEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.SettingsEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.SupplierEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.TerminalEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.UserEntity
import com.elfrikiamv.minegocio_puntodeventa.data.entity.VisitorEntity

/**
 * پایگاه‌دادهٔ Room برنامه «سازمان فروشگاه».
 *
 * نسخه ۱ — با `exportSchema = true` (خروجی اسکیمای JSON در app/schemas
 * با تنظیم room.schemaLocation در build.gradle.kts).
 *
 * ─── سیاست مهاجرت (مهم — قبل از هر تغییر اسکیما بخوانید) ───
 *
 * • نسخهٔ ۱ هم‌اکنون با `exportSchema = true` منتشر می‌شود.
 * • هر تغییر اسکیمای آینده باید همراه با یک شیء `Migration` واقعی باشد
 *   و از طریق `databaseBuilder(…).addMigrations(…)` ثبت شود؛
 *   سپس شمارهٔ `version` در `@Database` یک واحد افزایش یابد.
 * • هرگز از `fallbackToDestructiveMigration()` استفاده نکنید —
 *   این متد کل جداول (از جمله داده‌های فروش واقعی) را پاک می‌کند.
 * • پوشهٔ `app/schemas` تاریخچهٔ نسخه‌های اسکیما را نگه می‌دارد؛
 *   برای مقایسهٔ تغییرات بین دو نسخه از فایل‌های JSON همین پوشه استفاده کنید.
 */
@Database(
    entities = [
        ProductEntity::class,
        CustomerEntity::class,
        SupplierEntity::class,
        VisitorEntity::class,
        BankEntity::class,
        UserEntity::class,
        TerminalEntity::class,
        SaleEntity::class,
        PurchaseEntity::class,
        LedgerEntryEntity::class,
        EventEntity::class,
        ErrorLogEntity::class,
        SessionEntity::class,
        SettingsEntity::class,
        CounterEntity::class,
        ScanLogEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class SazmanDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun supplierDao(): SupplierDao
    abstract fun visitorDao(): VisitorDao
    abstract fun bankDao(): BankDao
    abstract fun userDao(): UserDao
    abstract fun terminalDao(): TerminalDao
    abstract fun saleDao(): SaleDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun ledgerEntryDao(): LedgerEntryDao
    abstract fun eventDao(): EventDao
    abstract fun errorLogDao(): ErrorLogDao
    abstract fun sessionDao(): SessionDao
    abstract fun settingsDao(): SettingsDao
    abstract fun counterDao(): CounterDao
    abstract fun scanLogDao(): ScanLogDao

    companion object {
        @Volatile
        private var INSTANCE: SazmanDatabase? = null

        /** نمونهٔ یکتای پایگاه‌داده. */
        fun getDatabase(context: Context): SazmanDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SazmanDatabase::class.java,
                    "sazman_forooshgah.db"
                ).build().also { INSTANCE = it }
            }
    }
}
