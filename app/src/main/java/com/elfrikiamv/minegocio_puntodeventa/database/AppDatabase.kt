package com.elfrikiamv.minegocio_puntodeventa.database

// AppDatabase.kt

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.elfrikiamv.minegocio_puntodeventa.converter.ProductListConverter
import com.elfrikiamv.minegocio_puntodeventa.dao.home.expense.ExpenseDao
import com.elfrikiamv.minegocio_puntodeventa.dao.home.missing.MissingProductDao
import com.elfrikiamv.minegocio_puntodeventa.dao.inventory.ProductDao
import com.elfrikiamv.minegocio_puntodeventa.dao.shopping.TicketDao
import com.elfrikiamv.minegocio_puntodeventa.model.home.expenses.ExpenseEntity
import com.elfrikiamv.minegocio_puntodeventa.model.home.missing.MissingProductEntity
import com.elfrikiamv.minegocio_puntodeventa.model.inventory.ProductEntity
import com.elfrikiamv.minegocio_puntodeventa.model.shopping.TicketEntity

@Database(
    entities = [ProductEntity::class, TicketEntity::class, ExpenseEntity::class, MissingProductEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(ProductListConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun ticketDao(): TicketDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun missingProductDao(): MissingProductDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "product_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}